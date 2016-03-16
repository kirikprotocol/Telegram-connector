package com.eyelinecom.whoisd.sads2.telegram.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class MarshalUtils {

  private static final ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  public static JsonNode parse(String json) throws IOException {
    return mapper.readTree(json);
  }

  public static <T> T unmarshal(JsonNode obj, Class<T> clazz)
      throws IOException {

    //noinspection unchecked
    return (T) mapper.readerFor(clazz).readValue(obj);
  }

  public static <T> String marshal(T obj, Class<T> clazz) throws JsonProcessingException {
    return mapper.writer().writeValueAsString(obj);
  }

}
