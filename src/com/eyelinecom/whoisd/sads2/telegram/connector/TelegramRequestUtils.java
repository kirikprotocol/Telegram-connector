package com.eyelinecom.whoisd.sads2.telegram.connector;

import com.eyelinecom.whoisd.sads2.common.ArrayUtil;
import com.eyelinecom.whoisd.sads2.content.attributes.AttributeUtil;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Update;
import com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramPushBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.util.List;

import static com.eyelinecom.whoisd.sads2.content.attributes.AttributeUtil.isBooleanSet;
import static com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType.unmarshal;
import static com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils.parse;
import static com.google.common.base.Predicates.not;

public class TelegramRequestUtils {

  public static Update parseUpdate(String webHookRequest) throws TelegramApiException {
    try {
      return unmarshal(parse(webHookRequest), Update.class);

    } catch (IOException e) {
      throw new TelegramApiException("Unable to read update message", e);
    }
  }

  public static class ExtLink {
    @JsonProperty public final String href;
    @JsonProperty public final String label;

    public ExtLink(String href, String label) {
      this.href = href;
      this.label = label;
    }
  }

  public static ExtLink[][] collectExtLinks(Document doc) {
    @SuppressWarnings("unchecked")
    final List<Element> buttons = AttributeUtil.filter(
        (List<Element>) doc.getRootElement().elements("button"),
        not(isBooleanSet("telegram.inline"))
    );

    if (CollectionUtils.isEmpty(buttons)) {
      return null;
    }

    final ExtLink[][] keyboard = TelegramPushBase.collectButtons(
        ExtLink.class,
        buttons,
        new Function<Element, ExtLink>() {
          @Override
          public ExtLink apply(Element btn) {
            final String href = DocumentHelper.createXPath("@href").valueOf(btn);
            final String label = btn.getTextTrim();
            return
                !"telegram://request-contact".equalsIgnoreCase(href) &&
                    !"telegram://request-location".equalsIgnoreCase(href) ?
                    new ExtLink(href, label) : null;
          }
        });

    return ArrayUtil.isEmpty(keyboard) ? null : keyboard;
  }
}
