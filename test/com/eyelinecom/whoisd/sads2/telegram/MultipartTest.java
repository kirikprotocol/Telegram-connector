package com.eyelinecom.whoisd.sads2.telegram;

import com.eyelinecom.whoisd.sads2.multipart.MultipartObjectMapper;
import com.eyelinecom.whoisd.sads2.multipart.RequestPart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.eyelinecom.whoisd.sads2.telegram.util.MultipartMatchers.isStringPart;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MultipartTest {

  private MultipartObjectMapper multipartObjectMapper;

  @Before
  public void setUp() {
    multipartObjectMapper = new MultipartObjectMapper();
  }

  @Test
  public void test1() throws Exception {

    class Test {
      @RequestPart
      private String field1 = "foo";

      public String getField1() { return field1; }

      public void setField1(String field1) { this.field1 = field1; }
    }

    final List<Part> obj = multipartObjectMapper.map(new Test());
    assertEquals(obj.size(), 1);

    assertThat(obj.get(0), isStringPart("field1", "foo"));
  }

  @Test
  public void test2() throws Exception {
    class Test {
      @RequestPart(name = "field2")
      private String field1 = "123";

      public String getField1() { return field1; }

      public void setField1(String field1) { this.field1 = field1; }
    }

    final List<Part> obj = multipartObjectMapper.map(new Test());
    assertEquals(obj.size(), 1);

    assertThat(obj.get(0), isStringPart("field2", "123"));
  }

  @Test
  public void test3() throws Exception {
    class Test {
      @RequestPart(name = "field2")
      private String field1 = null;

      public String getField1() { return field1; }

      public void setField1(String field1) { this.field1 = field1; }
    }

    final List<Part> obj = multipartObjectMapper.map(new Test());
    assertEquals("NULL values should be ignored", obj.size(), 0);
  }

  @Test
  public void test4() throws Exception {
    class Test {
      private String field1 = "abc";

      public String getField1() { return field1; }

      public void setField1(String field1) { this.field1 = field1; }
    }

    final List<Part> obj = multipartObjectMapper.map(new Test());
    assertEquals("No annotations present", obj.size(), 0);
  }

  @Test
  public void testAnnotationOnAccessor1() throws Exception {
    class Test {
      private String field1 = "abc";

      @RequestPart
      public String getField1() { return field1; }

      public void setField1(String field1) { this.field1 = field1; }
    }

    final List<Part> obj = multipartObjectMapper.map(new Test());
    assertEquals(obj.size(), 1);

    assertThat(obj.get(0), isStringPart("field1", "abc"));
  }

  @Test
  public void testAnnotationOnAccessor2() throws Exception {
    class Test {
      private String field1 = "abc";

      public String getField1() { return field1; }

      @RequestPart(name = "foobar")
      public void setField1(String field1) { this.field1 = field1; }
    }

    final List<Part> obj = multipartObjectMapper.map(new Test());
    assertEquals(obj.size(), 1);

    assertThat(obj.get(0), isStringPart("foobar", "abc"));
  }
}
