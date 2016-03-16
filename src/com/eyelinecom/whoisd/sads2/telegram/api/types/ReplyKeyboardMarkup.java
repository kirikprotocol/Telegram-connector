package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReplyKeyboardMarkup extends Keyboard<ReplyKeyboardMarkup> {

  @Override
  protected Class<ReplyKeyboardMarkup> getEntityClass() { return ReplyKeyboardMarkup.class; }

  private String[][] keyboard;

  /** Resize vertically for optimal fit. */
  @JsonProperty(value = "resize_keyboard")
  private Boolean resizeKeyboard;

  /** Hide the keyboard as soon as it's been used. */
  @JsonProperty(value = "one_time_keyboard")
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
