package com.eyelinecom.whoisd.sads2.telegram.xslt;


import com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramPushInterceptor;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static junit.framework.Assert.assertEquals;

public class ContentExtractionTest {

  @Test
  public void test() throws Exception {
    final String text = "<message>Text 1 <b>bold</b> text2</message>";
    final Document rawDocument =
        new SAXReader().read(new ByteArrayInputStream(text.getBytes()));

    final String content =
        TelegramPushInterceptor.getContent(rawDocument.getRootElement());

    assertEquals("Text 1 <b>bold</b> text2", content);
  }

}
