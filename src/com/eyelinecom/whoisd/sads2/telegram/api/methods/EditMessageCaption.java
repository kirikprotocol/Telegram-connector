package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.api.types.InlineKeyboardMarkup;
import com.eyelinecom.whoisd.sads2.telegram.api.types.VoidType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Use this method to edit captions of messages sent by the bot or via the bot (for inline bots).
 *
 * On success, if edited message is sent by the bot, the edited Message is returned,
 * otherwise True is returned.
 */
public class EditMessageCaption extends ApiMethod<EditMessageCaption, VoidType> {

  /**
   * Required if inline_message_id is not specified.
   * Unique identifier for the target chat or username of the target channel
   * (in the format @channelusername).
   */
  @JsonProperty("chat_id")
  private String chatId;

  /**
   * Required if inline_message_id is not specified. Unique identifier of the sent message.
   */
  @JsonProperty("message_id")
  private String messageId;

  /**
   * Required if chat_id and message_id are not specified. Identifier of the inline message.
   */
  @JsonProperty("inline_message_id")
  private String inlineMessageId;

  /**
   * Optional. New caption of the message.
   */
  @JsonProperty
  private String caption;

  /**
   * A JSON-serialized object for an inline keyboard.
   */
  @JsonProperty("reply_markup")
  private InlineKeyboardMarkup replyMarkup;

  public EditMessageCaption() {}

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

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public InlineKeyboardMarkup getReplyMarkup() {
    return replyMarkup;
  }

  public void setReplyMarkup(InlineKeyboardMarkup replyMarkup) {
    this.replyMarkup = replyMarkup;
  }
}
