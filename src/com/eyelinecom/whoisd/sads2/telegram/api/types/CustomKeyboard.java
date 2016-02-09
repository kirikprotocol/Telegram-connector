package com.eyelinecom.whoisd.sads2.telegram.api.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomKeyboard extends Keyboard<CustomKeyboard> {

  @Override
  protected Class<CustomKeyboard> getEntityClass() { return CustomKeyboard.class; }

  @XmlElement(name = "keyboard")
  private List<List<String>> keyboard = new ArrayList<List<String>>();

  /** Resize vertically for optimal fit. */
  @XmlElement(name = "resize_keyboard")
  private Boolean resizeKeyboard;

  /** Hide the keyboard as soon as it's been used. */
  @XmlElement(name = "one_time_keyboard")
  private Boolean oneTimeKeyboard;

  public CustomKeyboard() {}

  public List<List<String>> getKeyboard() {
    return keyboard;
  }

  public void setKeyboard(List<List<String>> keyboard) {
    this.keyboard = keyboard;
  }

  public Boolean getResizeKeyboard() {
    return resizeKeyboard;
  }

  public void setResizeKeyboard(Boolean resizeKeyboard) {
    this.resizeKeyboard = resizeKeyboard;
  }

  public Boolean getOneTimeKeyboard() {
    return oneTimeKeyboard;
  }

  public void setOneTimeKeyboard(Boolean oneTimeKeyboard) {
    this.oneTimeKeyboard = oneTimeKeyboard;
  }
}
