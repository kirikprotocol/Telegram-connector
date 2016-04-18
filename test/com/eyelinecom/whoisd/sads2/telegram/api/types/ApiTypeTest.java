package com.eyelinecom.whoisd.sads2.telegram.api.types;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ApiTypeTest {

  @Test
  public void test1() {
    final InlineKeyboardButton bean = new InlineKeyboardButton() {{
      setText("text");
    }};

    assertTrue(bean.getEntityClass() == InlineKeyboardButton.class);
  }

}
