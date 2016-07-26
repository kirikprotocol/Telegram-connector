<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<%!
  private final Logger log = Logger.getLogger("error-handler");

  public static String toString(Map<String, String[]> map) {
    final StringBuilder result = new StringBuilder();

    result.append("{");
    for (Iterator<Map.Entry<String, String[]>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
      final Map.Entry<String, String[]> entry = iterator.next();
      result
          .append("\"")
          .append(entry.getKey())
          .append("\"=")
          .append(Arrays.toString(entry.getValue()));
      if (iterator.hasNext()) {
        result.append(", ");
      }
    }
    result.append("}");

    return result.toString();
  }



  //
  //  Locales.
  //

  private static final String BUNDLE_BASE = "error_page";


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

  request.setAttribute("error.message.code", "error.message");

  final String message = request.getParameter("error");
  if (message != null) {
    log.warn(message);

    try {
      final JSONObject obj = new JSONObject(message);

      final String startPage = obj.optString("serviceStartPage");
      if (startPage != null) {
        request.setAttribute("startPage", startPage);
      }

      if ("TG_UNSUPPORTED_CLIENT".equals(obj.optString("code"))) {
        request.setAttribute("error.message.code", "error.message.tg_unsupported_client");
      }

    } catch (Exception e) {
      log.warn("Failed parsing error message", e);
    }

  } else {
    log.warn("No error message present." +
        " Request uri = [" + request.getRequestURI() + "]," +
        " parameters = [" + toString(request.getParameterMap()) + "]");
  }
%>

<page version="2.0">
  <div>
    <%= _((String) request.getAttribute("error.message.code"), request) %>
  </div>

  <% if (request.getAttribute("startPage") != null) { %>
    <navigation>
      <link pageId="<%= request.getAttribute("startPage") %>"><%= _("start.page", request) %></link>
    </navigation>
  <% } %>
</page>