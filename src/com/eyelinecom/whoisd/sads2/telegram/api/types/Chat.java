package com.eyelinecom.whoisd.sads2.telegram.api.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Chat extends ApiType<Chat> {

  enum Type {
    PRIVATE("private"),
    GROUP("group"),
    CHANNEL("channel"),
    SUPERGROUP("supergroup");

    private final String type;
    Type(String type) {
      this.type = type;
    }
  }

  /** Unique identifier of this chat */
  @XmlElement(name = "id")
  private Long id;

  /** Type of the chat, one of “private”, “group” or “channel” */
  @XmlElement(name = "type")
  private String type;

  /** Title of the chat, only for channels and group chat */
  @XmlElement(name = "title")
  private String title;

  @XmlElement(name = "first_name")
  private String firstName;

  @XmlElement(name = "last_name")
  private String lastName;

  @XmlElement(name = "username")
  private String userName;

  public Chat() {}


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }
}
