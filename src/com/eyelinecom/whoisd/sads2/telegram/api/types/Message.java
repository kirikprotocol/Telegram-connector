package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message extends ApiType<Message> {

  @JsonProperty(value = "message_id")
  private Integer messageId;

  private User from;

  /** Epoch */
  private Integer date;

  private Chat chat;

  /** For text messages, the actual UTF-8 text of the message. */
  private String text;

  @JsonProperty(value = "new_chat_participant")
  private User newChatParticipant;

  @JsonProperty(value = "left_chat_participant")
  private User leftChatParticipant;

  @JsonProperty(value = "group_chat_created")
  private Boolean groupchatCreated;

  @JsonProperty(value = "reply_to_message")
  private Message replyToMessage;

  /** Informs that the supergroup has been created */
  @JsonProperty(value = "supergroup_chat_created")
  private Boolean superGroupCreated;

  /** Informs that the channel has been created */
  @JsonProperty(value = "channel_chat_created")
  private Boolean channelChatCreated;

  /** The chat has been migrated to a chat with specified identifier */
  @JsonProperty(value = "migrate_to_chat_id")
  private Long migrateToChatId;

  /** The chat has been migrated from a chat with specified identifier */
  @JsonProperty(value = "migrate_from_chat_id")
  private Long migrateFromChatId;

  public Message() {}

  public Integer getMessageId() {
    return messageId;
  }

  public void setMessageId(Integer messageId) {
    this.messageId = messageId;
  }

  public User getFrom() {
    return from;
  }

  public void setFrom(User from) {
    this.from = from;
  }

  public Integer getDate() {
    return date;
  }

  public void setDate(Integer date) {
    this.date = date;
  }

  public Chat getChat() {
    return chat;
  }

  public void setChat(Chat chat) {
    this.chat = chat;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public User getNewChatParticipant() {
    return newChatParticipant;
  }

  public void setNewChatParticipant(User newChatParticipant) {
    this.newChatParticipant = newChatParticipant;
  }

  public User getLeftChatParticipant() {
    return leftChatParticipant;
  }

  public void setLeftChatParticipant(User leftChatParticipant) {
    this.leftChatParticipant = leftChatParticipant;
  }

  public Boolean getGroupchatCreated() {
    return groupchatCreated;
  }

  public void setGroupchatCreated(Boolean groupchatCreated) {
    this.groupchatCreated = groupchatCreated;
  }

  public Message getReplyToMessage() {
    return replyToMessage;
  }

  public void setReplyToMessage(Message replyToMessage) {
    this.replyToMessage = replyToMessage;
  }

  public Boolean getSuperGroupCreated() {
    return superGroupCreated;
  }

  public void setSuperGroupCreated(Boolean superGroupCreated) {
    this.superGroupCreated = superGroupCreated;
  }

  public Boolean getChannelChatCreated() {
    return channelChatCreated;
  }

  public void setChannelChatCreated(Boolean channelChatCreated) {
    this.channelChatCreated = channelChatCreated;
  }

  public Long getMigrateToChatId() {
    return migrateToChatId;
  }

  public void setMigrateToChatId(Long migrateToChatId) {
    this.migrateToChatId = migrateToChatId;
  }

  public Long getMigrateFromChatId() {
    return migrateFromChatId;
  }

  public void setMigrateFromChatId(Long migrateFromChatId) {
    this.migrateFromChatId = migrateFromChatId;
  }
}
