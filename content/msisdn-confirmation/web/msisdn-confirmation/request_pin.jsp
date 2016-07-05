<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<page version="2.0">

  <div>
    <input navigationId="submit"
           name="pin"
           title="<%= _("request.pin", request)%>"/>
  </div>
  <navigation id="submit">
    <link pageId="check_pin.jsp">OK</link>
  </navigation>

  <navigation>
    <link pageId="cancel.jsp"><%= _("cancel", request) %></link>
  </navigation>
</page>