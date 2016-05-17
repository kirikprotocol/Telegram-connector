package com.eyelinecom.whoisd.sads2.telegram.api;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.content.attachments.Attachment;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.ApiSendMethod;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendAudio;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendDocument;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendLocation;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendPhoto;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendSticker;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendVideo;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendVoice;
import com.google.common.base.Function;
import org.apache.log4j.Logger;

import static com.eyelinecom.whoisd.sads2.content.attachments.Attachment.Type;
import static com.eyelinecom.whoisd.sads2.content.attachments.Attachment.Type.fromString;

public class TgAttachmentMethodConverter
    implements Function<Attachment, ApiSendMethod> {

  private final Logger log;
  private final HttpDataLoader loader;
  private final String resourceBaseUrl;

  public TgAttachmentMethodConverter(Logger log,
                                     HttpDataLoader loader,
                                     String resourceBaseUrl) {

    this.log = log;
    this.loader = loader;
    this.resourceBaseUrl = resourceBaseUrl;
  }

  @Override
  public ApiSendMethod apply(final Attachment _) {

    final Type type = fromString(_.getType());
    if (type == null) {
      return null;
    }

    switch (type) {
      case VIDEO:
        return new SendVideo() {{
          setVideo(_.asFileId());
          setVideoFile(_.asFileUpload(log, loader, resourceBaseUrl));
          setDuration(_.getDuration());
          setWidth(_.getWidth());
          setHeight(_.getHeight());
          setCaption(_.getCaption());
        }};

      case PHOTO:
        return new SendPhoto() {{
          setPhoto(_.asFileId());
          setPhotoFile(_.asFileUpload(log, loader, resourceBaseUrl));
          setCaption(_.getCaption());
        }};

      case VOICE:
        return new SendVoice() {{
          setVoice(_.asFileId());
          setVoiceFile(_.asFileUpload(log, loader, resourceBaseUrl));
        }};

      case DOCUMENT:
        return new SendDocument() {{
          setDocument(_.asFileId());
          setDocumentFile(_.asFileUpload(log, loader, resourceBaseUrl));
          setCaption(_.getCaption());
        }};

      case AUDIO:
        return new SendAudio() {{
          setAudio(_.asFileId());
          setAudioFile(_.asFileUpload(log, loader, resourceBaseUrl));
          setDuration(_.getDuration());
          setPerformer(_.getPerformer());
          setTitle(_.getCaption());
        }};

      case STICKER:
        return new SendSticker() {{
          setSticker(_.asFileId());
          setStickerFile(_.asFileUpload(log, loader, resourceBaseUrl));
        }};

      case LOCATION:
        return new SendLocation() {{
          setLatitude(_.getLatitude());
          setLongitude(_.getLongitude());
        }};

      default:
        throw new AssertionError();
    }
  }

}
