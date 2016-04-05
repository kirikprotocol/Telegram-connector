<%@ page import="com.eyelinecom.whoisd.sads2.telegram.confirmation.Context" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.Profile" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.ProfileStorage" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.QueryRestrictions" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="static com.eyelinecom.whoisd.sads2.wstorage.profile.QueryRestrictions.property" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<%@include file="common.jspf" %>

<%!

  final ProfileStorage storage = Context.getInstance().getProfileStorage();

  private void pushConfirmation(String serviceId,
                                String chatId) throws Exception {

    sendGet("http://devel.globalussd.mobi/push?" +
        "service=" + serviceId +
        "&subscriber=" + chatId +
        "&protocol=telegram" +
        "&scenario=default");
  }

  private void sendGet(String url) throws Exception {
    final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
    con.setRequestMethod("GET");
    con.getInputStream().close();
  }

  private void handle(HttpServletRequest request) throws Exception {
    Log log = getLog();

    final String msisdn = request.getParameter("subscriber");
    final Profile profile = storage
        .query()
        .where(QueryRestrictions.property("services", "auth-*", "entered-msisdn").eq(msisdn))
        .get();

    if (profile != null) {
      log.debug("Verifying MSISDN = [" + msisdn + "]" +
          " for profile wnumber = [" + profile.getWnumber() + "]");

      final String serviceId =
          profile.query().property("services", "auth-*", "service-id").getValue();
      profile.query().property("mobile", "msisdn").set(msisdn);

      profile.query().property("services", "auth-" + serviceId, "service-id").delete();
      profile.query().property("services", "auth-" + serviceId, "phase").delete();
      profile.query().property("services", "auth-" + serviceId, "entered-msisdn").delete();

      pushConfirmation(serviceId, profile.getWnumber());

    } else {
      log.warn("Profile not found for MSISDN verification, MSISDN = [" + msisdn + "]");
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