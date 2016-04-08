package com.eyelinecom.whoisd.sads2.telegram.util;

import com.eyelinecom.whoisd.sads2.common.DataUri;
import org.junit.Test;

import java.nio.charset.Charset;
import java.text.ParseException;

import static com.eyelinecom.whoisd.sads2.common.DataUri.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class DataUriTest {

  @Test
  public void test1() throws Exception {
    final DataUri uri = parse(
        "data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==");

    assertEquals("image/gif", uri.getMime());
    assertNull(uri.getCharset());
    assertNull(uri.getFilename());
    assertNull(uri.getContentDisposition());
  }

  @Test
  public void test2() throws Exception {
    try {
      parse("dato:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==");
      fail();
    } catch (ParseException ignored) {}
  }


  @Test
  public void test3() throws Exception {
    final DataUri uri =
        parse("DaTa:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==");

    assertEquals("image/gif", uri.getMime());
    assertNull(uri.getCharset());
    assertNull(uri.getFilename());
    assertNull(uri.getContentDisposition());
  }

  @Test
  public void test4() throws Exception {
    try {
      parse("DaTa:image/gif;base64;R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==");
      fail();
    } catch (ParseException ignored) {}
  }

  @Test
  public void test5() throws Exception {
    final DataUri uri = parse("" +
        "data:image/gif;" +
        "charset=utf-8;" +
        "filename=test.txt;" +
        "content-disposition=inline;" +
        "base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==");

    assertEquals("image/gif", uri.getMime());
    assertEquals(Charset.forName("UTF-8"), uri.getCharset());
    assertEquals("test.txt", uri.getFilename());
    assertEquals("inline", uri.getContentDisposition());
  }
}