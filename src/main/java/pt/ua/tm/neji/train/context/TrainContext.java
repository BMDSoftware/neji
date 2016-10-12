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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.tm.neji.train.context;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextConfiguration;
import pt.ua.tm.neji.core.Constants.Parsing;
import pt.ua.tm.neji.train.config.ModelConfig;

/**
 *
 * @author jeronimo
 */
public class TrainContext extends Context {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(TrainContext.class);
    
    // Attributes
    private ModelConfig features;
    private int phase;
    private String serialiedCorpusPath;
    private String dictionariesPath;
    private String modelPath;
    
    /**
     * Constructor.
     * @param configuration The context configuration
     * @param features Features to be used in the training
     * @param dictionariesFolder Dictionaries folder path
     * @param serialiedCorpusPath path to the folder where serialized corpus should be write/reade
     * @param modelPath path to the folder where model should be write
     * @param phase Current processing phase
     */
    public TrainContext(ContextConfiguration configuration, ModelConfig features,
                        final String dictionariesFolder, String serialiedCorpusPath, String modelPath, int phase) {
        
        super(configuration, null, dictionariesFolder, null);

        this.features = features;
        this.phase = phase;
        this.serialiedCorpusPath = serialiedCorpusPath;
        this.dictionariesPath = dictionariesFolder;
        this.modelPath = modelPath;
    }
    
    /**
     * Gets entity target list.
     * @return The entity target list
     */
    public List<String> getEntity() {
        return features.getEntity();
    }
    
    /**
     * Gets parsing direction.
     * @return The parsing direction
     */
    public Parsing getParsing() {
        return features.getParsing();
    }
    
    /**
     * Gets features.
     * @return The file that contains the features to be used in model training
     */
    public ModelConfig getFeatures() {
        return features;
    }
    
    /**
     * Sets features.
     * @param features The file that contains the features to be used in model training
     */
    public void setFeatures(ModelConfig features) {
        this.features = features;
    }
    
    /**
     * Gets phase.
     * @return the current processing phase
     */
    public int getPhase() {
        return phase;
    }
    
    /**
     * Sets phase.
     * @param phase the current processing phase
     */
    public void setPhase(int phase) {
        this.phase = phase;
    }
    
    /**
     * Gets serialized corpus path.
     * @return the model path
     */
    public String getSerializedCorpusPath() {
        return serialiedCorpusPath;
    }
        
    /**
     * Get dictionaries path.
     * @return dictionaries path
     */
    public String getDictionariesPath() {
        return dictionariesPath;
    }
    
    /**
     * Gets model path.
     * @return the model path
     */
    public String getModelPath() {
        return modelPath;
    }
}
