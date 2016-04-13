package com.eyelinecom.whoisd.sads2.telegram.api.types;

/**
 * @see RequestContactButton
 * @see RequestLocationButton
 * @see TextButton
 */
public abstract class KeyboardButton<T extends KeyboardButton> extends ApiType<T> {

  private String text;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
