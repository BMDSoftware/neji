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

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Dictionary class.
 * 
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 */
@XmlRootElement
public class Dictionary {
    
    // Attributes
    private Integer id;
    private String name;
    private String file;
    private List<String> services;
    private List<String> models;
    private String group;

    public Dictionary() {
    }

    public Dictionary(Integer id, String name, String file, List<String> services, List<String> models, 
            String group) {
        this.id = id;
        this.name = name;
        this.file = file;
        this.services = services;
        this.models = models;
        this.group = group;
    }
    
    public Dictionary(String name, String file, List<String> services, List<String> models, 
            String group) {
        this(null, name, file, services, models, group);
    }
    
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public String getFile() {
        return file;
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getModels() {
        return models;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setFile(String file) {
        this.file = file;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }   
    
    public void setGroup(String group) {
        this.group = group;
    }
}
