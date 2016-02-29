package com.eyelinecom.whoisd.sads2.telegram.adaptors;

import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import junit.framework.AssertionFailedError;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

class LinksRealignmentAdaptorTestHarness {

  public LinksRealignmentAdaptor initAdaptor(final boolean enabled,
                                             final int maxLength) {

    return new LinksRealignmentAdaptor() {
      @Override protected int getMaxLineLength(ServiceConfig serviceConfig) { return maxLength; }
      @Override protected boolean isEnabled(ServiceConfig serviceConfig) { return enabled; }
      @Override protected void checkRequest(ContentResponse response) { }
      @Override protected ServiceConfig getServiceScenario(ContentResponse response) { return null; }
    };
  }

  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  public Document initDocument(String... buttons) {
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

  public String getRowMap(LinksRealignmentAdaptor adaptor,
                          Document document) {
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

  public void check(LinksRealignmentAdaptor adaptor,
                    String expected,
                    String... buttons) {
    try {
      assertEquals(
          expected,
          getRowMap(adaptor, adaptor.transform(initDocument(buttons), null))
      );

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
