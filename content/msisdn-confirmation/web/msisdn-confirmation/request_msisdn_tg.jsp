<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<page version="2.0">
  <div>
    <input navigationId="submit"
           name="confirm_msisdn"
           title="<%= _("request.msisdn.tg", request)%>"/>
  </div>
  <navigation id="submit">
    <link pageId="index.jsp"><%= _("ready", request)%></link>
  </navigation>
  <navigation>
    <link pageId="telegram://request-contact"><%= _("send.contact", request)%></link>
  </navigation>
</page>