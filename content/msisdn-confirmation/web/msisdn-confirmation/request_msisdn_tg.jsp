<%@ page contentType="application/xml; charset=UTF-8" language="java" %>

<page version="2.0">
  <div>
    <input navigationId="submit"
           name="confirm_msisdn"
           title="Для продолжения, введите номер телефона или отправьте свой контакт"/>
  </div>
  <navigation id="submit">
    <link pageId="index.jsp">Готово</link>
  </navigation>
  <navigation>
    <link pageId="telegram://request-contact">Отправить контактные данные</link>
  </navigation>
</page>