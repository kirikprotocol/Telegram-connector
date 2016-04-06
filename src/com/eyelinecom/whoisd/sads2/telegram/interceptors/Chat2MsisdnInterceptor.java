package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import org.apache.commons.logging.Log;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Created by jeck on 18/02/16
 */
public class Chat2MsisdnInterceptor extends BlankInterceptor {

  @Override
  public void beforeContentRequest(SADSRequest request,
                                   ContentRequest contentRequest,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    final ExtendedSadsRequest tgRequest = (ExtendedSadsRequest) request;

    final String serviceId = request.getServiceId();
    final Log log = SADSLogger.getLogger(serviceId, serviceId, getClass());

    try {
      final String msisdn = tgRequest
          .getProfile()
          .query()
          .property("mobile", "msisdn")
          .getValue();

      if (isNotBlank(msisdn)) {
        contentRequest.getParameters().put("wnumber", contentRequest.getAbonent());
        contentRequest.setAbonent(msisdn);
      }
    } catch (Exception e) {
      log.warn("", e);
    }

  }

}
