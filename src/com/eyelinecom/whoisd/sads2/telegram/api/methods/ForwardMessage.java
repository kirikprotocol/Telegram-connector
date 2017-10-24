package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;
import com.eyelinecom.whoisd.sads2.telegram.util.StringAsNumericSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by jeck on 20/09/17.
 */
public class ForwardMessage extends ApiMethod<ForwardMessage, Message>  {
    /**
     * Unique identifier for the target chat or username of the target channel
     * (in the format {@code @channelusername}).
     */
    @SuppressWarnings("SpellCheckingInspection")
    @JsonProperty(value = "chat_id", required = true)
    @JsonSerialize(using = StringAsNumericSerializer.class)
    private String chatId;

    /**
     * Unique identifier for the chat where the original message was sent
     * (or channel username in the format {@code @channelusername})
     */
    @SuppressWarnings("SpellCheckingInspection")
    @JsonProperty(value = "from_chat_id", required = true)
    @JsonSerialize(using = StringAsNumericSerializer.class)
    private String fromChatId;

    /**
     * Sends the message silently. Users will receive a notification with no sound.
     */
    @JsonProperty(value = "disable_notification", required = false)
    private Boolean disableNotification;

    /**
     * Message identifier in the chat specified in from_chat_id
     */
    @JsonProperty(value = "message_id", required = true)
    private Integer messageId;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getFromChatId() {
        return fromChatId;
    }

    public void setFromChatId(String fromChatId) {
        this.fromChatId = fromChatId;
    }

    public Boolean getDisableNotification() {
        return disableNotification;
    }

    public void setDisableNotification(Boolean disableNotification) {
        this.disableNotification = disableNotification;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public ForwardMessage() {
        super();
    }
}
