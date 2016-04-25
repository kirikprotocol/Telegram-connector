package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.executors.interceptor.BlankConnectorInterceptor;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class RedirectInterceptor extends BlankConnectorInterceptor {

  @Override
  public void onOuterRequest(SADSRequest request, Object outerRequest) throws Exception {
    final HttpServletRequest req = (HttpServletRequest) outerRequest;

    final String pageId = req.getParameter("pageId");
    if (isNotBlank(pageId)) {

      final String serviceRoot = request.getServiceScenario().getAttributes().getProperty("start-page");
      final String redirectTarget = UrlUtils.merge(serviceRoot, pageId);

      final Log log = SADSLogger.getLogger(request.getServiceId(), getClass());
      if (log.isDebugEnabled()) {
        log.debug("Redirect attribute found in the request: [" + pageId + "]," +
            " redirecting to [" + redirectTarget + "]");
      }

      request.setResourceURI(redirectTarget);
    }

  }
}
