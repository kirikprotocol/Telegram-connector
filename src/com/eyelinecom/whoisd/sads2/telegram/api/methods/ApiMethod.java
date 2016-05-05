package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.common.TypeUtil;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public abstract class ApiMethod<Self extends ApiMethod, Response extends ApiType>
    extends BaseApiMethod<Self, Response> {

  private static final String METHOD_FIELD = "method";

  ApiMethod() {
    methodClass = TypeUtil.getGenericType(getClass(), 0);
    responseClass = TypeUtil.getGenericType(getClass(), 1);
  }

  public String marshal() throws TelegramApiException {
    //noinspection unchecked
    return marshal((Self) this, methodClass);
  }

  public String marshalAsWebHookResponse() throws TelegramApiException {
    try {
      final ObjectNode json = (ObjectNode) MarshalUtils.parse(marshal());
      json.put(METHOD_FIELD, getMethod());
      return json.toString();

    } catch (IOException e) {
      throw new TelegramApiException("Unable to marshal API method [" + this + "]", e);
    }
  }

  private static <T extends ApiMethod> String marshal(T obj,
                                                      Class<T> clazz) throws TelegramApiException {
    try {
      return MarshalUtils.marshal(obj);

    } catch (Exception e) {
      throw new TelegramApiException("Unable to marshal API method [" + obj + "]", e);
    }
  }
}
