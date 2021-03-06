package org.jboss.resteasy.wadl;

import org.jboss.resteasy.spi.ResteasyDeployment;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:l.weinan@gmail.com">Weinan Li</a>
 */
public abstract class ResteasyWadlDefaultResource {
    
   private static final Logger log = Logger.getLogger("io.quarkus.resteasy");

   private Map<String, ResteasyWadlServiceRegistry> services = new HashMap<>();

   private void loadServices(ResteasyDeployment deployment) {
         services.put("/", ResteasyWadlGenerator.generateServiceRegistry(deployment));
   }

   public Map<String, ResteasyWadlServiceRegistry> getServices() {
      return services;
   }

   ResteasyWadlWriter wadlWriter = new ResteasyWadlWriter(); // create a default servlet writer.

   public ResteasyWadlWriter getWadlWriter() {
      return wadlWriter;
   }

   @GET
   @Path("/application.xml")
   @Produces("application/xml")
   public String output(@Context ResteasyDeployment deployment) {
      loadServices(deployment);

      try {
         return wadlWriter.getStringWriter("", services).toString();
      } catch (JAXBException e) {
         log.error("Error while processing WADL", e);
      }
      return null;
   }


   @GET
   @Path("/wadl-extended/{path}")
   @Produces("application/xml")
   public Response grammars(@PathParam("path") String path, @Context ResteasyDeployment deployment) {
      loadServices(deployment);
      wadlWriter.createApplication("", services);

      return Response
            .ok()
            .type(MediaType.APPLICATION_XML_TYPE)
            .entity(wadlWriter.getWadlGrammar().getSchemaOfUrl(path))
            .build();
   }
}
