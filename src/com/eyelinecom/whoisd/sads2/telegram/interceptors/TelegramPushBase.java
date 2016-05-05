package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.common.ArrayUtil;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.content.attributes.AttributeReader;
import com.eyelinecom.whoisd.sads2.content.attributes.AttributeUtil;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.api.internal.InlineCallbackQuery;
import com.eyelinecom.whoisd.sads2.telegram.api.types.InlineKeyboardButton;
import com.eyelinecom.whoisd.sads2.telegram.api.types.InlineKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.api.types.KeyboardButton;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ReplyKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.api.types.RequestContactButton;
import com.eyelinecom.whoisd.sads2.telegram.api.types.RequestLocationButton;
import com.eyelinecom.whoisd.sads2.telegram.api.types.TextButton;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Function;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.content.attributes.AttributeUtil.isBooleanSet;
import static com.google.common.base.Predicates.not;

public abstract class TelegramPushBase extends BlankInterceptor {

  private static final Logger log = Logger.getLogger(TelegramPushBase.class);

  protected boolean isResizeKeyboard(SADSRequest request,
                                     ContentResponse contentResponse) {

    final Properties serviceAttrs = request.getServiceScenario().getAttributes();
    final Object pageAttr = contentResponse.getAttributes().get("telegram.keyboard-resize");

    return pageAttr != null ?
        Boolean.parseBoolean(String.valueOf(pageAttr)) :
        InitUtils.getBoolean("telegram.keyboard-resize", true, serviceAttrs);
  }

  protected boolean isOneTimeKeyboard(SADSRequest request,
                                      ContentResponse contentResponse) {

    final Properties serviceAttrs = request.getServiceScenario().getAttributes();
    final Object pageAttr = contentResponse.getAttributes().get("telegram.keyboard-onetime");

    return pageAttr != null ?
        Boolean.parseBoolean(String.valueOf(pageAttr)) :
        InitUtils.getBoolean("telegram.keyboard-onetime", true, serviceAttrs);
  }

  public static ReplyKeyboardMarkup getKeyboard(Document doc) {

    @SuppressWarnings("unchecked")
    final List<Element> buttons = AttributeUtil.filter(
        (List<Element>) doc.getRootElement().elements("button"),
        not(isBooleanSet("telegram.inline"))
    );

    if (CollectionUtils.isEmpty(buttons)) {
      return null;
    }

    final KeyboardButton[][] keyboard = collectButtons(
        KeyboardButton.class,
        buttons,
        new Function<Element, KeyboardButton>() {
          @Override
          public KeyboardButton apply(Element btn) {
            String href = DocumentHelper.createXPath("@href").valueOf(btn);
            String label = btn.getTextTrim();
            return "telegram://request-contact".equalsIgnoreCase(href) ?
                new RequestContactButton(label) :

                "telegram://request-location".equalsIgnoreCase(href) ?
                    new RequestLocationButton(label) :

                    new TextButton(label);
          }
        });

    return new ReplyKeyboardMarkup(keyboard);
  }

  public static <T> T[][] collectButtons(final Class<T> btnType,
                                         final List<Element> buttons,
                                         final Function<Element, T> asButton) {
    // { <row number> -> [<button>] }
    final Map<Integer, List<T>> keyTable = new HashMap<Integer, List<T>>() {{
      for (Element button : buttons) {
        final String rowAttr = button.attributeValue("row");
        final int nRow = StringUtils.isBlank(rowAttr) ? 0 : Integer.valueOf(rowAttr) - 1;

        List<T> rowButtons = get(nRow);
        if (rowButtons == null) {
          put(nRow, rowButtons = new ArrayList<>());
        }

        final T btn = asButton.apply(button);
        if (btn != null) {
          rowButtons.add(btn);
        }
      }
    }};

    return ArrayUtil.mapToTable(btnType, keyTable);
  }

  public static InlineKeyboardMarkup getInlineKeyboard(Document doc) {

    @SuppressWarnings("unchecked")
    final List<Element> inlineButtons = AttributeUtil.filter(
        (List<Element>) doc.getRootElement().elements("button"),
        isBooleanSet("telegram.inline")
    );

    if (CollectionUtils.isEmpty(inlineButtons)) {
      return null;
    }

    final InlineKeyboardButton[][] keyboard = collectButtons(InlineKeyboardButton.class,
        inlineButtons,
        new Function<Element, InlineKeyboardButton>() {
          @Override
          public InlineKeyboardButton apply(final Element btn) {
            return new InlineKeyboardButton() {{
              setText(btn.getTextTrim());

              final String href = DocumentHelper.createXPath("@href").valueOf(btn);
              final boolean isExternalUrl =
                  AttributeReader.getAttributes(btn).getBoolean("telegram.external.url").or(false);
              if (isExternalUrl) {
                setUrl(href);

              } else {
                try {
                  setCallbackData(MarshalUtils.marshal(new InlineCallbackQuery(href)));

                } catch (JsonProcessingException e) {
                  log.error(e.getMessage(), e);
                }
              }
            }};
          }
        });

    return new InlineKeyboardMarkup(keyboard);
  }

}
