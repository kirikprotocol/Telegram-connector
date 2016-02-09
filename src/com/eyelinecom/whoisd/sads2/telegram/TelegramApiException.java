package com.eyelinecom.whoisd.sads2.telegram;

public class TelegramApiException extends Exception {

  private final String apiResponse;

  public TelegramApiException(String message, Throwable cause) {
    super(message, cause);

    apiResponse = null;
  }

  public TelegramApiException(String message, String apiResponse) {
    super(message);
    this.apiResponse = apiResponse;
  }
}
