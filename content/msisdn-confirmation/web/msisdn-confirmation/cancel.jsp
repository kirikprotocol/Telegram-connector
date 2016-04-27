<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%

  final String serviceId = request.getParameter("serviceId");
  final String safeSid = serviceId.replace(".", "_");

  final String wnumber = request.getParameter("subscriber");

  getLog().debug("Cancelling verification:" +
      " wnumber = [" + wnumber + "], serviceId = [" + serviceId + "]");

  new RestClient()
      .json(API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".service-id", delete());
  new RestClient()
      .json(API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".phase", delete());
  new RestClient()
      .json(API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".entered-msisdn", delete());

  new RestClient()
      .json(API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".MSISDN_CONFIRMATION_REDIRECTED", delete());

  sendGet(MOBILIZER_ROOT + "/push?" +
      "service=" + serviceId +
      "&subscriber=" + wnumber +
      "&protocol=telegram" +
      "&scenario=default-noinform");
%>

<page version="2.0" attributes="telegram.keep.session: true">
  <div/>
  <navigation/>
</page>