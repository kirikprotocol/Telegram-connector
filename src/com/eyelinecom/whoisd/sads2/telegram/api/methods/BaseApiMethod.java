package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;

public abstract class BaseApiMethod<Self extends BaseApiMethod, Response extends ApiType> {

  Class<Self> methodClass;
  Class<Response> responseClass;

  /**
   * @return Method name, as should be passed in {@code method} parameter of a WebHook response.
   */
  @JsonIgnore
  public String getMethod() {
    return getPath();
  }

  /**
   * @return Method path as in API_ROOT/token/METHOD_PATH.
   */
  @JsonIgnore
  public String getPath() {
    return StringUtils.uncapitalize(methodClass.getSimpleName());
  }

  /**
   * Deserialize a json answer to the response type of a method.
   */
  public Response toResponse(JsonNode answer) throws TelegramApiException {
    return ApiType.unmarshalResult(answer, responseClass);
  }
}
