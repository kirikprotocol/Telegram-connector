package com.eyelinecom.whoisd.sads2.telegram.resource;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.BotApiClient;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.ApiMethod;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class TelegramApiImpl implements TelegramApi {

  private final HttpDataLoader loader;
  private final String publicKey;
  private final String baseUrl;

  public TelegramApiImpl(HttpDataLoader loader, String publicKey, Properties properties) {
    this.loader = loader;
    this.publicKey = publicKey;
    this.baseUrl = properties.getProperty("base.url");
  }

  private BotApiClient getClient(String token) {
    return new BotApiClient(token, baseUrl, loader);
  }

  @Override
  public void registerWebHook(String token, String url) throws TelegramApiException {
    File certTmp = null;
    try {
      certTmp = File.createTempFile("tg", "pem");
      certTmp.deleteOnExit();

      FileUtils.writeStringToFile(certTmp, publicKey);

      getClient(token).setWebhook(url, certTmp.getAbsolutePath());

    } catch (IOException e) {
      throw new TelegramApiException("Registering WebHook failed", e);

    } finally {
      if (certTmp != null) {
        //noinspection ResultOfMethodCallIgnored
        certTmp.delete();
      }
    }
  }

  @Override
  public void unRegisterWebHook(String token) throws TelegramApiException {
    getClient(token).setWebhook(null, null);
  }

  public <R extends ApiType, M extends ApiMethod<M, R>> R call(String token,
                                                               M method) throws TelegramApiException {
    return getClient(token).call(method);
  }
}
