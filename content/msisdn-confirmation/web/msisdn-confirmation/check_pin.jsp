<%@ page import="java.io.IOException" %>
<%@ page import="static mobi.eyeline.utils.restclient.web.RestClient.post" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!

  private String getPin(String wnumber, String serviceId) throws IOException {
    try {
      return new RestClient()
          .json(API_ROOT + "/profile/" + wnumber + "/services.auth-" + serviceId.replace(".", "_") + ".pin")
          .object()
          .getString("value");

    } catch (Exception e) {
      return null;
    }
  }

  private String handle(HttpServletRequest request) throws Exception {
    final String wnumber = getWnumber(request);
    final String serviceId = request.getParameter("serviceId");
    final String safeSid = serviceId.replace(".", "_");

    final String enteredPin = request.getParameter("pin");
    final String sentPin = getPin(wnumber, serviceId);

    if (enteredPin != null && enteredPin.trim().equals(sentPin)) {
      final String enteredMsisdn = new RestClient()
          .json(
              API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".entered-msisdn")
          .object()
          .getString("value");

      verify(enteredMsisdn);
      return "PIN_OK";

    } else {
      final String protocol = request.getParameter("protocol");

      getLog().debug("Cancelling verification:" +
          " wnumber = [" + wnumber + "], serviceId = [" + serviceId + "]");

      clearAll(wnumber, serviceId);

      sendGet(MOBILIZER_ROOT + "/push?" +
          "service=" + serviceId +
          "&subscriber=" + wnumber +
          "&protocol=" + protocol +
          "&scenario=default-noinform");
      return "PIN_FAILED";
    }
  }

%>

<%
  final String target = handle(request);
%>

<% if (target.equals("PIN_OK")) { %>
  <page version="2.0"
        attributes="telegram.keep.session: true; skype.keep.session: true; mbf.keep.session: true">
    <div/>
    <navigation/>
  </page>

<% } else if (target.equals("PIN_FAILED")) { %>
  <page version="2.0"
        attributes="telegram.keep.session: true; skype.keep.session: true; mbf.keep.session: true">
    <div>
      <%= _("pin.failed", request) %>
    </div>
    <navigation/>
  </page>

<% } %>