package com.eyelinecom.whoisd.sads2.telegram.resource;

import com.eyelinecom.whoisd.sads2.telegram.SessionManager;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.User;

public interface TelegramApi {

  String START_MESSAGE = "/start";

  String getServiceUrl(String serviceId, String token);

  void registerWebHook(String token, String url) throws TelegramApiException;
  void unRegisterWebHook(String token) throws TelegramApiException;

  void sendMessage(SessionManager sessionManager,
                   String token,
                   String chatId,
                   String text,
                   Keyboard keyboard) throws TelegramApiException;

  void sendMessage(SessionManager sessionManager,
                   String token,
                   String chatId,
                   String text) throws TelegramApiException;

  User getMe(String token) throws TelegramApiException;
}
