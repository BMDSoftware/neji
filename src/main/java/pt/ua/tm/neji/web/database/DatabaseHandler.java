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

import java.util.List;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.web.manage.Dictionary;
import pt.ua.tm.neji.web.manage.Model;
import pt.ua.tm.neji.web.services.Service;

/**
 * Default database handler.
 *
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public interface DatabaseHandler {
    
    /**
     * Add new dictionary.
     * @param dictionary dictionary
     * @throws NejiException
     */
    void addDictionary(Dictionary dictionary) throws NejiException;
    
    /**
     * Verify if a dictionary already exists.
     * @param dictionary dictionary
     * @return 0 if doesn't exist, 1 if name already exists or 2 if file already exists
     * @throws NejiException
     */
    int existsDictionary(Dictionary dictionary) throws NejiException;        
        
    /**
     * Get dictionaries.
     * @return dictionaries list
     * @throws NejiException
     */
    List<Dictionary> getDictionaries() throws NejiException;
    
    /**
    * Get dictionaries of a service.
     * @param serviceId service id
    * @return dictionaries list
     * @throws NejiException
    */
    List<Dictionary> getDictionaries(int serviceId) throws NejiException;
    
    /**
     * Get dictionary.
     * @param dictionaryId dictionary id
     * @return dictionary
     * @throws NejiException
     */
    Dictionary getDictionary(int dictionaryId) throws NejiException;
    
    /**
     * Remove a dictionary.
     * @param dictionaryId dictionary id
     * @throws NejiException 
     */
    void removeDictionary(int dictionaryId) throws NejiException;
    
    /**
     * Get dictionary file.
     * @param dictionaryName dictionaryName
     * @return dictionary file name
     * @throws NejiException
     */
    String getDictionaryFile(String dictionaryName) throws NejiException;
    
    /**
     * Add new model.
     * @param model model
     * @throws NejiException
     */
    void addModel(Model model) throws NejiException;
    
    /**
     * Verify if a model already exists.
     * @param model model
     * @return 0 if doesn't exist, 1 if name already exists or 2 if file already exists
     * @throws NejiException
     */
    int existsModel(Model model) throws NejiException;   
    
    /**
     * Get models.
     * @return models list
     * @throws NejiException
     */
    List<Model> getModels() throws NejiException;
    
    /**
     * Get models of a service.
     * @param serviceId service id
     * @return models list
     * @throws NejiException
     */
    List<Model> getModels(int serviceId) throws NejiException;
    
    /**
     * Get model.
     * @param modelId model id
     * @return model
     * @throws NejiException
     */
    Model getModel(int modelId) throws NejiException;
    
    /**
     * Remove a model.
     * @param modelId model id
     * @throws NejiException 
     */
    void removeModel(int modelId) throws NejiException;
    
    /**
     * Edit a model.
     * @param model model
     * @throws NejiException 
     */
    void editModel(Model model) throws NejiException;
    
    /**
     * Add new service.
     * @param service service
     * @throws NejiException
     */
    void addService(Service service) throws NejiException;
    
    /**
     * Verify if a service already exists.
     * @param service service
     * @return true if it exists, false otherwise
     * @throws NejiException
     */
    boolean existsService(Service service) throws NejiException;
    
    /**
     * Get services.
     * @return services list
     * @throws NejiException
     */
    List<Service> getServices() throws NejiException;
    
    /**
     * Get service.
     * @param serviceName service name
     * @return service
     * @throws NejiException
     */
    Service getService(String serviceName) throws NejiException;    
    
    /**
     * Get service.
     * @param serviceId service id
     * @return service
     * @throws NejiException
     */
    Service getService(int serviceId) throws NejiException;
    
    /**
     * Remove a service.
     * @param serviceId service id
     * @throws NejiException 
     */
    void removeService(int serviceId) throws NejiException;
    
    /**
     * Edit a service.
     * @param service service
     * @throws NejiException 
     */
    void editService(Service service) throws NejiException;
}
    