package com.eyelinecom.whoisd.sads2.telegram.util;

import com.eyelinecom.whoisd.sads2.multipart.MultipartObjectMapper.MultipartObjectMappingException;
import com.eyelinecom.whoisd.sads2.multipart.RequestPartSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonSerializer<T> extends RequestPartSerializer<T> {

  @Override
  public String serialize(T obj) throws MultipartObjectMappingException {
    try {
      return MarshalUtils.marshal(obj);
    } catch (JsonProcessingException e) {
      throw new MultipartObjectMappingException(e);
    }
  }
}
