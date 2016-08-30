<%@ page import="mobi.eyeline.utils.restclient.web.RestClientException" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!

  private String handle(HttpServletRequest request) throws Exception {
    final String event = request.getParameter("event");
    final String eventType = request.getParameter("event.type");
    final String eventText = request.getParameter("event.text");

    if ("message".equals(event) && "text".equals(eventType)) {
      if (eventText != null && eventText.startsWith("proxy ")) {
        final String serviceId = eventText.substring("proxy ".length());

        final ServiceRegistryEntry serviceRegistryEntry = findServiceInRegistry(serviceId);
        if (serviceRegistryEntry != null) {
          return doProxy(request, serviceId, serviceRegistryEntry);
        }

        return "PROXY_SERVICE_INVALID";
      }
    }

    return "PROXY_START_PAGE";
  }

  private String doProxy(HttpServletRequest req,
                         final String serviceId,
                         ServiceRegistryEntry serviceRegistryEntry) {

    req.setAttribute(
        "PROXY_SERVICE_TITLE",
        serviceRegistryEntry.getTitle() != null ? serviceRegistryEntry.getTitle() : serviceId
    );

    {
      final String safeSid = serviceId.replace(".", "_");
      final String safeMyId = req.getParameter("serviceId").replace(".", "_");
      final String userId = req.getParameter("user_id");

      final String path = API_ROOT + "/profile/" + userId + "/services.proxy-" + safeMyId;
      new RestClient().json(path, RestClient.put(RestClient.content(safeSid)));
    }

    {
      final String userId = req.getParameter("user_id");
      final String protocol = req.getParameter("protocol");
      new Thread(new Runnable() {
        @Override
        public void run() {
          sendGet(MOBILIZER_ROOT + "/push?" +
              "service=" + serviceId +
              "&user_id=" + userId +
              "&protocol=" + protocol +
              "&scenario=default-noinform");
        }
      }).start();
    }

    return "PROXY_OK";
  }
%>

<%
  final String target = handle(request);
%>

<% if (target.equals("PROXY_START_PAGE")) { %>
  <page version="2.0">
    <div>
      Hello! I am <b>MiniApps Proxy</b> bot.
      <br/>
      Here you can instantly test your bot without having to actually publish it.
      Note that only bots built on top of the <a href="http://miniapps.run">MiniApps platform</a>
      can be tested here.
    </div>
    <div>
      <br/>
      Use the command: <b>proxy [your-service-id]</b>.
    </div>
  </page>

<% } else if (target.equals("PROXY_SERVICE_INVALID")) { %>
  <page version="2.0">
    <div>
      Invalid service identifier. Please, try again.
    </div>
  </page>

<% } else if (target.equals("PROXY_OK")) { %>
  <page version="2.0" attributes="telegram.keep.session: true; mbf.keep.session: true;">
    <div>
      Okay, now proxying <b><%=request.getAttribute("PROXY_SERVICE_TITLE")%></b>. Type
      "proxy [your-service-id]" at any time to switch to another service.
    </div>
  </page>
<% } %>