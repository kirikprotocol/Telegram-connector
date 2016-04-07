package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.multipart.FileUpload;
import com.eyelinecom.whoisd.sads2.multipart.RequestPart;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;

/**
 * Use this method to send photos. On success, the sent Message is returned.
 */
public class SendPhoto extends ApiSendMethod<SendPhoto, Message> {

  /**
   * file_id as String to resend a photo that is already on the Telegram servers
   */
  @RequestPart(name = "photo")
  private String photo;

  /**
   * Supported filename extensions: .jpg, .jpeg, .gif, .png, .tif, .bmp.
   */
  @RequestPart(name = "photo")
  private FileUpload photoFile;

  /**
   * Optional Photo caption (may also be used when resending photos by file_id).
   */
  @RequestPart
  private String caption;

  public SendPhoto() {}

  public String getPhoto() {
    return photo;
  }

  public void setPhoto(String photo) {
    this.photo = photo;
  }

  public FileUpload getPhotoFile() {
    return photoFile;
  }

  public void setPhotoFile(FileUpload photoFile) {
    this.photoFile = photoFile;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }
}
