<%@ page import="com.eyelinecom.whoisd.sads2.telegram.confirmation.Context" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.ProfileStorage" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.Profile" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.Profile.Query.PropertyQuery" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!

  private void handle(HttpServletRequest request) throws Exception {
    final ProfileStorage storage = Context.getInstance().getProfileStorage();
    final String wnumber = getWnumber(request);

    getLog().debug("Clearing profile wnumber = [" + wnumber + "]");

    final PropertyQuery mobileData = storage
        .find(wnumber)
        .query()
        .property("mobile");

    if (mobileData != null) {
      mobileData.delete();
    }
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