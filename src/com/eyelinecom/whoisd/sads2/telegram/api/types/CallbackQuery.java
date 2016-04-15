package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CallbackQuery extends ApiType<CallbackQuery> {

  /**
   * Unique identifier for this query.
   */
  @JsonProperty(required = true)
  private String id;

  /**
   * Sender.
   */
  @JsonProperty(required = true)
  private User from;

  /**
   * Optional. Message with the callback button that originated the query.
   * Note that message content and message date will not be available if the message is too old.
   */
  @JsonProperty
  private Message message;

  /**
   * Optional. Identifier of the message sent via the bot in inline mode, that originated the query.
   */
  @JsonProperty("inline_message_id")
  private String inlineMessageId;

  /**
   * Data associated with the callback button.
   * Be aware that a bad client can send arbitrary data in this field.
   */
  @JsonProperty(required = true)
  private String data;

  public CallbackQuery() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public User getFrom() {
    return from;
  }

  public void setFrom(User from) {
    this.from = from;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public String getInlineMessageId() {
    return inlineMessageId;
  }

  public void setInlineMessageId(String inlineMessageId) {
    this.inlineMessageId = inlineMessageId;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
