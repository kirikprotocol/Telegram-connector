package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TypeMarshallingTest {

  @Test
  public void test1() throws TelegramApiException {
    final SendMessage method = new SendMessage();
    method.setChatId("123456");
    method.setText("Hello");

    final String json = method.marshal();
    assertEquals("{\"chat_id\":123456,\"text\":\"Hello\"}", json);
  }

  @Test
  public void test2() throws TelegramApiException {
    final SendMessage method = new SendMessage();
    method.setChatId("123456");
    method.setText("Hello");

    final ReplyKeyboardMarkup kbd = new ReplyKeyboardMarkup();
    method.setReplyMarkup(kbd);

    kbd.setResizeKeyboard(true);
    kbd.setKeyboard(new String[][] {
        new String[] { "a", "b" },
        new String[] { "c", "d" },
    });

    final String json = method.marshal();

    assertEquals("{\"chat_id\":123456,\"text\":\"Hello\",\"reply_markup\":{\"keyboard\":[[\"a\",\"b\"],[\"c\",\"d\"]],\"resize_keyboard\":true}}", json);
  }

  @Test
  public void test3() throws TelegramApiException {
    final SendMessage method = new SendMessage();
    method.setChatId("user");
    method.setText("Hello");

    final Keyboard kbd = new ReplyKeyboardHide();
    method.setReplyMarkup(kbd);

    final String json = method.marshal();

    assertEquals("{\"chat_id\":\"user\",\"text\":\"Hello\",\"reply_markup\":{\"hide_keyboard\":true}}", json);
  }

}
