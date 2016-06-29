<%@page language="java" import="java.util.*" 
%><%!
    private final static org.apache.log4j.Category log =
            org.apache.log4j.Category.getInstance("test.parameters");
%><%
        Enumeration parameterNames = request.getParameterNames();
        log.info("Request: "+request.getMethod());
        %>Request: <%=request.getMethod()%>;
        <%
        while(parameterNames.hasMoreElements()){
            String name = (String)parameterNames.nextElement();
            String value = request.getParameter(name);
            log.info("Parameter "+name+" = "+value);
            %><%=name%>=<%=value%>;
<%
        }
%>