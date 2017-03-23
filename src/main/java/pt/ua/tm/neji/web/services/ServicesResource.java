/*
 * Copyright (c) 2016 BMD Software and University of Aveiro.
 *
 * Neji is a flexible and powerful platform for biomedical information extraction from text.
 *
 * This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.
 *
 * This project is a free software, you are free to copy, distribute, change and transmit it.
 * However, you may not use it for commercial purposes.
 *
 * It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package pt.ua.tm.neji.web.services;

import com.google.gson.Gson;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.web.server.Server;

/**
 * REST Resource for calls on path "/services20/".
 *
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>))
 * @version 1.0
 * @since 1.0
 */
@Path("/")
public class ServicesResource {

    private static Logger logger = LoggerFactory.getLogger(ServicesResource.class);
    private List<String> pathSegments = new ArrayList<>();
    
    /**
     * Add Service.
     * @param logoFile logo file
     * @param logoInfo logo info
     * @param fpFile false positives file
     * @param fpInfo false positives info
     * @param serviceData service data
     */
    @POST
    @Path("/addService")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void addService(@FormDataParam("service_logo") InputStream logoFile,
            @FormDataParam("service_logo") FormDataContentDisposition logoInfo,
            @FormDataParam("service_fp") InputStream fpFile,
            @FormDataParam("service_fp") FormDataContentDisposition fpInfo,
            @FormDataParam("service_data") String serviceData) {

        // Get server instance
        Server server = Server.getInstance();
        
        // Create service
        Gson gson = new Gson();
        Service service = gson.fromJson(serviceData, Service.class);
        if (logoInfo.getFileName() == null) {
            service.setLogo(null);
        }
        if (fpInfo.getFileName() == null) {
            service.setFalsePositives(null);
        }
                
        // Add service
        try {
            server.addService(service, logoFile, fpFile);
        } catch (NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
    }
    
    /**
     * Get Services.
     * @return services list
     */
    @GET
    @Path("/getServices")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Service> getServices() {
                
        Server server = Server.getInstance();
        
        // Get services
        List<Service> services;
        try {
            services = server.getServices();
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
                
        return services;
    }
    
    /**
     * Get Service.
     * @param name service name
     * @return service
     */
    @GET
    @Path("/getService/name={name}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Service getService(@PathParam("name") String name) {
                
        Server server = Server.getInstance();

        // Get services
        Service service;
        try {
            service = server.getService(name);
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
        
        return service;
    }
    
    /**
     * Remove service.
     * @param id service id
     */
    @POST
    @Path("/removeService/id={id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void removeService(@PathParam("id") int id) {
                        
        Server server = Server.getInstance();
        
        // Remove service
        try {
            server.removeService(id);
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
    }
    
        
    /**
     * Edit Service.
     * @param logoFile logo file
     * @param logoInfo logo info
     * @param fpFile false positives file
     * @param fpInfo false positives info
     * @param serviceData service data
     */
    @POST
    @Path("/editService")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void editService(@FormDataParam("service_logo") InputStream logoFile,
            @FormDataParam("service_logo") FormDataContentDisposition logoInfo,
            @FormDataParam("service_fp") InputStream fpFile,
            @FormDataParam("service_fp") FormDataContentDisposition fpInfo,
            @FormDataParam("service_data") String serviceData) {
        
        // Get server instance
        Server server = Server.getInstance();
        
        // Create service
        Gson gson = new Gson();
        Service service = gson.fromJson(serviceData, Service.class);
        if (logoInfo.getFileName() == null) {
            service.setLogo(null);
        }
        if (fpInfo.getFileName() == null) {
            service.setFalsePositives(null);
        }
        
        // Edit service
        try {
            server.editService(service, logoFile, fpFile);
        } catch (NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
    }
    
    /**
     * Get service groups.
     * @param name service name
     * @return service
     */
    @GET
    @Path("/getGroups/name={name}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Map<String, String> getGroups(@PathParam("name") String name) {
                
        Server server = Server.getInstance();

        // Get service groups (with normalized names)
        Map<String, String> groups;
        try {
            groups = server.getService(name).getGroupsNormalization();
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
        
        return groups;
    }
    
    /**
     * Build a response
     * @param ex exception
     * @return response
     */
    private Response toResponse(Exception ex) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(ex.getLocalizedMessage())
                       .build();
    }
}
