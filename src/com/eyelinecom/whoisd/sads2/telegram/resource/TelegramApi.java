package com.eyelinecom.whoisd.sads2.telegram.resource;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;

public interface TelegramApi {

  void registerWebHook(String token, String url) throws TelegramApiException;

  void unRegisterWebHook(String token) throws TelegramApiException;

}
