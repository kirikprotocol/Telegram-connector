<%@ page import="com.eyelinecom.whoisd.sads2.telegram.confirmation.Context" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.Profile" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.Profile.Query.PropertyQuery" %>
<%@ page import="static com.eyelinecom.whoisd.sads2.wstorage.profile.QueryRestrictions.*" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.ProfileStorage" %>
<%@ page import="org.codehaus.jettison.json.JSONArray" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!
  private final ProfileStorage storage = Context.getInstance().getProfileStorage();

  private String handle(HttpServletRequest request) throws Exception {
    final String wnumber = getWnumber(request);
    final Profile profile = storage.find(wnumber);

    String serviceId = request.getParameter("serviceId");
    if (StringUtils.isNotBlank(serviceId)) {
      profile
          .query()
          .property("services", "auth-" + serviceId, "service-id")
          .set(serviceId);

    } else {
      serviceId = profile
          .query()
          .property("services", "auth-*", "service-id")
          .getValue();
    }

    PropertyQuery pq = profile
        .query()
        .property("services", "auth-" + serviceId, "phase");

    if (pq.get() == null) {
      pq.set(PHASE_ASKED_FOR_MSISDN);
      return "PAGE_REQUEST_MSISDN";

    } else {
      final String stage = pq.getValue();

      if (PHASE_HAS_MSISDN.equals(stage)) {
        return "PAGE_REQUEST_CALLBACK";

      } else if (PHASE_ASKED_FOR_MSISDN.equals(stage)) {
        // Must be MSISDN.

        final String payload = request.getParameter("confirm_msisdn");

        String enteredMsisdn = null;
        try {
          enteredMsisdn = normalize(
              "json".equals(request.getParameter("input_type")) ?
                  new JSONArray(payload).getJSONObject(0).getString("msisdn") :
                  payload
          );

        } catch (Exception e) {
          getLog().warn("Failed parsing user input [" + payload + "]");
        }

        if (enteredMsisdn == null) {
          return "PAGE_REQUEST_MSISDN_INVALID";

        } else {
          pq.set(PHASE_HAS_MSISDN);

          profile
              .query()
              .property("services", "auth-" + serviceId, "entered-msisdn")
              .set(enteredMsisdn);

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

