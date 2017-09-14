package com.eyelinecom.whoisd.sads2.telegram.xslt;


import com.eyelinecom.whoisd.sads2.common.XSLTransformer;
import com.eyelinecom.whoisd.sads2.telegram.util.Matchers;
import com.google.common.io.Resources;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TransformTest {

  @SuppressWarnings("FieldCanBeLocal")
  private final String XSL_RESOURCE = "/sads2-telegram.xsl";

  private XSLTransformer transformer;

  @Before
  public void setUp() throws Exception {
    transformer = new XSLTransformer(getClass().getResourceAsStream(XSL_RESOURCE));
  }

  private void checkTransform(String resourceRaw,
                              String resourceExpected) throws Exception {

    final Document rawDocument =
        new SAXReader().read(getClass().getResourceAsStream(resourceRaw));
    final String actual = transformer.transform(rawDocument).asXML();

    final String expected =
        Resources.toString(getClass().getResource(resourceExpected), UTF_8);

    Assert.assertThat(
        "Transformation result of [" + resourceRaw + "] differs from [" + resourceExpected + "]",
        actual,
        Matchers.equalToIgnoringWhiteSpace(true, expected));
//
//    Assert.assertEquals(expected, actual);
  }

  @Test
  public void test1() throws Exception {
    checkTransform("content-01.xml", "response-01.xml");
  }

  @Test
  public void test2() throws Exception {
    checkTransform("content-02.xml", "response-02.xml");
  }

  @Test
  public void test3() throws Exception {
    checkTransform("content-03.xml", "response-03.xml");
  }

  @Test
  public void test4() throws Exception {
    checkTransform("content-04.xml", "response-04.xml");
  }

  @Test
  public void test5() throws Exception {
    checkTransform("content-05.xml", "response-05.xml");
  }

  @Test
  public void test6() throws Exception {
    checkTransform("content-06.xml", "response-06.xml");
  }

  @Test
  public void test7() throws Exception {
    checkTransform("content-07.xml", "response-07.xml");
  }

  @Test
  public void test8() throws Exception {
    checkTransform("content-08.xml", "response-08.xml");
  }

  @Test
  public void test9() throws Exception {
    checkTransform("content-09.xml", "response-09.xml");
  }

  @Test
  public void test10() throws Exception {
    checkTransform("content-10.xml", "response-10.xml");
  }

  @Test
  public void test11() throws Exception {
    checkTransform("content-11.xml", "response-11.xml");
  }

  @Test
  public void test12() throws Exception {
    checkTransform("content-12.xml", "response-12.xml");
  }

  @Test
  public void test13() throws Exception {
    checkTransform("content-13.xml", "response-13.xml");
  }
}
