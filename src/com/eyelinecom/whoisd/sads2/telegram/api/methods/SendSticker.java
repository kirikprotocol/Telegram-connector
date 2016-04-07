package com.eyelinecom.whoisd.sads2.telegram.api.methods;


import com.eyelinecom.whoisd.sads2.multipart.FileUpload;
import com.eyelinecom.whoisd.sads2.multipart.RequestPart;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;

/**
 * Use this method to send .webp stickers. On success, the sent Message is returned.
 */
public class SendSticker extends ApiSendMethod<SendSticker, Message> {

  /**
   * file_id as String to resend a sticker that is already on the Telegram servers
   */
  @RequestPart(name = "sticker")
  private String sticker;

  /**
   * Sticker to send.
   */
  @RequestPart(name = "sticker")
  private FileUpload stickerFile;

  public SendSticker() {}

  public String getSticker() {
    return sticker;
  }

  public void setSticker(String sticker) {
    this.sticker = sticker;
  }

  public FileUpload getStickerFile() {
    return stickerFile;
  }

  public void setStickerFile(FileUpload stickerFile) {
    this.stickerFile = stickerFile;
  }

}
