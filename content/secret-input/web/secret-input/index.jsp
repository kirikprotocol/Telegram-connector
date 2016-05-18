<%@ page import="mobi.eyeline.utils.restclient.web.RestClient" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="javax.xml.bind.DatatypeConverter" %>
<%@ page import="static mobi.eyeline.utils.restclient.web.RestClient.post" %>
<%@ page import="static java.nio.charset.StandardCharsets.UTF_8" %>
<%@ page import="static javax.xml.bind.DatatypeConverter.parseBase64Binary" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>


<%!
  static final String API_ROOT = "http:///wstorage";
  static final String MOBILIZER_ROOT = "http://devel.globalussd.mobi/";

  private void sendGet(String url) throws Exception {
    final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
    con.setRequestMethod("GET");
    con.getInputStream().close();
  }

  private void pushBack(String serviceId,
                        String wnumber) throws Exception {

    sendGet(MOBILIZER_ROOT + "/push?" +
        "service=" + serviceId +
        "&subscriber=" + wnumber +
        "&protocol=telegram" +
        "&scenario=default-noinform");
  }

  private String payloadText(HttpServletRequest req, int total, int current) {
    final StringBuilder buf = new StringBuilder();

    buf.append(_("prompt", req) + " ");
    buf.append("<b>");

    for (int i = 0; i < total; i++)
      buf.append(i < current ? "*" : "-");

    buf.append("</b>");

    return buf.toString();
  }

  private String linksToLabels(String links) throws UnsupportedEncodingException {
    if (links == null || links.isEmpty()) {
      return "";
    }

    final JSONArray target = new JSONArray();

    final String json = new String(DatatypeConverter.parseBase64Binary(links), "UTF-8");
    final JSONArray rows = new JSONArray(json);
    for (Object row0 : rows) {
      if (!(row0 instanceof JSONArray)) continue;

      final JSONArray row = (JSONArray) row0;

      final JSONArray labelRow = new JSONArray();
      for (Object btn0 : row) {
        if (!(btn0 instanceof JSONObject)) continue;

        final JSONObject btn = (JSONObject) btn0;
        labelRow.put(btn.getString("label"));
      }

      if (labelRow.length() != 0) {
        target.put(labelRow);
      }
    }

    return target.length() == 0 ? "" : target.toString();
  }

  private Map<String, String> parseLinks(String links) throws UnsupportedEncodingException {
    if (links == null || links.isEmpty()) {
      return Collections.emptyMap();
    }

    final Map<String, String> target = new HashMap<String, String>();

    final String json = new String(DatatypeConverter.parseBase64Binary(links), "UTF-8");
    final JSONArray rows = new JSONArray(json);
    for (Object row0 : rows) {
      if (!(row0 instanceof JSONArray)) continue;

      final JSONArray row = (JSONArray) row0;

      for (Object btn0 : row) {
        if (!(btn0 instanceof JSONObject)) continue;

        final JSONObject btn = (JSONObject) btn0;
        target.put(btn.getString("label"), btn.getString("href"));
      }
    }

    return target;
  }



  //
  //  Locales.
  //

  private static final String BUNDLE_BASE = "secret_input";

  public static String _(String key, String lang) {
    if (lang == null) {
      lang = "ru";
    }

    final Locale expectedLocale = new Locale(lang);
    ResourceBundle rb =
        ResourceBundle.getBundle("/" + BUNDLE_BASE, expectedLocale);

    if (!rb.getLocale().equals(expectedLocale)) {
      // Falls back to system locale -> replace with default one
      rb = ResourceBundle.getBundle("/" + BUNDLE_BASE, new Locale("en"));
    }

    return new String(
        rb.getString(key).getBytes(StandardCharsets.ISO_8859_1),
        StandardCharsets.UTF_8);
  }

  public static String _(String key, HttpServletRequest req) {
    return _(key, req.getParameter("lang"));
  }
%>

<%
  String text = null;
  String pageId = session.getId();

  String wnumber = request.getParameter("wnumber");
  if (wnumber==null) {
    wnumber = request.getParameter("subscriber");
  }	
  final String key = request.getParameter("key");
  final String badCommand = request.getParameter("bad_command");

  final Map<String, String> contentLinks =
      (Map<String, String>) session.getAttribute("password-links");

  if (badCommand != null && contentLinks != null && contentLinks.containsKey(badCommand)) {
    final String linkId = contentLinks.get(badCommand);

    final String fromServiceId = (String) session.getAttribute("password-sid");

    sendGet(MOBILIZER_ROOT + "/push?" +
        "service=" + fromServiceId +
        "&subscriber=" + wnumber +
        "&protocol=telegram" +
        "&pageId=" + URLEncoder.encode(linkId, "UTF-8") +
        "&scenario=default");

    request.setAttribute("isEdit", true);
    request.setAttribute("hideKeyboard", true);
    request.setAttribute("keepSession", true);
    text = _("cancelled", request);

  } else {

    if (key == null && badCommand == null) {
      // Initial page load.

      final String fromServiceId = request.getParameter("password-sid");
      session.setAttribute("password-sid", fromServiceId);

      final String prevTextParam = request.getParameter("password-prompt");
      final String prevText =
          prevTextParam == null || prevTextParam.length() == 0 ?
              "" :
              new String(parseBase64Binary(prevTextParam), "UTF-8");
      session.setAttribute("password-prompt", prevText);

      session.setAttribute("entered-value", "");

      text = payloadText(request, 4, 0);
      request.setAttribute("isEdit", false);

      final String links = request.getParameter("password-links");
      session.setAttribute("password-links", parseLinks(links));

      sendGet(MOBILIZER_ROOT + "/push?" +
          "service=" + fromServiceId +
          "&subscriber=" + wnumber +
          "&protocol=telegram" +
          "&scenario=push" +
          "&message=" + URLEncoder.encode(prevText, "UTF-8") +
          "&keyboard=" + linksToLabels(links));

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
          pushBack(prevSid, wnumber);
        } catch (Exception e) {
          e.printStackTrace();
        }

        request.setAttribute("hideKeyboard", true);
        request.setAttribute("keepSession", true);
        text = _("password.entered", request);

      } else {
        pageId = session.getId();
        text = payloadText(request, 4, currentLength);
      }
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
