package com.eyelinecom.whoisd.sads2.telegram.util;

import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.io.IOException;
import java.util.Objects;

import static com.eyelinecom.whoisd.sads2.telegram.util.MultipartMatchers.StringPartWrapper.wrap;


public class MultipartMatchers {

  public static Matcher<Part> isStringPart(final String name, final String value) {
    return new TypeSafeMatcher<Part>() {

      @Override
      public boolean matchesSafely(Part item) {
        return item instanceof StringPart &&
            (Objects.equals(name, item.getName()) &&
                Objects.equals(value, wrap((StringPart) item).getContent()));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("isStringPart(")
            .appendValue("name = [" + name + "], value = [" + value + "]")
            .appendText(")");
      }
    };
  }

  static class StringPartWrapper extends StringPart {

    public StringPartWrapper(StringPart impl) {
      super(impl.getName(), getContent(impl), impl.getCharSet());
    }

    private static String getContent(StringPart impl) {
      try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
        impl.send(buf);
        return buf.toString(impl.getCharSet())
            .replaceAll("(?s)(.+\r\n\r\n)", "")
            .replaceAll("\r\n$", "");

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public String getContent() {
      return getContent(this);
    }

    public static StringPartWrapper wrap(StringPart impl) {
      return new StringPartWrapper(impl);
    }
  }
}
