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

package pt.ua.tm.neji.web.manage;

import com.google.gson.Gson;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.web.server.Server;

/**
 * REST Resource for calls on path "/manage/".
 *
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>))
 * @version 1.0
 * @since 1.0
 */
@Path("/")
public class ManageResource {

    /**
     * {@link Logger} to be used in the class.
     */
    private static final Logger logger = LoggerFactory.getLogger(ManageResource.class);
    
    /**
     * Add new dictionary.
     * @param dictionaryData dictionary data
     * @param dictionaryFile dictionary file
     * @param dictionaryFileInfo dictionary file info
     */
    @POST
    @Path("/addDictionary")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void addDictionary(@FormDataParam("dictionary_data") String dictionaryData, 
            @FormDataParam("dictionary_file") InputStream dictionaryFile,
            @FormDataParam("dictionary_file") FormDataContentDisposition dictionaryFileInfo) {
                        
        Server server = Server.getInstance();
        
        // Create model
        Gson gson = new Gson();
        Dictionary dictionary = gson.fromJson(dictionaryData, Dictionary.class);
        if (dictionaryFileInfo.getFileName() == null) {
            dictionary.setFile(null);
        }
        
        // Add dictionary
        try {
            server.addDictionary(dictionary, dictionaryFile);
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
    }    
     
    /**
     * Get all dictionaries.
     * @return dictionaries list
     */
    @GET
    @Path("/getDictionaries")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Dictionary> getDictionaries() {
                
        Server server = Server.getInstance();

        // Get dictionaries
        List<Dictionary> dictionaries;
        try {
            dictionaries = server.getDictionaries();
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
        
        return dictionaries;
    }
    
    /**
     * Remove dictionary.
     * @param id dictionary id
     */
    @POST
    @Path("/removeDictionary/id={id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void removeDictionary(@PathParam("id") int id) {
                        
        Server server = Server.getInstance();
        
        // Remove dictionary
        try {
            server.removeDictionary(id);
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
    }
    
    /**
     * Add new model.
     * @param modelFile model file
     * @param modelFileDetail model file details
     * @param configurationFile configuration file
     * @param configurationDetail configuration file details
     * @param propertiesFile properties file
     * @param propertiesDetail properties file details
     * @param modelData model data
     */
    @POST
    @Path("/addModel")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void addModel(@FormDataParam("model_data") String modelData,
            @FormDataParam("model_file") InputStream modelFile,
            @FormDataParam("model_file") FormDataContentDisposition modelFileDetail,
            @FormDataParam("configuration_file") InputStream configurationFile,
            @FormDataParam("configuration_file") FormDataContentDisposition configurationDetail,
            @FormDataParam("properties_file") InputStream propertiesFile,
            @FormDataParam("properties_file") FormDataContentDisposition propertiesDetail) {
                        
        Server server = Server.getInstance();
        
        // Create model
        Gson gson = new Gson();
        Model model = gson.fromJson(modelData, Model.class);
        if (modelFileDetail.getFileName() == null) {
            model.setFile(null);
        }
        
        // Add model
        try {
            server.addModel(model, modelFile, configurationFile, configurationDetail.getFileName(),
                    propertiesFile, propertiesDetail.getFileName());
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
    }
        
    /**
     * Get all models.
     * @return models list
     */
    @GET
    @Path("/getModels")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Model> getModels() {
                
        Server server = Server.getInstance();

        // Get models
        List<Model> models;
        try {
            models = server.getModels();
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
        
        return models;
    }
    
    /**
     * Remove model.
     * @param id model id
     */
    @POST
    @Path("/removeModel/id={id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void removeModel(@PathParam("id") int id) {
                        
        Server server = Server.getInstance();
        
        // Remove model
        try {
            server.removeModel(id);
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
    }
    
    /**
     * Edit model.
     * @param model model data
     */
    @POST
    @Path("/editModel")
    @Consumes(MediaType.APPLICATION_JSON)
    public void editModel(Model model) {
                        
        Server server = Server.getInstance();
        
        // Remove model
        try {
            server.editModel(model);
        } catch(NejiException ex) {
            ex.printStackTrace();
            throw new WebApplicationException(toResponse(ex));
        }
    }
        
    /**
     * Build a response
     * @param ex exception
     * @return response
     */
    private Response toResponse(Exception ex) {
        return Response.status(Status.BAD_REQUEST)
                       .entity(ex.getLocalizedMessage())
                       .build();
    }
    
    /**
     * Get logged user username.
     * @param req request
     * @return user username
     */
    @GET
    @Path("/getUsername")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String getUsername(@Context HttpServletRequest req) {
                
        if (req.getUserPrincipal() == null) {
            return null;
        }
        
        return req.getUserPrincipal().getName();
    }
    
    /**
     * Logout.
     * @param req request
     */
    @POST
    @Path("/logout")
    public void logout(@Context HttpServletRequest req) {
        req.getSession().invalidate();
    }
}
