package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SendMessage extends ApiMethod<SendMessage, Message> {

  /**
   * Unique identifier for the target chat or username of the target channel
   * (in the format {@code @channelusername}).
   */
  @SuppressWarnings("SpellCheckingInspection")
  @XmlElement(name = "chat_id", required = true)
  private String chatId;

  /**
   * Text of the message to be sent.
   */
  @XmlElement(name = "text", required = true)
  private String text;

  /**
   * If the message is a reply, ID of the original message.
   */
  @XmlElement(name = "reply_to_message_id")
  private Integer replyToMessageId;

  /**
   * Additional interface options.
   *
   * A JSON-serialized object for a custom reply keyboard, instructions to hide keyboard
   * or to force a reply from the user.
   */
  @XmlElement(name = "reply_markup")
  private Keyboard replyMarkup;

  @XmlElement(name = "parse_mode")
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

  @Override
  public String getPath() {
    return "sendmessage";
  }

}
