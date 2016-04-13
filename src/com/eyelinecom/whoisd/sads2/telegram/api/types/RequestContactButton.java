package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestContactButton extends KeyboardButton<RequestContactButton> {

  @JsonProperty("request_contact")
  private boolean requestContact = true;

  public RequestContactButton(String text) {
    setText(text);
  }
}
