package com.eyelinecom.whoisd.sads2.telegram.util;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class Matchers {

  /**
   * Same as Hamcrest's own {@linkplain IsEqualIgnoringWhiteSpace},
   * but displays un-stripped string on error.
   */
  @SuppressWarnings("JavadocReference")
  public static Matcher<String> equalToIgnoringWhiteSpace(final boolean stripAllSpaces,
                                                          final String expectedString) {

    return new IsEqualIgnoringWhiteSpace(expectedString) {

      @Override
      public String stripSpace(String toBeStripped) {
        if (!stripAllSpaces) {
          return super.stripSpace(toBeStripped);

        } else {
          final StringBuilder result = new StringBuilder();
          for (int i = 0; i < toBeStripped.length(); i++) {
            final char c = toBeStripped.charAt(i);
            if (!Character.isWhitespace(c)) {
              result.append(c);
            }
          }

          return result.toString().trim();
        }

      }

    };
  }

  public static Matcher<String> equalToIgnoringWhiteSpace(String expectedString) {
    return equalToIgnoringWhiteSpace(false, expectedString);
  }

  public static Matcher<String> containsIgnoringWhiteSpace(final String expectedString) {
    return new IsEqualIgnoringWhiteSpace(expectedString) {

      private String stripSpaceAll(String toBeStripped) {
        return toBeStripped.replaceAll("\\s", "");
      }

      @Override
      public boolean matchesSafely(String item) {
        return stripSpaceAll(item).contains(stripSpaceAll(expectedString));
      }

    };
  }


  //
  //
  //

  private static class IsEqualIgnoringWhiteSpace extends TypeSafeMatcher<String> {
    private final String string;

    public IsEqualIgnoringWhiteSpace(String string) {
      if (string == null) {
        throw new IllegalArgumentException("Non-null value required by IsEqualIgnoringCase()");
      } else {
        this.string = string;
      }
    }

    @Override
    public boolean matchesSafely(String item) {
      return stripSpace(this.string).equalsIgnoreCase(stripSpace(item));
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("equalToIgnoringWhiteSpace(").appendValue(this.string).appendText(")");
    }

    // Hamcrest's implementation as-is.
    public String stripSpace(String toBeStripped) {
      final StringBuilder buf = new StringBuilder();
      boolean lastWasSpace = true;

      for (int i = 0; i < toBeStripped.length(); ++i) {
        char c = toBeStripped.charAt(i);
        if (Character.isWhitespace(c)) {
          if (!lastWasSpace) {
            buf.append(' ');
          }

          lastWasSpace = true;
        } else {
          buf.append(c);
          lastWasSpace = false;
        }
      }

      return buf.toString().trim();
    }

  }

}
