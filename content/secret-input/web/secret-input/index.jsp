<%@ page import="mobi.eyeline.utils.restclient.web.RestClient" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="static mobi.eyeline.utils.restclient.web.RestClient.post" %>
<%@ page import="static java.nio.charset.StandardCharsets.UTF_8" %>
<%@ page import="static javax.xml.bind.DatatypeConverter.parseBase64Binary" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>


<%!
  static final String API_ROOT = "http://localhost:7890/wstorage";

  private void sendGet(String url) throws Exception {
    final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
    con.setRequestMethod("GET");
    con.getInputStream().close();
  }

  private void pushBack(String serviceId,
                        String wnumber) throws Exception {

    sendGet("http://devel.globalussd.mobi/push?" +
        "service=" + serviceId +
        "&subscriber=" + wnumber +
        "&protocol=telegram" +
        "&scenario=default");
  }

  private String payloadText(int total, int current) {
    final StringBuilder buf = new StringBuilder();

    buf.append("Вы можете использовать клавиатуру ниже, или ввести пароль вручную: ");
    buf.append("<b>");

    for (int i = 0; i < total; i++)
      buf.append(i < current ? "*" : "-");

    buf.append("</b>");

    return buf.toString();
  }
%>

<%
  String text = null;
  String pageId = session.getId();

  final String wnumber = request.getParameter("subscriber");
  final String key = request.getParameter("key");
  final String badCommand = request.getParameter("bad_command");

  if (key == null && badCommand == null) {
    // Initial page load.

    final String fromServiceId = request.getParameter("password-sid");
    session.setAttribute("password-sid", fromServiceId);

    final String prevTextParam = request.getParameter("password-prompt");
    final String prevText =
        prevTextParam == null || prevTextParam.length() == 0 ?
            "" :
            new String(parseBase64Binary(prevTextParam), UTF_8);
    session.setAttribute("password-prompt", prevText);

    session.setAttribute("entered-value", "");

    text = payloadText(4, 0);
    request.setAttribute("isEdit", false);

    sendGet("http://devel.globalussd.mobi/push?" +
        "service=" + fromServiceId +
        "&subscriber=" + wnumber +
        "&protocol=telegram" +
        "&scenario=push" +
        "&message=" + URLEncoder.encode(prevText, "UTF-8"));

  } else {

    request.setAttribute("isEdit", true);

    final String currentValue = badCommand != null ?
        badCommand : (session.getAttribute("entered-value") + key);

    session.setAttribute("entered-value", currentValue);

    final int currentLength = currentValue.length();

    if (currentLength == 4 || badCommand != null) {
      final String prevSid = (String) session.getAttribute("password-sid");
      new RestClient()
          .json(API_ROOT + "/profile/" + wnumber + "/services.password-" + prevSid.replace(".", "_"), post(RestClient.content(currentValue)));

      try {
        pushBack(prevSid, request.getParameter("subscriber"));
      } catch (Exception e) {
        e.printStackTrace();
      }

      request.setAttribute("hideKeyboard", true);
      request.setAttribute("keepSession", true);
      text = "Пароль введён.";

    } else {
      pageId = session.getId();
      text = payloadText(4, currentLength);
    }
  }

  request.setAttribute("pageId", pageId);
  request.setAttribute("text", text);
%>


<page version="2.0" attributes="telegram.message.id: ${pageId}; telegram.message.edit: ${isEdit}; telegram.keep.session: ${keepSession}">
  <div>
      ${text}
  </div>

  <% if (request.getAttribute("hideKeyboard") == null) { %>
    <navigation attributes="telegram.inline: true">
      <link pageId="index.jsp?key=1">1</link>
      <link pageId="index.jsp?key=2">2</link>
      <link pageId="index.jsp?key=3">3</link>
      <link pageId="index.jsp?key=4">4</link>
      <link pageId="index.jsp?key=5">5</link>
    </navigation>

    <navigation attributes="telegram.inline: true">
      <link pageId="index.jsp?key=6">6</link>
      <link pageId="index.jsp?key=7">7</link>
      <link pageId="index.jsp?key=8">8</link>
      <link pageId="index.jsp?key=9">9</link>
      <link pageId="index.jsp?key=0">0</link>
    </navigation>
  <% } %>

</page>