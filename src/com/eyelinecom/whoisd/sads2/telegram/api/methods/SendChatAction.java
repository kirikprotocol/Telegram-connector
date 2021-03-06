package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.api.types.VoidType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tell the user that something is happening on the bot's side.
 * The status is set for 5 seconds or less (once the next message arrives, Telegram clients clear
 * the typing status).
 */
public class SendChatAction extends ApiMethod<SendChatAction, VoidType> {

  @JsonProperty(value = "chat_id")
  private String chatId;

  private String action;

  public SendChatAction() {}

  public SendChatAction(String chatId, ChatAction action) {
    this.chatId = chatId;
    this.action = action.getName();
  }

  public String getChatId() {
    return chatId;
  }

  public void setChatId(String chatId) {
    this.chatId = chatId;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  @SuppressWarnings("unused")
  public enum ChatAction {

    TYPING("typing"),
    UPLOAD_PHOTO("upload_photo"),
    RECORD_VIDEO("record_video"),
    RECORD_AUDIO("record_audio"),
    UPLOAD_DOCUMENT("upload_document"),
    FIND_LOCATION("find_location");

    private final String name;
    ChatAction(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

}
