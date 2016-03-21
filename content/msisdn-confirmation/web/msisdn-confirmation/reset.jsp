<%@ page import="com.eyelinecom.whoisd.personalization.helpers.PersonalizationClient" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!
  private void handle(HttpServletRequest request) throws Exception {
    final PersonalizationClient client = getClient();

    final String chatId = request.getParameter("chatId");
    final String msisdn = request.getParameter("msisdn");

    if (client.isExists(chatId, VAR_PHASE)) {
      client.remove(chatId, VAR_PHASE);
    }

    if (client.isExists(chatId, VAR_CHAT2SERVICE)) {
      client.remove(chatId, VAR_CHAT2SERVICE);
    }

    if (client.isExists(msisdn, VAR_MSISDN2CHAT)) {
      client.remove(msisdn, VAR_MSISDN2CHAT);
    }

    if (client.isExists(chatId, "telegram-msisdn")) {
      client.remove(chatId, "telegram-msisdn");
    }
  }
%>

<%
  handle(request);
%>