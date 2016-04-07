package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.multipart.RequestPart;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.util.JsonSerializer;

public abstract class ApiSendMethod<Self extends ApiSendMethod, Response extends ApiType>
    extends BaseApiMethod<Self, Response> {

  /**
   * Unique identifier for the chat to send the message to (Or username for channels)
   */
  @RequestPart(name = "chat_id", required = true)
  private String chatId;

  /**
   * Sends the message silently.
   */
  @RequestPart(name = "disable_notification")
  private Boolean disableNotification;

  /**
   * If the message is a reply, ID of the original message
   */
  @RequestPart(name = "reply_to_message_id")
  private Integer replyToMessageId;

  /**
   * Optional. JSON-serialized object for a custom reply keyboard.
   */
  @RequestPart(name = "reply_markup", serializer = JsonSerializer.class)
  private Keyboard replyMarkup;


  ApiSendMethod() {
    methodClass = getEntityClass(getClass(), 0);
    responseClass = getEntityClass(getClass(), 1);
  }

  public String getChatId() {
    return chatId;
  }

  public void setChatId(String chatId) {
    this.chatId = chatId;
  }

  public Boolean getDisableNotification() {
    return disableNotification;
  }

  public void setDisableNotification(Boolean disableNotification) {
    this.disableNotification = disableNotification;
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


}
