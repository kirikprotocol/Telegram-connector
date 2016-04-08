package com.eyelinecom.whoisd.sads2.telegram.api;

import com.eyelinecom.whoisd.sads2.common.DataUri;
import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.multipart.FileUpload;
import com.eyelinecom.whoisd.sads2.multipart.FileUpload.ByteFileUpload;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.ApiSendMethod;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendAudio;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendDocument;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendLocation;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendPhoto;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendSticker;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendVideo;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendVoice;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.text.ParseException;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;

public class Attachment {

  protected String type;

  protected String src;
  protected String fileName;

  protected Integer duration;

  protected Integer width;
  protected Integer height;

  protected String caption;

  protected String performer;

  protected String latitude;
  protected String longitude;

  public static Attachment parse(final Element e) {
    return new Attachment() {
      {
        type = str("type");

        src = str("src");
        fileName = str("fileName");

        duration = num("duration");

        width = num("width");
        height = num("height");

        caption = str("caption");

        performer = str("performer");

        latitude = str("latitude");
        longitude = str("longitude");
      }

      private String str(String attr)      { return trimToNull(e.attributeValue(attr)); }
      private Integer num(String attr)     { return tryParse(str(attr)); }

      private Integer tryParse(String val) {
        try                             { return Integer.parseInt(val); }
        catch (NumberFormatException e) { return null; }
      }

    };
  }

  public ApiSendMethod asTelegramMethod(final Logger log,
                                        final HttpDataLoader loader,
                                        final String resourceBaseUrl) {

    final Type type = Type.fromString(this.type);
    if (type == null) {
      return null;
    }

    switch (type) {
      case VIDEO:
        return new SendVideo() {{
          setVideo(asFileId());
          setVideoFile(asFileUpload(log, loader, resourceBaseUrl));
          setDuration(duration);
          setWidth(width);
          setHeight(height);
          setCaption(caption);
        }};

      case PHOTO:
        return new SendPhoto() {{
          setPhoto(asFileId());
          setPhotoFile(asFileUpload(log, loader, resourceBaseUrl));
          setCaption(caption);
        }};

      case VOICE:
        return new SendVoice() {{
          setVoice(asFileId());
          setVoiceFile(asFileUpload(log, loader, resourceBaseUrl));
        }};

      case DOCUMENT:
        return new SendDocument() {{
          setDocument(asFileId());
          setDocumentFile(asFileUpload(log, loader, resourceBaseUrl));
          setCaption(caption);
        }};

      case AUDIO:
        return new SendAudio() {{
          setAudio(asFileId());
          setAudioFile(asFileUpload(log, loader, resourceBaseUrl));
          setDuration(duration);
          setPerformer(performer);
          setTitle(caption);
        }};

      case STICKER:
        return new SendSticker() {{
          setSticker(asFileId());
          setStickerFile(asFileUpload(log, loader, resourceBaseUrl));
        }};

      case LOCATION:
        return new SendLocation() {{
          setLatitude(latitude);
          setLongitude(longitude);
        }};

      default:
        throw new AssertionError();
    }
  }

  private String asFileId() {
    return NumberUtils.isDigits(src) ? src : null;
  }

  private FileUpload asFileUpload(Logger log,
                                  HttpDataLoader loader,
                                  String resourceBaseUrl) {
    if (isBlank(src)) {
      return null;
    }

    try {
      final DataUri data = DataUri.parse(src);
      return new ByteFileUpload(
          data.getData(),
          isBlank(fileName) ? data.getFilename() : fileName
      );

    } catch (ParseException ignored) {}

    // Okay, that might be an URL.
    try {
      final String url = UrlUtils.merge(resourceBaseUrl, src);

      return new ByteFileUpload(
          loader.load(url).getBuffer(),
          isBlank(fileName) ? FilenameUtils.getName(url) : fileName
      );

    } catch (Exception e) {
      log.warn("", e);
      return null;
    }
  }

  enum Type {
    VIDEO(SendVideo.class),
    PHOTO(SendPhoto.class),
    VOICE(SendVoice.class),
    DOCUMENT(SendDocument.class),
    AUDIO(SendAudio.class),
    STICKER(SendSticker.class),
    LOCATION(SendLocation.class);

    private final Class<? extends ApiSendMethod> method;

    Type(Class<? extends ApiSendMethod> method) {
      this.method = method;
    }

    public Class<? extends ApiSendMethod> getMethod() {
      return method;
    }

    public static Type fromString(String val) {
      for (Type type : values()) {
        if (type.name().equalsIgnoreCase(val)) return type;
      }

      return null;
    }
  }
}
