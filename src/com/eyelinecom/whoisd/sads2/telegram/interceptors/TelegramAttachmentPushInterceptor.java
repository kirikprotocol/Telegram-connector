package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.PageBuilder;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.content.attachments.Attachment;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSInitializer;
import com.eyelinecom.whoisd.sads2.resource.ResourceStorage;
import com.eyelinecom.whoisd.sads2.telegram.api.TgAttachmentMethodConverter;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.ApiSendMethod;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ReplyKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.registry.WebHookConfigListener;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("unused")
public class TelegramAttachmentPushInterceptor extends TelegramPushBase implements Initable {

  private static final Logger log = Logger.getLogger(TelegramAttachmentPushInterceptor.class);

  private TelegramApi client;
  private HttpDataLoader loader;

  @Override
  public void afterResponseRender(SADSRequest request,
                                  ContentResponse content,
                                  SADSResponse response,
                                  RequestDispatcher dispatcher) throws InterceptionException {

    if (log.isTraceEnabled()) {
      log.trace("TelegramAttachmentPushInterceptor.afterContentResponse" +
          " request = [" + request + "]," +
          " response = [" + response + "]," +
          " content = [" + content + "]," +
          " dispatcher = [" + dispatcher + "]");
    }

    try {
      final ResourceStorage resourceStorage = SADSInitializer.getResourceStorage();

      if (StringUtils.isBlank(request.getParameters().get("sadsSmsMessage"))) {
        sendTelegramMessage(request, content, response);
      }

      dispatcher.stop(response);

    } catch (Exception e) {
      throw new InterceptionException(e);
    }
  }

  private void sendTelegramMessage(SADSRequest request,
                                   ContentResponse contentResponse,
                                   SADSResponse response) throws Exception {

    final String serviceId = request.getServiceId();
    final Document doc = (Document) response.getAttributes().get(PageBuilder.VALUE_DOCUMENT);

    final Collection<Attachment> attachments = Attachment.extract(log, doc);
    if (attachments.isEmpty()) {
      return;
    }

    final String token =
        request.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);
    final String chatId = request.getProfile()
        .property("telegram-chats", token)
        .getValue();

    // Resend keyboard along with the attachments so it stays on the screen.
    final ReplyKeyboardMarkup keyboard = getKeyboard(doc);
    if (keyboard != null) {
      if (isOneTimeKeyboard(request, contentResponse))  keyboard.setOneTimeKeyboard(true);
      if (isResizeKeyboard(request, contentResponse))   keyboard.setResizeKeyboard(true);
    }

    final TgAttachmentMethodConverter converter =
        new TgAttachmentMethodConverter(log, loader, request.getResourceURI());

    for (Attachment attachment : attachments) {
      final ApiSendMethod method = converter.apply(attachment);
      if (method == null) {
        continue;
      }

      method.setChatId(chatId);
      method.setReplyMarkup(keyboard);

      client.sendData(
          request.getSession(),
          token,
          chatId,
          method
      );
    }
  }

  private Collection<Attachment> getAttachments(final Document doc) throws DocumentException {
    return new ArrayList<Attachment>() {{
      //noinspection unchecked
      for (Element el : (List<Element>) doc.getRootElement().elements("attachment")) {
        try {
          add(Attachment.parse(el));
        } catch (Exception e) {
          log.warn("Failed processing attachment", e);
        }
      }
    }};
  }

  @Override
  public void init(Properties config) throws Exception {
    client = SADSInitUtils.getResource("client", config);
    loader = SADSInitUtils.getResource("loader", config);
  }

  @Override
  public void destroy() {
    // Nothing here.
  }
}
