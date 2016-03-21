<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!

  public static final String VAR_MSISDN2CHAT = "telegram-chat-id";
  public static final String VAR_CHAT2MSISDN = "telegram-msisdn";

  private void clearMsisdn(HttpServletRequest request,
                           PersonalizationClient client) throws Exception {

    final String msisdn = request.getParameter("subscriber");

    if (client.isExists(msisdn, VAR_MSISDN2CHAT)) {
      final String chatId = client.getString(msisdn, VAR_MSISDN2CHAT);
      clearChat(client, chatId);

      client.remove(msisdn, VAR_MSISDN2CHAT);
    }

    final String chatId = request.getParameter("chatId");
    if (chatId != null) {
      clearChat(client, chatId);
    }
  }

  private void clearChat(PersonalizationClient client,
                         String chatId) throws Exception {

    if (client.isExists(chatId, VAR_CHAT2MSISDN)) {
      client.remove(chatId, VAR_CHAT2MSISDN);
    }
  }

  private void handle(HttpServletRequest request) throws Exception {
    final PersonalizationClient client = getClient();

    clearMsisdn(request, client);
  }

%>

<%
  handle(request);
%>

<page version="2.0">
  <div>
    Профиль очищен. А теперь почистим сессии через /reset ...
  </div>
  <navigation>
    <link/>
  </navigation>
</page>