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

package pt.ua.tm.neji.web.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.web.manage.Dictionary;
import pt.ua.tm.neji.web.manage.Model;
import pt.ua.tm.neji.web.services.Service;

/**
 * Database handler implemented using a SQL database (SQLite).
 *
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class DefaultDatabaseHandler implements DatabaseHandler {
    
    // Attributes
    private Connection connection;
    
    /**
     * Constructor. Creates a new instance.
     * @param databaseName database name
     * @param createTablesFlag create tables flag
     */
    public DefaultDatabaseHandler(String databaseName, boolean createTablesFlag) {
        
        // Connect to database
        connect(databaseName);
        
        // Create tables (if not exists)
        if (createTablesFlag) {
            createTables();
            createAuthenticationTables();
        }
    }
    
    /**
     * Connects to the database.
     * @param databaseName database name
     */
    private void connect(String databaseName) {
        
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
        } catch (ClassNotFoundException | SQLException ex) {
            throw new RuntimeException("There was a problem connecting to the database.", ex);
        }
    }
    
    /**
     * Creates the necessary tables (if they not exist in the database).
     */
    private void createTables() {
        
        Statement statement;
        
        try {
            
            // Service table
            statement = connection.createStatement();
            String serviceTableSql = "CREATE TABLE IF NOT EXISTS Service ("
                    + " id              INTEGER     PRIMARY KEY     NOT NULL, "
                    + " name            TEXT        NOT NULL    UNIQUE, "
                    + " logo            BLOB, "
                    + " parser_level    TEXT        NOT NULL, "
                    + " no_ids          INTEGER     NOT NULL,"
                    + " false_positives TEXT,"
                    + " abbreviations    INTEGER     DEFAULT 0,"
                    + " disambiguation    INTEGER     DEFAULT 0)";

            statement.executeUpdate(serviceTableSql);
            statement.close();

            // Dictionary table
            statement = connection.createStatement();
            String dictionaryTableSql = "CREATE TABLE IF NOT EXISTS Dictionary ("
                    + " id          INTEGER     PRIMARY KEY     NOT NULL, "
                    + " name        TEXT        NOT NULL    UNIQUE, "
                    + " file        TEXT        NOT NULL, "
                    + " [group]     TEXT        NOT NULL)";

            statement.executeUpdate(dictionaryTableSql);
            statement.close();

            // Model table
            statement = connection.createStatement();
            String modelTableSql = "CREATE TABLE IF NOT EXISTS Model ("
                    + " id          INTEGER     PRIMARY KEY     NOT NULL, "
                    + " name        TEXT        NOT NULL    UNIQUE, "
                    + " file        TEXT        NOT NULL, "
                    + " [group]     TEXT        NOT NULL)";

            statement.executeUpdate(modelTableSql);
            statement.close();
            
            // Service dictionaries table
            statement = connection.createStatement();
            String serviceDictionariesTableSql = "CREATE TABLE IF NOT EXISTS ServiceDictionaries ("
                    + " service_id          INTEGER    NOT NULL, "
                    + " dictionary_id     INTEGER    NOT NULL,"
                    + " PRIMARY KEY(service_id, dictionary_id), "
                    + " FOREIGN KEY(service_id) REFERENCES Service(id), "
                    + " FOREIGN KEY(dictionary_id) REFERENCES Dictionary(id))";
            
            statement.executeUpdate(serviceDictionariesTableSql);
            statement.close();
            
            // Service models table
            statement = connection.createStatement();
            String serviceModelTableSql = "CREATE TABLE IF NOT EXISTS ServiceModels ("
                    + " service_id          INTEGER    NOT NULL, "
                    + " model_id            INETEGR    NOT NULL, "
                    + " PRIMARY KEY(service_id, model_id), "
                    + " FOREIGN KEY(service_id) REFERENCES Service(id), "
                    + " FOREIGN KEY(model_id) REFERENCES Model(id))";

            statement.executeUpdate(serviceModelTableSql);
            statement.close();
            
            // Model normalization dictionaries table 
            statement = connection.createStatement();
            String modelDictionariesTableSql = "CREATE TABLE IF NOT EXISTS ModelDictionaries ("
                    + " model_id            INTEGER    NOT NULL, "
                    + " dictionary_id       INTEGER    NOT NULL, "
                    + " PRIMARY KEY(model_id, dictionary_id), "
                    + " FOREIGN KEY(model_id) REFERENCES Model(id), "
                    + " FOREIGN KEY(dictionary_id) REFERENCES Dictionary(id))";

            statement.executeUpdate(modelDictionariesTableSql);
            statement.close();
            
            // Service groups normalization table
            statement = connection.createStatement();
            String serviceGroupsTableSql = "CREATE TABLE IF NOT EXISTS ServiceGroups ("
                    + " service_id    TEXT    NOT NULL, "
                    + " [group]         TEXT    NOT NULL, "
                    + " normalized_name TEXT    NOT NULL, "
                    + " PRIMARY KEY(service_id, [group]), "
                    + " FOREIGN KEY(service_id) REFERENCES Service(id))";

            statement.executeUpdate(serviceGroupsTableSql);
            statement.close();
            
        } catch (SQLException ex) {
            throw new RuntimeException("There was a problem creating the tables.", ex);
        }
    }
    
    /**
     * Creates the authentication tables (if they not exist in the database).
     */
    private void createAuthenticationTables() {
        
        Statement statement;
        
        try {
            
            // Users table
            statement = connection.createStatement();
            String usersTableSql = "CREATE TABLE IF NOT EXISTS users ("
                    + " id          INTEGER         PRIMARY KEY NOT NULL, "
                    + " username    VARCHAR(100)    NOT NULL    UNIQUE, "
                    + " pwd         VARCHAR(50)     NOT NULL)";

            statement.executeUpdate(usersTableSql);
            statement.close();

            // Roles table
            statement = connection.createStatement();
            String rolesTableSql = "CREATE TABLE IF NOT EXISTS roles ("
                    + " id          INTEGER         PRIMARY KEY NOT NULL, "
                    + " role        VARCHAR(100)    NOT NULL    UNIQUE)";

            statement.executeUpdate(rolesTableSql);
            statement.close();

            // User roles table
            statement = connection.createStatement();
            String userRolesTableSql = "CREATE TABLE IF NOT EXISTS user_roles ("
                    + " user_id     INTEGER     NOT NULL, "
                    + " role_id     INTEGER     NOT NULL, "
                    + " PRIMARY KEY(user_id, role_id), "
                    + " FOREIGN KEY(user_id) REFERENCES users(id), "
                    + " FOREIGN KEY(role_id) REFERENCES roles(id))";
            
            statement.executeUpdate(userRolesTableSql);
            statement.close();
            
            /* Add default admin-admin user */
            
            // Add admin role
            statement = connection.createStatement();
            String query = "INSERT INTO roles(role) "
                    + "SELECT 'admin' "
                    + "WHERE NOT EXISTS(SELECT 1 FROM roles WHERE role = 'admin');";
            statement.executeUpdate(query);          
            statement.close();
        
            // Add user admin
            statement = connection.createStatement();
            query = "INSERT INTO users(username, pwd) "
                    + "SELECT 'admin', 'MD5:21232f297a57a5a743894a0e4a801fc3' "
                    + "WHERE NOT EXISTS(SELECT 1 FROM users WHERE username = 'admin' "
                    + "AND pwd = 'MD5:21232f297a57a5a743894a0e4a801fc3');";
            statement.executeUpdate(query);          
            statement.close();
        
            // Associate user and role (admin - admin)
            statement = connection.createStatement();
            query = "INSERT INTO user_roles(user_id, role_id) "
                    + "SELECT 1, 1 "
                    + "WHERE NOT EXISTS(SELECT 1 FROM user_roles WHERE user_id = 1 "
                    + "AND role_id = 1);";
            statement.executeUpdate(query);          
            statement.close();
            
        } catch (SQLException ex) {
            throw new RuntimeException("There was a problem creating the authentication tables.", ex);
        }
    }

    @Override
    public void addDictionary(Dictionary dictionary) throws NejiException {
        
        PreparedStatement statement;
        
        try {
            String addDictionarySql = "INSERT INTO Dictionary(name, file, [group]) VALUES (?, ?, ?)";
            statement = connection.prepareStatement(addDictionarySql);

            statement.setString(1, dictionary.getName());
            statement.setString(2, dictionary.getFile());
            statement.setString(3, dictionary.getGroup());

            statement.addBatch();
            statement.executeBatch();            
            statement.close();
        } catch (SQLException ex) {
            throw new NejiException("There was a problem executing a query.", ex);
        }
    }
    
    @Override
    public int existsDictionary(Dictionary dictionary) throws NejiException {
        
        PreparedStatement statement;
                          
        try {
            // Verify if the name already exists
            String query = "SELECT name FROM Dictionary WHERE name=?";
            statement = connection.prepareStatement(query);

            statement.setString(1, dictionary.getName());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return 1;
            }

            // Verify if the file already exists
            query = "SELECT file FROM Dictionary WHERE file=?";
            statement = connection.prepareStatement(query);

            statement.setString(1, dictionary.getFile());

            rs = statement.executeQuery();
            if (rs.next()) {
                return 2;
            }
        } catch (SQLException ex) {
            throw new NejiException("There was a problem executing a query.", ex);
        }
        
        return 0;
    }
    
    @Override
    public List<Dictionary> getDictionaries() throws NejiException {
        
        List<Dictionary> dictionariesList = new ArrayList<>();
        PreparedStatement statement;
                
        try {
                        
            String query = "SELECT * FROM Dictionary";
            statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String file = rs.getString("file");
                String group = rs.getString("group");
                
                // Get services that use this dictionary
                List<String> services = getDictionaryServices(id);
                
                // Get models that use this dictionary
                List<String> models = getDictionaryModels(id);
                
                Dictionary dictionary = new Dictionary(id, name, file, services, models, group);               
                dictionariesList.add(dictionary);
            }
            
            statement.close();
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem getting the dictionaries.", ex);
        }
        
        return dictionariesList;
    }
    
    @Override
    public List<Dictionary> getDictionaries(int serviceId) throws NejiException {
        
        List<Dictionary> dictionariesList = new ArrayList<>();
        PreparedStatement statement;
                
        try {
                        
            String query = "SELECT D.id, D.name, D.file, D.[group] FROM Dictionary AS D JOIN ServiceDictionaries AS SD "
                    + " ON D.id = SD.dictionary_id WHERE SD.service_id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, serviceId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String file = rs.getString("file");
                String group = rs.getString("group");
                
                 // Get services that use this dictionary
                List<String> services = getDictionaryServices(id);

                // Get models that use this dictionary for normalization
                List<String> models = getDictionaryModels(id);
                
                Dictionary dictionary = new Dictionary(id, name, file, services, models, group);
                dictionariesList.add(dictionary);
            }
            
            statement.close();
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem getting the dictionaries.", ex);
        }
        
        return dictionariesList;
    }
    
    @Override
    public Dictionary getDictionary(int dictionaryId) throws NejiException {
        
        PreparedStatement statement;
                
        try {
                        
            String query = "SELECT * FROM Dictionary WHERE id=?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, dictionaryId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String file = rs.getString("file");
                String group = rs.getString("group");
                
                Dictionary dictionary = new Dictionary(id, name, file, 
                        getDictionaryServices(dictionaryId), 
                        getDictionaryModels(dictionaryId), group);
                
                statement.close();
                return dictionary;
            }
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem getting a dictionary.", ex);
        }
        
        return null;        
    }
    
    @Override
    public void removeDictionary(int dictionaryId) throws NejiException {
        
        PreparedStatement statement;
        
        try {
            String query = "DELETE FROM Dictionary WHERE id=?";
            statement = connection.prepareStatement(query);

            statement.setInt(1, dictionaryId);

            statement.addBatch();
            statement.executeBatch();            
            statement.close();
        } catch (SQLException ex) {
            throw new NejiException("There was a problem removing a dictionary.", ex);
        }
    }
    
    
    @Override
    public String getDictionaryFile(String dictionaryName) throws NejiException {
        
        String file = null;
        PreparedStatement statement;
                
        try {
                        
            String query = "SELECT file FROM Dictionary WHERE name=?";
            statement = connection.prepareStatement(query);
            statement.setString(1, dictionaryName);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                file = rs.getString("file");
            }
            
            statement.close();            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem getting a dictionary.", ex);
        }
        
        return file;    
    }
    
    @Override
    public void addModel(Model model) throws NejiException {
        
        PreparedStatement statement;
                
        try {
            
            String addModelSql = "INSERT INTO Model(name, file, [group]) VALUES (?, ?, ?)";
            statement = connection.prepareStatement(addModelSql);

            statement.setString(1, model.getName());
            statement.setString(2, model.getFile());
            statement.setString(3, model.getGroup());
            
            statement.addBatch();
            statement.executeBatch();
            int id = statement.getGeneratedKeys().getInt(1);
            statement.close();
            
            // Insert normalization dictionaries
            if (!model.getDictionaries().isEmpty()) {
                String addModelDictionariesSql = "INSERT INTO "
                        + "ModelDictionaries(model_id, dictionary_id) VALUES (?, ?)";
                
                for (int i=1 ; i<model.getDictionaries().size(); i++) {
                    addModelDictionariesSql += ", (?, ?)";
                }

                statement = connection.prepareStatement(addModelDictionariesSql);
                
                int index = 1;
                for (String dictionary : model.getDictionaries()) {
                    statement.setInt(index++, id);
                    statement.setInt(index++, getDictionaryId(dictionary));
                }               

                statement.addBatch();
                statement.executeBatch();            
                statement.close();
            }
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem inserting a model.", ex);
        }
    }
    
    @Override
    public int existsModel(Model model) throws NejiException {
        
        PreparedStatement statement;
                          
        try {
            // Verify if the name already exists
            String query = "SELECT name FROM Model WHERE name=?";
            statement = connection.prepareStatement(query);

            statement.setString(1, model.getName());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return 1;
            }

            // Verify if the file already exists
            query = "SELECT file FROM Model WHERE file=?";
            statement = connection.prepareStatement(query);

            statement.setString(1, model.getFile());

            rs = statement.executeQuery();
            if (rs.next()) {
                return 2;
            }
        } catch (SQLException ex) {
            throw new NejiException("There was a problem executing a query.", ex);
        }
        
        return 0;
    }
    
    @Override
    public List<Model> getModels() throws NejiException {
        
        List<Model> modelsList = new ArrayList<>();
        PreparedStatement statement;
                
        try {
                        
            String query = "SELECT * FROM Model";
            statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String file = rs.getString("file");
                String group = rs.getString("group");
                
                // Get dictionaries that use this model
                List<String> dictionaries = getModelDictionaries(id);
                
                // Get services that use this model
                List<String> services = getModelServices(id);
                
                Model model = new Model(id, name, file, dictionaries, services, group);               
                modelsList.add(model);
            }
            
            statement.close();
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem getting the models.", ex);
        }
        
        return modelsList;
        
    }
    
    @Override
    public List<Model> getModels(int serviceId) throws NejiException {
        
        List<Model> modelsList = new ArrayList<>();
        PreparedStatement statement;
                
        try {
                        
            String query = "SELECT M.id, M.name, M.file, M.[group] FROM Model AS M JOIN ServiceModels AS MD "
                    + " ON M.id = MD.model_id WHERE MD.service_id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, serviceId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String file = rs.getString("file");
                String group = rs.getString("group");
                
                 // Get services that use this model
                List<String> services = getModelServices(id);

                // Get models that use this dictionary for normalization
                List<String> dictionaries = getModelDictionaries(id);
                
                Model model = new Model(id, name, file, dictionaries, services, group);
                modelsList.add(model);
            }
            
            statement.close();
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem getting the dictionaries.", ex);
        }
        
        return modelsList;
    }
    
    @Override
    public Model getModel(int modelId) throws NejiException {
        
        PreparedStatement statement;
                
        try {
                        
            String query = "SELECT * FROM Model WHERE id=?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, modelId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String file = rs.getString("file");
                String group = rs.getString("group");
                
                Model model = new Model(id, name, file, getModelDictionaries(modelId),
                        getModelServices(modelId), group);
                
                statement.close();
                return model;
            }
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem getting a model.", ex);
        }
        
        return null;        
    }
    
    @Override
    public void removeModel(int modelId) throws NejiException {
        
        PreparedStatement statement;
        
        try {
            
            // Delete association bteween model and dictionaries
            deleteModelDictionaries(modelId);
            
            // Delete model
            String query = "DELETE FROM Model WHERE id=?";
            statement = connection.prepareStatement(query);

            statement.setInt(1, modelId);

            statement.addBatch();
            statement.executeBatch();            
            statement.close();           
        } catch (SQLException ex) {
            throw new NejiException("There was a problem removing a model.", ex);
        }
    }
    
    @Override
    public void editModel(Model model) throws NejiException {
                        
        try {
            // Update dictionaries
            deleteModelDictionaries(model.getId());
            addModelDictionaries(model.getId(), model.getDictionaries());           
        } catch (SQLException ex) {
            throw new NejiException("There was a problem editing a model.", ex);
        }
    }
    
    @Override
    public void addService(Service service) throws NejiException {
        
        PreparedStatement statement;
                
        try {            
            // Insert service
            String addServiceSql = "INSERT INTO Service(name, logo, parser_level, "
                    + "no_ids, false_positives, abbreviations, disambiguation) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(addServiceSql);

            statement.setString(1, service.getName());
            statement.setBytes(2, service.getLogo());
            statement.setString(3, service.getParsingLevel());
            int noIdsInt = (service.isNoIds()) ? 1 : 0;
            statement.setInt(4, noIdsInt);
            statement.setString(5, service.getFalsePositives());
            int abbreviationsInt = (service.getAbbreviations()) ? 1 : 0;
            int disambiguationInt = (service.getDisambiguation()) ? 1 : 0;
            statement.setInt(6, abbreviationsInt);
            statement.setInt(7, disambiguationInt);
            
            statement.addBatch();
            statement.executeBatch();
            int serviceId = statement.getGeneratedKeys().getInt(1);
            statement.close();
            
            // Insert dictionaries
            addServiceDictionaries(serviceId, service.getDictionaries());
            
            // Insert models
            addServiceModels(serviceId, service.getModels());
            
            // Insert groups normalization
            addServiceGroupsNormalization(serviceId, service.getGroupsNormalization());
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem inserting a service.", ex);
        }
    }
    
    @Override
    public boolean existsService(Service service) throws NejiException {
        
        PreparedStatement statement;
                          
        try {
            // Verify if the name already exists
            String query = "SELECT name FROM Service WHERE name=?";
            statement = connection.prepareStatement(query);

            statement.setString(1, service.getName());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            throw new NejiException("There was a problem executing a query.", ex);
        }
        
        return false;
    }
    
    @Override
    public List<Service> getServices() throws NejiException {
        
        List<Service> servicesList = new ArrayList<>();
        Statement statement;
                
        try {
            statement = connection.createStatement();
            
            String query = "SELECT * FROM Service";
            ResultSet rs = statement.executeQuery(query);
            
            while (rs.next()) {
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] logo = rs.getBytes("logo");
                String parsingLevel = rs.getString("parser_level");
                int noIds = rs.getInt("no_ids");
                String falsePositives = rs.getString("false_positives");
                int abbreviations = rs.getInt("abbreviations");
                int disambiguation = rs.getInt("disambiguation");
                
                // Get dicitionaries
                List<String> dictionaries = getServiceDictionaries(id);
                
                // Get models
                List<String> models = getServiceModels(id);
                
                // Get groups normalization
                Map<String, String> groupsNormalization = getGroupsNormalization(id);
                
                Service service = new Service(id, name, logo, parsingLevel, (noIds == 1), 
                        dictionaries, models, groupsNormalization, falsePositives, 
                        (abbreviations == 1), (disambiguation == 1));
                
                servicesList.add(service);
            }
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem inserting a service.", ex);
        }
        
        return servicesList;
    }

    @Override
    public Service getService(String serviceName) throws NejiException {
        
        PreparedStatement statement;
                
        try {
                        
            String query = "SELECT * FROM Service WHERE name=?";
            statement = connection.prepareStatement(query);
            statement.setString(1, serviceName);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] logo = rs.getBytes("logo");
                String parsingLevel = rs.getString("parser_level");
                int noIds = rs.getInt("no_ids");
                String falsePositives = rs.getString("false_positives");
                int abbreviations = rs.getInt("abbreviations");
                int disambiguation = rs.getInt("disambiguation");
                
                Service service = new Service(id, name, logo, parsingLevel, (noIds == 1),
                        getServiceDictionaries(id), getServiceModels(id),
                        getGroupsNormalization(id), falsePositives,
                        (abbreviations == 1), (disambiguation == 1));
                
                statement.close();
                return service;
            }
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem inserting a service.", ex);
        }
        
        return null;        
    }
    
    @Override
    public Service getService(int serviceId) throws NejiException {
        
        PreparedStatement statement;
                
        try {
                        
            String query = "SELECT * FROM Service WHERE id=?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, serviceId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] logo = rs.getBytes("logo");
                String parsingLevel = rs.getString("parser_level");
                int noIds = rs.getInt("no_ids");
                String falsePositives = rs.getString("false_positives");
                int abbreviations = rs.getInt("abbreviations");
                int disambiguation = rs.getInt("disambiguation");
                
                Service service = new Service(id, name, logo, parsingLevel, (noIds == 1),
                        getServiceDictionaries(id), getServiceModels(id),
                        getGroupsNormalization(id), falsePositives, 
                        (abbreviations == 1), (disambiguation == 1));
                
                statement.close();
                return service;
            }
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem inserting a service.", ex);
        }
        
        return null;        
    }
    
    @Override
    public void removeService(int serviceId) throws NejiException {       
        
        PreparedStatement statement;       
        
        try {
            
            // Delete dictionaries associated with this service
            deleteServiceDictionaries(serviceId);
        
            // Delete models associated with this service
            deleteServiceModels(serviceId);
            
            // Delete semantic groups normalization
            deleteServiceGroupsNormalization(serviceId);
            
            // Delete service
            String query = "DELETE FROM Service WHERE id=?";
            statement = connection.prepareStatement(query);

            statement.setInt(1, serviceId);

            statement.addBatch();
            statement.executeBatch();            
            statement.close();
        } catch (SQLException ex) {
            throw new NejiException("There was a problem removing a model.", ex);
        }
    }
    
    @Override
    public void editService(Service service) throws NejiException {
        
        PreparedStatement statement;
                
        try {            
            // Edit service
            if (service.getLogo() != null) {
                String query = "UPDATE Service SET logo=?, parser_level=?, no_ids=?, "
                        + "false_positives=?, abbreviations=?, disambiguation=? WHERE id=?";
                statement = connection.prepareStatement(query);
                statement.setBytes(1, service.getLogo());
                statement.setString(2, service.getParsingLevel());
                int noIdsInt = (service.isNoIds()) ? 1 : 0;
                statement.setInt(3, noIdsInt);
                statement.setString(4, service.getFalsePositives());
                int abbreviationsInt = (service.getAbbreviations()) ? 1 : 0;
                int disambiguationInt = (service.getDisambiguation()) ? 1 : 0;
                statement.setInt(5, abbreviationsInt);
                statement.setInt(6, disambiguationInt);
                statement.setInt(7, service.getId());
            } else {
                String query = "UPDATE Service SET parser_level=?, no_ids=?, "
                        + "false_positives=?, abbreviations=?, disambiguation=? WHERE id=?";
                statement = connection.prepareStatement(query);
                statement.setString(1, service.getParsingLevel());
                int noIdsInt = (service.isNoIds()) ? 1 : 0;
                statement.setInt(2, noIdsInt);
                statement.setString(3, service.getFalsePositives());
                int abbreviationsInt = (service.getAbbreviations()) ? 1 : 0;
                int disambiguationInt = (service.getDisambiguation()) ? 1 : 0;
                statement.setInt(4, abbreviationsInt);
                statement.setInt(5, disambiguationInt);
                statement.setInt(6, service.getId());
            }
            
            statement.addBatch();
            statement.executeBatch();            
            statement.close();
            
            // Update dictionaries
            deleteServiceDictionaries(service.getId());
            addServiceDictionaries(service.getId(), service.getDictionaries());
                        
            // Update models
            deleteServiceModels(service.getId());
            addServiceModels(service.getId(), service.getModels());
            
            // Update semantic groups normalization
            deleteServiceGroupsNormalization(service.getId());
            addServiceGroupsNormalization(service.getId(), service.getGroupsNormalization());
            
        } catch (SQLException ex) {
            throw new NejiException("There was a problem editing a service.", ex);
        }
    }
    
    /**
     * Get services that use a dictionary.
     * @param dictionaryId dictionary id
     * @return services list
     */
    private List<String> getDictionaryServices(int dictionaryId) throws SQLException {
        
        List<String> dictionariesServicesList = new ArrayList<>();
        PreparedStatement statement;
                                        
        String query = "SELECT S.name FROM Service AS S JOIN ServiceDictionaries AS SD "
                + " ON S.id = SD.service_id WHERE SD.dictionary_id=?";
        statement = connection.prepareStatement(query);
        statement.setInt(1, dictionaryId);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {

            String serviceName = rs.getString("name");              
            dictionariesServicesList.add(serviceName);
        }

        statement.close();
        
        return dictionariesServicesList;
    }
    
    /**
     * Get models that use a dictionary.
     * @param dictionaryId dictionary id
     * @return models list
     */
    private List<String> getDictionaryModels(int dictionaryId) throws SQLException {
        
        List<String> dictionariesModelsList = new ArrayList<>();
        PreparedStatement statement;
                                        
        String query = "SELECT M.name FROM Model AS M JOIN ModelDictionaries AS MD "
                + " ON M.id = MD.model_id WHERE MD.dictionary_id=?";
        statement = connection.prepareStatement(query);
        statement.setInt(1, dictionaryId);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {

            String modelName = rs.getString("name");              
            dictionariesModelsList.add(modelName);
        }

        statement.close();
        
        return dictionariesModelsList;
    }   
    
    /**
     * Get dictionaries that use a model.
     * @param modelId model id
     * @return dictionaries list
     */
    private List<String> getModelDictionaries(int modelId) throws SQLException {
        
        List<String> modelDictionariesList = new ArrayList<>();
        PreparedStatement statement;
                                      
        String query = "SELECT name FROM Dictionary AS D JOIN ModelDictionaries AS MD "
                + " ON D.id = MD.dictionary_id WHERE model_id=?";
        statement = connection.prepareStatement(query);
        statement.setInt(1, modelId);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {

            String dictionaryName = rs.getString("name");              
            modelDictionariesList.add(dictionaryName);
        }

        statement.close();
        
        return modelDictionariesList;
    }
    
    /**
     * Get services that use a model.
     * @param modelId model id
     * @return services list
     */
    private List<String> getModelServices(int modelId) throws SQLException {
        
        List<String> modelServicesList = new ArrayList<>();
        PreparedStatement statement;
                                        
        String query = "SELECT name FROM Service AS S JOIN ServiceModels AS SM "
                + " ON S.id = SM.service_id WHERE model_id=?";
        statement = connection.prepareStatement(query);
        statement.setInt(1, modelId);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {

            String serviceName = rs.getString("name");              
            modelServicesList.add(serviceName);
        }
            
        statement.close();
        
        return modelServicesList;
    }
    
    /**
     * Get dictionaries of a service.
     * @param serviceId service id
     * @return services list
     */
    private List<String> getServiceDictionaries(int serviceId) throws SQLException {
        
        List<String> serviceDictionariesList = new ArrayList<>();
        PreparedStatement statement;
                                        
        String query = "SELECT D.name FROM Dictionary AS D JOIN "
                + " ServiceDictionaries AS SD ON D.id = SD.dictionary_id WHERE SD.service_id = ?";
        statement = connection.prepareStatement(query);
        statement.setInt(1, serviceId);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {

            String dictionaryName = rs.getString("name");              
            serviceDictionariesList.add(dictionaryName);
        }

        statement.close();
        
        return serviceDictionariesList;
    }
    
    /**
     * Get models of a service.
     * @param serviceId service id
     * @return models list
     */
    private List<String> getServiceModels(int serviceId) throws SQLException {
        
        List<String> serviceModelsList = new ArrayList<>();
        PreparedStatement statement;
                        
        String query = "SELECT M.name FROM Model AS M JOIN ServiceModels AS SM "
                + " ON M.id = SM.model_id WHERE service_id=?";
        statement = connection.prepareStatement(query);
        statement.setInt(1, serviceId);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {

            String modelName = rs.getString("name");              
            serviceModelsList.add(modelName);
        }

        statement.close();
        
        return serviceModelsList;
    }
    
    /**
     * Get groups normalization names of a service.
     * @param serviceId service id
     * @return groups normalization map
     */
    private Map<String, String> getGroupsNormalization(int serviceId) throws SQLException {
        
        Map<String, String> groupsNormalization = new HashMap<>();
        PreparedStatement statement;
                        
        String query = "SELECT [group], normalized_name FROM ServiceGroups "
                + " WHERE service_id=?";
        statement = connection.prepareStatement(query);
        statement.setInt(1, serviceId);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {

            String group = rs.getString("group");
            String normalizedName = rs.getString("normalized_name");
            groupsNormalization.put(group, normalizedName);
        }

        statement.close();
        
        return groupsNormalization;
    }
    
    /**
     * Delete associations between a service and dictionaries.
     * @param serviceId service id
     * @throws NejiException 
     */
    private void deleteServiceDictionaries(int serviceId) throws SQLException {
        
        PreparedStatement statement;
        
        String query = "DELETE FROM ServiceDictionaries WHERE service_id=?";
        statement = connection.prepareStatement(query);

        statement.setInt(1, serviceId);

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
    }
    
    /**
     * Delete associations between a service and models.
     * @param serviceId service id
     * @throws NejiException 
     */
    private void deleteServiceModels(int serviceId) throws SQLException {
        
        PreparedStatement statement;        
        
        String query = "DELETE FROM ServiceModels WHERE service_id=?";
        statement = connection.prepareStatement(query);

        statement.setInt(1, serviceId);

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
    }
    
    /**
     * Add dictionaries to a service.
     * @param serviceId service id
     * @param dictionaries dictionaries
     * @throws SQLException 
     */
    private void addServiceDictionaries(int serviceId, List<String> dictionaries) 
            throws SQLException {
        
        if (dictionaries == null || dictionaries.isEmpty()) {
            return;
        }
        
        PreparedStatement statement;
        
        String query = "INSERT INTO ServiceDictionaries(service_id, dictionary_id) "
                + " VALUES (?, ?)";
                
        for (int i=1 ; i<dictionaries.size(); i++) {
            query += ", (?, ?)";
        }

        statement = connection.prepareStatement(query);

        int index = 1;
        for (String dictionaryName: dictionaries) {
            statement.setInt(index++, serviceId);
            statement.setInt(index++, getDictionaryId(dictionaryName));
        }               

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
    }
    
    /**
     * Add models to a service.
     * @param serviceId service id
     * @param models models
     * @throws SQLException 
     */
    private void addServiceModels(int serviceId, List<String> models) 
            throws SQLException {
        
        if (models == null || models.isEmpty()) {
            return;
        }
        
        PreparedStatement statement;
        
        String query = "INSERT INTO ServiceModels(service_id, model_id) "
                + " VALUES (?, ?)";
                
        for (int i=1 ; i<models.size(); i++) {
            query += ", (?, ?)";
        }

        statement = connection.prepareStatement(query);

        int index = 1;
        for (String modelName : models) {
            statement.setInt(index++, serviceId);
            statement.setInt(index++, getModelId(modelName));
        }               

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
    }
    
    /**
     * Delete associations between a model and dictionaries.
     * @param modelId model id
     * @throws NejiException 
     */
    private void deleteModelDictionaries(int modelId) throws SQLException {
        
        PreparedStatement statement;
        
        String query = "DELETE FROM ModelDictionaries WHERE model_id=?";
        statement = connection.prepareStatement(query);

        statement.setInt(1, modelId);

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
    }
    
    /**
     * Add dictionaries to a model.
     * @param modelId model id
     * @param dictionaries dictionaries
     * @throws SQLException 
     */
    private void addModelDictionaries(int modelId, List<String> dictionaries) 
            throws SQLException {
        
        if (dictionaries == null || dictionaries.isEmpty()) {
            return;
        }
        
        PreparedStatement statement;
        
        String query = "INSERT INTO ModelDictionaries(model_id, dictionary_id) "
                + " VALUES (?, ?)";
                
        for (int i=1 ; i<dictionaries.size(); i++) {
            query += ", (?, ?)";
        }

        statement = connection.prepareStatement(query);

        int index = 1;
        for (String dictionaryName : dictionaries) {
            statement.setInt(index++, modelId);
            statement.setInt(index++, getDictionaryId(dictionaryName));
        }               

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
    }

    /**
     * Add service groups normalization.
     * @param serviceId service id
     * @throws NejiException 
     */
    private void addServiceGroupsNormalization(int serviceId, 
            Map<String, String> groupsNormalization) throws SQLException {
        
        if (groupsNormalization == null || groupsNormalization.isEmpty()) {
            return;
        }
        
        PreparedStatement statement;
        String query = "INSERT INTO ServiceGroups(service_id, [group], normalized_name) "
                + " VALUES (?, ?, ?)";
                
        for (int i=1 ; i<groupsNormalization.size(); i++) {
            query += ", (?, ?, ?)";
        }

        statement = connection.prepareStatement(query);

        int index = 1;
        for (Entry<String, String> group : groupsNormalization.entrySet()) {
            statement.setInt(index++, serviceId);
            statement.setString(index++, group.getKey());
            statement.setString(index++, group.getValue());
        }               

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
    }
    
    /**
     * Delete service groups normalization.
     * @param serviceId service id
     * @throws NejiException 
     */
    private void deleteServiceGroupsNormalization(int serviceId) throws SQLException {
        
        PreparedStatement statement;        
        
        String query = "DELETE FROM ServiceGroups WHERE service_id=?";
        statement = connection.prepareStatement(query);

        statement.setInt(1, serviceId);

        statement.addBatch();
        statement.executeBatch();            
        statement.close();
    }
    
    /**
     * Get dictionary id given the dictionary name.
     * @param dictionaryId dictionary name
     * @return dictionary id
     */
    private int getDictionaryId(String dictionaryName) throws SQLException {
        
        int id = -1;
        PreparedStatement statement;
                                        
        String query = "SELECT id FROM Dictionary WHERE name=?";
        statement = connection.prepareStatement(query);
        statement.setString(1, dictionaryName);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            id = rs.getInt("id");              
        }

        statement.close();
        
        return id;        
    }
    
    /**
     * Get model id given the model name.
     * @param modelName model name
     * @return model id
     */
    private int getModelId(String modelName) throws SQLException {
        
        int id = -1;
        PreparedStatement statement;
                                        
        String query = "SELECT id FROM Model WHERE name=?";
        statement = connection.prepareStatement(query);
        statement.setString(1, modelName);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            id = rs.getInt("id");              
        }

        statement.close();
        
        return id;        
    }
}
