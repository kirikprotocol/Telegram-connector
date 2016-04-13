package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestLocationButton extends KeyboardButton<RequestLocationButton> {

  @JsonProperty("request_location")
  private boolean requestLocation = true;

  public RequestLocationButton(String text) {
    setText(text);
  }
}
