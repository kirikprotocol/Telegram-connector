package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This object represents an incoming update.
 * Only one of the optional parameters can be present in any given update.
 */
@JsonPropertyOrder({"update_id", "message"})
public class Update extends ApiType<Update> {

  @JsonProperty(value = "update_id")
  private Integer updateId;

  // Optional. New incoming message of any kind — text, photo, sticker, etc.
  private Message message;

  public Update() {}

  public Integer getUpdateId() {
    return updateId;
  }

  public void setUpdateId(Integer updateId) {
    this.updateId = updateId;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }
}
