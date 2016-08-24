package com.eyelinecom.whoisd.sads2.telegram.connector;

import com.eyelinecom.whoisd.sads2.common.StoredHttpRequest;
import com.eyelinecom.whoisd.sads2.events.Event;
import com.eyelinecom.whoisd.sads2.profile.Profile;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Update;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Telegram WebHook request.
 * <br/>
 * Expected to land at {@literal <MOBILIZER_ROOT>/<TELEGRAM_CONNECTOR>/<telegram.token>/<service.id>}.
 */
public class TelegramWebhookRequest extends StoredHttpRequest {

  private Update update;

  private final String serviceToken;
  private final String serviceId;

  private transient Profile profile;
  private transient Event event;

  TelegramWebhookRequest(HttpServletRequest request) {
    super(request);

    final String[] parts = getRequestURI().split("/");
    serviceToken = parts[parts.length - 1];
    serviceId = parts[parts.length - 2];
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

  /**
   * Extract service token as a part of registered WebHook URL.
   */
  public String getServiceToken() {
    return serviceToken;
  }

  /**
   * Extract service ID as a part of registered WebHook URL.
   */
  public String getServiceId() {
    return serviceId;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }
}
