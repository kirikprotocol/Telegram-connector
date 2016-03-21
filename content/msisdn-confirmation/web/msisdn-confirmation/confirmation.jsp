<%@ page import="com.eyelinecom.whoisd.personalization.helpers.PersonalizationClient" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<%@include file="common.jspf" %>

<%!
  private void pushConfirmation(String serviceId,
                                String chatId) throws Exception {

    final String kbd = "[[\"Продолжить\"]]";
    final String message = "Ваш номер телефона подтвержден.";

    // TODO: call `default' scenario on another page to avoid PUSH here.
    sendGet("http://devel.globalussd.mobi/push?" +
        "service=" + serviceId +
        "&subscriber=" + chatId +
        "&protocol=telegram" +
        "&scenario=push" +
        "&message=" + URLEncoder.encode(message) +
        "&keyboard=" + URLEncoder.encode(kbd));
  }

  private void sendGet(String url) throws Exception {
    final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
    con.setRequestMethod("GET");
    con.getInputStream().close();
  }

  private void handle(HttpServletRequest request) throws Exception {
    final PersonalizationClient client = getClient();
    final String msisdn = request.getParameter("subscriber");

    if (client.isExists(msisdn, VAR_MSISDN2CHAT, getLog())) {
      final String chatId = client.getString(msisdn, VAR_MSISDN2CHAT, getLog());

      if (!client.isExists(chatId, VAR_CHAT2SERVICE, getLog())) {
        client.remove(msisdn, VAR_MSISDN2CHAT, getLog());
        return;
      }

      final String serviceId = client.getString(chatId, VAR_CHAT2SERVICE);

      client.remove(chatId, VAR_CHAT2SERVICE, getLog());
      client.remove(chatId, VAR_PHASE, getLog());
      client.remove(msisdn, VAR_MSISDN2CHAT, getLog());

      client.set(chatId, "telegram-msisdn", msisdn);
      pushConfirmation(serviceId, chatId);
    }
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