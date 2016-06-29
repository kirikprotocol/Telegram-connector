<%@page language="java" import="java.util.*" %><jsp:forward page="<%=execute(request, response)%>"/>
<%!
    private final static org.apache.log4j.Category log =
            org.apache.log4j.Category.getInstance("smartcash.ctrl_check_operation");

    //private final static String VIEW_OK_ENTER_PIN = "enter_pin.jsp";
    private final static String VIEW_OK_ENTER_PIN = "_ctrl_check_pin.jsp";
    private final static String VIEW_FAIL_INVALID_OPERATION = "invalid_operation.jsp";

    private boolean isValidOperation(String operation){
        return "get_balance".equals(operation) || "move_money".equals(operation);
    }

    private String execute(HttpServletRequest request, HttpServletResponse response) {
        String abonent = request.getParameter("abonent");
        try {
            HttpSession session = request.getSession();
            String operation = request.getParameter("operation");
            if (isValidOperation(operation)) {
                session.setAttribute("operation", operation);
                return VIEW_OK_ENTER_PIN;
            }
            return VIEW_FAIL_INVALID_OPERATION;
        } catch (Exception e) {
            log.warn("Some error: abonent "+abonent,e);
            return VIEW_FAIL_INVALID_OPERATION;
        }
    }
%>
