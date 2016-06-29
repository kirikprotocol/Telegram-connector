<%@page language="java" contentType="text/xml; charset=utf8"%><?xml version="1.0" encoding="UTF-8"?>
<%
  Double balance = (Double) request.getAttribute("balance");
%>
<page version="2.0" style="post_ussd">
  <div>Your Avialable Balance is:<br/><%=balance%>
  </div>
  <navigation>
    <link pageId="http://localhost:9380/miniapps-test/index.jsp" accesskey="0" type="back"><div protocol="telegram">🔙</div>Return to Main Menu</link>
  </navigation>
</page>
