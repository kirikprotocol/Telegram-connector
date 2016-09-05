<%@ page import="mobi.eyeline.utils.restclient.web.RestClientException" %>
<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<%@include file="common.jspf" %>

<%!

  private String handle(HttpServletRequest request) throws Exception {
    final String event = request.getParameter("event");
    final String eventType = request.getParameter("event.type");
    final String eventText = request.getParameter("event.text");

    if (eventText != null && eventText.trim().equalsIgnoreCase("proxy stop")) {
      return "PROXY_STOP";

    } else if (eventText != null && eventText.startsWith("proxy ")) {
      final String serviceId = eventText.substring("proxy ".length());

      final ServiceRegistryEntry serviceRegistryEntry = findServiceInRegistry(serviceId);
      if (serviceRegistryEntry != null) {
        return doProxy(request, serviceRegistryEntry);
      }

      return "PROXY_SERVICE_INVALID";
    }

    return "PROXY_START_PAGE";
  }

  private String doProxy(HttpServletRequest req,
                         ServiceRegistryEntry registryEntry) {

    final String serviceId = registryEntry.getId();

    req.setAttribute(
        "PROXY_SERVICE_TITLE",
        registryEntry.getTitle() != null ? registryEntry.getTitle() : serviceId
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
      Now just type <b>proxy [service-id]</b> to start proxying your service (e.g. proxy globalussd-lk.44.1444333222111),
      and then <b>proxy stop</b> to stop.
    </div>
  </page>

<% } else if (target.equals("PROXY_SERVICE_INVALID")) { %>
  <page version="2.0">
    <div>
      Invalid service identifier. Please, try again.
    </div>
  </page>

<% } else if (target.equals("PROXY_STOP")) { %>
  <page version="2.0">
    <div>
      Okay, your proxy session is terminated.
      <br/>
      Type <b>proxy [your-service-id]</b> to start again.
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