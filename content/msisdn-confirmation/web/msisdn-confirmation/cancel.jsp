<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%
  final String serviceId = request.getParameter("serviceId");
  final String protocol = request.getParameter("protocol");
  final String wnumber = request.getParameter("user_id");

  getLog().debug("Cancelling verification:" +
      " wnumber = [" + wnumber + "], serviceId = [" + serviceId + "]");

  clearAll(wnumber, serviceId);

  sendGet(MOBILIZER_ROOT + "/push?" +
      "service=" + serviceId +
      "&subscriber=" + wnumber +
      "&protocol=" + protocol +
      "&scenario=default-noinform");
%>

<page version="2.0"
      attributes="telegram.keep.session: true; skype.keep.session: true; mbf.keep.session: true; vkontakte.keep.session: true; line.keep.session: true">
  <div/>
  <navigation/>
</page>