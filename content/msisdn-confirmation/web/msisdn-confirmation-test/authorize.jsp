<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0">
  <%--<attributes>--%>
    <%--<attribute name="msisdn-required" value="true"/>--%>
  <%--</attributes>--%>
  <div>
    Ваш номер телефона подтвержден: <b><%= request.getParameter("subscriber") %></b>.
  </div>
  <navigation>
    <link pageId="index.jsp">Начать сначала</link>
  </navigation>
</page>