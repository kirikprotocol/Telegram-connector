<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0">
  <attributes>
    <attribute name="msisdn-required" value="true"/>
  </attributes>
  <div>
    А вот теперь мы абсоолютно уверены, что твой номер: <b><%= request.getParameter("subscriber") %></b>.
    <br/>
    И да начнется <b>СПАМ</b>!
  </div>
  <navigation>
    <link pageId="index.jsp">Начать сначала</link>
  </navigation>
</page>