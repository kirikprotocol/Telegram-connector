package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.AbonentUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.input.InputContact;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import java.io.IOException;
import java.util.Map;

/**
 * Created by jeck on 25/04/16.
 */
public class ContactAsInputInterceptor extends BlankInterceptor{
  @Override
  public void beforeContentRequest(SADSRequest request, ContentRequest contentRequest, RequestDispatcher dispatcher) throws InterceptionException {
    Log log = SADSLogger.getLogger(request.getServiceId(), request.getAbonent(), this.getClass());
    log.debug("I'm alive!!!!!");
    if (contentRequest.getParameters().containsKey("input_type")){
      log.debug("Got input type");
      String parameterName = null;
      String parameterValue = null;
      for (Map.Entry<String,String> pEntry: contentRequest.getParameters().entrySet()) {
        try {
          if (log.isDebugEnabled()) {
            log.debug("Try parameter: "+pEntry.getKey());
          }
          InputContact[] contactArray = MarshalUtils.unmarshal(MarshalUtils.parse(pEntry.getValue()), InputContact[].class);
          InputContact contact;
          if (contactArray!=null && contactArray.length == 1) {
            contact = contactArray[0];
          } else {
            log.debug("empty array, return");
            return;
          }
          if (log.isDebugEnabled()) {
            log.debug("Got contact: "+contact+ " ("+pEntry.getValue()+")");
          }
          if (StringUtils.isNotBlank(contact.getMsisdn())) {
            parameterName = pEntry.getKey();
            parameterValue = AbonentUtils.removePlus(contact.getMsisdn());
            break;
          }
        } catch (IOException e) {
          if (log.isDebugEnabled()) {
            log.debug("Parameter: "+pEntry.getKey()+" with value "+pEntry.getValue()+" contact test filed ");
          }
          continue;
        }
      }
      if (parameterValue!=null && parameterName!=null) {
        log.debug("request changed");
        contentRequest.getParameters().put(parameterName, parameterValue);
        contentRequest.getParameters().remove("input_type");
      } else {
        log.debug("request not changed");
      }
    }
  }
}
