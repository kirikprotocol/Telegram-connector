package com.eyelinecom.whoisd.sads2.telegram.resource;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.BotApiClient;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.ApiMethod;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendMessage;
import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Keyboard;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Update;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.telegram.api.MarshalUtils.parse;
import static com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType.unmarshal;

public class TelegramApiImpl implements TelegramApi {

  private final HttpDataLoader loader;
  private final String publicKeyPath;
  private final String baseUrl;
  private final String connectorBaseUrl;

  public TelegramApiImpl(HttpDataLoader loader, Properties properties) throws Exception {
    this.loader = loader;

    this.publicKeyPath = SADSInitUtils.getFilename("certificate.pem", properties);
    this.baseUrl = properties.getProperty("base.url");
    this.connectorBaseUrl = properties.getProperty("connector.url");
  }

  @Override
  public String getServiceUrl(String serviceId, String apiKey) {
    return StringUtils.join(new String[]{connectorBaseUrl, serviceId, apiKey}, "/");
  }

  private BotApiClient getClient(String token) {
    return new BotApiClient(token, baseUrl, loader);
  }

  @Override
  public void registerWebHook(String token, String url) throws TelegramApiException {
    getClient(token).setWebhook(url, publicKeyPath);
  }

  @Override
  public void unRegisterWebHook(String token) throws TelegramApiException {
    getClient(token).setWebhook(null, null);
  }

  @Override
  public void sendMessage(String token,
                          String chatId,
                          String text,
                          Keyboard keyboard) throws TelegramApiException {

    final SendMessage method = new SendMessage();
    method.setChatId(chatId);
    method.setText(text);
    if (keyboard != null) {
      method.setReplyMarkup(keyboard);
    }

    call(token, method);
  }

  @Override
  public void sendMessage(String token, String chatId, String text) throws TelegramApiException {
    sendMessage(token, chatId, text, null);
  }

  @Override
  public Update readUpdate(String json) throws TelegramApiException {
    try {
      return unmarshal(parse(json), Update.class);

    } catch (JSONException e) {
      throw new TelegramApiException("Unable to read update message", e);
    }
  }

  private <R extends ApiType, M extends ApiMethod<M, R>> R call(
      String token, M method) throws TelegramApiException {

    return getClient(token).call(method);
  }

    public static class Factory implements ResourceFactory {

      @Override
      public TelegramApi build(String id,
                               Properties properties,
                               HierarchicalConfiguration config) throws Exception {

        final HttpDataLoader loader =
            (HttpDataLoader) SADSInitUtils.getResource("loader", properties);

        return new TelegramApiImpl(loader, properties);
      }

      @Override public boolean isHeavyResource() { return false; }
    }
}
