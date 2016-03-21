<%@ page import="com.eyelinecom.whoisd.personalization.helpers.PersonalizationClient" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!

  private String handle(HttpServletRequest request) throws Exception {
    final PersonalizationClient client = getClient();
    final String chatId = request.getParameter("subscriber");

    final String serviceId = request.getParameter("serviceId");
    if (chatId != null && serviceId != null) {
      client.set(chatId, VAR_CHAT2SERVICE, serviceId, LIFE_TIME);
    }

    if (!client.isExists(chatId, VAR_PHASE)) {
      client.set(chatId, VAR_PHASE, PHASE_ASKED_FOR_MSISDN, LIFE_TIME);
      return "PAGE_REQUEST_MSISDN";

    } else {
      final String stage = client.getString(chatId, VAR_PHASE);

      if (PHASE_HAS_MSISDN.equals(stage)) {
        return "PAGE_REQUEST_CALLBACK";

      } else if (PHASE_ASKED_FOR_MSISDN.equals(stage)) {
        // Must be MSISDN.

        String enteredMsisdn = request.getParameter("confirm_msisdn");
        enteredMsisdn = normalize(enteredMsisdn);

        if (enteredMsisdn == null) {
          return "PAGE_REQUEST_MSISDN_INVALID";

        } else {
          client.set(chatId, VAR_PHASE, PHASE_HAS_MSISDN, LIFE_TIME);
          client.set(enteredMsisdn, VAR_MSISDN2CHAT, chatId, LIFE_TIME);

          return "PAGE_REQUEST_CALLBACK";
        }

      } else {
        throw new RuntimeException();
      }
    }
  }

  String normalize(String msisdn) {
    if (msisdn == null) {
      return null;
    }

    msisdn = msisdn.replaceAll("[^0-9]+", "");
    if (msisdn.length() < 11) {
      return null;
    }

    if (msisdn.length() == 11 && msisdn.startsWith("8")) {
      msisdn = msisdn.replaceFirst("8", "7");
    }

    return msisdn;
  }
%>

<%
  final String target = handle(request);
%>

<% if (target.equals("PAGE_REQUEST_MSISDN")) { %>
  <jsp:include page="request_msisdn.jsp" flush="true"/>

<% } else if (target.equals("PAGE_REQUEST_MSISDN_INVALID")) { %>
  <jsp:include page="request_msisdn_invalid.jsp" flush="true"/>

<% } else if (target.equals("PAGE_REQUEST_CALLBACK")) { %>
  <jsp:include page="request_callback.jsp" flush="true"/>

<% } %>

