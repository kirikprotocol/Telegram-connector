package com.eyelinecom.whoisd.sads2.telegram.xslt;


import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static com.eyelinecom.whoisd.sads2.content.attributes.AttributeReader.getAttributes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AttributeReaderTest {

  @Test
  public void test1() throws Exception {
    final String text =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<page>\n" +
            "  <message>Hello!</message>\n" +
            "\n" +
            "  <button href=\"/one.jsp\" attributes=\"telegram.inline: true;\">One</button>\n" +
            "  <button href=\"/two.jsp\" attributes=\"telegram.inline: false;\">Two</button>\n" +
            "</page>";

    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));
    final Element root = rawDocument.getRootElement();

    final Element link1 = (Element) root.selectSingleNode("//button[@href='/one.jsp']");
    assertTrue(getAttributes(link1).getBoolean("telegram.inline").or(false));
    assertNull(getAttributes(link1).getBoolean("missing.property").orNull());

    final Element link2 = (Element) root.selectSingleNode("//button[@href='/two.jsp']");
    assertFalse(getAttributes(link2).getBoolean("telegram.inline").or(false));
  }

  @Test
  public void test2() throws Exception {
    final String text =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<page>\n" +
            "  <attributes>\n" +
            "    <attribute name=\"keywords\" value=\"kw1,kw2\"/>\n" +
            "  </attributes>\n" +
            "\n" +
            "  <message>\n" +
            "    <attributes>\n" +
            "      <attribute name=\"foo\" value=\"bar\"/>\n" +
            "      <attribute name=\"bar\">baz</attribute>\n" +
            "    </attributes>\n" +
            "\n" +
            "    Hello!\n" +
            "  </message>\n" +
            "</page>";

    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));
    final Element root = rawDocument.getRootElement();

    final Element page = (Element) root.selectSingleNode("//page");
    assertEquals("kw1,kw2", getAttributes(page).getString("keywords").orNull());

    final Element message = (Element) root.selectSingleNode("//message");

    assertEquals("bar", getAttributes(message).getString("foo").orNull());
    assertEquals("baz", getAttributes(message).getString("bar").orNull());
  }

  @Test
  public void testInherited() throws Exception {
    final String text =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<page>\n" +
            "  <attributes>\n" +
            "    <attribute name=\"foo\" value=\"bar1\"/>\n" +
            "    <attribute name=\"bar\">baz</attribute>\n" +
            "  </attributes>\n" +
            "\n" +
            "  <message>\n" +
            "    <attributes>\n" +
            "      <attribute name=\"foo\" value=\"bar2\"/>\n" +
            "    </attributes>\n" +
            "\n" +
            "    Hello!\n" +
            "  </message>\n" +
            "</page>";

    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));
    final Element root = rawDocument.getRootElement();

    final Element message = (Element) root.selectSingleNode("//message");

    assertEquals("bar2", getAttributes(message).getString("foo").orNull());
    assertEquals("baz", getAttributes(message).getString("bar").orNull());
  }

}
