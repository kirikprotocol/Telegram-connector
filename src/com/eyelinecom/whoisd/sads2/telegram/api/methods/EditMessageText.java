package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.api.types.InlineKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;
import com.eyelinecom.whoisd.sads2.telegram.api.types.VoidType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Use this method to edit text messages sent by the bot or via the bot (for inline bots).
 * On success, if edited message is sent by the bot, the edited {@linkplain Message} is returned,
 * otherwise {@code True} is returned.
 */
public class EditMessageText extends ApiMethod<EditMessageText, VoidType> {

  /**
   * Required if inline_message_id is not specified.
   * Unique identifier for the target chat or username of the target channel
   * (in the format @channelusername).
   */
  @JsonProperty("chat_id")
  private String chatId;

  /**
   * Required if inline_message_id is not specified. Unique identifier of the sent message
   */
  @JsonProperty("message_id")
  private String messageId;

  /**
   * Required if chat_id and message_id are not specified. Identifier of the inline message.
   */
  @JsonProperty("inline_message_id")
  private String inlineMessageId;

  /**
   * New text of the message.
   */
  @JsonProperty(required = true)
  private String text;

  /**
   * Optional. Send Markdown or HTML, if you want Telegram apps to show bold, italic, fixed-width text or inline URLs in your bot's message.
   */
  @JsonProperty("parse_mode")
  private String parseMode;

  /**
   * Optional. Disables link previews for links in this message
   */
  @JsonProperty("disable_web_page_preview")
  private String disableWebPagePreview;

  /**
   * Optional	A JSON-serialized object for an inline keyboard.
   */
  @JsonProperty("reply_markup")
  private InlineKeyboardMarkup replyMarkup;

  public EditMessageText() {}

  public String getChatId() {
    return chatId;
  }

  public void setChatId(String chatId) {
    this.chatId = chatId;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getInlineMessageId() {
    return inlineMessageId;
  }

  public void setInlineMessageId(String inlineMessageId) {
    this.inlineMessageId = inlineMessageId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getParseMode() {
    return parseMode;
  }

  public void setParseMode(String parseMode) {
    this.parseMode = parseMode;
  }

  public String getDisableWebPagePreview() {
    return disableWebPagePreview;
  }

  public void setDisableWebPagePreview(String disableWebPagePreview) {
    this.disableWebPagePreview = disableWebPagePreview;
  }

  public InlineKeyboardMarkup getReplyMarkup() {
    return replyMarkup;
  }

  public void setReplyMarkup(InlineKeyboardMarkup replyMarkup) {
    this.replyMarkup = replyMarkup;
  }
}
