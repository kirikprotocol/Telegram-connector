package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ReplyKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class TelegramPushBase extends BlankInterceptor {

  protected boolean isResizeKeyboard(ExtendedSadsRequest request,
                                     ContentResponse contentResponse) {

    final Properties serviceAttrs = request.getServiceScenario().getAttributes();
    final Object pageAttr = contentResponse.getAttributes().get("telegram.keyboard-resize");

    return pageAttr != null ?
        Boolean.parseBoolean(String.valueOf(pageAttr)) :
        InitUtils.getBoolean("telegram.keyboard-resize", true, serviceAttrs);
  }

  protected boolean isOneTimeKeyboard(ExtendedSadsRequest request,
                                      ContentResponse contentResponse) {

    final Properties serviceAttrs = request.getServiceScenario().getAttributes();
    final Object pageAttr = contentResponse.getAttributes().get("telegram.keyboard-onetime");

    return pageAttr != null ?
        Boolean.parseBoolean(String.valueOf(pageAttr)) :
        InitUtils.getBoolean("telegram.keyboard-onetime", true, serviceAttrs);
  }

  public static Keyboard getKeyboard(Document doc, boolean onetime, boolean resize) {

    @SuppressWarnings("unchecked")
    final List<Element> buttons = (List<Element>) doc.getRootElement().elements("button");
    if (CollectionUtils.isEmpty(buttons)) {
      return null;
    }

    final Map<Integer, List<String>> keyTable = new HashMap<Integer, List<String>>() {{
      for (Element button : buttons) {
        final String rowAttr = button.attributeValue("row");
        final int nRow = StringUtils.isBlank(rowAttr) ? 0 : Integer.valueOf(rowAttr) - 1;

        List<String> rowButtons = get(nRow);
        if (rowButtons == null) {
          put(nRow, rowButtons = new ArrayList<>());
        }

        rowButtons.add(button.getTextTrim());
      }
    }};

    final ReplyKeyboardMarkup kbd = new ReplyKeyboardMarkup();
    kbd.setOneTimeKeyboard(onetime);
    kbd.setResizeKeyboard(resize);
    kbd.setKeyboard(mapToTable(keyTable));
    return kbd;
  }

  private static String[][] mapToTable(Map<Integer, List<String>> keyTable) {
    final String[][] keys = new String[keyTable.size()][];

    final List<Map.Entry<Integer,List<String>>> rows = new ArrayList<>(keyTable.entrySet());
    Collections.sort(
        rows,
        new Comparator<Map.Entry<Integer, ?>>() {
          @Override
          public int compare(Map.Entry<Integer, ?> _1, Map.Entry<Integer, ?> _2) {
            return Integer.compare(_1.getKey(), _2.getKey());
          }
        });

    int i = 0;
    for (Map.Entry<Integer, List<String>> row : rows) {
      final List<String> value = row.getValue();
      keys[i++] = value.toArray(new String[value.size()]);
    }

    return keys;
  }
}
