<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0">
  <div>
    Привет!
    <br/>
    Текущий протокол: <b><%= request.getParameter("protocol") %></b>.
  </div>
  <navigation>
    <link pageId="msisdn-confirmation/index.jsp">Верифицировать MSISDN</link>
    <link pageId="secret-input/index.jsp">Ввод пароля</link>
    <link pageId="send-file/index.jsp" protocol="telegram|skype|facebook">Отправить картинку</link>
    <link pageId="smartcash/index.xml">Smartcash</link>
  </navigation>
</page>