package com.eyelinecom.whoisd.sads2.telegram.api.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data associated with
 * {@linkplain com.eyelinecom.whoisd.sads2.telegram.api.types.InlineKeyboardButton#callbackData}.
 */
public class InlineCallbackQuery {

  /**
   * Initial inline button `href` value.
   */
  @JsonProperty("callback_url")
  private String callbackUrl;

  public InlineCallbackQuery() {}

  public InlineCallbackQuery(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }
}
