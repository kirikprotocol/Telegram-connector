package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.PageBuilder;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSMessage;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.content.ContentRequestUtils;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSExecutor;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSInitializer;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.resource.ResourceStorage;
import com.eyelinecom.whoisd.sads2.telegram.SessionManager;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ReplyKeyboardHide;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ReplyKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.connector.TelegramMessageConnector;
import com.eyelinecom.whoisd.sads2.telegram.registry.WebHookConfigListener;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import org.apache.commons.collections.CollectionUtils;
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

@SuppressWarnings("unused")
public class TelegramPushInterceptor extends BlankInterceptor implements Initable {

  private static final Logger log = Logger.getLogger(TelegramPushInterceptor.class);

  private TelegramApi client;
  private SessionManager sessionManager;

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
      final ResourceStorage resourceStorage = SADSInitializer.getResourceStorage();

      if (CollectionUtils.isNotEmpty(response.getMessages())) {
        sendTelegramMessage(request, response.getMessages());

      } else {
        sendTelegramMessage(request, response);
      }

      dispatcher.stop(response);

    } catch (Exception e) {
      throw new InterceptionException(e);
    }
  }


  private void sendTelegramMessage(SADSRequest request,
                                   SADSResponse response) throws Exception {

    final Document doc = (Document) response.getAttributes().get(PageBuilder.VALUE_DOCUMENT);

    final String text = getText(doc);
    final Keyboard keyboard = getKeyboard(doc);

    final boolean hasInputs = !doc.getRootElement().elements("input").isEmpty();
    final Session session = sessionManager.getSession(request.getAbonent());

    if (keyboard != null || hasInputs) {
      session.setAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE, doc);
      session.setAttribute(
          TelegramMessageConnector.ATTR_SESSION_PREVIOUS_PAGE_URI,
          response.getAttributes().get(ContentRequestUtils.ATTR_REQUEST_URI));

    } else {
      // No inputs mean that the dialog is over.
      session.close();
    }

    final String token =
        request.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);

    client.sendMessage(token, request.getAbonent(), text, keyboard);
  }

  private void sendTelegramMessage(SADSRequest request,
                                   final List<SADSMessage> messages) throws Exception {

    final String token =
        request.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);

    final List<String> textMessages = new ArrayList<String>() {{
      for (SADSMessage message : messages) {
        add(message.getText());
      }
    }};

    client.sendMessage(
        token,
        request.getAbonent(),
        StringUtils.join(textMessages, "\n"),
        new ReplyKeyboardHide());
  }

  private String getText(final Document doc) throws DocumentException {
    final Collection<String> messages = new ArrayList<String>() {{
      //noinspection unchecked
      for (Element e : (List<Element>) doc.getRootElement().elements("message")) {
        add(getContent(e));
      }
    }};

    final String messageText = StringUtils.join(messages, "\n").trim();
    return messageText.isEmpty() ? "." : messageText;
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

  private Keyboard getKeyboard(final Document doc) {
    final List<String> buttons = new ArrayList<String>() {{
      //noinspection unchecked
      for (Element e : (List<Element>) doc.getRootElement().elements("button")) {
        add(e.getTextTrim());
      }
    }};

    if (buttons.isEmpty()) {
      return null;

    } else {
      final ReplyKeyboardMarkup kbd = new ReplyKeyboardMarkup();
      kbd.setOneTimeKeyboard(true);
      kbd.setResizeKeyboard(true);
      kbd.setKeyboard(new String[][]{buttons.toArray(new String[buttons.size()])});

      return kbd;
    }
  }


  @Override
  public void init(Properties config) throws Exception {
    client = (TelegramApi) SADSInitUtils.getResource("client", config);
    sessionManager = (SessionManager) SADSInitUtils.getResource("session-manager", config);
  }

  @Override
  public void destroy() {
    // Nothing here.
  }
}
