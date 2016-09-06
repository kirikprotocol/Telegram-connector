<%@ page import="static mobi.eyeline.utils.restclient.web.RestClient.post" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!

  private String handle(HttpServletRequest request) throws Exception {
    final String msisdn = request.getParameter("subscriber");

    final String response = request.getParameter("confirmed");
    final boolean isConfirmed = "ok".equals(response);

    if (isConfirmed) {
      verify(msisdn);
      return "CONFIRMATION_OK";

    } else {
      final Collection<VerificationEntry> entries = findVerificationEntries(msisdn);
      for (VerificationEntry entry : entries) {
        clearAll(entry.wnumber, entry.serviceId);

        sendGet(MOBILIZER_ROOT + "/push?" +
            "service=" + entry.serviceId +
            "&subscriber=" + entry.wnumber +
            "&protocol=" + entry.protocol +
            "&scenario=default-noinform");
      }

      return "CONFIRMATION_FAILED";
    }
  }

%>

<%
  final String target = handle(request);
%>

<% if (target.equals("CONFIRMATION_OK")) { %>
  <page version="2.0">
    <div>
      <%= _("ussd.dialog.end.message", request) %>
    </div>
    <navigation/>
  </page>

<% } else if (target.equals("CONFIRMATION_FAILED")) { %>
  <page version="2.0">
    <div>
      <%= _("ussd.dialog.end.message", request) %>
    </div>
    <navigation/>
  </page>

<% } %>