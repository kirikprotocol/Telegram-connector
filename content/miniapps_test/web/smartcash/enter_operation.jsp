<%@page language="java" contentType="text/xml; charset=utf8"%><?xml version="1.0" encoding="UTF-8"?>

<page version="2.0" style="category">
  <title id="">Main menu</title>
  <div protocol="telegram">Choose financial operation</div>
  <navigation>
    <link pageId="_ctrl_check_operation.jsp?operation=get_balance" accesskey="1"><div protocol="telegram">📃</div>Get Balance</link>
    <%--<link pageId="_ctrl_check_operation.jsp?operation=move_money"  accesskey="2"><div protocol="telegram">💸</div>Move Money</link>--%>

    <!--link protocol="ussd" pageId="waplink.xml" accesskey="3">WAP version</link-->
  </navigation>

  <navigation>
    <link pageId="index.xml"  accesskey="3" protocol="telegram"><div protocol="telegram">🔝</div>Main menu</link>
  </navigation>
</page>
