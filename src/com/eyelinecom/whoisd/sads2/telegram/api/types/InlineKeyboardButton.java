package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This object represents one button of an inline keyboard.
 * You <strong>must</strong> use exactly one of the optional fields.
 */
public class InlineKeyboardButton extends ApiType<InlineKeyboardButton> {

  /**
   * Label text on the button.
   */
  @JsonProperty(required = true)
  private String text;

  /**
   * Optional. HTTP url to be opened when button is pressed
   */
  @JsonProperty
  private String url;

  /**
   * Optional. Data to be sent in a callback query to the bot when button is pressed
   */
  @JsonProperty("callback_data")
  private String callbackData;

  /**
   * Optional. If set, pressing the button will prompt the user to select one of their chats,
   * open that chat and insert the bot‘s username and the specified inline query in the input field.
   * Can be empty, in which case just the bot’s username will be inserted.
   *
   * <br/>
   *
   * Note: This offers an easy way for users to start using your bot in inline mode when they are
   * currently in a private chat with it. Especially useful when combined with
   * switch_pm… actions – in this case the user will be automatically returned to the chat
   * they switched from, skipping the chat selection screen.
   */
  @JsonProperty("switch_inline_query")
  private String switchInlineQuery;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getCallbackData() {
    return callbackData;
  }

  public void setCallbackData(String callbackData) {
    this.callbackData = callbackData;
  }

  public String getSwitchInlineQuery() {
    return switchInlineQuery;
  }

  public void setSwitchInlineQuery(String switchInlineQuery) {
    this.switchInlineQuery = switchInlineQuery;
  }
}
