<%@page language="java" contentType="text/xml; charset=UTF-8"%>

<jsp:forward page="<%=execute(request, response)%>"/>

<%!
  private final static String VIEW_OK = "survey_kids.jsp";
  private final static String VIEW_FAIL = "survey_sex.jsp";

  private boolean isValidSex(String sex, HttpSession session){
    if ("female".equals(sex) || "male".equals(sex)) {
      session.setAttribute("sex", sex);
      return true;
    }
    return false;
  }

  private String execute(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    try {
      String sex = request.getParameter("sex");
      if (isValidSex(sex, session)) {
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
