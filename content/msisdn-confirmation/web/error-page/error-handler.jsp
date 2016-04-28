<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
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
    An error occurred.
  </div>
</page>