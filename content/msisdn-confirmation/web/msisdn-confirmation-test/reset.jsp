<%@ page import="com.eyelinecom.whoisd.sads2.telegram.confirmation.Context" %>
<%@ page import="com.eyelinecom.whoisd.sads2.wstorage.profile.ProfileStorage" %>
<%@ page import="static org.apache.commons.collections.CollectionUtils.isNotEmpty" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!

  private void handle(HttpServletRequest request) throws Exception {
    final ProfileStorage storage = Context.getInstance().getProfileStorage();
    final String wnumber = getWnumber(request);

    getLog().debug("Clearing profile wnumber = [" + wnumber + "]");

    storage
        .find(wnumber)
        .query()
        .property("mobile")
        .delete();

    storage
        .find(wnumber)
        .query()
        .property("services")
        .delete();
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