<%@page language="java" import="java.util.*"
%><%@page import="com.eyelinecom.whoisd.sads2.common.SMSDB"
%><%@ page import="javax.naming.InitialContext"
%><%@ page import="javax.sql.DataSource"
%><jsp:forward page="<%=execute(request, response)%>"/><%!

    private final static org.apache.log4j.Category log =
            org.apache.log4j.Category.getInstance("smartcash.ctrl_check_number");

    private final static String VIEW_OK_TRANSACTION = "ok_transaction.jsp";
    private final static String VIEW_FAIL_INVALID_NUMBER = "enter_number.jsp";
	
	private final static String SMS_SENDER = "java:/comp/env/jdbc/sms";

    private SMSDB smsSender;

    private SMSDB getSMSDB() throws Exception{
    	if (smsSender == null) {
			Properties config = new Properties();
			config.setProperty("sql_insert", "insert into outgoing_sms (ABONENT, BODY, SOURCE, CONNECTION_NAME, CONTENT_TYPE, send_time) VALUES(?, ?, '+11112228', 'msag2', ?, timestampadd(SECOND, -9000, now()))");
			config.setProperty("default_content_type", "D_SRV");
			InitialContext cxt = new InitialContext();
			DataSource ds = (DataSource) cxt.lookup(SMS_SENDER);
			smsSender = new SMSDB(config, ds);
    	}
    	return smsSender;
    
    }
    
    
    private Map<String, Double> getAbonentsBalance(HttpServletRequest request) throws Exception {
        ServletContext context = request.getSession().getServletContext();
        return (Map<String, Double>) context.getAttribute("abonentsBalance");
    }

    private String execute(HttpServletRequest request, HttpServletResponse response) {
        String abonent = request.getParameter("abonent");
        try {
            HttpSession session = request.getSession();
            double amount = (Double)session.getAttribute("amount");
            String number = request.getParameter("number");
            if (number!=null && number.matches("[1-9][0-9]{10,}")){
                Map<String,Double> abonentsBalance = getAbonentsBalance(request);
                double yourBalance = abonentsBalance.get(abonent) - amount;
                abonentsBalance.put(abonent, yourBalance);
                if (abonentsBalance.containsKey(number)) {
                    double numberBalance = abonentsBalance.get(number) + amount;
                    abonentsBalance.put(number, numberBalance);
                } else {
					abonentsBalance.put(number, (1000+amount));
				}
				try{
					getSMSDB().sendSMS("You received transfer of "+amount+" from "+abonent, number);
				} catch (Exception es) {
					log.warn("Unable to send SMS",es);
				}
            	request.setAttribute("number", number);
            	request.setAttribute("balance", yourBalance);
                return VIEW_OK_TRANSACTION;
            } else {
                request.setAttribute("error_message", "Number is invalid");
                return VIEW_FAIL_INVALID_NUMBER;
            }
        } catch (Exception e) {
            log.debug("Some error: abonent "+abonent, e);
            return VIEW_FAIL_INVALID_NUMBER;
        }
    }
%>
