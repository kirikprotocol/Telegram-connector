package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;
import com.eyelinecom.whoisd.sads2.telegram.util.StringAsNumericSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonPropertyOrder({"chat_id", "text", "replyToMessageId", "replyMarkup", "parseMode"})
public class SendMessage extends ApiMethod<SendMessage, Message> {

  /**
   * Unique identifier for the target chat or username of the target channel
   * (in the format {@code @channelusername}).
   */
  @SuppressWarnings("SpellCheckingInspection")
  @JsonProperty(value = "chat_id", required = true)
  @JsonSerialize(using = StringAsNumericSerializer.class)
  private String chatId;

  /**
   * Text of the message to be sent.
   */
  @JsonProperty(required = true)
  private String text;

  /**
   * If the message is a reply, ID of the original message.
   */
  @JsonProperty(value = "reply_to_message_id")
  private Integer replyToMessageId;

  /**
   * Additional interface options.
   *
   * A JSON-serialized object for a custom reply keyboard, instructions to hide keyboard
   * or to force a reply from the user.
   */
  @JsonProperty(value = "reply_markup")
  private Keyboard replyMarkup;

  @JsonProperty(value = "parse_mode")
  private String parseMode;

  public SendMessage() {
    super();
  }

  public String getChatId() {
    return chatId;
  }

  public void setChatId(String chatId) {
    this.chatId = chatId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Integer getReplyToMessageId() {
    return replyToMessageId;
  }

  public void setReplyToMessageId(Integer replyToMessageId) {
    this.replyToMessageId = replyToMessageId;
  }

  public Keyboard getReplyMarkup() {
    return replyMarkup;
  }

  public void setReplyMarkup(Keyboard replyMarkup) {
    this.replyMarkup = replyMarkup;
  }

  public String getParseMode() {
    return parseMode;
  }

  public void setParseMode(String parseMode) {
    this.parseMode = parseMode;
  }

}
