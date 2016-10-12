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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.ml;

import cc.mallet.fst.CRF;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants.Parsing;
import pt.ua.tm.neji.train.config.ModelConfig;
import pt.ua.tm.neji.train.model.CRFBase;
import pt.ua.tm.neji.train.model.CRFModel;
import pt.ua.tm.neji.dictionary.DictionariesLoader;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.VariantMatcherLoader;
import pt.ua.tm.neji.exception.NejiException;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;

/**
 * Machine Learning model representation.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class MLModel {

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(MLModel.class);

    private LinkedBlockingQueue<CRFBase> models;
    private ModelConfig config;
    private Parsing parsing;
    private String semanticGroup;
    private String dictionariesPath;
    private Map<String, Dictionary> normalization;
    private boolean hasNormalizationDictionaries;
    private boolean readyForMultiThreading;
    private boolean isInitialized;
    // To initialize
    private final String modelName, modelFile, configFile, normalizationDictionariesFolder;

    public MLModel(final String modelName, final File propertiesFile) {
        Properties prop = new Properties();
        try {
            prop.load(new FileReader(propertiesFile));
        } catch (IOException e) {
            throw new RuntimeException("There was a problem loading the model properties file.", e);
        }

        String folderPath = propertiesFile.getParent() + File.separator;


        this.modelName = modelName;
        this.modelFile = FilenameUtils.normalize(folderPath + prop.getProperty("file"));
        this.configFile = FilenameUtils.normalize(folderPath + prop.getProperty("config"));
        this.parsing = Parsing.valueOf(prop.getProperty("parsing"));
        this.semanticGroup = prop.getProperty("group");
        this.dictionariesPath = prop.getProperty("dictionaries");
        if (dictionariesPath == null) {
            this.normalizationDictionariesFolder = null;
        } else {
            this.normalizationDictionariesFolder = FilenameUtils.normalize(folderPath + dictionariesPath);
        }
        this.models = new LinkedBlockingQueue<>();
        this.readyForMultiThreading = false;
        this.isInitialized = false;
    }

    public String getModelName() {
        return modelName;
    }

    public void initialize() throws NejiException {
        if (isInitialized) {
            return;
        }
        try(GZIPInputStream in = new GZIPInputStream(new FileInputStream(modelFile))) {
            this.config = new ModelConfig(configFile);
            CRFModel model = new CRFModel(config, parsing, in);
            this.models.put(model);

            // Load normalization dictionaries
            this.normalization = new LinkedHashMap<>();
            this.hasNormalizationDictionaries = false;
            if (normalizationDictionariesFolder != null) {
                String priorityFileName = normalizationDictionariesFolder + "_priority";
                try(FileInputStream dictsPriorityStream = new FileInputStream(priorityFileName)) {

                    DictionariesLoader dl = new DictionariesLoader(dictsPriorityStream);
                    dl.load(new File(normalizationDictionariesFolder), false);
                    this.normalization = dl.getDictionaries();
                    this.hasNormalizationDictionaries = true;
                }
            }
        } catch (IOException | InterruptedException ex) {
            throw new NejiException("There was a problem loading the model files.", ex);
        }
        isInitialized = true;
    }

    public void addMultiThreadingSupport(final int numThreads) throws InterruptedException {
        if (readyForMultiThreading) {
            return;
        }

        CRF crf = models.peek().getCRF();
        for (int j = 1; j < numThreads; j++) {
            CRFModel m = new CRFModel(config, parsing);
            m.setCRF(new CRF(crf));
            models.put(m);
        }
        readyForMultiThreading = true;
    }

    public CRFBase take() throws InterruptedException {
        return models.take();
    }

    public void put(final CRFBase model) throws InterruptedException {
        models.put(model);
    }

    public boolean hasNormalizationDictionaries() {
        return hasNormalizationDictionaries;
    }


    public CRFBase getCrf() {
        return models.peek();
    }

    public ModelConfig getConfig() {
        return config;
    }

    public Parsing getParsing() {
        return parsing;
    }

    public String getSemanticGroup() {
        return semanticGroup;
    }

    public Collection<Dictionary> getNormalizationDictionaries() {
        return normalization.values();
    }

    public Set<String> getNormalizationDictionaryNames() {
        return normalization.keySet();
    }

    public boolean addNormalizationDictionary(String dictionaryName, List<String> lines) throws NejiException, IOException{
        
        if(!normalization.containsKey(dictionaryName)) {
            Dictionary d = VariantMatcherLoader.loadDictionaryFromLines(lines);
            normalization.put(dictionaryName, d);
            
            logger.info("Normalization dictionary {} was added to model {}.", dictionaryName, modelName);
        }

        return false;
    }

    public void removeNormalizationDictionary(String dictionaryName) throws IOException {
        if(normalization.containsKey(dictionaryName)) {
            normalization.remove(dictionaryName);

            logger.info("Normalization dictionary {} was removed from model {}.", dictionaryName, modelName);
        }
    }

    /**
     * Gets isInitialized.
     *
     * @return Value of isInitialized.
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Gets configuration file path.
     * @return configuration file path
     */
    public String getConfigFile() {
        return configFile;
    }
    
    /**
     * Gets model file.
     * @return model file
     */
    public String getModelFile() {
        return modelFile;
    }
    
    /**
     * Gets normalization dictionaries folder.
     * @return normalization dictionaries folder
     */
    public String getNormalizationDictionariesFolder() {
        return normalizationDictionariesFolder;
    }
}
