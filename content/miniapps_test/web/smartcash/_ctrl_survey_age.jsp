<%@page language="java" contentType="text/xml; charset=UTF-8"%>

<jsp:forward page="<%=execute(request, response)%>"/>

<%!

  private final static String VIEW_OK = "survey_sex.jsp";
  private final static String VIEW_FAIL = "survey_age.jsp";

  private boolean isValidAge(String ageStr, HttpSession session) {
    try {
      int age = Integer.parseInt(ageStr);
      session.setAttribute("age", age);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String execute(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    try {
      String age = request.getParameter("age");
      if (isValidAge(age, session)) {
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
