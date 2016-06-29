<%@page language="java" contentType="text/xml; charset=utf8"%><?xml version="1.0" encoding="UTF-8"?>
<%
  Double amount = (Double) session.getAttribute("amount");
  String number = (String) request.getAttribute("number");
  Double balance = (Double) request.getAttribute("balance");
%>
<page version="2.0" style="post_ussd">
  <div>Transaction OK<br/><%=amount%> sent to <%=number%><br/>Your Available balance is: <%=balance%>
  </div>
  <navigation>
    <link pageId="_ctrl_register.jsp" accesskey="0" type="back"><div protocol="telegram">🔙</div>Return to Main Menu</link>
  </navigation>
</page>
