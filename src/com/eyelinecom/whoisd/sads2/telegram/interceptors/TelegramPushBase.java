package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.KeyboardButton;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ReplyKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.api.types.RequestContactButton;
import com.eyelinecom.whoisd.sads2.telegram.api.types.RequestLocationButton;
import com.eyelinecom.whoisd.sads2.telegram.api.types.TextButton;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import com.google.common.base.Functions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.common.ArrayUtil.transformArray;

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

  public static Keyboard getKeyboard(Document doc, final boolean onetime, final boolean resize) {

    @SuppressWarnings("unchecked")
    final List<Element> buttons = (List<Element>) doc.getRootElement().elements("button");
    if (CollectionUtils.isEmpty(buttons)) {
      return null;
    }

    final Map<Integer, List<KeyboardButton>> keyTable = new HashMap<Integer, List<KeyboardButton>>() {{
      for (Element button : buttons) {
        final String rowAttr = button.attributeValue("row");
        final int nRow = StringUtils.isBlank(rowAttr) ? 0 : Integer.valueOf(rowAttr) - 1;

        List<KeyboardButton> rowButtons = get(nRow);
        if (rowButtons == null) {
          put(nRow, rowButtons = new ArrayList<>());
        }

        final String pageId = DocumentHelper.createXPath("@href").valueOf(button);
        final String label = button.getTextTrim();

        rowButtons.add(
            "telegram://request-contact".equalsIgnoreCase(pageId) ?
                new RequestContactButton(label) :

                "telegram://request-location".equalsIgnoreCase(pageId) ?
                    new RequestLocationButton(label) :

                    new TextButton(label)
        );
      }
    }};

    return new ReplyKeyboardMarkup() {{
      if (onetime)  setOneTimeKeyboard(true);
      if (resize)   setResizeKeyboard(true);

      setKeyboard(mapToTable(KeyboardButton.class, keyTable));
    }};
  }

  private static <T> T[][] mapToTable(Class<T> clazz, Map<Integer, List<T>> keyTable) {
    @SuppressWarnings("unchecked")
    final T[][] keys = (T[][]) Array.newInstance(clazz, keyTable.size(), 1);

    final List<Map.Entry<Integer, List<T>>> rows = new ArrayList<>(keyTable.entrySet());
    Collections.sort(
        rows,
        new Comparator<Map.Entry<Integer, ?>>() {
          @Override
          public int compare(Map.Entry<Integer, ?> _1, Map.Entry<Integer, ?> _2) {
            return Integer.compare(_1.getKey(), _2.getKey());
          }
        });

    int i = 0;
    for (Map.Entry<Integer, List<T>> row : rows) {
      //noinspection unchecked
      keys[i++] = transformArray(clazz, (T[]) row.getValue().toArray(), Functions.<T>identity());
    }

    return keys;
  }

}
