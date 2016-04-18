package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.PageBuilder;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSInitializer;
import com.eyelinecom.whoisd.sads2.resource.ResourceStorage;
import com.eyelinecom.whoisd.sads2.telegram.api.Attachment;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.ApiSendMethod;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ReplyKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import com.eyelinecom.whoisd.sads2.telegram.registry.WebHookConfigListener;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import com.eyelinecom.whoisd.sads2.telegram.session.ServiceSessionManager;
import com.eyelinecom.whoisd.sads2.telegram.session.SessionManager;
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
  private ServiceSessionManager sessionManager;
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
      final ExtendedSadsRequest tgRequest = (ExtendedSadsRequest) request;
      final ResourceStorage resourceStorage = SADSInitializer.getResourceStorage();

      if (StringUtils.isBlank(request.getParameters().get("sadsSmsMessage"))) {
        sendTelegramMessage(tgRequest, content, response);
      }

      dispatcher.stop(response);

    } catch (Exception e) {
      throw new InterceptionException(e);
    }
  }

  private void sendTelegramMessage(ExtendedSadsRequest request,
                                   ContentResponse contentResponse,
                                   SADSResponse response) throws Exception {

    final String serviceId = request.getServiceId();
    final Document doc = (Document) response.getAttributes().get(PageBuilder.VALUE_DOCUMENT);

    final Collection<Attachment> attachments = getAttachments(doc);
    if (attachments.isEmpty()) {
      return;
    }

    final String token =
        request.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);
    final String chatId = request.getProfile()
        .query()
        .property("telegram-chats", token)
        .getValue();
    final SessionManager sessionManager = this.sessionManager.getSessionManager(serviceId);

    // Resend keyboard along with the attachments so it stays on the screen.
    final ReplyKeyboardMarkup keyboard = getKeyboard(doc);
    if (keyboard != null) {
      if (isOneTimeKeyboard(request, contentResponse))  keyboard.setOneTimeKeyboard(true);
      if (isResizeKeyboard(request, contentResponse))   keyboard.setResizeKeyboard(true);
    }

    for (Attachment attachment : attachments) {
      final ApiSendMethod method =
          attachment.asTelegramMethod(log, loader, request.getResourceURI());
      method.setChatId(chatId);
      method.setReplyMarkup(keyboard);

      client.sendData(
          sessionManager,
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
    client = (TelegramApi) SADSInitUtils.getResource("client", config);
    sessionManager = (ServiceSessionManager) SADSInitUtils.getResource("session-manager", config);
    loader = (HttpDataLoader) SADSInitUtils.getResource("loader", config);
  }

  @Override
  public void destroy() {
    // Nothing here.
  }
}
