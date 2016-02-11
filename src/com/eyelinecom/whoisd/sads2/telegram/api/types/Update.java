package com.eyelinecom.whoisd.sads2.telegram.api.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This object represents an incoming update.
 * Only one of the optional parameters can be present in any given update.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Update extends ApiType<Update> {

  @XmlElement(name = "update_id")
  private Integer updateId;

  // Optional. New incoming message of any kind — text, photo, sticker, etc.
  @XmlElement(name = "message")
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
