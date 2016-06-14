<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%
    //сохзраняем в профиль локаль, и перенаправляем по адресу из сессии
    final String wnumber =getWnumber(request);
    String lang = request.getParameter("selected_lang");
    if(lang==null){
        throw new RuntimeException(_("error.message", lang));
    }

    try {
        saveLang(lang,wnumber);
    } catch (Exception e) {
        getLog().error("can't save lang = " + lang + " to profile wnumber = " + wnumber,e);
        throw new RuntimeException(e);
    }
    response.sendRedirect((String)request.getSession().getAttribute("forward-url"));
%>