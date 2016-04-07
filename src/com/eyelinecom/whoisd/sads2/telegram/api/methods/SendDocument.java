package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.multipart.FileUpload;
import com.eyelinecom.whoisd.sads2.multipart.RequestPart;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;

/**
 * Use this method to send general files. On success, the sent Message is returned.
 */
public class SendDocument extends ApiSendMethod<SendDocument, Message> {

  /**
   * file_id as String to resend a document that is already on the Telegram servers
   */
  @RequestPart(name = "document")
  private String document;

  /**
   * File to send.
   */
  @RequestPart(name = "document")
  private FileUpload documentFile;

  /**
   * Optional Document caption (may also be used when resending by file_id).
   */
  @RequestPart
  private String caption;

  public SendDocument() {}

  public String getDocument() {
    return document;
  }

  public void setDocument(String document) {
    this.document = document;
  }

  public FileUpload getDocumentFile() {
    return documentFile;
  }

  public void setDocumentFile(FileUpload documentFile) {
    this.documentFile = documentFile;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

}
