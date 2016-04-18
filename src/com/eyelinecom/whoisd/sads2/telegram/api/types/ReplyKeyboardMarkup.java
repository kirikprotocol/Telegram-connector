package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.eyelinecom.whoisd.sads2.common.ArrayUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

public class ReplyKeyboardMarkup extends Keyboard<ReplyKeyboardMarkup> {

  @Override
  protected Class<ReplyKeyboardMarkup> getEntityClass() { return ReplyKeyboardMarkup.class; }

  private KeyboardButton[][] keyboard;

  /** Resize vertically for optimal fit. */
  @JsonInclude(NON_DEFAULT)
  @JsonProperty(value = "resize_keyboard", defaultValue = "false")
  private boolean resizeKeyboard;

  /** Hide the keyboard as soon as it's been used. */
  @JsonInclude(NON_DEFAULT)
  @JsonProperty(value = "one_time_keyboard", defaultValue = "false")
  private boolean oneTimeKeyboard;

  public ReplyKeyboardMarkup() {}

  public ReplyKeyboardMarkup(KeyboardButton[][] keyboard) {
    this.keyboard = keyboard;
  }

  public KeyboardButton[][] getKeyboard() {
    return keyboard;
  }

  public void setKeyboard(KeyboardButton[][] keyboard) {
    this.keyboard = keyboard;
  }

  public void setKeyboard(String[][] keyboard) {
    setKeyboard(ArrayUtil.transformArray(KeyboardButton.class, keyboard, TextButton.FROM_STRING));
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
