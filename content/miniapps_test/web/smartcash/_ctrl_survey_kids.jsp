<%@page language="java" contentType="text/xml; charset=UTF-8"%>

<jsp:forward page="<%=execute(request, response)%>"/>

<%!
  private final static String VIEW_OK = "survey_result.jsp";
  private final static String VIEW_FAIL = "survey_kids.jsp";

  private boolean isValid(String str, HttpSession session){
    try{
      int num = Integer.parseInt(str);
      session.setAttribute("kids", num);
      return true;
    } catch (Exception e){
      return false;
    }
  }

  private String execute(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    try {
      String kids = request.getParameter("kids");
      if (isValid(kids, session)) {
        return VIEW_OK;
      } else {
        request.setAttribute("error_message", "Invalid input");
        return VIEW_FAIL;
      }
    } catch (Exception e) {
      return VIEW_FAIL;
    }
  }
%>
