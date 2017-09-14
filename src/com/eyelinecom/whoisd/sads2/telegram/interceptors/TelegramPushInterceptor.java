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
import com.eyelinecom.whoisd.sads2.content.attributes.AttributeSet;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSExecutor;
import com.eyelinecom.whoisd.sads2.telegram.api.types.*;
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
import static com.eyelinecom.whoisd.sads2.content.attributes.AttributeReader.getAttributes;
import static com.eyelinecom.whoisd.sads2.executors.connector.ProfileEnabledMessageConnector.ATTR_SESSION_PREVIOUS_PAGE_URI;
import static com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils.parse;
import static com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils.unmarshal;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@SuppressWarnings("unused")
public class TelegramPushInterceptor extends TelegramPushBase implements Initable {

  private static final Logger log = Logger.getLogger(TelegramPushInterceptor.class);

  private TelegramApi client;

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
      if (isNotBlank(request.getParameters().get("sadsSmsMessage"))) {
        // TODO: rely on MessagesAdaptor, use concatenated message text & clear them after processing.
        sendTelegramMessage(
            request,
            content,
            request.getParameters().get("sadsSmsMessage"),
            request.getParameters().get("keyboard"));

      } else {
        sendTelegramMessage(request, content, response);
      }

    } catch (Exception e) {
      throw new InterceptionException(e);
    }
  }

  /**
   * Processes content-originated messages.
   */
  private void sendTelegramMessage(SADSRequest request,
                                   ContentResponse contentResponse,
                                   SADSResponse response) throws Exception {

    final String serviceId = request.getServiceId();
    final Document doc = (Document) response.getAttributes().get(PageBuilder.VALUE_DOCUMENT);

    final Keyboard keyboard;
    {
      final ReplyKeyboardMarkup replyKbd = getKeyboard(doc);
      if (replyKbd != null) {
        if (isOneTimeKeyboard(request, contentResponse))  replyKbd.setOneTimeKeyboard(true);
        if (isResizeKeyboard(request, contentResponse))   replyKbd.setResizeKeyboard(true);
        keyboard = replyKbd;

      } else {
        keyboard = getInlineKeyboard(doc);
      }
    }

    String text = getText(doc);

    final boolean isNothingToSend = StringUtils.isBlank(text) && keyboard == null;
    if (!isNothingToSend) {
      // Empty message text is not allowed by Telegram Bot API.
      text = text.isEmpty() ? "." : text;
    }

    final boolean shouldCloseSession;
    {
      if (keyboard != null || !doc.getRootElement().elements("input").isEmpty()) {
        shouldCloseSession = false;

      } else {
        final AttributeSet pageAttributes = getAttributes(doc.getRootElement());
        shouldCloseSession = !pageAttributes.getBoolean("telegram.keep.session")
            .or(pageAttributes.getBoolean("keep.session"))
            .or(false);
      }
    }

    final Session session = request.getSession();

    if (!shouldCloseSession) {
      session.setAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE, doc);
      session.setAttribute(
          ATTR_SESSION_PREVIOUS_PAGE_URI,
          response.getAttributes().get(ContentRequestUtils.ATTR_REQUEST_URI));
    }

    if (!isNothingToSend) {
      final String token =
          request.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);
      final String chatId = request.getProfile()
          .property("telegram-chats", token)
          .getValue();

      if (!isEditRequest(doc)) {
        final AttributeSet pageAttributes = getAttributes(doc.getRootElement());
        boolean shouldReply = pageAttributes.getBoolean("telegram.reply")
                .or(false);
        Message message = null;
        if (shouldReply) {
          Update originalRequest = (Update) request.getAttributes().get(TelegramMessageConnector.ATTR_TELEGRAM_RAW_REQUEST_UPDATE);
          if (originalRequest!=null) {
            Message originalMessage = originalRequest.getMessage();
            if (originalMessage!=null) {
              message = client.sendMessage(
                      session, token, chatId, text, originalMessage.getMessageId(), keyboard != null ? keyboard : new ReplyKeyboardHide());
            }
          }
        }
        if (message == null) {
          // Hide keyboard if none is present in the current page.
          // This will hide the keyboard if previous page has links and the current one doesn't.
          message = client.sendMessage(
                  session, token, chatId, text, keyboard != null ? keyboard : new ReplyKeyboardHide());
        }

        // Save `content page ID` -> `Telegram message ID mapping`.
        final String messageId = getMessageId(doc);
        if (isNotBlank(messageId) && message.getMessageId() != null) {
          session.setAttribute("message-" + messageId, message.getMessageId());
        }

      } else {
        final String contentMessageId = getMessageId(doc);
        final Integer tgMessageId = (Integer) session.getAttribute("message-" + contentMessageId);
        if (tgMessageId != null) {
          client.editMessage(
              session,
              token,
              chatId,
              String.valueOf(tgMessageId),
              text,
              keyboard instanceof InlineKeyboardMarkup ? (InlineKeyboardMarkup) keyboard : null
          );

        } else {
          log.warn("Cannot find Telegram MessageId for content page ID [" + contentMessageId + "]");
        }
      }
    }

    if (shouldCloseSession) {
      // No inputs mean that the dialog is over.
      session.close();
    }
  }

  /**
   * Processes PUSH messages.
   */
  private void sendTelegramMessage(final SADSRequest request,
                                   final ContentResponse content,
                                   String message,
                                   String keyboard) throws Exception {

    final String serviceId = request.getServiceId();
    final String token =
        request.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);

    final String chatId = request
        .getProfile()
        .property("telegram-chats", token)
        .getValue();

    Keyboard kbd = new ReplyKeyboardHide();
    try {
      if (isNotBlank(keyboard)) {
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
        request.getSession(),
        token,
        chatId,
        message,
        kbd);
  }

  public static String getText(final Document doc) throws DocumentException {
    final Collection<String> messages = new ArrayList<String>() {{
      //noinspection unchecked
      for (Element e : (List<Element>) doc.getRootElement().elements("message")) {
        add(getContent(e));
      }
    }};

    return StringUtils.join(messages, "\n").trim();
  }

  private String getMessageId(final Document doc) throws DocumentException {
    return getAttributes(doc.getRootElement())
        .getString("telegram.message.id")
        .orNull();
  }

  private boolean isEditRequest(final Document doc) throws DocumentException {
    return getAttributes(doc.getRootElement())
        .getBoolean("telegram.message.edit")
        .or(false);
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
          e.setText(e.getText().replaceAll("\\n[ \\t]+", "\n"));
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
    client = SADSInitUtils.getResource("client", config);
  }

  @Override
  public void destroy() {
    // Nothing here.
  }
}
