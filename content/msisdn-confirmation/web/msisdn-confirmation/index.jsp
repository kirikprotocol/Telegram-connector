<%@ page import="org.json.JSONArray" %>
<%@ page import="static mobi.eyeline.utils.restclient.web.RestClient.post" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.io.IOException" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!
  private String getPhase(String wnumber, String serviceId) {
    try {
      return new RestClient()
          .json(API_ROOT + "/profile/" + wnumber + "/services.auth-" + serviceId.replace(".", "_") + ".phase")
          .object()
          .getString("value");

    } catch (Exception e) {
      return null;
    }
  }

  private void setPhase(String wnumber, String serviceId, String phase) throws IOException {
    new RestClient()
        .json(
            API_ROOT + "/profile/" + wnumber + "/services.auth-" + serviceId.replace(".", "_") + ".phase",
            post(RestClient.content(phase))
        );
  }

  private String handle(HttpServletRequest request) throws Exception {
    final String wnumber = getWnumber(request);
    final String serviceId = request.getParameter("serviceId");
    final String phase = getPhase(wnumber, serviceId);

    if (phase == null) {
      setPhase(wnumber, serviceId, PHASE_ASKED_FOR_MSISDN);

      return "PAGE_REQUEST_MSISDN";

    } else {

      if (PHASE_HAS_MSISDN.equals(phase)) {
        return "PAGE_REQUEST_CALLBACK";

      } else if (PHASE_ASKED_FOR_MSISDN.equals(phase)) {
        // Must be MSISDN.

        final String payload = request.getParameter("confirm_msisdn");

        String enteredMsisdn = null;
        try {
          if ("json".equals(request.getParameter("input_type"))) {
            // Attachment. Treat it as a concact input (or fail and ignore).
            final JSONObject contact = new JSONArray(payload).getJSONObject(0);
            enteredMsisdn = normalize(contact.getString("msisdn"));

            final String contactWnumber = contact.optString("id");
            if (wnumber.equals(contactWnumber) && enteredMsisdn != null) {
              // Got a contact info with:
              //   - Valid MSISDN, which is
              //   - Already persisted to the profile storage and associated to the current profile.
              //
              // No need to request any confirmation: mark verified & redirect back to content.
              verify(enteredMsisdn);
              return "PAGE_EMPTY";
            }

          } else {
            // Plain text input.
            enteredMsisdn = normalize(payload);
          }

        } catch (Exception e) {
          getLog().warn("Failed parsing user input [" + payload + "]");
        }

        if (enteredMsisdn == null) {
          return "PAGE_REQUEST_MSISDN_INVALID";

        } else {
          setPhase(wnumber, serviceId, PHASE_HAS_MSISDN);

          final String safeSid = serviceId.replace(".", "_");
          new RestClient()
              .json(
                  API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".entered-msisdn",
                  post(RestClient.content(enteredMsisdn)));

          new RestClient()
              .json(
                  API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".service-id",
                  post(RestClient.content(serviceId)));

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

<% } else if (target.equals("PAGE_EMPTY")) { %>
  <jsp:include page="empty.jsp" flush="true"/>

<% } %>

