package org.jboss.resteasy.wadl;

import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.jboss.resteasy.spi.ResteasyDeployment;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:l.weinan@gmail.com">Weinan Li</a>
 * This servlet does not support grammars.
 * Use @org.jboss.resteasy.wadl.ResteasyWadlDefaultServlet instead.
 */
// TODO: remove this in master in proper time
@Deprecated
@WebServlet(name = "ResteasyWadlServlet", urlPatterns = "/application.xml")
public class ResteasyWadlServlet extends HttpServlet {
    
   private static final Logger log = Logger.getLogger("io.quarkus.resteasy");

   private Map<String, ResteasyWadlServiceRegistry> services;

   private ResteasyWadlServletWriter wadlWriter = new ResteasyWadlServletWriter();

   public ResteasyWadlServletWriter getWadlWriter() {
      return wadlWriter;
   }

   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      log.debug("Loading ResteasyWadlServlet");

      scanResources();

      log.debug("ResteasyWadlServlet loaded");

      // make it possible to get to us for rescanning
      ServletContext servletContext = config.getServletContext();
      servletContext.setAttribute(getClass().getName(), this);
   }

   @Override
   protected void service(HttpServletRequest req, HttpServletResponse resp)
         throws IOException {
      String pathInfo = req.getPathInfo();
      String uri = req.getRequestURL().toString();
      uri = uri.substring(0, uri.length() - req.getServletPath().length());
      log.debug("Serving " + pathInfo);
      log.debug("Query " + req.getQueryString());
      if (this.services == null) scanResources();
      if (this.services == null) {
         resp.sendError(503, "There are no Resteasy deployments initialized yet to scan from.");
         return;
      }
      resp.setContentType(MediaType.APPLICATION_XML);
      this.wadlWriter.writeWadl(uri, req, resp, services);
   }

   public void scanResources() {

      ServletConfig config = getServletConfig();
      ServletContext servletContext = config.getServletContext();

      @SuppressWarnings(value = "unchecked")
      Map<String, ResteasyDeployment> deployments = (Map<String, ResteasyDeployment>) servletContext.getAttribute(ResteasyContextParameters.RESTEASY_DEPLOYMENTS);
      if (deployments == null) return;
      synchronized (this) {
         services = new HashMap<>();
         for (Map.Entry<String, ResteasyDeployment> entry : deployments.entrySet()) {
            services.put(entry.getKey(), ResteasyWadlGenerator.generateServiceRegistry(entry.getValue()));
         }
      }
   }

}
