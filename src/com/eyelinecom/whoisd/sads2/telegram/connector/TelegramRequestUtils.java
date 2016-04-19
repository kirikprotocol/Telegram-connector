package com.eyelinecom.whoisd.sads2.telegram.connector;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Update;

import java.io.IOException;

import static com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType.unmarshal;
import static com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils.parse;

public class TelegramRequestUtils {

  public static Update parseUpdate(String webHookRequest) throws TelegramApiException {
    try {
      return unmarshal(parse(webHookRequest), Update.class);

    } catch (IOException e) {
      throw new TelegramApiException("Unable to read update message", e);
    }
  }

  public static String getChatId(String webHookRequest) throws TelegramApiException {
    final Update update = parseUpdate(webHookRequest);

    if (update.getMessage() != null) {
      return String.valueOf(update.getMessage().getChat().getId());

    } else {
      return String.valueOf(update.getCallbackQuery().getMessage().getChat().getId());
    }
  }

  public static String getMessageText(String webHookRequest) throws TelegramApiException {
    final Update update = parseUpdate(webHookRequest);
    return update.getMessage() != null ? update.getMessage().getText() : null;
  }

}
