<?xml version="1.0" encoding="UTF-8"?>
<%@page language="java" contentType="text/xml; charset=UTF-8"%>

<page version="2.0" style="post_ussd">
  <div>
	Thank you!<br/>
	Your age: <%=session.getAttribute("age")%> years old, <%=session.getAttribute("sex")%>, with <%=session.getAttribute("kids")%> kids.
  </div>
  <navigation>
    <link pageId="index.xml" accesskey="0" type="back"><div protocol="telegram">🔙</div>Return to Main Menu</link>
    <link accesskey="00" type="exit" protocol="ussd">Exit</link>
  </navigation>
</page>
