package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.multipart.RequestPart;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;

/**
 * Use this method to send point on the map. On success, the sent Message is returned.
 */
public class SendLocation extends ApiSendMethod<SendLocation, Message> {

  /**
   * Latitude of location
   */
  @RequestPart
  private String latitude;

  /**
   * Longitude of location
   */
  @RequestPart
  private String longitude;

  public SendLocation() {}

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

}
