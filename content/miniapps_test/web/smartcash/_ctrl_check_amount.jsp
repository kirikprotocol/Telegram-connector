<%@page language="java" import="java.util.*" %><jsp:forward page="<%=execute(request, response)%>"/>
<%!
    private final static org.apache.log4j.Category log =
            org.apache.log4j.Category.getInstance("smartcash.ctrl_check_amount");

    private final static String VIEW_OK_AMOUNT_VALID = "enter_number.jsp";
    private final static String VIEW_FAIL_AMOUNT_INVALID = "enter_amount.jsp";

    private Map<String, Double> getAbonentsBalance(HttpServletRequest request) throws Exception {
        ServletContext context = request.getSession().getServletContext();
        return (Map<String, Double>) context.getAttribute("abonentsBalance");
    }

    private String execute(HttpServletRequest request, HttpServletResponse response) {
        String abonent = request.getParameter("abonent");
        try {
            double amount = Double.parseDouble(request.getParameter("amount"));
            Map<String, Double> abonentsBalance = getAbonentsBalance(request);
            double balance = abonentsBalance.get(abonent);
            if (amount>0 && amount<=100 && balance>=amount) {
                HttpSession session = request.getSession();
                session.setAttribute("amount", amount);
                return VIEW_OK_AMOUNT_VALID;
            } else {
                request.setAttribute("error_message", "Amount is invalid");
                return VIEW_FAIL_AMOUNT_INVALID;
            }
        } catch (Exception e) {
            log.warn("Some error: abonent "+abonent, e);
            return VIEW_FAIL_AMOUNT_INVALID;
        }
    }
%>
