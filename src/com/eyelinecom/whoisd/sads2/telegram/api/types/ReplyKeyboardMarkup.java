package com.eyelinecom.whoisd.sads2.telegram.api.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReplyKeyboardMarkup extends Keyboard<ReplyKeyboardMarkup> {

  @Override
  protected Class<ReplyKeyboardMarkup> getEntityClass() { return ReplyKeyboardMarkup.class; }

  @XmlElement(name = "keyboard", type = String[][].class)
  private String[][] keyboard;

  /** Resize vertically for optimal fit. */
  @XmlElement(name = "resize_keyboard")
  private Boolean resizeKeyboard;

  /** Hide the keyboard as soon as it's been used. */
  @XmlElement(name = "one_time_keyboard")
  private Boolean oneTimeKeyboard;

  public ReplyKeyboardMarkup() {}

  public String[][] getKeyboard() {
    return keyboard;
  }

  public void setKeyboard(String[][] keyboard) {
    this.keyboard = keyboard;
  }

  public Boolean getResizeKeyboard() {
    return resizeKeyboard;
  }

  public void setResizeKeyboard(Boolean resizeKeyboard) {
    this.resizeKeyboard = resizeKeyboard;
  }

  public Boolean getOneTimeKeyboard() {
    return oneTimeKeyboard;
  }

  public void setOneTimeKeyboard(Boolean oneTimeKeyboard) {
    this.oneTimeKeyboard = oneTimeKeyboard;
  }
}
