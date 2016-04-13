package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.PageBuilder;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.content.ContentRequestUtils;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSExecutor;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSInitializer;
import com.eyelinecom.whoisd.sads2.resource.ResourceStorage;
import com.eyelinecom.whoisd.sads2.telegram.ServiceSessionManager;
import com.eyelinecom.whoisd.sads2.telegram.SessionManager;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.KeyboardButton;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ReplyKeyboardHide;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ReplyKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.api.types.TextButton;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import com.eyelinecom.whoisd.sads2.telegram.connector.TelegramMessageConnector;
import com.eyelinecom.whoisd.sads2.telegram.registry.WebHookConfigListener;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.common.ArrayUtil.transformArray;
import static com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils.parse;
import static com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils.unmarshal;

@SuppressWarnings("unused")
public class TelegramPushInterceptor extends TelegramPushBase implements Initable {

  private static final Logger log = Logger.getLogger(TelegramPushInterceptor.class);

  private TelegramApi client;
  private ServiceSessionManager sessionManager;

  @Override
  public void afterResponseRender(SADSRequest request,
                                  ContentResponse content,
                                  SADSResponse response,
                                  RequestDispatcher dispatcher) throws InterceptionException {

    if (log.isTraceEnabled()) {
      log.trace("TelegramPushInterceptor.afterContentResponse" +
          " request = [" + request + "]," +
          " response = [" + response + "]," +
          " content = [" + content + "]," +
          " dispatcher = [" + dispatcher + "]");
    }

    try {
      final ExtendedSadsRequest tgRequest = (ExtendedSadsRequest) request;
      final ResourceStorage resourceStorage = SADSInitializer.getResourceStorage();

      if (StringUtils.isNotBlank(request.getParameters().get("sadsSmsMessage"))) {
        // TODO: rely on MessagesAdaptor, use concatenated message text & clear them after processing.
        sendTelegramMessage(
            tgRequest,
            content,
            request.getParameters().get("sadsSmsMessage"),
            request.getParameters().get("keyboard"));

      } else {
        sendTelegramMessage(tgRequest, content, response);
      }

    } catch (Exception e) {
      throw new InterceptionException(e);
    }
  }

  private void sendTelegramMessage(ExtendedSadsRequest request,
                                   ContentResponse contentResponse,
                                   SADSResponse response) throws Exception {

    final String serviceId = request.getServiceId();
    final Document doc = (Document) response.getAttributes().get(PageBuilder.VALUE_DOCUMENT);

    final Keyboard keyboard = getKeyboard(
        doc,
        isOneTimeKeyboard(request, contentResponse),
        isResizeKeyboard(request, contentResponse));

    String text = getText(doc);

    final boolean shouldPass = StringUtils.isBlank(text) && keyboard == null;
    if (!shouldPass) {
      // Empty message text is not allowed by Telegram Bot API.
      text = text.isEmpty() ? "." : text;
    }

    final boolean shouldCloseSession =
        keyboard == null && doc.getRootElement().elements("input").isEmpty();
    final SessionManager sessionManager = this.sessionManager.getSessionManager(serviceId);
    final Session session = request.getSession();

    if (!shouldCloseSession) {
      session.setAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE, doc);
      session.setAttribute(
          TelegramMessageConnector.ATTR_SESSION_PREVIOUS_PAGE_URI,
          response.getAttributes().get(ContentRequestUtils.ATTR_REQUEST_URI));
    }

    if (!shouldPass) {
      final String token =
          request.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);
      final String chatId = request.getProfile()
          .query()
          .property("telegram-chats", token)
          .getValue();
      client.sendMessage(sessionManager, token, chatId, text, keyboard);
    }

    if (shouldCloseSession) {
      // No inputs mean that the dialog is over.
      session.close();
    }
  }

  private void sendTelegramMessage(final ExtendedSadsRequest request,
                                   final ContentResponse content,
                                   String message,
                                   String keyboard) throws Exception {

    final String serviceId = request.getServiceId();
    final SessionManager sessionManager =
        this.sessionManager.getSessionManager(serviceId);

    final String token =
        request.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);

    final String chatId = request
        .getProfile()
        .query().property("telegram-chats", token)
        .getValue();

    Keyboard kbd = new ReplyKeyboardHide();
    try {
      if (StringUtils.isNotBlank(keyboard)) {
        final String[][] buttons = unmarshal(parse(keyboard), String[][].class);
        if (buttons != null) {
          kbd = new ReplyKeyboardMarkup() {{
            setOneTimeKeyboard(isOneTimeKeyboard(request, content));
            setResizeKeyboard(isResizeKeyboard(request, content));
            setKeyboard(
                transformArray(KeyboardButton.class, buttons, TextButton.FROM_STRING));
          }};
        }
      }

    } catch (Exception e) {
      log.error("Keyboard construction failed for value = [" + keyboard + "]", e);
    }

    client.sendMessage(
        sessionManager,
        token,
        chatId,
        message,
        kbd);
  }

  private String getText(final Document doc) throws DocumentException {
    final Collection<String> messages = new ArrayList<String>() {{
      //noinspection unchecked
      for (Element e : (List<Element>) doc.getRootElement().elements("message")) {
        add(getContent(e));
      }
    }};

    return StringUtils.join(messages, "\n").trim();
  }

  public static String getContent(Element element) throws DocumentException {
    final StringBuilder buf = new StringBuilder();

    try {
      final Element messageElement = new SAXReader()
          .read(new ByteArrayInputStream(element.asXML().getBytes("UTF-8")))
          .getRootElement();

      //noinspection unchecked
      for (Node e : (List<Node>) messageElement.selectNodes("//text()")) {
        if (!"pre".equals(e.getParent().getName())) {
          e.setText(e.getText().replaceAll("\\n\\s+", "\n"));
        }
      }

      //noinspection unchecked
      for (Node e : (Collection<Node>) IteratorUtils.toList(messageElement.nodeIterator())) {
        buf.append(e.asXML());
      }
      return buf.toString().trim();

    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void init(Properties config) throws Exception {
    client = (TelegramApi) SADSInitUtils.getResource("client", config);
    sessionManager = (ServiceSessionManager) SADSInitUtils.getResource("session-manager", config);
  }

  @Override
  public void destroy() {
    // Nothing here.
  }
}
