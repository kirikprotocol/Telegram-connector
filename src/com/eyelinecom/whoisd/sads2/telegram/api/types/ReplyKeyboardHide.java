package com.eyelinecom.whoisd.sads2.telegram.api.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Hide current custom keyboard and display the default one.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReplyKeyboardHide extends Keyboard<ReplyKeyboardHide> {

  @Override
  protected Class<ReplyKeyboardHide> getEntityClass() { return ReplyKeyboardHide.class; }

  @XmlElement(name = "hide_keyboard")
  private Boolean hideKeyboard;

  public ReplyKeyboardHide() {}

  public Boolean getHideKeyboard() {
    return hideKeyboard;
  }

  public void setHideKeyboard(Boolean hideKeyboard) {
    this.hideKeyboard = hideKeyboard;
  }
}
