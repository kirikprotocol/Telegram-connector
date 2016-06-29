<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0">
  <div>
    Привет!
    <br/>
    Для перехода на следующую страницу может потребоваться подтверждение номера телефона.
    Сейчас ваш идентификатор: <%= request.getParameter("subscriber") %>.
  </div>
  <navigation>
    <link pageId="verify://msisdn?success_url=http://localhost:9380/miniapps-test/msisdn-confirmation/authorize.jsp">Авторизоваться</link>
    <link pageId="reset.jsp" protocol="telegram|skype|facebook">Сбросить профиль</link>
  </navigation>
</page>