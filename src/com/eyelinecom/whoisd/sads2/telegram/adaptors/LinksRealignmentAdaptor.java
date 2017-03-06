package com.eyelinecom.whoisd.sads2.telegram.adaptors;

import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.adaptor.DocumentAdaptor;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponseUtils;
import com.eyelinecom.whoisd.sads2.content.attributes.AttributeSet;
import com.eyelinecom.whoisd.sads2.exception.AdaptationException;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.google.common.base.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.eyelinecom.whoisd.sads2.content.attributes.AttributeReader.getAttributes;

/**
 * Splits buttons into several rows to not exceed maximal allowed line length.
 * In case
 */
public class LinksRealignmentAdaptor extends DocumentAdaptor {

  public static final String CONF_TELEGRAM_LINKS_REALIGNMENT_ENABLED =
      "telegram.links.realignment.enabled";

  public static final String CONF_TELEGRAM_LINKS_REALIGNMENT_THRESHOLD =
      "telegram.links.realignment.threshold";

  private static final int TELEGRAM_LINKS_REALIGNMENT_THRESHOLD_DEFAULT = 10;

  @Override
  public Document transform(Document document,
                            ContentResponse response) throws Exception {

    checkRequest(response);

    final ServiceConfig serviceConfig = getServiceScenario(response);

    if (!isEnabled(serviceConfig, document)) {
      return document;
    }

    final int maxLineLength = getMaxLineLength(serviceConfig, document);

    return realign(document, maxLineLength);
  }

  private Document realign(Document document, int maxLineLength) {

    @SuppressWarnings("unchecked")
    final List<Element> buttons = (List<Element>) document.selectNodes("//button");
    if (!isRealignmentRequired(buttons, maxLineLength)) {
      return document;
    }

    final Map<Element, Integer> rows = calculatePositions(maxLineLength, buttons);

    for (Element button : buttons) {
      final Integer assignedRow = rows.get(button);
      button.addAttribute(
          "row",
          assignedRow == null ? null : String.valueOf(assignedRow)
      );
    }

    return document;
  }

  private boolean isRealignmentRequired(final List<Element> buttons, int maxLineLength) {
    if (CollectionUtils.isEmpty(buttons)) {
      return false;
    }

    //
    // Check if the current row alignment violates maximal length constraints.
    //

    final Map<Integer, List<Element>> buttonsByRow = buttonsByRow(buttons);

    for (Integer row : buttonsByRow.keySet()) {
      int rowTextLength = 0;

      final List<Element> buttonsOnRow = buttonsByRow.get(row);
      if (buttonsOnRow.size() == 1) {
        // Doesn't matter as even in the case if maximal length is exceeded, there's no
        // other way to place this link.
        continue;
      }

      for (Element button : buttonsOnRow){
        if ((rowTextLength += button.getTextTrim().length()) > maxLineLength) {
          return true;
        }
      }
    }

    return false;
  }

  public Map<Integer, List<Element>> buttonsByRow(final List<Element> buttons) {
    @SuppressWarnings("unchecked") Map<Integer, List<Element>> defaultsOrderedMap =
        new LinkedHashMap<Integer, List<Element>>() {
          @Override
          public List<Element> get(Object key) {
            List<Element> value = super.get(key);
            if (value == null) {
              put((Integer) key, value = new ArrayList<>());
            }
            return value;
          }
        };

    for (Element button : buttons) {
      defaultsOrderedMap.get(Integer.valueOf(button.attributeValue("row", "1"))).add(button);
    }

    return defaultsOrderedMap;
  }

  /**
   * Returns mapping of "button" -> "row number (starting with 1)".
   */
  public Map<Element, Integer> calculatePositions(int maxLineLength,
                                                  List<Element> buttons) {

    final LinkedHashMap<Element, Integer> rows = new LinkedHashMap<>();

    int currentRow = 1;
    int currentStringLength = 0;
    for (Element button : buttons) {
      final int labelLength = button.getTextTrim().length();
      if (
          // Got a place for this button on a current row.
          (currentStringLength + labelLength <= maxLineLength) ||

          // Nothing else is on this row but still the label doesn't fit - this means there's
          // no other way than to leave this button here.
          !rows.containsValue(currentRow)) {

        rows.put(button, currentRow);
        currentStringLength += labelLength;

      } else {
        rows.put(button, ++currentRow);
        currentStringLength = labelLength;
      }
    }

    return rows;
  }

  protected int getMaxLineLength(ServiceConfig serviceConfig, Document doc) {
    final AttributeSet pageAttributes = getAttributes(doc.getRootElement());
    final Integer threshold = pageAttributes.getInteger(CONF_TELEGRAM_LINKS_REALIGNMENT_THRESHOLD)
            .or(InitUtils.getInt(CONF_TELEGRAM_LINKS_REALIGNMENT_THRESHOLD,
                    TELEGRAM_LINKS_REALIGNMENT_THRESHOLD_DEFAULT,
                    serviceConfig.getAttributes()));
    if (threshold==null || threshold<=0) {
      return TELEGRAM_LINKS_REALIGNMENT_THRESHOLD_DEFAULT;
    }
    return threshold;
  }

  protected boolean isEnabled(ServiceConfig serviceConfig, Document doc) {
    final AttributeSet pageAttributes = getAttributes(doc.getRootElement());
      Optional<Boolean> pageOption = pageAttributes.getBoolean(CONF_TELEGRAM_LINKS_REALIGNMENT_ENABLED);
      if (pageOption.isPresent()) {
          return pageOption.get();
      } else {
          return InitUtils.getBoolean(CONF_TELEGRAM_LINKS_REALIGNMENT_ENABLED, false, serviceConfig.getAttributes());
      }
  }

  protected void checkRequest(ContentResponse response) throws AdaptationException {
    final Protocol protocol = ContentResponseUtils.getProtocol(response);
    if (protocol != Protocol.TELEGRAM) {
      throw new AdaptationException("Unsupported protocol:" +
          " row ordering supported only for [" + Protocol.TELEGRAM + "], got [" + protocol + "]");
    }
  }

  protected ServiceConfig getServiceScenario(ContentResponse response) {
    return response.getServiceScenario();
  }
}
