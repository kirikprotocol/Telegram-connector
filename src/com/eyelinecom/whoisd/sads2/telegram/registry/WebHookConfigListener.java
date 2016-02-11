package com.eyelinecom.whoisd.sads2.telegram.registry;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.exception.ConfigurationException;
import com.eyelinecom.whoisd.sads2.exception.NotFoundResourceException;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSInitializer;
import com.eyelinecom.whoisd.sads2.registry.Config;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfigListener;
import com.eyelinecom.whoisd.sads2.resource.ResourceStorage;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;

import java.util.HashMap;
import java.util.Map;

public class WebHookConfigListener extends ServiceConfigListener {

  public static final String CONF_TOKEN = "telegram.token";

  private final ResourceStorage resourceStorage = SADSInitializer.getResourceStorage();

  private final Map<String, String> serviceToToken = new HashMap<>();

  @Override
  protected void process(Config config) throws ConfigurationException {
    final String serviceId = config.getId();

    if (config.isEmpty()) {
      unRegisterWebHook(serviceId);

    } else if (config instanceof ServiceConfig) {
      final ServiceConfig serviceConfig = (ServiceConfig) config;
      final String token = InitUtils.getString(CONF_TOKEN, null, serviceConfig.getAttributes());
      if (token == null) {
        unRegisterWebHook(serviceId);

      } else {
        registerWebHook(serviceId, token);
      }
    }
  }

  private void registerWebHook(String serviceId,
                               String token) throws ConfigurationException {

    try {
      getApiClient().registerWebHook(token, getApiClient().getServiceUrl(serviceId, token));
      serviceToToken.put(serviceId, token);

    } catch (TelegramApiException | NotFoundResourceException e) {
      throw new ConfigurationException(serviceId, e.getMessage());
    }
  }

  private void unRegisterWebHook(String serviceId) throws ConfigurationException {
    try {
      final String token = serviceToToken.get(serviceId);
      if (token != null) {
        getApiClient().unRegisterWebHook(token);
        serviceToToken.remove(token);
      }

    } catch (TelegramApiException | NotFoundResourceException e) {
      throw new ConfigurationException(serviceId, e.getMessage());
    }
  }

  private TelegramApi getApiClient() throws NotFoundResourceException {
    return (TelegramApi) resourceStorage.get("telegram-api");
  }
}
