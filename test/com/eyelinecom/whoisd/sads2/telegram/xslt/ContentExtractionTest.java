package com.eyelinecom.whoisd.sads2.telegram.xslt;


import com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramPushInterceptor;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static junit.framework.Assert.assertEquals;

public class ContentExtractionTest {

  @Test
  public void test1() throws Exception {
    final String text = "<message>Text 1 <b>bold</b> text2</message>";
    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    final String content =
        TelegramPushInterceptor.getContent(rawDocument.getRootElement());

    assertEquals("Text 1 <b>bold</b> text2", content);
  }

  @Test
  public void test2() throws Exception {
    final String text =
        "<message>" +
            " Text 1 <b>bold</b> text2\n" +
            " Text3" +
            "</message>";
    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    final String content =
        TelegramPushInterceptor.getContent(rawDocument.getRootElement());

    assertEquals(
        "Text 1 <b>bold</b> text2\n" +
        "Text3", content);
  }

  @Test
  public void test3() throws Exception {
    final String text =
        "<message>" +
            " Text 1\n" +
            " <pre>\n" +
            "    Text\n" +
            "      Pre\n" +
            "</pre>\n" +
            "</message>";
    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    final String content =
        TelegramPushInterceptor.getContent(rawDocument.getRootElement());

    assertEquals(
      "Text 1\n" +
          "<pre>\n" +
          "    Text\n" +
          "      Pre\n" +
          "</pre>",
        content);
  }

  @Test
  public void test4() throws Exception {
    final String text =
      "<message>Welcome to Telegram formatting test! \n" +
          " This is a <b>bold</b> and <i>italic</i> text. \n" +
          " You can also use <strong>strong</strong> and <em>em</em> tags. \n" +
          " \n" +
          " This is an <code>inline code block</code>. And this is a pre-formatted block: \n" +
          " <pre>\n" +
          "      pre {\n" +
          "        white-space: pre-wrap;\n" +
          "      }\n" +
          "    </pre> \n" +
          "</message>";
    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    final String content =
        TelegramPushInterceptor.getContent(rawDocument.getRootElement());

    assertEquals(
        "Welcome to Telegram formatting test! \n" +
            "This is a <b>bold</b> and <i>italic</i> text. \n" +
            "You can also use <strong>strong</strong> and <em>em</em> tags. \n" +
            "\n" +
            "This is an <code>inline code block</code>. And this is a pre-formatted block: \n" +
            "<pre>\n" +
            "      pre {\n" +
            "        white-space: pre-wrap;\n" +
            "      }\n" +
            "    </pre>",
        content);
  }

  @Test
  public void test5() throws Exception {
    final String text =
        "<message>\n" +
            "Link example: <a href=\"http://google.com\">Google</a>\n" +
            "</message>";
    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    final String content =
        TelegramPushInterceptor.getContent(rawDocument.getRootElement());

    assertEquals(
        "Link example: <a href=\"http://google.com\">Google</a>",
        content);
  }

  @Test
  public void test6() throws Exception {
    final String text =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<page>\n" +
        "  <message>Multiple navigation blocks.</message>\n" +
        "  <button href=\"page.xml\" row=\"1\">Page 1</button>\n" +
        "  <button href=\"page.xml\" row=\"1\">Page 2</button>\n" +
        "  <button href=\"page.xml\" row=\"2\">Page 3</button>\n" +
        "</page>";

    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    @SuppressWarnings("ConstantConditions")
    final String kbd =
        TelegramPushInterceptor.getKeyboard(rawDocument, true, true).marshal();

    assertEquals(
        "{\"keyboard\":[[\"Page 1\",\"Page 2\"],[\"Page 3\"]],\"resize_keyboard\":true,\"one_time_keyboard\":true}",
        kbd);
  }

  @Test
  public void test7() throws Exception {
    final String text =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<page>\n" +
            "  <message>Multiple navigation blocks.</message>\n" +
            "  <button href=\"page.xml\" row=\"1\">Page 1</button>\n" +
            "  <button href=\"page.xml\" row=\"2\">Page 2</button>\n" +
            "</page>";

    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    @SuppressWarnings("ConstantConditions")
    final String kbd =
        TelegramPushInterceptor.getKeyboard(rawDocument, true, true).marshal();

    assertEquals(
        "{\"keyboard\":[[\"Page 1\"],[\"Page 2\"]],\"resize_keyboard\":true,\"one_time_keyboard\":true}",
        kbd);
  }

  @Test
  public void testSparseRows() throws Exception {
    final String text =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
          "<page>\n" +
          "  <message>\n" +
          "    Enter PIN:\n" +
          "\n" +
          "  </message>\n" +
          "  <input href=\"_1.jsp\" name=\"pin\"/>\n" +
          "  <button href=\"_2.jsp\" row=\"2\">Back</button>\n" +
          "</page>";

    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    @SuppressWarnings("ConstantConditions")
    final String kbd =
        TelegramPushInterceptor.getKeyboard(rawDocument, true, true).marshal();

    assertEquals(
        "{\"keyboard\":[[\"Back\"]],\"resize_keyboard\":true,\"one_time_keyboard\":true}",
        kbd);
  }

  @Test
  public void test8() throws Exception {
    final String text =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<page>\n" +
            "  <message>Message</message>\n" +
            "  <button href=\"/link-1\">123456</button>\n" +
            "  <button href=\"/link-2\">Text</button>\n" +
            "</page>";

    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    @SuppressWarnings("ConstantConditions")
    final String kbd =
        TelegramPushInterceptor.getKeyboard(rawDocument, true, true).marshal();

    assertEquals(
        "{\"keyboard\":[[\"123456\",\"Text\"]],\"resize_keyboard\":true,\"one_time_keyboard\":true}",
        kbd);
  }
}
