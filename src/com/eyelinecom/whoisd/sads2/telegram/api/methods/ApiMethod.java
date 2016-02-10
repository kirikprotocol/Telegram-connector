package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.MarshalUtils;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import org.codehaus.jettison.json.JSONObject;

import java.lang.reflect.ParameterizedType;

public abstract class ApiMethod<Self extends ApiMethod, Response extends ApiType> {

  protected static final String METHOD_FIELD = "method";

  protected final Class<Self> methodClass;
  protected final Class<Response> responseClass;

  public ApiMethod() {
    this.methodClass = getEntityClass(0);
    this.responseClass = getEntityClass(1);
  }

  protected <X> Class<X> getEntityClass(int argNo) {
    final ParameterizedType genericSuperclass =
        (ParameterizedType) getClass().getGenericSuperclass();
    //noinspection unchecked
    return (Class<X>) genericSuperclass.getActualTypeArguments()[argNo];
  }

  public abstract String getPath();

  /**
   * Deserialize a json answer to the response type of a method.
   */
  public Response toResponse(JSONObject answer) throws TelegramApiException {
    return ApiType.unmarshalResult(answer, responseClass);
  }

  public String marshal() throws TelegramApiException {
    //noinspection unchecked
    return marshal((Self) this, methodClass);
  }

  protected static <T extends ApiMethod> String marshal(T obj,
                                                        Class<T> clazz) throws TelegramApiException {
    try {
      return MarshalUtils.marshal(obj, clazz);

    } catch (Exception e) {
      throw new TelegramApiException("Unable to marshal API method [" + obj + "]", e);
    }
  }
}
