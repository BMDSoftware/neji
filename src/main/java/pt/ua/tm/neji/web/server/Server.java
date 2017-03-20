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

package pt.ua.tm.neji.web.server;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.security.JDBCLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.web.cli.ServerConfiguration;
import pt.ua.tm.neji.web.cli.WebMain;
import pt.ua.tm.neji.web.database.DatabaseHandler;
import pt.ua.tm.neji.web.database.DefaultDatabaseHandler;
import pt.ua.tm.neji.web.manage.Dictionary;
import pt.ua.tm.neji.web.manage.Model;
import pt.ua.tm.neji.web.services.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static pt.ua.tm.neji.web.WebConstants.*;

/**
 * Server to execute the deployable web services.
 *
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Server extends org.eclipse.jetty.server.Server implements LifeCycle.Listener {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Server.class);
    private static Server instance;

    private boolean initialized;
    private final Map<String, Service> serviceMap;
    private Context context;
    private int numThreads;
    private ExecutorService executor;
    private final DatabaseHandler db;

    /**
     * Get the server instance.
     *
     * @return server instance
     */
    public static Server getInstance() {

        if (instance == null) {
            instance = new Server();
        }

        return instance;
    }

    /**
     * Constructor. Instantiates a new server object.
     */
    private Server() {
        super();
        serviceMap = new HashMap<>();
        initialized = false;
        db = new DefaultDatabaseHandler("neji.db", true);
    }

    /**
     * Initialize the server.
     *
     * @param context    context
     * @param port       port
     * @param numThreads number of threads
     * @throws NejiException
     */
    public void initialize(Context context, int port, int numThreads) throws NejiException {

        this.context = context;
        this.numThreads = numThreads;
        this.executor = Executors.newFixedThreadPool(numThreads);

        // Get services
        for (Service s : db.getServices()) {
            serviceMap.put(s.getName(), s);
        }

        // Start a Jetty server with some sensible defaults
        setStopAtShutdown(true);

        // Increase thread pool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(1);
        threadPool.setMaxThreads(100);
        threadPool.setDetailedDump(false);
        setThreadPool(threadPool);

        // HTTPS
        SslSelectChannelConnector sslconector = new SslSelectChannelConnector();
        sslconector.setKeystore(ServerConfiguration.getInstance().getHttpsKeystoreFile());
        sslconector.setKeyPassword(ServerConfiguration.getInstance().getHttpsKeystorePassword());

        sslconector.setTruststore(ServerConfiguration.getInstance().getHttpsTruststoreFile());
        sslconector.setTrustPassword(ServerConfiguration.getInstance().getHttpsTruststorePassword());

        sslconector.setAcceptors(4);
        sslconector.setMaxIdleTime(30000);
        sslconector.setPort(port);
        setConnectors(new Connector[]{sslconector});

        // Gets the war-file
        ProtectionDomain protectionDomain = WebMain.class.getProtectionDomain();
        String warFile = protectionDomain.getCodeSource().getLocation().toExternalForm();

        String contextPath = System.getProperty("jetty.contextPath", "/");
        String serverDescriptorPath = File.separator + "WEB-INF" + File.separator + "web.xml";

        // Web context configuration
        WebAppContext webContext = new WebAppContext();
        webContext.setContextPath(contextPath);
        webContext.setWar(warFile);
        webContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        setStopAtShutdown(true);
        webContext.setDescriptor(serverDescriptorPath);
        webContext.addServlet(new ServletHolder(new JSPServlet()), "/contextadd");

        // Adds the configured context to the handlers
        HandlerList handlers = new HandlerList();
        handlers.addHandler(webContext);
        setHandler(handlers);

        // Authentication        
        try {
            JDBCLoginService loginService = new JDBCLoginService("LoginRealm");
            loginService.setConfig("loginRealm.properties");
            addBean(loginService);
        } catch (IOException ex) {
            throw new NejiException("There was a problem initialazing the jetty authentication mechanism.");
        }

        addLifeCycleListener(this);
        this.initialized = true;
    }

    /**
     * Get server context.
     *
     * @return context
     */
    public Context getContext() {

        if (!initialized) {
            throw new RuntimeException("Server was not initialized.");
        }

        return context;
    }

    /**
     * Get executor.
     *
     * @return executor
     */
    public ExecutorService getExecutor() {

        if (!initialized) {
            throw new RuntimeException("Server was not initialized.");
        }

        return executor;
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {

        try {
            logger.info("Initializing context...");
            context.initialize();
            logger.info("Installing multi-threading support...");
            context.addMultiThreadingSupport(numThreads);
        } catch (NejiException ex) {
            ex.printStackTrace();
            System.exit(2);
        }
    }

    @Override
    public void lifeCycleStopping(LifeCycle event) {

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            String m = "There was a problem terminating the server executor.";
            logger.error(m, ex);
        }

        try {
            logger.info("Terminating context...");
            context.terminate();

        } catch (NejiException ex) {
            ex.printStackTrace();
            System.exit(2);
        }
    }

    @Override
    public void lifeCycleStarted(LifeCycle event) {
    }

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
    }

    /**
     * Add new dictionary. The dictionary file is saved at dictionaries directory
     * and is added to the server context.
     *
     * @param dictionary dictionary
     * @param file       dictionary file
     * @throws NejiException
     */
    public void addDictionary(Dictionary dictionary, InputStream file) throws NejiException {

        // Validate the dictionary
        validateDictionary(dictionary);

        // Save dictionary into dictionaries folder
        File dictionaryFile = new File(DICTIONARIES_PATH + dictionary.getFile());
        try {
            IOUtils.copy(file, new FileOutputStream(dictionaryFile));
        } catch (IOException ex) {
            throw new NejiException("There was a problem adding the dictionary.\n"
                    + "Please try again later.", ex);
        }

        // Add dictionary to current server context
        try {
            List<String> dictionaryLines = IOUtils.readLines(
                    new FileInputStream(DICTIONARIES_PATH + dictionary.getFile()), "UTF-8");
            context.addNewDictionary(dictionary.getFile(), dictionaryLines);
        } catch (Exception ex) {

            // Delete dictionary file
            dictionaryFile.delete();

            throw new NejiException("The dictionary file '" + dictionary.getFile() +
                    "' is not in the correct format.\nIt needs to be in TSV format.");
        }

        // Save dictionary details in database 
        try {
            // Extract group from dictionary
            int dictionaryIndex = context.getDictionaries().size() - 1; // is the last one added
            String group = context.getDictionaries().get(dictionaryIndex).getGroup();
            dictionary.setGroup(group);

            db.addDictionary(dictionary);
        } catch (NejiException ex) {

            // Delete dictionary file
            dictionaryFile.delete();

            // Remove dicitionary from context
            context.removeDictionary(dictionary.getFile());

            throw new NejiException("There was a problem adding the dictionary.\n"
                    + "Please try again later.", ex);
        }

        // Update _priority file
        try {
            FileWriter fw = new FileWriter(DICTIONARIES_PRIORITY_PATH, true);
            fw.write(dictionary.getFile() + "\n");
            fw.close();
        } catch (IOException ex) {

            // Delete dictionary file
            dictionaryFile.delete();

            // Remove dicitionary from context
            context.removeDictionary(dictionary.getFile());

            // Delete dictionary data from database
            // ....

            throw new NejiException("There was a problem adding the dictionary.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Get all dictionaries.
     *
     * @return dictionaries list
     * @throws NejiException
     */
    public List<Dictionary> getDictionaries() throws NejiException {
        try {
            return db.getDictionaries();
        } catch (NejiException ex) {
            throw new NejiException("An error has ocurred.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Remove a dictionary. The dictionary file is deleted at dictionaries directory
     * and is removed from the server context.
     *
     * @param id dictionary id
     * @throws NejiException
     */
    public void removeDictionary(int id) throws NejiException {

        // Get dicitionary
        Dictionary dictionary;
        try {
            dictionary = db.getDictionary(id);
        } catch (NejiException ex) {
            throw new NejiException("There was a problem removing the dictionary.\n"
                    + "Please try again later.", ex);
        }

        // Verify if there are services or models that use this dicitionary
        if (!dictionary.getServices().isEmpty()) {
            throw new NejiException("Can't remove the dicitionay '" + dictionary.getName()
                    + "' because it is being used by one or more services.");
        } else if (!dictionary.getModels().isEmpty()) {
            throw new NejiException("Can't remove the dicitionay '" + dictionary.getName()
                    + "' because it is being used by one or more models.");
        }

        try {
            // Delete dicitionary from database
            db.removeDictionary(id);

            // Delete dicitionary from _priority file.
            File priorityFile = new File(DICTIONARIES_PRIORITY_PATH);
            List<String> lines = FileUtils.readLines(priorityFile);
            lines.remove(dictionary.getFile());
            FileUtils.writeLines(priorityFile, lines, false);

            // Remove dictionary from server context
            context.removeDictionary(dictionary.getFile());

            // Delete dicitionary file
            File dictionaryFile = new File(DICTIONARIES_PATH + dictionary.getFile());
            dictionaryFile.delete();

        } catch (NejiException | IOException ex) {
            throw new NejiException("There was a problem removing the dictionary.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Add new model to server. The model file is saved at models directory
     * and is added to the server context.
     *
     * @param model                 model
     * @param file                  model file
     * @param configurationFile     configuration file
     * @param configurationFileName configuration file name
     * @param propertiesFile        properties file
     * @param propertiesFileName    properties file name
     * @throws NejiException
     */
    public void addModel(Model model, InputStream file, InputStream configurationFile,
                         String configurationFileName, InputStream propertiesFile,
                         String propertiesFileName) throws NejiException {

        // Validate the model
        validateModel(model, configurationFileName, propertiesFileName, true);

        // Create model directory
        File modelDir = new File(MODELS_PATH + model.getName() + File.separator);
        File normalizationModelDir = new File(MODELS_PATH + model.getName() + File.separator +
                "normalization");
        modelDir.mkdir();
        normalizationModelDir.mkdir();

        // Save model into models folder
        try {
            IOUtils.copy(file, new FileOutputStream(modelDir.getAbsolutePath()
                    + File.separator + model.getFile()));
        } catch (IOException ex) {
            throw new NejiException("There was a problem adding the model.\n"
                    + "Please try again later.", ex);
        }

        // Save configuration file
        try {
            String configurationFilePath = modelDir.getAbsolutePath()
                    + File.separator + configurationFileName;
            IOUtils.copy(configurationFile, new FileOutputStream(configurationFilePath));
        } catch (IOException ex) {

            // Delete dir
            try {
                FileUtils.deleteDirectory(modelDir);
            } catch (IOException ex2) {
            }

            throw new NejiException("There was a problem adding the model.\n"
                    + "Please try again later.", ex);
        }

        // Save properties file
        String propertiesFilePath = modelDir.getAbsolutePath() + File.separator
                + propertiesFileName;
        File propFile = new File(propertiesFilePath);
        try {
            IOUtils.copy(propertiesFile, new FileOutputStream(propFile));

            // Add normalization line
            List<String> lines = FileUtils.readLines(propFile);
            int i = 0;
            for (String line : lines) {
                if (line.startsWith("dictionaries=")) {
                    break;
                }
                i++;
            }

            if (i < lines.size()) {
                lines.remove(i);
            }

            lines.add("dictionaries=normalization" + File.separator);

            FileUtils.writeLines(propFile, lines, false);

        } catch (IOException ex) {

            // Delete dir
            try {
                FileUtils.deleteDirectory(modelDir);
            } catch (IOException ex2) {
            }

            throw new NejiException("There was a problem adding the model.\n"
                    + "Please try again later.", ex);
        }

        // Save normalization info
        try {

            // Get normalization dictionaries
            List<String> lines = new ArrayList<>();
            for (String dictionaryName : model.getDictionaries()) {
                lines.add("../../../dictionaries/" + db.getDictionaryFile(dictionaryName));
            }

            // Write normalization _priority file
            File normalizationPriorityFile = new File(normalizationModelDir.getAbsoluteFile() +
                    File.separator + "_priority");
            FileUtils.writeLines(normalizationPriorityFile, lines);

        } catch (IOException ex) {

            // Delete dir
            try {
                FileUtils.deleteDirectory(modelDir);
            } catch (IOException ex2) {
            }

            throw new NejiException("There was a problem adding the model.\n"
                    + "Please try again later.", ex);
        }

        // Add model to current server context  
        try {
            MLModel ml = new MLModel(model.getName(), new File(propertiesFilePath));
            context.addNewModel(ml.getModelName(), ml);
        } catch (Exception ex) {

            // Delete dir
            try {
                FileUtils.deleteDirectory(modelDir);
            } catch (IOException ex2) {
            }

            throw new NejiException("The model file '" + model.getFile() +
                    "' is not in the correct format.\nIt needs to be in GZIP format, "
                    + "generated by Neji training features..");
        }

        // Save model details in database
        try {
            // Extract group from model
            String group = context.getModel(model.getName()).getSemanticGroup();
            model.setGroup(group);

            db.addModel(model);
        } catch (NejiException ex) {

            ex.printStackTrace();

            // Delete dir
            try {
                FileUtils.deleteDirectory(modelDir);
            } catch (IOException ex2) {
            }

            // Remove model from server context
            context.removeModel(model.getName());

            throw new NejiException("There was a problem adding the model.\n"
                    + "Please try again later.", ex);
        }

        // Update _priority file
        try {

            FileWriter fw = new FileWriter(MODELS_PATH + "_priority", true);
            fw.write(model.getName() + File.separator + propertiesFileName + "\n");
            fw.close();
        } catch (IOException ex) {

            ex.printStackTrace();

            // Delete dir
            try {
                FileUtils.deleteDirectory(modelDir);
            } catch (IOException ex2) {
            }

            // Remove model from server context
            context.removeModel(model.getName());

            // Delete model data from database
            // ....

            throw new NejiException("There was a problem adding the model.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * @return
     * @throws NejiException
     */
    public List<Model> getModels() throws NejiException {
        try {
            return db.getModels();
        } catch (NejiException ex) {
            throw new NejiException("An error has ocurred.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Remove a model. The model files are deleted at models directory
     * and it is removed from the server context.
     *
     * @param id model id
     * @throws NejiException
     */
    public void removeModel(int id) throws NejiException {

        // Get model
        Model model;
        try {
            model = db.getModel(id);
        } catch (NejiException ex) {
            throw new NejiException("There was a problem removing the model.\n"
                    + "Please try again later.", ex);
        }

        // Verify if there are services that use this model
        if (!model.getServices().isEmpty()) {
            throw new NejiException("Can't remove the dicitionay '" + model.getName()
                    + "' because it is being used by one or more services.");
        }

        try {
            // Delete model from database
            db.removeModel(id);

            // Delete model from _priority file.
            File priorityFile = new File(MODELS_PATH + "_priority");
            List<String> lines = FileUtils.readLines(priorityFile);
            int i = 0;
            for (String line : lines) {
                if (line.startsWith(model.getName() + File.separator)) {
                    break;
                }
                i++;
            }
            lines.remove(i);
            FileUtils.writeLines(priorityFile, lines, false);

            // Remove model from server context
            context.removeModel(model.getName());

            // Delete model files
            File modelDir = new File(MODELS_PATH + model.getName() + File.separator);
            FileUtils.deleteDirectory(modelDir);

        } catch (NejiException | IOException ex) {
            throw new NejiException("There was a problem removing the model.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Edit a model.
     *
     * @param model model
     * @throws NejiException
     */
    public void editModel(Model model) throws NejiException {

        // Validate the model
        validateModel(model, null, null, false);

        // Update model context
        try {
            Model oldModel = db.getModel(model.getId());
            List<String> addedDictionaries = ListUtils.subtract(model.getDictionaries(),
                    oldModel.getDictionaries());
            List<String> removedDictionaries = ListUtils.subtract(oldModel.getDictionaries(),
                    model.getDictionaries());

            MLModel ml = context.getModel(model.getName());

            // Remove removed dictionaries from model context
            for (String dictionaryName : removedDictionaries) {
                String dictionaryFile = db.getDictionaryFile(dictionaryName);
                ml.removeNormalizationDictionary("../../../dictionaries/" + dictionaryFile);
            }

            // Add added dictionaries to model context
            for (String dictionaryName : addedDictionaries) {
                String dictionaryFile = db.getDictionaryFile(dictionaryName);
                List<String> lines = FileUtils.readLines(new File(DICTIONARIES_PATH + dictionaryFile));
                ml.addNormalizationDictionary("../../../dictionaries/" + dictionaryFile, lines);
            }
        } catch (IOException | NejiException ex) {

            throw new NejiException("There was a problem editing the model.\n"
                    + "Please try again later.", ex);
        }

        // Update normalization info
        try {
            // Get normalization dictionaries
            List<String> lines = new ArrayList<>();
            for (String dictionaryName : model.getDictionaries()) {
                String dictionaryFile = db.getDictionaryFile(dictionaryName);
                lines.add("../../../dictionaries/" + dictionaryFile);
            }

            // Write normalization _priority file            
            File normalizationPriorityFile = new File(MODELS_PATH + model.getName() +
                    File.separator + "normalization" + File.separator + "_priority");
            FileUtils.writeLines(normalizationPriorityFile, lines);

        } catch (IOException ex) {

            throw new NejiException("There was a problem editing the model.\n"
                    + "Please try again later.", ex);
        }

        // Edit model data in database
        try {
            db.editModel(model);
        } catch (NejiException ex) {
            throw new NejiException("There was a problem editing the model.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Add new service.
     *
     * @param service service
     * @param logo    service logo
     * @param fp      service false positives
     * @throws NejiException
     */
    public void addService(Service service, InputStream logo, InputStream fp) 
            throws NejiException {
        
        // Validate service
        validateService(service, logo, fp, true);
        
        // Add service data to database
        try {
            db.addService(service);
        } catch (NejiException ex) {
            throw new NejiException("There was a problem adding the service.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Get all services.
     *
     * @return services list
     * @throws NejiException
     */
    public List<Service> getServices() throws NejiException {
        try {
            return db.getServices();
        } catch (NejiException ex) {
            throw new NejiException("An error has ocurred.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Get a service given it's name.
     *
     * @param serviceName service name
     * @return service
     * @throws NejiException
     */
    public Service getService(String serviceName) throws NejiException {
        try {
            return db.getService(serviceName);
        } catch (NejiException ex) {
            throw new NejiException("An error has ocurred.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Remove a service.
     *
     * @param id service id
     * @throws NejiException
     */
    public void removeService(int id) throws NejiException {

        // Delete model from database
        try {
            db.removeService(id);

        } catch (NejiException ex) {
            throw new NejiException("There was a problem removing the service.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Edit service.
     *
     * @param service service
     * @param logo    service logo
     * @param fp      service false positives
     * @throws NejiException
     */
    public void editService(Service service, InputStream logo, InputStream fp) 
            throws NejiException {

        // Validate service
        validateService(service, logo, fp, false);

        // Edit service data in database
        try {
            db.editService(service);
        } catch (NejiException ex) {
            throw new NejiException("There was a problem editing the service.\n"
                    + "Please try again later.", ex);
        }
    }

    /**
     * Validates the service parameters.
     *
     * @param service   service to validate
     * @param logo      logo
     * @param fp        false positives 
     * @param addOrEdit true if it is a validation for an add, false if it is for an edit
     * @throws NejiException
     */
    private void validateService(Service service, InputStream logo, 
            InputStream fp, boolean addOrEdit) throws NejiException {

        boolean stat = false;

        // Verify if name has invalid characters
        if (service.getName().trim().length() == 0) {
            throw new NejiException("The name is required");
        } else if (!service.getName().matches("^[a-zA-Z0-9._-]+$")) {
            throw new NejiException("The name can only consist of alphabetical, number, underscore(_), hifen(-) and dot(.) characters");
        }

        // Verify if already exists a service with the same name
        try {
            stat = db.existsService(service);
        } catch (NejiException ex) {
            throw new NejiException("There was a problem adding the service.\n"
                    + "Please try again later.", ex);
        }

        // In add should not exist
        if (addOrEdit && stat) {
            throw new NejiException("There is already a service with the name '" +
                    service.getName() + "'.\nPlease change it and try again.");
        } else if ((!addOrEdit) && (!stat)) {
            throw new NejiException("Wasn't found a service with the name '" +
                    service.getName() + "'.\nPlease try again.");
        }

        // Verify if were added at least one dictionary or model
        if (((service.getDictionaries() == null) || (service.getDictionaries().isEmpty())) &&
                ((service.getModels() == null) || (service.getModels().isEmpty()))) {
            throw new NejiException("It is required at least one dictionary or model");
        }

        // Verify groups normalization
        Map<String, String> groupsNormalization = service.getGroupsNormalization();
        List<String> normalizedNames = new ArrayList<>(groupsNormalization.values());
        for (Entry<String, String> group : groupsNormalization.entrySet()) {

            // Verify if mapping name is valid
            if (!group.getValue().matches("^[a-zA-Z0-9._\\s-]*$")) {
                throw new NejiException("The mapping name " + group.getValue()
                        + " is invalid. The mapping name can only consist of alphabetical, "
                        + "number, underscore(_), hifen(-) and dot(.) characters");
            }

            // If no normalization, maintain the group name
            if (group.getValue().trim().length() == 0) {
                group.setValue(group.getKey());
                normalizedNames.add(group.getKey());
            }

            // Verify if there are repeated names
            if (Collections.frequency(normalizedNames, group.getValue()) != 1) {
                throw new NejiException("Two or more semantic groups can't be mapped "
                        + "to the same name '" + group.getValue() + "'.\nPlease "
                        + "change it and try again.");
            }
        }

        // Set service logo
        if (service.getLogo() != null) {
            try {
                byte[] logoBytes = IOUtils.toByteArray(logo);
                service.setLogo(logoBytes);
            } catch (IOException ex) {
                service.setLogo(null);
                throw new NejiException("The provided logo is not valid.", ex);
            }
        }
        
        // Set service false positives
        if (service.getFalsePositives() != null) {
            try {
                String falsePositivesString = 
                        IOUtils.toString(fp, StandardCharsets.UTF_8.name());
                service.setFalsePositives(falsePositivesString);
            } catch (IOException ex) {
                service.setFalsePositives(null);
                throw new NejiException("There was an error loading the false "
                        + "positives file.", ex);
            }
        }
    }

    /**
     * Validates the dictionary parameters.
     *
     * @param dictionary dictionary to validate
     * @throws NejiException
     */
    private void validateDictionary(Dictionary dictionary)
            throws NejiException {

        int stat;

        // Verify if name has invalid characters
        if (dictionary.getName().trim().length() == 0) {
            throw new NejiException("The name is required");
        } else if (!dictionary.getName().matches("^[a-zA-Z0-9._-]+$")) {
            throw new NejiException("The name can only consist of alphabetical, number, "
                    + "underscore(_), hifen(-) and dot(.) characters");
        }

        // Verify if the name already exists
        try {
            stat = db.existsDictionary(dictionary);
        } catch (NejiException ex) {
            throw new NejiException("There was a problem adding the dictionary.\n"
                    + "Please try again later.", ex);
        }

        if (stat == 1) {
            throw new NejiException("There is already a dictionary with the name '" +
                    dictionary.getName() + "'.\nPlease change it and try again.");
        } else if (stat == 2) {
            throw new NejiException("There is already a dictionary with the file '" +
                    dictionary.getFile() + "'.\nPlease change the file and try again.");
        }

        // Verify file
        if (dictionary.getFile() == null) {
            throw new NejiException("The dictionary file is required");
        }
    }

    /**
     * Validates the model parameters.
     *
     * @param model                 model to validate
     * @param configurationFileName configuration file name
     * @param propertiesFileName    properties file name
     * @param propertiesFileName    properties file name
     * @param addOrEdit             true if it is a validation for an add, false if it is for an edit
     * @throws NejiException
     */
    private void validateModel(Model model, String configurationFileName,
                               String propertiesFileName, boolean addOrEdit) throws NejiException {

        int stat;

        // Verify if name has invalid characters
        if (model.getName().trim().length() == 0) {
            throw new NejiException("The name is required");
        } else if (!model.getName().matches("^[a-zA-Z0-9._-]+$")) {
            throw new NejiException("The name can only consist of alphabetical, number, "
                    + "underscore(_), hifen(-) and dot(.) characters");
        }

        // Verify if model already exists in the database
        try {
            stat = db.existsModel(model);
        } catch (NejiException ex) {
            throw new NejiException("There was a problem adding the model.\n"
                    + "Please try again later.", ex);
        }

        // In add should not exist
        if (addOrEdit) {
            if (stat == 1) {
                throw new NejiException("There is already a model with the name '" +
                        model.getName() + "'.\nPlease change it and try again.");
            } else if (stat == 2) {
                throw new NejiException("There is already a model with the file '" +
                        model.getFile() + "'.\nPlease change the file and try again.");
            }
        } else if ((!addOrEdit) && (stat == 0)) {
            throw new NejiException("Wasn't found a model with the name '" +
                    model.getName() + "'.\nPlease try again.");
        }

        // Validate files
        if (addOrEdit) {
            if (model.getFile() == null) {
                throw new NejiException("The model file is required");
            } else if (configurationFileName == null) {
                throw new NejiException("The model configuration file is required");
            } else if (propertiesFileName == null) {
                throw new NejiException("The model properties file is required");
            }
        }
    }
}
