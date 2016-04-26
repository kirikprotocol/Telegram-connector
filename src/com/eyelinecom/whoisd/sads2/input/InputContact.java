package com.eyelinecom.whoisd.sads2.input;

import com.eyelinecom.whoisd.sads2.wstorage.profile.Profile;

/**
 * Created by jeck on 31/03/16
 */
public class InputContact extends AbstractInputType<InputLocation>  {
  private String msisdn;

  private String name;

  /**
   * Optional, contact {@linkplain Profile#getWnumber()}.
   */
  private String id;

  public InputContact() {
  }

  public String getMsisdn() {
    return msisdn;
  }

  public void setMsisdn(String msisdn) {
    this.msisdn = msisdn;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  protected String getTypeValue() {
    return "contact";
  }
}
