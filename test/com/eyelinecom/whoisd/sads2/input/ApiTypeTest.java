package com.eyelinecom.whoisd.sads2.input;

import com.eyelinecom.whoisd.sads2.common.TypeUtil;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ApiTypeTest {

  @Test
  public void test1() {
    final InputFile bean = new InputFile() {{
      setUrl("foo");
    }};

    assertTrue((Class<?>) TypeUtil.getGenericType(bean.getClass(), 0) == InputFile.class);
  }
}
