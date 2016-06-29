<%@page language="java" contentType="text/xml; charset=utf8"%><?xml version="1.0" encoding="UTF-8"?>

<%
	String errorMessage = (String)request.getAttribute("error_message");
%>
<page version="2.0" style="post_ussd">
  <title id="">Number menu</title>
  <div id="error">
	<%if (errorMessage!=null){%>
		<%=errorMessage%>
	<%}%>
  </div>
  <div>
    <input navigationId="submit" name="number" title="Enter mobile number" />
  </div>
  <navigation id="submit">
    <link accesskey="1" pageId="_ctrl_check_number.jsp">Ok</link>
  </navigation>
  <navigation>
    <link pageId="_ctrl_register.jsp" accesskey="0" type="back"><div protocol="telegram">🔙</div>Return to Main Menu</link>
  </navigation>
</page>
