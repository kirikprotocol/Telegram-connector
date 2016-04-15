package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This object represents an incoming inline query.
 * When the user sends an empty query, your bot could return some default or trending results.
 */
public class InlineQuery {

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
   * Optional. Sender location, only for bots that request user location.
   */
  @JsonProperty
  private Location location;

  /**
   * Text of the query.
   */
  private String query;

  /**
   * Offset of the results to be returned, can be controlled by the bot
   */
  private String offset;

  public InlineQuery() {}

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

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getOffset() {
    return offset;
  }

  public void setOffset(String offset) {
    this.offset = offset;
  }
}
