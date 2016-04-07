package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.multipart.FileUpload;
import com.eyelinecom.whoisd.sads2.multipart.RequestPart;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;

/**
 * Use this method to send voice notes, if you want Telegram clients to display
 * the file as a playable voice message.
 * For this to work, your audio must be in an .ogg file encoded with OPUS
 * (other formats may be sent as Audio or Document).
 */
public class SendVoice extends ApiSendMethod<SendVoice, Message> {

  /**
   * Audio file to send. file_id as String to resend an audio that is already on the Telegram servers
   */
  @RequestPart(name = "voice")
  private String voice;

  /**
   * Audio file to send.
   */
  @RequestPart(name = "voice")
  private FileUpload voiceFile;

  public SendVoice() {
    super();
  }

  public String getVoice() {
    return voice;
  }

  public void setVoice(String voice) {
    this.voice = voice;
  }

  public FileUpload getVoiceFile() {
    return voiceFile;
  }

  public void setVoiceFile(FileUpload voiceFile) {
    this.voiceFile = voiceFile;
  }

}
