package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.multipart.FileUpload;
import com.eyelinecom.whoisd.sads2.telegram.api.BotApiClient;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendPhoto;

public class SendMethodSample {

  private static final String API_ROOT = "https://api.telegram.org";
  private static final String TOKEN = "185794693:AAHgiwUUtdF86BQF35ClJH1l_QPmwQF_F1I";

  private static final ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup() {{
    setResizeKeyboard(true);
    setKeyboard(new String[][] {
        new String[] { "a", "b" },
        new String[] { "c", "d" },
    });
  }};

  public static void main(String[] args) throws Exception {

    final BotApiClient client =
        new BotApiClient(TOKEN, API_ROOT, 1, new HttpDataLoader());

    final SendPhoto method = new SendPhoto();
    method.setChatId("96622607");
    method.setCaption("My photo caption");
    method.setPhotoFile(new FileUpload.ByteFileUpload(
        new java.io.File("/Users/andy/Downloads/IPK0MlK5JQo.jpg")
    ));

    method.setReplyMarkup(keyboard);

    final Message rc = client.call(method);
    System.out.println("rc = " + rc.marshal());
  }
}
