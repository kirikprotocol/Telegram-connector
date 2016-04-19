<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0" attributes="telegram.id: ">
  <div>
    <%=request.getParameter("message")%>
  </div>

  <navigation attributes="telegram.inline">
    <link pageId="index.jsp?key=1">1</link>
    <link pageId="index.jsp?key=2">2</link>
    <link pageId="index.jsp?key=3">3</link>
    <link pageId="index.jsp?key=4">4</link>
    <link pageId="index.jsp?key=5">5</link>
  </navigation>

  <navigation attributes="telegram.inline">
    <link pageId="index.jsp?key=6">6</link>
    <link pageId="index.jsp?key=7">7</link>
    <link pageId="index.jsp?key=8">8</link>
    <link pageId="index.jsp?key=9">9</link>
    <link pageId="index.jsp?key=0">0</link>
  </navigation>

  <div>
      <%=request.getParameter("message")%>
  </div>
</page>