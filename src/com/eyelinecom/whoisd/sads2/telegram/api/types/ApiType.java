package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import org.codehaus.jettison.json.JSONObject;

import java.lang.reflect.ParameterizedType;

public abstract class ApiType<T extends ApiType> {

  protected final Class<T> entityClass;

  public ApiType() {
    this.entityClass = getEntityClass();
  }

  /**
   * Note: should be overridden for indirect subclasses.
   */
  protected Class<T> getEntityClass() {
    final ParameterizedType genericSuperclass =
        (ParameterizedType) getClass().getGenericSuperclass();
    //noinspection unchecked
    return (Class<T>) genericSuperclass.getActualTypeArguments()[0];
  }

  public static <T extends ApiType> T unmarshal(JSONObject obj,
                                                Class<T> clazz) throws TelegramApiException {

    try {
      return MarshalUtils.unmarshal(obj, clazz);

    } catch (Exception e) {
      throw new TelegramApiException("Unable to unmarshal API object [" + obj + "]", e);
    }
  }

  public static <T extends ApiType> T unmarshalResult(JSONObject obj,
                                                      Class<T> clazz) throws TelegramApiException {

    try {
      if (clazz == VoidType.class) {
        //noinspection unchecked
        return (T) new VoidType();

      } else {
        return unmarshal(obj.getJSONObject("result"), clazz);
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
      return MarshalUtils.marshal(obj, clazz);

    } catch (Exception e) {
      throw new TelegramApiException("Unable to marshal API object [" + obj + "]", e);
    }
  }

}
