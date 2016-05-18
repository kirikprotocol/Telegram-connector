<%@ page import="org.apache.log4j.Logger" %>
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

  private static final String BUNDLE_BASE = "msisdn_confirmation";

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

  final String message = request.getParameter("error");
  if (message != null) {
    log.warn(message);

  } else {
    log.warn("No error message present." +
        " Request uri = [" + request.getRequestURI() + "]," +
        " parameters = [" + toString(request.getParameterMap()) + "]");
  }
%>

<page version="2.0">
  <div>
    <%= _("error.message", request) %>
  </div>
</page>