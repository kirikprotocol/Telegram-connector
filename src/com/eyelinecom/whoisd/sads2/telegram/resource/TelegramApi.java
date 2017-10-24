package com.eyelinecom.whoisd.sads2.telegram.resource;

import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.ApiSendMethod;
import com.eyelinecom.whoisd.sads2.telegram.api.types.File;
import com.eyelinecom.whoisd.sads2.telegram.api.types.InlineKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;
import com.eyelinecom.whoisd.sads2.telegram.api.types.User;

public interface TelegramApi {

  String START_MESSAGE = "/start";

  String getServiceUrl(String serviceId, String token);

  void registerWebHook(String token, String url) throws TelegramApiException;
  void unRegisterWebHook(String token) throws TelegramApiException;

  Message sendMessage(Session session,
                      String token,
                      String chatId,
                      String text,
                      Integer replyToMessageId,
                      Keyboard keyboard) throws TelegramApiException;

  Message forwardMessage(Session session,
                      String token,
                      String chatId,
                      String fromChatId,
                      boolean disableNotification,
                      Integer messageId) throws TelegramApiException;

  Message sendMessage(Session session,
                      String token,
                      String chatId,
                      String text,
                      Keyboard keyboard) throws TelegramApiException;

  Message sendMessage(Session session,
                      String token,
                      String chatId,
                      String text) throws TelegramApiException;

  void editMessage(Session session,
                   String token,
                   String chatId,
                   String messageId,
                   String text,
                   InlineKeyboardMarkup keyboard) throws TelegramApiException;

  void sendData(Session session,
                String token,
                String chatId,
                ApiSendMethod method) throws TelegramApiException;

  User getMe(String token) throws TelegramApiException;

  File getFile(String token, String fileId)  throws TelegramApiException;

}
