package com.eyelinecom.whoisd.sads2.telegram.connector;

import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.connector.DeliveryParameters;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSRequestFactory;
import com.eyelinecom.whoisd.sads2.connector.SadsRequestFactoryResource;
import com.eyelinecom.whoisd.sads2.executors.connector.SimpleSADSRequestFactory;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.eyelinecom.whoisd.sads2.telegram.session.ServiceSessionManager;
import com.eyelinecom.whoisd.sads2.wstorage.profile.ProfileStorage;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.Log;

import java.util.Properties;

public class SadsRequestFactoryResourceImpl implements SadsRequestFactoryResource {

  private final ServiceSessionManager sessionManager;
  private final ProfileStorage profileStorage;

  public SadsRequestFactoryResourceImpl(ServiceSessionManager sessionManager, ProfileStorage profileStorage) {
    this.sessionManager = sessionManager;
    this.profileStorage = profileStorage;
  }

  @Override
  public SADSRequestFactory getFactory(String subscriberId, String scenarioId, ServiceConfig serviceConfig, Log log, DeliveryParameters deliveryParameters, final Protocol protocol, String connectorName, String requestDescription, String resUri, String csName) {
    return new SimpleSADSRequestFactory(log, serviceConfig, scenarioId, subscriberId, protocol, csName, resUri, deliveryParameters, connectorName, requestDescription) {
      @Override
      protected SADSRequest newSadsRequest() {
        if (protocol == Protocol.TELEGRAM) {
          return new ExtendedSadsRequest();
        } else {
          return new SADSRequest();
        }
      }

      @Override
      public SADSRequest buildSADSRequest() throws Exception {
        final SADSRequest request = super.buildSADSRequest();

        if (request instanceof ExtendedSadsRequest) {
          final ExtendedSadsRequest ext = (ExtendedSadsRequest) request;
          ext.setSession(sessionManager.getSessionManager(ext.getServiceId()).getSession(ext.getAbonent()));
          ext.setProfile(profileStorage.find(ext.getAbonent()));
        }

        return request;
      }
    };
  }


  public static class Factory implements ResourceFactory {

    @Override
    public SadsRequestFactoryResource build(String id,
                                            Properties properties,
                                            HierarchicalConfiguration config) throws Exception {

      final ServiceSessionManager sessionManager =
          (ServiceSessionManager) SADSInitUtils.getResource("session-manager", properties);
      final ProfileStorage profileStorage =
          (ProfileStorage) SADSInitUtils.getResource("profile-storage", properties);

      return new SadsRequestFactoryResourceImpl(sessionManager, profileStorage);
    }

    @Override
    public boolean isHeavyResource() {
      return false;
    }
  }
}
