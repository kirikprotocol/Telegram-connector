<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<%@include file="common.jspf" %>

<%!
  private void handle(HttpServletRequest request) throws Exception {
    final String msisdn = request.getParameter("subscriber");
    verify(msisdn);
  }
%>

<%
  handle(request);
%>

<page version="2.0">
  <div/>

  <navigation>
    <link/>
  </navigation>
</page>