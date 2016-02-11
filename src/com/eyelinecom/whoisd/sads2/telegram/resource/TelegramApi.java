package com.eyelinecom.whoisd.sads2.telegram.resource;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Update;

public interface TelegramApi {

  String getServiceUrl(String serviceId, String token);

  void registerWebHook(String token, String url) throws TelegramApiException;
  void unRegisterWebHook(String token) throws TelegramApiException;

  void sendMessage(String token, String chatId, String text, Keyboard keyboard) throws TelegramApiException;
  void sendMessage(String token, String chatId, String text) throws TelegramApiException;

  Update readUpdate(String json) throws TelegramApiException;
}
