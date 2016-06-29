<%@page language="java" import="java.util.*" %><jsp:forward page="<%=execute(request, response)%>"/>
<%!
    private final static org.apache.log4j.Category log =
            org.apache.log4j.Category.getInstance("smartcash.ctrl_check_pin");

    private final static String VIEW_OK_GET_BALANCE = "ok_balance.jsp";
    private final static String VIEW_OK_MOVE_MONEY = "enter_amount.jsp";
    private final static String VIEW_FAIL_INVALID_PIN = "enter_pin.jsp";

    private Map<String, Double> getAbonentsBalance(HttpServletRequest request) throws Exception {
        ServletContext context = request.getSession().getServletContext();
        return (Map<String, Double>) context.getAttribute("abonentsBalance");
    }

    private boolean isValidPin(String enter, HttpSession session){
        String pin = null;
		if (enter!=null && !enter.trim().equals("")) pin = enter;
		else pin = (String)session.getAttribute("pin");
		if ("1111".equals(pin)){
			session.setAttribute("pin", pin);
			return true;
		} else return false;
    }

    private String execute(HttpServletRequest request, HttpServletResponse response) {
        String abonent = request.getParameter("abonent");
		HttpSession session = request.getSession();
		if (session.getAttribute("pin") == null && request.getParameter("pin")==null) return VIEW_FAIL_INVALID_PIN;
        try {
            String pin = request.getParameter("pin");
            if (isValidPin(pin, session)) {
                String operation = (String)session.getAttribute("operation");
		        log.debug("Operation: "+operation);
                if ("get_balance".equals(operation)) {
                    Map<String, Double> abonentsBalance = getAbonentsBalance(request);
                    request.setAttribute("balance", abonentsBalance.get(abonent));
                    return VIEW_OK_GET_BALANCE;
                } else {
                    return VIEW_OK_MOVE_MONEY;
                }
            } else {
                request.setAttribute("error_message", "PIN is invalid");
                return VIEW_FAIL_INVALID_PIN;
            }
        } catch (Exception e) {
            log.warn("Some error: abonent "+abonent,e);
            return VIEW_FAIL_INVALID_PIN;
        }
    }
%>
