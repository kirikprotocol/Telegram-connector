package com.eyelinecom.whoisd.sads2.telegram.resource;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.RateLimiter;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.eventstat.DetailedStatLogger;
import com.eyelinecom.whoisd.sads2.executors.connector.Context;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.BotApiClient;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.*;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import com.eyelinecom.whoisd.sads2.telegram.api.types.File;
import com.eyelinecom.whoisd.sads2.telegram.api.types.InlineKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;
import com.eyelinecom.whoisd.sads2.telegram.api.types.User;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Properties;

@SuppressWarnings("unused")
public class TelegramApiImpl implements TelegramApi {

  private static final Logger log = Logger.getLogger(TelegramApiImpl.class);

  private final HttpDataLoader loader;
  private final DetailedStatLogger detailedStatLogger;
  private final String publicKeyPath;
  private final String baseUrl;
  private final String connectorBaseUrl;

  /**
   * Maximal allowed messages per second in a single chat.
   */
  private final float limitChatMessagesPerSecond;

  /**
   * Maximal allowed messages per second, overall.
   */
  private final RateLimiter messagesPerSecondLimit;

  private final int maxRateLimitRetries;

  public TelegramApiImpl(HttpDataLoader loader,
                         DetailedStatLogger detailedStatLogger,
                         Properties properties) throws Exception {

    this.loader = loader;
    this.detailedStatLogger = detailedStatLogger;

    this.publicKeyPath = getPublicKeyPath(properties);
    this.baseUrl = properties.getProperty("base.url");
    this.connectorBaseUrl = properties.getProperty("connector.url");

    this.limitChatMessagesPerSecond =
        Float.parseFloat(properties.getProperty("telegram.limit.chat.messages.per.second", "1"));

    final float limitMessagesPerSecond =
        Float.parseFloat(properties.getProperty("telegram.limit.messages.per.second", "30"));
    this.messagesPerSecondLimit = RateLimiter.create(limitMessagesPerSecond);

    this.maxRateLimitRetries =
        Integer.parseInt(properties.getProperty("telegram.max.rate.limit.retries", "5"));
  }

  private String getPublicKeyPath(Properties properties) {
    try {
      return SADSInitUtils.getFilename("certificate.pem", properties);

    } catch (Exception e) {
      log.info(
          "Property 'certificate.pem' is missing, public certificate PEM file cannot be used", e);
      return null;
    }
  }

  @Override
  public String getServiceUrl(String serviceId, String apiKey) {
    return StringUtils.join(new String[]{connectorBaseUrl, serviceId, apiKey}, "/");
  }

  private BotApiClient getClient(String token) {
    return new BotApiClient(token, baseUrl, maxRateLimitRetries, loader);
  }

  @Override
  public void registerWebHook(String token, String url) throws TelegramApiException {
    acquireOverallLimit();
    getClient(token).setWebhook(url, publicKeyPath);
  }

  @Override
  public void unRegisterWebHook(String token) throws TelegramApiException {
    acquireOverallLimit();
    getClient(token).setWebhook(null, null);
  }

  @Override
  public Message sendMessage(Session session,
                             String token,
                             String chatId,
                             String text,
                             Integer replyToMessageId,
                             Keyboard keyboard) throws TelegramApiException {

    acquireChatLimit(session, chatId);

    final SendMessage method = new SendMessage();
    method.setChatId(chatId);
    method.setText(text);
    // Always using HTML seems quite safe.
    method.setParseMode("HTML");
    if (replyToMessageId!=null) {
      method.setReplyToMessageId(replyToMessageId);
    }
    if (keyboard != null) {
      method.setReplyMarkup(keyboard);
    }

    if (Context.getSadsRequest() != null) {
      detailedStatLogger.onOuterResponse(Context.getSadsRequest(), method);
    }

    return call(token, method);
  }

