<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0">
  <div>
    <input navigationId="submit" type="password" name="pin" title="Для какой-то операции нам потребуется ваш персональный пароль (4 цифры)"/>
  </div>
  <navigation id="submit">
    <link pageId="finish.jsp">Готово</link>
  </navigation>
  <navigation>
    <link pageId="index.jsp">Отмена</link>
  </navigation>
</page>