<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%
    //сохзраняем в профиль локаль, и перенаправляем по адресу из сессии
    final String wnumber =getWnumber(request);
    String lang = request.getParameter("selected_lang");
    if(lang==null){
%>
<page version="2.0">
    <div>
            <%= _("error.message", lang)%>
    </div>

</page>

<%
        return;
    }

    try {
        saveLang(lang,wnumber);
    } catch (Exception e) {
        getLog().error("can't save lang = " + lang + " to profile wnumber = " + wnumber,e);
%>
    <page version="2.0">
    <div>
            <%= _("error.message", lang)%>
    </div>

    </page>

<%
        return;
    }
    response.sendRedirect((String)request.getSession().getAttribute("forward-url"));
%>