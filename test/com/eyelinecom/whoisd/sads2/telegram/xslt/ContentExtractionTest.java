package com.eyelinecom.whoisd.sads2.telegram.xslt;


import com.eyelinecom.whoisd.sads2.telegram.connector.TelegramRequestUtils;
import com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramPushInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        TelegramPushInterceptor.getKeyboard(rawDocument).marshal();

    assertEquals(
        "{\"keyboard\":[[\"Page 1\",\"Page 2\"],[\"Page 3\"]]}",
        kbd);

    final TelegramRequestUtils.ExtLink[][] links =
        TelegramRequestUtils.collectExtLinks(rawDocument);

    assertEquals(
        "[" +
            "[" +
              "{\"href\":\"page.xml\",\"label\":\"Page 1\"}," +
              "{\"href\":\"page.xml\",\"label\":\"Page 2\"}" +
            "]," +
            "[" +
              "{\"href\":\"page.xml\",\"label\":\"Page 3\"}" +
            "]" +
        "]",
        MarshalUtils.marshal(links));
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
        TelegramPushInterceptor.getKeyboard(rawDocument).marshal();

    assertEquals(
        "{\"keyboard\":[[\"Page 1\"],[\"Page 2\"]]}",
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
        TelegramPushInterceptor.getKeyboard(rawDocument).marshal();

    assertEquals(
        "{\"keyboard\":[[\"Back\"]]}",
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
        TelegramPushInterceptor.getKeyboard(rawDocument).marshal();

    assertEquals(
        "{\"keyboard\":[[\"123456\",\"Text\"]]}",
        kbd);
  }

  @Test
  public void test9() throws Exception {
    final String text =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<page>\n" +
            "  <message>Test!</message>\n" +
            "  <attachment type=\"location\" latitude=\"55.008353\" longitude=\"82.935733\"/>\n" +
            "</page>";

    final Document doc =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    assertEquals(1, doc.getRootElement().elements("attachment").size());
  }

  @Test
  public void test10() throws Exception {
    final String text =
        "<message>  <b>Attention</b> this is very important task\n" +
            "</message>";
    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    final String content =
        TelegramPushInterceptor.getContent(rawDocument.getRootElement());

    assertEquals(
        "<b>Attention</b> this is very important task",
        content);
  }

  @Test
  public void test11() throws Exception {
    final String text =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<page>\n" +
            "  <message>Foo</message>" +
            "  <button href=\"/link-1\">Ok</button>\n" +
            "  <button href=\"telegram://request-contact\">Send my contact data</button>\n" +
            "  <button href=\"telegram://request-location\">Send my position</button>\n" +
            "</page>";

    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    @SuppressWarnings("ConstantConditions")
    final String kbd =
        TelegramPushInterceptor.getKeyboard(rawDocument).marshal();

    assertEquals(
        "{\"keyboard\":[[\"Ok\",{\"text\":\"Send my contact data\",\"request_contact\":true},{\"text\":\"Send my position\",\"request_location\":true}]]}",
        kbd);
  }

  @Test
  public void test12() throws Exception {
    final String text =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<page>\n" +
            "  <message>Enter PIN</message>\n" +
            "  <input href=\"submit.jsp\" name=\"input\" type=\"password\"/>\n" +
            "  <button href=\"/cancel.jsp\" row=\"2\">Cancel</button>\n" +
            "</page>";

    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));
    final Element root = rawDocument.getRootElement();

    assertNotNull(root.selectSingleNode("//input[@type='password']"));
    assertNull(root.selectSingleNode("//input[@type='hidden']"));
  }

  @Test
  public void test13() throws Exception {
    final String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<page><message>1\n" +
        "\n" +
        "  2\n" +
        "\n" +
        "\n" +
        "  3\n" +
        "\n" +
        "</message></page>";
    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    final String content =
        TelegramPushInterceptor.getContent(rawDocument.getRootElement());

    assertEquals(
        "<message>1\n" +
            "\n" +
            "2\n" +
            "\n" +
            "\n" +
            "3\n" +
            "\n" +
            "</message>",
        content
    );
  }

}
