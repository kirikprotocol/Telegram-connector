package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SendMessage extends ApiMethod<SendMessage, Message> {

  /**
   * Chat ID to send the message to, username for channels.
   */
  @XmlElement(name = "chat_id")
  private String chatId;

  /**
   * Text of the message to be sent
   */
  @XmlElement(name = "text")
  private String text;

  /**
   * If the message is a reply, ID of the original message
   */
  @XmlElement(name = "reply_to_message_id")
  private Integer replyToMessageId;

  @XmlElement(name = "reply_markup")
  private Keyboard replyMarkup;

  public SendMessage() {
    super();
  }

  public String getChatId() {
    return chatId;
  }

  public void setChatId(String chatId) {
    this.chatId = chatId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Integer getReplyToMessageId() {
    return replyToMessageId;
  }

  public void setReplyToMessageId(Integer replyToMessageId) {
    this.replyToMessageId = replyToMessageId;
  }

  public Keyboard getReplyMarkup() {
    return replyMarkup;
  }

  public void setReplyMarkup(Keyboard replyMarkup) {
    this.replyMarkup = replyMarkup;
  }

  @Override
  public String getPath() {
    return "sendmessage";
  }

  @Override
  public Message toResponse(JSONObject answer) throws TelegramApiException {
    try {
      return Message.unmarshal(answer.getJSONObject("result"), Message.class);

    } catch (JSONException e) {
      throw new TelegramApiException("Unable to get response from [" + answer + "]", e);
    }
  }

}
