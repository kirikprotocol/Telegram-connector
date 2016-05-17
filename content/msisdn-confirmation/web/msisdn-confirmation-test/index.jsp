<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0">
  <div>
    Привет!
    <br/>
    Для перехода на следующую страницу может потребоваться подтверждение номера телефона.
    Сейчас ваш идентификатор:
      <%= request.getParameter("subscriber") %>.
    Это либо внутренний идентификатор Mobilizer, либо действительно MSISDN (если он уже был получен ранее).
  </div>
  <navigation>
    <link pageId="verify://msisdn?success_url=authorize.jsp">Авторизоваться</link>
    <link pageId="reset.jsp">Сбросить профиль</link>
  </navigation>
</page>