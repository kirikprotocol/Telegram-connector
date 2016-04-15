package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This object represents an incoming update.
 * Only one of the optional parameters can be present in any given update.
 */
@JsonPropertyOrder({"update_id", "message", "inline_query", "chosen_inline_result", "callback_query"})
public class Update extends ApiType<Update> {

  /**
   * The update‘s unique identifier.
   * <p>
   * Update identifiers start from a certain positive number and increase sequentially.
   * This ID becomes especially handy if you’re using Webhooks, since it allows you to ignore
   * repeated updates or to restore the correct update sequence, should they get out of order.
   * </p>
   */
  @JsonProperty(value = "update_id", required = true)
  private Integer updateId;

  /**
   * Optional. New incoming message of any kind — text, photo, sticker, etc.
   */
  private Message message;

  /**
   * Optional. New incoming inline query.
   */
  @JsonProperty("inline_query")
  private InlineQuery inlineQuery;

  /**
   * Optional. The result of an inline query that was chosen by a user and sent to their chat partner.
   */
  @JsonProperty("chosen_inline_result")
  private ChosenInlineResult chosenInlineResult;

  /**
   * Optional. New incoming callback query
   */
  @JsonProperty("callback_query")
  private CallbackQuery callbackQuery;

  public Update() {}

  public Integer getUpdateId() {
    return updateId;
  }

  public void setUpdateId(Integer updateId) {
    this.updateId = updateId;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public InlineQuery getInlineQuery() {
    return inlineQuery;
  }

  public void setInlineQuery(InlineQuery inlineQuery) {
    this.inlineQuery = inlineQuery;
  }

  public ChosenInlineResult getChosenInlineResult() {
    return chosenInlineResult;
  }

  public void setChosenInlineResult(ChosenInlineResult chosenInlineResult) {
    this.chosenInlineResult = chosenInlineResult;
  }

  public CallbackQuery getCallbackQuery() {
    return callbackQuery;
  }

  public void setCallbackQuery(CallbackQuery callbackQuery) {
    this.callbackQuery = callbackQuery;
  }
}
