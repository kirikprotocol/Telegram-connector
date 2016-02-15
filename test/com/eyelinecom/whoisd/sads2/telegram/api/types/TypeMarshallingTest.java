package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendMessage;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static com.eyelinecom.whoisd.sads2.telegram.api.MarshalUtils.parse;
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

  @Test
  public void testMarshalUpdate() throws Exception {
    final Update update = new Update();
    update.setUpdateId(657656097);

    final Message message = new Message();
    message.setMessageId(38);

    update.setMessage(message);

    final String json = update.marshal();
    assertEquals("{\"update_id\":657656097,\"message\":{\"message_id\":38}}", json);

    final Update obj = Update.unmarshal(new JSONObject(json), Update.class);
    assertEquals(json, obj.marshal());
  }

  @Test
  public void test4() throws JSONException, TelegramApiException {
    final String updateJson = "{\"update_id\":657656097,\"message\":{\"message_id\":38,\"from\":{\"id\":58403748,\"first_name\":\"vit\",\"username\":\"VitNote\"},\"chat\":{\"id\":58403748,\"first_name\":\"vit\",\"username\":\"VitNote\",\"type\":\"private\"},\"date\":1455522117,\"text\":\"\\/start\"}}";

    final Update update = ApiType.unmarshal(parse(updateJson), Update.class);
    assertEquals(657656097, (long) update.getUpdateId());
  }

}
