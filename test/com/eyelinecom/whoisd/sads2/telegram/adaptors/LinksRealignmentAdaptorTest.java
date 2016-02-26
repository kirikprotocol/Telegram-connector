package com.eyelinecom.whoisd.sads2.telegram.adaptors;

import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import junit.framework.AssertionFailedError;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LinksRealignmentAdaptorTest {

  private LinksRealignmentAdaptor adaptor;

  @Before
  public void setUp() {
    adaptor = initAdaptor(true, 5);
  }

  private LinksRealignmentAdaptor initAdaptor(final boolean enabled,
                                              final int maxLength) {

    return new LinksRealignmentAdaptor() {
      @Override protected int getMaxLineLength(ServiceConfig serviceConfig) { return maxLength; }
      @Override protected boolean isEnabled(ServiceConfig serviceConfig) { return enabled; }
      @Override protected void checkRequest(ContentResponse response) { }
      @Override protected ServiceConfig getServiceScenario(ContentResponse response) { return null; }
    };
  }

  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  private Document initDocument(String... buttons) {
    final StringBuilder xml = new StringBuilder();

    xml.append("" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<page>\n" +
        "  <message>Test</message>\n");

    for (int i = 0; i < buttons.length; i+=2) {
      final String text = buttons[i];
      final String row = buttons[i + 1];

      if (row != null) {
        xml.append("<button href=\"link\" row=\"" + row + "\">" + text + "</button>");

      } else {
        xml.append("<button href=\"link\">" + text + "</button>");
      }
    }

    xml.append("</page>");

    try {
      return new SAXReader()
          .read(new ByteArrayInputStream(xml.toString().getBytes()));

    } catch (DocumentException e) {
      throw new AssertionFailedError(e.getMessage());
    }
  }

  private String getRowMap(Document document) {
    @SuppressWarnings("unchecked")
    final List<Element> allButtons = (List<Element>) document.selectNodes("//button");

    final ArrayList<Map.Entry<Integer, List<Element>>> entries =
        new ArrayList<>(adaptor.buttonsByRow(allButtons).entrySet());

    final StringBuilder buf = new StringBuilder();
    buf.append("{");

    for (int nRow = 0; nRow < entries.size(); nRow++) {
      final Map.Entry<Integer, List<Element>> row = entries.get(nRow);

      buf.append(row.getKey()).append(": {");

      final List<Element> buttons = row.getValue();
      for (int i = 0; i < buttons.size(); i++) {
        final Element button = buttons.get(i);
        buf.append("'").append(button.getTextTrim()).append("'");
        if (i != buttons.size() - 1) {
          buf.append(", ");
        }
      }

      buf.append("}");

      if (nRow != entries.size() - 1) {
        buf.append(", ");
      }
    }

    buf.append("}");

    return buf.toString();
  }

  private void check(String expected, String... buttons) {
    try {
      assertEquals(
          expected,
          getRowMap(adaptor.transform(initDocument(buttons), null))
      );

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testNoOrder() {
    // No buttons
    check("{}");

    check("{1: {'12345'}}",
        "12345", null);

    check("{1: {'123456'}}",
        "123456", null);

    check("{1: {'12', '34'}, 2: {'5678'}}",
        "12", null,
        "34", null,
        "5678", null
        );
  }

  @Test
  public void test1() {
    check("{1: {'12', '34'}, 2: {'56', '78'}}",
        "12", "1",
        "34", "1",
        "56", "1",
        "78", "1"
    );

    check("{1: {'12', '34'}, 2: {'567890'}}",
        "12", "1",
        "34", "1",
        "567890", "1"
    );

    check("{1: {'12'}, 2: {'3456'}, 3: {'5678', '0'}}",
        "12", "1",
        "3456", "2",
        "5678", "2",
        "0", "3"
    );
  }

}
