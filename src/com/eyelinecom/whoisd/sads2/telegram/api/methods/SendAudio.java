package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.multipart.FileUpload;
import com.eyelinecom.whoisd.sads2.multipart.RequestPart;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;

/**
 * Use this method to send audio files, if you want Telegram clients to display them in the music player.
 * Your audio must be in an .mp3 format. On success, the sent Message is returned.
 * Bots can currently send audio files of up to 50 MB in size, this limit may be changed in the future.
 *
 * For sending voice notes, use sendVoice method instead.
 */
public class SendAudio extends ApiSendMethod<SendAudio, Message> {

  /**
   * Audio file to send. file_id as String to resend an audio that is already on the Telegram servers
   */
  @RequestPart(name = "audio")
  private String audio;

  /**
   * Audio file to send.
   */
  @RequestPart(name = "audio")
  private FileUpload audioFile;

  /**
   * Duration of the audio in seconds as defined by sender.
   */
  @RequestPart
  private Integer duration;

  /**
   * Performer of sent audio.
   */
  @RequestPart
  private String performer;

  /**
   * Title of sent audio
   */
  @RequestPart
  private String title;

  public SendAudio() {
    super();
  }

  public String getAudio() {
    return audio;
  }

  public void setAudio(String audio) {
    this.audio = audio;
  }

  public FileUpload getAudioFile() {
    return audioFile;
  }

  public void setAudioFile(FileUpload audioFile) {
    this.audioFile = audioFile;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public String getPerformer() {
    return performer;
  }

  public void setPerformer(String performer) {
    this.performer = performer;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

}
