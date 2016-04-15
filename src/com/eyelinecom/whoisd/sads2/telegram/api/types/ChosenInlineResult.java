package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a result of an inline query that was chosen by the user
 * and sent to their chat partner.
 */
public class ChosenInlineResult {

  /**
   * The unique identifier for the result that was chosen
   */
  @JsonProperty(value = "result_id", required = true)
  private String resultId;

  /**
   * The user that chose the result
   */
  @JsonProperty(required = true)
  private User from;

  /**
   * Optional. Sender location, only for bots that require user location
   */
  @JsonProperty
  private Location location;

  /**
   * Optional. Identifier of the sent inline message.
   * <br/>
   * Available only if there is an inline keyboard attached to the message.
   * Will be also received in callback queries and can be used to edit the message.
   */
  @JsonProperty("inline_message_id")
  private String inlineMessageId;

  /**
   * The query that was used to obtain the result.
   */
  @JsonProperty
  private String query;

  public ChosenInlineResult() {}

  public String getResultId() {
    return resultId;
  }

  public void setResultId(String resultId) {
    this.resultId = resultId;
  }

  public User getFrom() {
    return from;
  }

  public void setFrom(User from) {
    this.from = from;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public String getInlineMessageId() {
    return inlineMessageId;
  }

  public void setInlineMessageId(String inlineMessageId) {
    this.inlineMessageId = inlineMessageId;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }
}
