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
    <link pageId="send-file/index.jsp" protocol="telegram|skype|facebook|vkontakte|line">Отправить картинку</link>
    <link pageId="smartcash/index.xml">Smartcash</link>
    <link pageId="http://plugins.miniapps.run/tipay-payment?order=%5b%7b%22price%22%3a12%2c%22description%22%3a%22bla%22%2c%22currency%22%3a%22USD%22%7d%5d&amp;locale=ru&amp;merchantName=MyStore">Оплата в типей</link>
  </navigation>
</page>