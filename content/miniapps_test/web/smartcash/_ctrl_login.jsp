<%@page language="java" import="java.util.*" %><jsp:forward page="<%=execute(request, response)%>"/>
<%!
    private final static org.apache.log4j.Category log =
            org.apache.log4j.Category.getInstance("smartcash.ctrl_login");

    private final static String VIEW_LOGIN_OK = "enter_operation.jsp";
    private final static String VIEW_LOGIN_FAILED = "invalid_login.jsp";

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
            log.debug("recreate Abonents Balance");
            return abonentsBalance;
        } else {
            return abonentsBalance;
        }
    }

    private String execute(HttpServletRequest request, HttpServletResponse response) {
        String abonent = request.getParameter("abonent");
        try {
            Map<String, Double> abonentsBalance = getAbonentsBalance(request);
            if (abonentsBalance.containsKey(abonent)) return VIEW_LOGIN_OK;
            else return VIEW_LOGIN_FAILED;
        } catch (Exception e) {
            log.warn("Some error: abonent"+abonent, e);
            return VIEW_LOGIN_FAILED;
        }
    }
%>
