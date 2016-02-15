package com.eyelinecom.whoisd.sads2.telegram.api;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.Loader;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.ApiMethod;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;

public class BotApiClient {

  private final String token;
  private final String apiRoot;
  private final HttpDataLoader loader;

  public BotApiClient(String token, String apiRoot, HttpDataLoader loader) {
    this.token = token;
    this.apiRoot = apiRoot;
    this.loader = loader;
  }

  public void setWebhook(final String webHookURL,
                         final String publicCertificatePath) throws TelegramApiException {

    final String PATH = "setwebhook";
    final String URL_FIELD = "url";
    final String CERTIFICATE_FIELD = "certificate";

    final Loader.Entity response;
    try {
      response = loader.postMultipart(
          methodUrl(PATH),
          Collections.<String, String>emptyMap(),
          Collections.<String, String>emptyMap(),
          new HashMap<String, String>() {{
            put(URL_FIELD, webHookURL);
          }},
          new HashMap<String, File>() {{
            if (publicCertificatePath != null) {
              put(CERTIFICATE_FIELD, new File(publicCertificatePath));
            }
          }}
      );

    } catch (Exception e) {
      throw new TelegramApiException("Failed setting webhook to [" + webHookURL + "]," +
          " token = [" + token + "]", e);
    }

    validate(parse(response),
        "Failed setting webhook to [" + webHookURL + "], token = [" + token + "]");
  }

  public <T extends ApiType> T call(ApiMethod<?, T> method) throws TelegramApiException {

    final Loader.Entity response;
    try {
      response = loader.load(
          methodUrl(method.getPath()),
          method.marshal(),
          "application/json",
          "UTF-8",
          HttpDataLoader.METHOD_POST);

    } catch (Exception e) {
      throw new TelegramApiException("Unable to call sendMessage", e);
    }

    final JSONObject rc = validate(parse(response));
    return method.toResponse(rc);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  private JSONObject parse(Loader.Entity response) throws TelegramApiException {
    try {
      return MarshalUtils.parse(new String(response.getBuffer(), "UTF-8"));

    } catch (JSONException e) {
      throw new TelegramApiException("Unable to parse response JSON", response.toString());

    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private JSONObject validate(JSONObject json) throws TelegramApiException {
    return validate(json, null);
  }

  private JSONObject validate(JSONObject json, String message) throws TelegramApiException {
    try {
      if (!json.getBoolean("ok")) {
        throw new TelegramApiException(message, json.toString());
      }
      return json;

    } catch (JSONException e) {
      throw new TelegramApiException(message, e);
    }
  }

  private String methodUrl(String method) {
    return apiRoot + "/" + token + "/" + method;
  }
}
