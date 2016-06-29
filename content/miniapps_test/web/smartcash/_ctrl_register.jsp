<%@page language="java" import="java.util.*" %><jsp:forward page="<%=execute(request, response)%>"/>
<%!
  private final static String VIEW_REGISTER_OK = "enter_operation.jsp";
  private final static String VIEW_REGISTER_FAILED = "invalid_login.jsp";

  private Map<String, Double> getAbonentsBalance(HttpServletRequest request) throws Exception {
    ServletContext context = request.getSession().getServletContext();
    Map<String, Double> abonentsBalance = (Map<String, Double>) context.getAttribute("abonentsBalance");

    if (abonentsBalance == null) {
      abonentsBalance = new HashMap<String, Double>();
      abonentsBalance.put("73333333333", 1000.0);
      abonentsBalance.put("73333333334", 1000.0);
      abonentsBalance.put("7XXXXXXXXXX", 1000.0);
      abonentsBalance.put("7XXXXXXXXXX", 1000.0);
      context.setAttribute("abonentsBalance", abonentsBalance);
      return abonentsBalance;
    } else {
      return abonentsBalance;
    }
  }

  private String execute(HttpServletRequest request, HttpServletResponse response) {
    String abonent = request.getParameter("abonent");
    try {
      Map<String, Double> abonentsBalance = getAbonentsBalance(request);
      if (!abonentsBalance.containsKey(abonent)) abonentsBalance.put(abonent, 1000.0);
      return VIEW_REGISTER_OK;

    } catch (Exception e) {
      return VIEW_REGISTER_FAILED;
    }
  }
%>