  @Override
  public Message forwardMessage(Session session, String token, String chatId, String fromChatId, boolean disableNotification, Integer messageId) throws TelegramApiException {
    ForwardMessage method = new ForwardMessage();
    method.setChatId(chatId);
    method.setFromChatId(fromChatId);
    method.setDisableNotification(disableNotification);
    method.setMessageId(messageId);
    if (Context.getSadsRequest() != null) {
      detailedStatLogger.onOuterResponse(Context.getSadsRequest(), method);
    }
    return call(token, method);
  }

  @Override
  public Message sendMessage(Session session,
                             String token,
                             String chatId,
                             String text,
                             Keyboard keyboard) throws TelegramApiException {
    return sendMessage(session, token, chatId, text, null, keyboard);
  }

  @Override
  public Message sendMessage(Session session,
                             String token,
                             String chatId,
                             String text) throws TelegramApiException {
    return sendMessage(session, token, chatId, text, null);
  }

  @Override
  public void editMessage(Session session,
                          String token,
                          String chatId,
                          String messageId,
                          String text,
                          InlineKeyboardMarkup keyboard) throws TelegramApiException {

    acquireChatLimit(session, chatId);

    final EditMessageText method = new EditMessageText();
    method.setChatId(chatId);
    method.setMessageId(messageId);
    method.setText(text);
    // Always using HTML seems quite safe.
    method.setParseMode("HTML");
    if (keyboard != null) {
      method.setReplyMarkup(keyboard);
    }

    if (Context.getSadsRequest() != null) {
      detailedStatLogger.onOuterResponse(Context.getSadsRequest(), method);
    }

    call(token, method);
  }

  @Override
  public void sendData(Session session,
                       String token,
                       String chatId,
                       ApiSendMethod method) throws TelegramApiException {

    acquireChatLimit(session, chatId);

    if (Context.getSadsRequest() != null) {
      detailedStatLogger.onOuterResponse(Context.getSadsRequest(), method);
    }

    call(token, method);
  }

  @Override
  public User getMe(String token) throws TelegramApiException {
    return call(token, new GetMe());
  }

  @Override
  public File getFile(String token, String fileId) throws TelegramApiException {
    final GetFile method = new GetFile(fileId);
    if (Context.getSadsRequest() != null) {
      detailedStatLogger.onOuterResponse(Context.getSadsRequest(), method);
    }

    final File file = call(token, method);
    file.setUrl(
        StringUtils.join(
            new String[]{baseUrl, "file", "bot" + token, file.getFilePath()},
            "/"
        )
    );
    return file;
  }

  private void acquireChatLimit(Session session, String chatId) {
    try {
      if (session != null) {
        RateLimiter rateLimiter = (RateLimiter) session.getAttribute("rate-limiter");
        if (rateLimiter == null) {
          rateLimiter = RateLimiter.create(limitChatMessagesPerSecond);
          session.setAttribute("rate-limiter", rateLimiter);
        }

        rateLimiter.acquire();
      }

    } catch (Exception e) {
      log.error("Telegram chat messages-per-second limit enforcing failed", e);
    }
  }

  private void acquireOverallLimit() {
    messagesPerSecondLimit.acquire();
  }

  private <R extends ApiType, M extends BaseApiMethod<M, R>> R call(
      String token, M method) throws TelegramApiException {

    acquireOverallLimit();
    return getClient(token).call(method);
  }

  @SuppressWarnings("unused")
  public static class Factory implements ResourceFactory {

    @Override
    public TelegramApi build(String id,
                             Properties properties,
                             HierarchicalConfiguration config) throws Exception {

      final HttpDataLoader loader = SADSInitUtils.getResource("loader", properties);
      final DetailedStatLogger detailedStatLogger = SADSInitUtils.getResource("detailed-stat-logger", properties);

      return new TelegramApiImpl(loader, detailedStatLogger, properties);
    }

    @Override public boolean isHeavyResource() { return false; }
  }
}
