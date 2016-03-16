package com.eyelinecom.whoisd.sads2.telegram.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isNumeric;

/**
 * Serializes string property as numeric if it consists of integers only.
 */
public class StringAsNumericSerializer extends JsonSerializer<String> {

  @Override
  public void serialize(String str,
                        JsonGenerator gen,
                        SerializerProvider serializerProvider) throws IOException {
    gen.writeObject(
        isNumeric(str) ? Integer.valueOf(Integer.parseInt(str)) : str
    );
  }
}