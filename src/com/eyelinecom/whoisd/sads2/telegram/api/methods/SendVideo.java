package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.multipart.FileUpload;
import com.eyelinecom.whoisd.sads2.multipart.RequestPart;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;

/**
 * Use this method to send video files,
 * Telegram clients support mp4 videos (other formats may be sent as Document).
 * On success, the sent Message is returned.
 */
public class SendVideo extends ApiSendMethod<SendVideo, Message> {

  /**
   * file_id as String to resend a video that is already on the Telegram servers
   */
  @RequestPart(name = "video")
  private String video;

  @RequestPart(name = "video")
  private FileUpload videoFile;

  /**
   * Optional. Duration of sent video in seconds
   */
  @RequestPart
  private Integer duration;

  /**
   * Optional. Video width
   */
  @RequestPart
  private Integer width;

  /**
   * Optional. Video height.
   */
  @RequestPart
  private Integer height;

  /**
   * Optional Photo caption (may also be used when resending photos by file_id).
   */
  @RequestPart
  private String caption;


  public SendVideo() {}

  public String getVideo() {
    return video;
  }

  public void setVideo(String video) {
    this.video = video;
  }

  public FileUpload getVideoFile() {
    return videoFile;
  }

  public void setVideoFile(FileUpload videoFile) {
    this.videoFile = videoFile;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public Integer getWidth() {
    return width;
  }

  public void setWidth(Integer width) {
    this.width = width;
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = height;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

}
