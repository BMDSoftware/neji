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

package pt.ua.tm.neji.web.annotate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.view.Viewable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree;
import pt.ua.tm.neji.tree.TreeNode;
import pt.ua.tm.neji.web.annotate.pubmed.Document;
import pt.ua.tm.neji.web.annotate.pubmed.Pubmed;
import pt.ua.tm.neji.web.annotate.pubmed.PubmedResponse;
import pt.ua.tm.neji.web.batch.ServerBatchExecutor;
import pt.ua.tm.neji.web.processor.ServerProcessor;
import pt.ua.tm.neji.web.server.Server;
import pt.ua.tm.neji.web.services.Service;

/**
 * Annotation web services.
 * 
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>)
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 2.0
 * @since 1.0
 */
@Path("/")
public class AnnotationResource {
    
    private static Logger logger = LoggerFactory.getLogger(AnnotationResource.class);
    private List<String> pathSegments = new ArrayList<>();

    // will be called when there are still trailing path segments
    @Path("{path}")
    public AnnotationResource readPath(@PathParam("path") String pathSegment) {
        pathSegments.add(pathSegment);
        return this;
    }

    /**
     * Invoked on GET call after the entire path has been processed.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response onEndOfPath() {
        Object responseToReturn;

        if(pathSegments.isEmpty() || pathSegments.get(0).equals("services")) {
            responseToReturn = new Viewable("/WEB-INF/list.jsp");

        } else {
            Server server = Server.getInstance();
            Service service = null;
            try {
                service = server.getService(pathSegments.get(0));
            } catch(NejiException ex) {}
            
            if (service == null) {
                responseToReturn = "Invalid service ID.";

            } else {
                if (pathSegments.size() == 1) {
                    responseToReturn = new Viewable("/WEB-INF/becas.jsp");

                } else if (pathSegments.size() == 2 && pathSegments.get(1).equals("edit")) {
                    Context context = Server.getInstance().getContext();
                    Map<String, Object> map = new HashMap<>();
                    map.put("context", context);
                    map.put("service", service);
                    responseToReturn = new Viewable("/WEB-INF/edit_service.jsp", map);
                } else {
                    return Response.ok().status(Response.Status.BAD_REQUEST).build();
                }
            }
        }

        return Response.ok(responseToReturn).build();
    }

    /**
     * Invoked on POST call by Becas widget annotate function
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,
            "application/a1", "application/neji",
            "application/conll", "application/bioc"})
    public Response onEndOfPath(final BecasRequest request, @HeaderParam("Accept") String accept) {
        Object responseToReturn;
        
        String serviceID = pathSegments.get(0);
        Service service = null;
        
        try {
            service = Server.getInstance().getService(serviceID);
        } catch(NejiException ex) {
            ex.printStackTrace();
        }
                
        if(service == null) {
            responseToReturn = "Invalid service ID.";

        } else {
            return evaluatePathSegment(service, pathSegments.get(1), request, accept);
        }

        return Response.ok(responseToReturn).build();
    }


    /**
     * Invoked on POST call by Becas widget export function
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response onEndOfPath(@DefaultValue("{}") @FormParam("groups") final String groupsJson,
                                @FormParam("text")   final String text,
                                @DefaultValue("a1") @FormParam("format") final String format,
                                @FormParam("pmid") final int pmid,
                                @DefaultValue("raw") @FormParam("inputformat") final String inputFormat,
                                @DefaultValue("{}") @FormParam("extraparameters") final String extraParametersJson) {
        Object responseToReturn;

        String serviceID = pathSegments.get(0);
        Service service = null;
        try {
            service = Server.getInstance().getService(serviceID);
        } catch(NejiException ex) {}
        
        if(service == null) {
            responseToReturn = "Invalid service ID.";

        } else {
            // Problema aqui !!!! -> lá em cima só apanha o groupsJson, text e format, tem que
            // apanhar também o pmid para se poder usar caso ele exista, ou tentar fazer um nova
            // funcao mas com inteiro em vez de String para ver se assume essa.
            return evaluatePathSegment(service, pathSegments.get(1), groupsJson, 
                    text, format, pmid, inputFormat, extraParametersJson);
        }

        return Response.ok(responseToReturn).build();
    }
    
    private Response evaluatePathSegment(Service service, String segment, Object... args){
        try{
            switch (segment){
                case "annotate":
                    BecasRequest request = (BecasRequest) args[0];
                    String accept = args[1].toString();
                    return annotate(service, request, accept);                    
                case "export":
                    String groupsJson = args[0].toString();
                    String text = args[1].toString();
                    String format = args[2].toString();
                    String inputFormat = args[4].toString();
                    String extraParametersJson = args[5].toString();                       
                    return export(service, groupsJson, text, format, inputFormat, extraParametersJson);
                case "pubmed-annotate":
                    request = (BecasRequest) args[0];
                    return annotatePubmed(service, request);
                case "pubmed-export":
                    groupsJson = args[0].toString();
                    format = args[2].toString();
                    inputFormat = args[4].toString();
                    extraParametersJson = args[5].toString();
                    String pmid = args[3].toString();
                    text = Pubmed.getCleanTextFromPM(pmid).toString();
                    return export(service, groupsJson, text, format, inputFormat, extraParametersJson);
                default:
                    return Response.ok("Invalid request.").status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.ok(ex.toString()).status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }
    
    private Response annotate(final Service service,
                              final BecasRequest request,
                              @HeaderParam("Accept") String accept) throws Exception {
                
        Server server = Server.getInstance();        
        
        InputFormat inputFormat = InputFormat.valueOf(request.getInputFormat());
        ServerBatchExecutor executor = new ServerBatchExecutor(service, server.getExecutor(), 
                request.getText(), request.getGroups(), inputFormat, null, 
                request.getExtraParameters());
        
        executor.run(ServerProcessor.class, server.getContext());
        Corpus corpus = executor.getProcessedCorpora().get(0);

        return convertCorpusToBecasJson(request.getText(), corpus);
    }
    
    private Response annotatePubmed(final Service service, final BecasRequest request) 
            throws Exception {
        
        String title = null;
        String abstract_ = null;
        List<String> title_entities = null;
        List<String> abstract_entities = null;
        
        Server server = Server.getInstance();
        
        // Get pubmed document (text)
        Document pubmed = Pubmed.getCleanTextFromPM(request.getPmid());  
        InputFormat inputFormat = InputFormat.valueOf(request.getInputFormat());
        
        // Annotate title
        if (pubmed.getTitle() != null) {
            
            // Get title from document
            title = pubmed.getTitle();
            
            // Annotate            
            ServerBatchExecutor executor = new ServerBatchExecutor(service, server.getExecutor(), 
                title, request.getGroups(), inputFormat, null, request.getExtraParameters());
            executor.run(ServerProcessor.class, server.getContext());
            Corpus titleCorpus = executor.getProcessedCorpora().get(0);
            
            // Get title entities
            title_entities = getEntitiesFromCorpus(title, titleCorpus);
        }
        
        // Annotate abstract
        if (pubmed.getBody() != null) {
            
            // Get title from document
            abstract_ = pubmed.getBody();
            
            // Annotate
            ServerBatchExecutor executor = new ServerBatchExecutor(service, server.getExecutor(), 
                abstract_, request.getGroups(), inputFormat, null, request.getExtraParameters());
            executor.run(ServerProcessor.class, server.getContext());
            Corpus abstractCorpus = executor.getProcessedCorpora().get(0);
            
            // Get title entities
            abstract_entities = getEntitiesFromCorpus(abstract_, abstractCorpus);
        }
        
        // Convert corpus to becas pubmed json                
        return convertCorpusToBecasPubmedJson(request.getPmid(), title, title_entities, 
                abstract_, abstract_entities);
    }
    
    private Response convertCorpusToBecasJson(String inputText, Corpus corpus) {
        List<String> entities = new ArrayList();
        List<String> ids = new ArrayList();

        for (Sentence s : corpus.getSentences()) {
            List<TreeNode<Annotation>> nodes = s.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);
            for (TreeNode<Annotation> node : nodes) {
                Annotation a = node.getData();
                if (a.getIDs().isEmpty()) {
                    // skipping concepts without ids
                    continue;
                }

                String idText = "";
                Iterator<Identifier> iter = a.getIDs().iterator();
                while (iter.hasNext()) {
                    idText += iter.next().toString();
                    if (iter.hasNext()) {
                        idText += ";";
                    }
                }

                int index = inputText.indexOf(s.getText()) + s.getToken(a.getStartIndex()).getStart();
                entities.add(a.getText() + "|" + idText + "|" + index);
                ids.add(idText);
            }
        }

        Map<String, Object> outputJsonMap = new TreeMap<>();
        outputJsonMap.put("entities", entities);
        outputJsonMap.put("ids", ids);
        outputJsonMap.put("text", inputText);
        return Response.ok(new Gson().toJson(outputJsonMap)).build();
    }
    
    private List<String> getEntitiesFromCorpus(String inputText, Corpus corpus) {
        List<String> entities = new ArrayList<>();

        for (Sentence s : corpus.getSentences()) {
            List<TreeNode<Annotation>> nodes = s.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);
            for (TreeNode<Annotation> node : nodes) {
                Annotation a = node.getData();
                if (a.getIDs().isEmpty()) {
                    // skipping concepts without ids
                    continue;
                }

                String idText = "";
                Iterator<Identifier> iter = a.getIDs().iterator();
                while (iter.hasNext()) {
                    idText += iter.next().toString();
                    if (iter.hasNext()) {
                        idText += ";";
                    }
                }

                int index = inputText.indexOf(s.getText()) + s.getToken(a.getStartIndex()).getStart();
                entities.add(a.getText() + "|" + idText + "|" + index);
            }
        }
        
        return entities;
    }
    
    
    private Response convertCorpusToBecasPubmedJson(String pmid, String title, 
            List<String> title_entities, String abstract_, List<String> abstract_entities) {
        
        PubmedResponse pubmed = new PubmedResponse(pmid, title, abstract_, title_entities, 
                abstract_entities, new ArrayList());
        
        return Response.ok(new Gson().toJson(pubmed)).build();      
    }
    
    private Response export(final Service service,
                            final String groupsJson,
                            final String text,
                            final String format,
                            final String inputFormat,
                            final String extraParametersJson) {
                                
        final OutputFormat outputFormat = getOutputFormat(Server.getInstance().getContext(), format);
        Gson gson = new Gson();
        final Map<String, Boolean> groups =
                gson.fromJson(groupsJson, new TypeToken<Map<String, Boolean>>() {}.getType());

        if(outputFormat==null) {
            return Response.ok(new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                    IOUtils.write("This file is empty because the requested deployed server was not set " +
                            "to export text in the " + format + " format, or because this is an invalid " +
                            "format.", outputStream, "UTF-8");
                }
            }, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition",
                            "attachment; filename=\"error.txt\"")
                    .build();

        } else {
            return Response.ok(new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                    Server server = Server.getInstance();
                    
                    try {
                        InputFormat inputFormatObj = getInputFormat(Server.getInstance().getContext(), inputFormat);
                        
                        Map<String, String> extraParameters = new HashMap();
                        if (extraParametersJson != null && !extraParametersJson.isEmpty()) {
                            extraParameters = gson.fromJson(extraParametersJson, new TypeToken<Map<String, String>>() {}.getType());
                        }
                        
                        ServerBatchExecutor executor = 
                                new ServerBatchExecutor(service, server.getExecutor(), 
                                        text, groups, inputFormatObj, outputFormat, extraParameters);
                        executor.run(ServerProcessor.class, server.getContext());
                        String outputText = executor.getAnnotatedText();
                                                
                        IOUtils.write(outputText, outputStream, "UTF-8");

                    } catch (Exception ex) {
                        throw new WebApplicationException(ex);
                    }
                }
            }, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition",
                            "attachment; filename=\"text." + outputFormat.name().toLowerCase() + "\"")
                    .build();
        }
    }
    
    private static OutputFormat getOutputFormat(Context context, String format){
        List<OutputFormat> contextFormat = context.getConfiguration().getOutputFormats();
        OutputFormat f = null;
        switch (format){                 
            case "a1":
                f = OutputFormat.A1;
                break;
            case "b64": 
                f = OutputFormat.B64; 
                break;
            case "bc2": 
                f = OutputFormat.BC2; 
                break;
            case "bioc": 
                f = OutputFormat.BIOC; 
                break;
            case "conll": 
                f = OutputFormat.CONLL; 
                break;
            case "json": 
                f = OutputFormat.JSON;
                break;
            case "neji": 
                f = OutputFormat.NEJI;
                break;
            case "pipe": 
                f = OutputFormat.PIPE;
                break;
            case "pipext": 
                f = OutputFormat.PIPEXT;
                break;
            case "xml": 
                f = OutputFormat.XML;
                break;
            case "a1_min":
                f = OutputFormat.A1_MIN;
                break;
        }
        if(contextFormat.contains(f)){
            return f;
        } else {
            return null;
        }
    }
    
    private static InputFormat getInputFormat(Context context, String format){
        InputFormat f = null;
        switch (format.toLowerCase()){                 
            case "raw":
                f = InputFormat.RAW;
                break;
            case "xml": 
                f = InputFormat.XML; 
                break;
            case "bioc": 
                f = InputFormat.BIOC; 
                break;
            default:
                f = InputFormat.RAW;
        }
        
        return f;
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
