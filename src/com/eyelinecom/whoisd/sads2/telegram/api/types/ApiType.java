package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.eyelinecom.whoisd.sads2.common.TypeUtil;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class ApiType<T extends ApiType> {

  protected final Class<T> entityClass;

  public ApiType() {
    this.entityClass = getEntityClass();
  }

  protected Class<T> getEntityClass() {
    return TypeUtil.getGenericType(getClass(), 0);
  }

  public static <T extends ApiType> T unmarshal(JsonNode obj,
                                                Class<T> clazz) throws TelegramApiException {

    try {
      return MarshalUtils.unmarshal(obj, clazz);

    } catch (Exception e) {
      throw new TelegramApiException("Unable to unmarshal API object [" + obj + "]", e);
    }
  }

  public static <T extends ApiType> T unmarshalResult(JsonNode obj,
                                                      Class<T> clazz) throws TelegramApiException {

    try {
      if (clazz == VoidType.class) {
        //noinspection unchecked
        return (T) new VoidType();

      } else {
        return unmarshal(obj.get("result"), clazz);
      }

    } catch (Exception e) {
      throw new TelegramApiException("Unable to unmarshal API object [" + obj + "]", e);
    }
  }

  public String marshal() throws TelegramApiException {
    //noinspection unchecked
    return marshal((T) this, entityClass);
  }

  protected static <T extends ApiType> String marshal(T obj,
                                                      Class<T> clazz) throws TelegramApiException {
    try {
      return MarshalUtils.marshal(obj);

    } catch (Exception e) {
      throw new TelegramApiException("Unable to marshal API object [" + obj + "]", e);
    }
  }

  @Override
  public String toString() {
    try {
      return "ApiType{" +
          "entityClass=" + entityClass +
          ",json=" + marshal() +
          '}';
    } catch (Exception e) {
      return "ApiType{entityClass=" + entityClass + '}';
    }
  }
}
