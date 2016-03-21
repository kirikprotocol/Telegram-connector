<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0">
  <div>
    Привет!
    <br/>
    Мы не уверены, что знаем твой номер телефона обсолютно точно - для нас ты просто
      <%= request.getParameter("subscriber") %>.
    Это либо Telegram Chat ID, либо действительно MSISDN (если он уже был получен ранее).
    <br/>
    Зато после перехода по ссылке "Авторизоваться" будем знать точно.
  </div>
  <navigation>
    <link pageId="authorize.jsp">Авторизоваться</link>
    <link pageId="reset.jsp">Сбросить профиль Telegram</link>
  </navigation>
</page>