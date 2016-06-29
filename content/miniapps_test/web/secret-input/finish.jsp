<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0">
  <div>
    Ваш пароль: <b><%=request.getParameter("pin") %></b>.
  </div>
  <navigation>
    <link pageId="../index.jsp">Начать сначала</link>
  </navigation>
</page>