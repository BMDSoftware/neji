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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlRootElement;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.web.database.DatabaseHandler;
import pt.ua.tm.neji.web.database.DefaultDatabaseHandler;
import pt.ua.tm.neji.web.manage.Dictionary;

/**
 * Service class.
 *
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement
public class Service {
    
    // Attributes
    private Integer id;
    private String name;
    private byte[] logo;
    private String parsingLevel;
    private boolean noIds;
    private List<String> dictionaries;
    private List<String> models;
    private Map<String, String> groupsNormalization;
    private String falsePositives;
    private boolean abbreviations;
    private boolean disambiguation;

    public Service() {
    }
    
    public Service(Integer id, String name, byte[] logo, String parsingLevel, boolean noIds, 
            List<String> dictionaries, List<String> models, 
            Map<String, String> groupsNormalization, String falsePositives, 
            boolean abbreviations, boolean disambiguation) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.parsingLevel = parsingLevel;
        this.noIds = noIds;
        this.dictionaries = dictionaries;
        this.models = models;
        this.groupsNormalization = groupsNormalization;
        this.falsePositives = falsePositives;
        this.abbreviations = abbreviations;
        this.disambiguation = disambiguation;
    }
    
    public Service(String name, byte[] logo, String parsingLevel, boolean noIds, 
            List<String> dictionaries, List<String> models, 
            Map<String, String> groupsNormalization, String falsePositives, 
            boolean abbreviations, boolean groupDisambiguation, boolean disambiguation) {
        this(null, name, logo, parsingLevel, noIds, dictionaries, models, 
                groupsNormalization, falsePositives, abbreviations, 
                disambiguation);
    }    

    public Integer getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public byte[] getLogo() {
        return logo;
    }
    
    public String getParsingLevel() {
        return parsingLevel;
    }

    public boolean isNoIds() {
        return noIds;
    }

    public List<String> getDictionaries() {
        return dictionaries;
    }

    public List<String> getModels() {
        return models;
    }

    public Map<String, String> getGroupsNormalization() {
        return groupsNormalization;
    }
    
    public byte[] getGroupsNormalizationByteArray() {
        if (groupsNormalization.isEmpty()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (Entry<String, String> entry : groupsNormalization.entrySet()) {
            sb.append(entry.getKey());
            sb.append("|");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        
        return sb.toString().getBytes();
    }
    
    public String getFalsePositives() {
        return falsePositives;
    }  

    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setLogo(byte[] logo) {
        this.logo = logo;
    }    

    public void setParsingLevel(String parsingLevel) {
        this.parsingLevel = parsingLevel;
    }

    public void setNoIds(boolean noIds) {
        this.noIds = noIds;
    }

    public void setDictionaries(List<String> dictionaries) {
        this.dictionaries = dictionaries;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

    public void setGroupsNormalization(Map<String, String> groupsNormalization) {
        this.groupsNormalization = groupsNormalization;
    }
    
    public void setFalsePositives(String falsePositives) {
        this.falsePositives = falsePositives;
    } 

    public boolean getAbbreviations() {
        return abbreviations;
    }

    public void setAbbreviations(boolean abbreviations) {
        this.abbreviations = abbreviations;
    }

    public boolean getDisambiguation() {
        return disambiguation;
    }

    public void setDisambiguation(boolean disambiguation) {
        this.disambiguation = disambiguation;
    }
    
    public ParserLevel getParserLevel() {       
        
        switch (parsingLevel.toLowerCase()) {
            case "tokenization":
                return ParserLevel.TOKENIZATION;
            case "lemmatization":
                return ParserLevel.LEMMATIZATION;
            case "pos":
            case "part-of-speech tagging":
                return ParserLevel.POS;
            case "chunking":
                return ParserLevel.CHUNKING;
            case "dependency":
                return ParserLevel.DEPENDENCY;
            default:
                return null;
        }
        
    }
    
    public List<pt.ua.tm.neji.dictionary.Dictionary> takeDictionaries(Context context) throws NejiException {
        
        List<pt.ua.tm.neji.dictionary.Dictionary> result = new ArrayList<>();
        
        // Get dictonaries file names list
        DatabaseHandler db = new DefaultDatabaseHandler("neji.db", false);
        List<Dictionary> dictionariesList = db.getDictionaries(this.id);
        List<String> dictionariesFilesList = new ArrayList<>();
        for (Dictionary d : dictionariesList) {
            dictionariesFilesList.add(d.getFile());
        }
        
        for (Map.Entry<String, pt.ua.tm.neji.dictionary.Dictionary> e : context.getDictionaryPairs()) {
            if (dictionariesFilesList.contains(e.getKey())) {
                result.add(e.getValue());
            }
        }

        return result;
    }
    
    public List<MLModel> takeModels(Context context) {
        
        List<MLModel> result = new ArrayList<>();
       
        for (Map.Entry<String, MLModel> e : context.getModelPairs()) {
            if (this.models.contains(e.getKey())) {
                result.add(e.getValue());
            }
        }

        return result;
    }
                
    /*public Set<String> getGroups() throws NejiException {       
        
        Set<String> groups = new HashSet<>();       
        DatabaseHandler db = new DefaultDatabaseHandler("neji.db");
        
        // Get dicitionaries groups
        List<Dictionary> serviceDictionaries = db.getDictionaries(this.name);
        for (Dictionary d : serviceDictionaries) {
            groups.add(d.getGroup());
        }
        
        // Get models groups
        List<Model> serviceModel = db.getModels(this.name);
        for (Model m : serviceModel) {
            groups.add(m.getGroup());
        }
        
        return groups;
    }*/
}
