package com.eyelinecom.whoisd.sads2.telegram.adaptors;

import org.junit.Before;
import org.junit.Test;

public class LinksRealignmentAdaptorTest {

  private LinksRealignmentAdaptorTestHarness util =
      new LinksRealignmentAdaptorTestHarness();

  private LinksRealignmentAdaptor adaptor;

  @Before
  public void setUp() {
    adaptor = util.initAdaptor(true, 5);
  }

  private void check(String expected, String... buttons) {
    util.check(adaptor, expected, buttons);
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
