<%@ page import="java.io.IOException" %>
<%@ page import="static mobi.eyeline.utils.restclient.web.RestClient.post" %>
<%@ page import="static mobi.eyeline.utils.restclient.web.RestClient.put" %>
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
    final String safeSid = serviceId.replace(".", "_");
    final String path = API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".phase";

    new RestClient().json(path, put(RestClient.content(phase)));
  }

  private void setType(String wnumber, String serviceId, String type) throws IOException {
    final String safeSid = serviceId.replace(".", "_");
    final String path = API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".type";

    new RestClient().json(path, put(RestClient.content(type)));
  }

  private String getType(String wnumber, String serviceId) throws IOException {
    try {
      return new RestClient()
          .json(API_ROOT + "/profile/" + wnumber + "/services.auth-" + serviceId.replace(".", "_") + ".type")
          .object()
          .getString("value");

    } catch (Exception e) {
      return null;
    }
  }

  private void setPin(String wnumber, String serviceId, String pin) throws IOException {
    final String safeSid = serviceId.replace(".", "_");
    final String path = API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".pin";

    new RestClient().json(path, put(RestClient.content(pin)));
  }

  private String handle(HttpServletRequest request) throws Exception {
    final String wnumber = getWnumber(request);
    final String serviceId = request.getParameter("serviceId");
    final String phase = getPhase(wnumber, serviceId);

    if (phase == null) {
      setPhase(wnumber, serviceId, PHASE_ASKED_FOR_MSISDN);

      final String type = request.getParameter("type");
      setType(wnumber, serviceId, type == null ? "c2s" : type);

      return "PAGE_REQUEST_MSISDN";

    } else {

      if (PHASE_HAS_MSISDN.equals(phase)) {
        return "PAGE_REQUEST_CALLBACK";

      } else if (PHASE_ASKED_FOR_MSISDN.equals(phase)) {
        // Must be MSISDN.

        final String payload = request.getParameter("confirm_msisdn");

        String enteredMsisdn = null;
        try {
          final String event = request.getParameter("event");
          final String eventType = request.getParameter("event.type");
          if ("message".equals(event) && "contact".equals(eventType)) {
            // Contact data received.
            // Try matching to the current user and entered verification data (or fail and ignore).

            enteredMsisdn = normalize(request.getParameter("event.msisdn"));

            final String contactWnumber = request.getParameter("event.id");
            if (wnumber.equals(contactWnumber) && enteredMsisdn != null) {
              // Got a contact info with:
              //   - Valid MSISDN, which is
              //   - Already persisted to the profile storage and associated to the current profile.
              //
              // No need to request any confirmation: mark verified & redirect back to content.

              setPhase(wnumber, serviceId, PHASE_HAS_MSISDN);
              final String safeSid = serviceId.replace(".", "_");
              new RestClient()
                  .json(
                      API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".entered-msisdn",
                      put(RestClient.content(enteredMsisdn)));

              new RestClient()
                  .json(
                      API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".service-id",
                      put(RestClient.content(serviceId)));

              new RestClient()
                  .json(
                      API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".protocol",
                      put(RestClient.content(request.getParameter("protocol"))));

              verify(enteredMsisdn);
              return "PAGE_EMPTY";
            }

          } else {
            // Plain text input.
            enteredMsisdn = normalize(payload);
          }

        } catch (Exception e) {
          getLog().warn("Failed processing user input [" + payload + "]", e);
        }

        if (enteredMsisdn == null) {
          return "PAGE_REQUEST_MSISDN_INVALID";

        } else {
          setPhase(wnumber, serviceId, PHASE_HAS_MSISDN);

          final String safeSid = serviceId.replace(".", "_");
          new RestClient()
              .json(
                  API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".entered-msisdn",
                  put(RestClient.content(enteredMsisdn)));

          new RestClient()
              .json(
                  API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".service-id",
                  put(RestClient.content(serviceId)));

          new RestClient()
              .json(
                  API_ROOT + "/profile/" + wnumber + "/services.auth-" + safeSid + ".protocol",
                  put(RestClient.content(request.getParameter("protocol"))));

          final String type = getType(wnumber, serviceId);
          if ("c2s".equals(type)) {
            return "PAGE_REQUEST_CALLBACK";

          } else if ("sms".equals(type)) {
            // 4 digits.
            final String pin = "" + ((int) (Math.random() * 9000) + 1000);
            setPin(wnumber, serviceId, pin);

            sendGet(MOBILIZER_ROOT + "/push?" +
                "service=" + serviceId +
                "&subscriber=" + enteredMsisdn +
                "&protocol=sms" +
                "&message=" + URLEncoder.encode(_("pin.message", request, pin), "UTF-8") +
                "&scenario=push");

            return "PAGE_REQUEST_PIN";

          } else if ("ussd_dialog".equals(type)) {
            sendGet(MOBILIZER_ROOT + "/push?" +
                "service=" + serviceId +
                "&subscriber=" + enteredMsisdn +
                "&protocol=ussd" +
                "&document=" + URLEncoder.encode(_("ussd.dialog.confirmation", request), "UTF-8") +
                "&scenario=xmlpush");

            return "PAGE_REQUEST_USSD_CONFIRMATION";

          } else {
            getLog().warn("Unexpected verification type [" + type + "]." +
                " ServiceId = [" + serviceId + "], wnumber = [" + wnumber + "]");
            return "PAGE_REQUEST_CALLBACK";
          }

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

<% if (target.equals("PAGE_REQUEST_MSISDN") && "skype".equals(request.getParameter("protocol"))) { %>
  <jsp:include page="request_msisdn_skype.jsp" flush="true"/>

<% } else if (target.equals("PAGE_REQUEST_MSISDN")) { %>
  <jsp:include page="request_msisdn_tg.jsp" flush="true"/>

<% } else if (target.equals("PAGE_REQUEST_MSISDN_INVALID")) { %>
  <jsp:include page="request_msisdn_invalid.jsp" flush="true"/>

<% } else if (target.equals("PAGE_REQUEST_CALLBACK")) { %>
  <jsp:include page="request_callback.jsp" flush="true"/>

<% } else if (target.equals("PAGE_REQUEST_PIN")) { %>
  <jsp:include page="request_pin.jsp" flush="true"/>

<% } else if (target.equals("PAGE_REQUEST_USSD_CONFIRMATION")) { %>
  <jsp:include page="request_ussd_confirmation.jsp" flush="true"/>

<% } else if (target.equals("PAGE_EMPTY")) { %>
  <jsp:include page="empty.jsp" flush="true"/>

<% } %>

