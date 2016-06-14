<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@ page import="java.util.Enumeration" %>
<%@include file="common.jspf" %>

<%
    String lang = request.getParameter("lang");
    ResourceBundle bundle = getBundle(lang);
    String forwardUrl = request.getParameter("forwardUrl");
    if(forwardUrl==null){
        throw new RuntimeException("param 'forwardUrl' is empty");
    }
    request.getSession().setAttribute("forward-url",forwardUrl);
%>
<page version="2.0">
  <div>
          <%= _("choose.language", lang)%>
  </div>
  <navigation id="submit">
    <%
        Enumeration<String>keys = bundle.getKeys();

        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            if(key!=null&&key.startsWith("option.")){
                String option = key.substring("option.".length());
                %>
      <link pageId="save_selected_language.jsp?selected_lang=<%=option%>"><%=_(key,bundle)%></link>
      <%
            }

        }
    %>

  </navigation>
</page>