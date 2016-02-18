package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import org.codehaus.jettison.json.JSONException;
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

  /**
   * @return Method path as in API_ROOT/token/METHOD_PATH.
   */
  public abstract String getPath();

  /**
   * @return Method name, as should be passed in {@code method} parameter of a WebHook response.
   */
  public String getMethod() {
    return getPath();
  }

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

  public String marshalAsWebHookResponse() throws TelegramApiException {
    try {
      final JSONObject json = new JSONObject(marshal());
      json.put(METHOD_FIELD, getMethod());
      return json.toString();

    } catch (JSONException e) {
      throw new TelegramApiException("Unable to marshal API method [" + this + "]", e);
    }
  }

  private static <T extends ApiMethod> String marshal(T obj,
                                                      Class<T> clazz) throws TelegramApiException {
    try {
      return MarshalUtils.marshal(obj, clazz);

    } catch (Exception e) {
      throw new TelegramApiException("Unable to marshal API method [" + obj + "]", e);
    }
  }
}
