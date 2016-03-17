package com.eyelinecom.whoisd.sads2.telegram.api;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.Loader;
import com.eyelinecom.whoisd.sads2.exception.DataLoadException;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.ApiMethod;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import com.eyelinecom.whoisd.sads2.telegram.util.Reiterator;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class BotApiClient {

  private static final Logger log = Logger.getLogger(BotApiClient.class);

  private final String token;
  private final String apiRoot;
  private final HttpDataLoader loader;

  private final int maxRateLimitRetries;

  public BotApiClient(String token,
                      String apiRoot,
                      int maxRateLimitRetries,
                      HttpDataLoader loader) {

    this.token = token;
    this.apiRoot = apiRoot;
    this.maxRateLimitRetries = maxRateLimitRetries;
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
            put(URL_FIELD, webHookURL == null ? "" : webHookURL);
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
      response = callRetrying(
          methodUrl(method.getPath()),
          method.marshal(),
          "application/json",
          "UTF-8",
          HttpDataLoader.METHOD_POST);

    } catch (DataLoadException e) {
      throw new TelegramApiException("Call failed with HTTP code [" + e.getStatus() + "]," +
          " API method = [" + method.getMethod() + "]", e);

    } catch (Exception e) {
      throw new TelegramApiException("Call failed, API method = [" + method.getMethod() + "]", e);
    }

    final JsonNode rc = validate(parse(response));
    return method.toResponse(rc);
  }

  /**
   * Retry request on HTTP-429 "Too Many Requests" response.
   * This error shouldn't actually occur due to rate limiting performed prior to API calls.
   */
  private Loader.Entity callRetrying(final String url,
                                     final String content,
                                     final String contentType,
                                     final String encoding,
                                     final String method) throws Exception {

    final long initialDelayMillis = TimeUnit.SECONDS.toMillis(1);

    return new Reiterator(maxRateLimitRetries) {

      @Override
      protected boolean shouldIgnore(Exception e) {
        return (e instanceof DataLoadException) &&
            ((DataLoadException) e).getStatus() == 429;
      }

      @Override
      protected void onBeforeCall(int nAttempt) {
        if (nAttempt > 0) {
          try {
            Thread.sleep(initialDelayMillis * (long) Math.pow(2, nAttempt - 1));
          } catch (InterruptedException ignored) {}
        }
      }

      @Override
      protected void onError(Exception e) {
        log.warn("Got error on Telegram API method call", e);
      }

    }.call(new Reiterator.Action<Loader.Entity>() {
      @Override
      public Loader.Entity call() throws Exception {
        return loader.load(url, content, contentType, encoding, method);
      }
    });
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  private JsonNode parse(Loader.Entity response) throws TelegramApiException {
    try {
      return MarshalUtils.parse(new String(response.getBuffer(), "UTF-8"));

    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);

    } catch (IOException e) {
      throw new TelegramApiException("Unable to parse response JSON", response.toString());
    }
  }

  private JsonNode validate(JsonNode json) throws TelegramApiException {
    return validate(json, null);
  }

  private JsonNode validate(JsonNode json, String message) throws TelegramApiException {
    JsonNode ok = json.get("ok");
    if (ok != null && ok.isBoolean() && ok.asBoolean()) {
      return json;

    } else {
      throw new TelegramApiException(message, json.toString());
    }
  }

  private String methodUrl(String method) {
    return apiRoot + "/bot" + token + "/" + method;
  }
}
