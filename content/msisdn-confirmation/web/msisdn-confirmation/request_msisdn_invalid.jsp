<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<page version="2.0">
  <div>
    <input navigationId="submit"
           name="confirm_msisdn"
           title="<%= _("msisdn.invalid", request)%>"/>
  </div>
  <navigation id="submit">
    <link pageId="index.jsp"><%= _("ready", request)%></link>
  </navigation>
</page>