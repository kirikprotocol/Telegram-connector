package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Hide current custom keyboard and display the default one.
 */
public class ReplyKeyboardHide extends Keyboard<ReplyKeyboardHide> {

  @Override
  protected Class<ReplyKeyboardHide> getEntityClass() { return ReplyKeyboardHide.class; }

  @JsonProperty(value = "hide_keyboard")
  private Boolean hideKeyboard = true;

  public ReplyKeyboardHide() {}

  public Boolean getHideKeyboard() {
    return hideKeyboard;
  }

  public void setHideKeyboard(Boolean hideKeyboard) {
    this.hideKeyboard = hideKeyboard;
  }
}
