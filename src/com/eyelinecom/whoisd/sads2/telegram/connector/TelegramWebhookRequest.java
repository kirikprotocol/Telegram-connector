package com.eyelinecom.whoisd.sads2.telegram.connector;

import com.eyelinecom.whoisd.sads2.common.StoredHttpRequest;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Update;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class TelegramWebhookRequest extends StoredHttpRequest {

  private Update update;

  TelegramWebhookRequest(HttpServletRequest request) {
    super(request);
  }

  public Update asUpdate() throws IOException, TelegramApiException {
    if (update == null) {
      update = TelegramRequestUtils.parseUpdate(getContent());
    }

    return update;
  }

  public String getChatId() throws TelegramApiException, IOException {
    final Update update = asUpdate();

    if (update.getMessage() != null) {
      return String.valueOf(update.getMessage().getChat().getId());

    } else {
      return String.valueOf(update.getCallbackQuery().getMessage().getChat().getId());
    }
  }

  public String getMessageText() throws TelegramApiException, IOException {
    final Update update = asUpdate();
    return update.getMessage() != null ? update.getMessage().getText() : null;
  }
}
