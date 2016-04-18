package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InlineKeyboardMarkup extends Keyboard<InlineKeyboardMarkup> {

  @Override
  protected Class<InlineKeyboardMarkup> getEntityClass() { return InlineKeyboardMarkup.class; }

  @JsonProperty(value = "inline_keyboard", required = true)
  private InlineKeyboardButton[][] keyboard;

  public InlineKeyboardMarkup() {}

  public InlineKeyboardMarkup(InlineKeyboardButton[][] keyboard) {
    this.keyboard = keyboard;
  }

  public InlineKeyboardButton[][] getKeyboard() {
    return keyboard;
  }

  public void setKeyboard(InlineKeyboardButton[][] keyboard) {
    this.keyboard = keyboard;
  }

}
