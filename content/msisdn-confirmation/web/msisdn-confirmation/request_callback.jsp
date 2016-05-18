<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<page version="2.0">
  <div>
    <%= _("request.callback", request) %>
  </div>
  <navigation>
    <link pageId="cancel.jsp"><%= _("cancel", request) %></link>
  </navigation>
</page>