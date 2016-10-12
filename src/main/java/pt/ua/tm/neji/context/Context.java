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

package pt.ua.tm.neji.context;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.train.model.CRFBase;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.dictionary.DictionariesLoader;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.VariantMatcherLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.ml.MLModelsLoader;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.sentencesplitter.SentenceSplitter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Context provider that manages sentence splitters, parsers, dictionaries and ML models..
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class Context {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Context.class);

    private ContextConfiguration configuration;
    private LinkedBlockingQueue<Parser> parsersTS;
    private LinkedBlockingQueue<SentenceSplitter> sentenceSplittersTS;
    private Map<String, Dictionary> dictionariesTS;
    private Map<String, MLModel> modelsTS;
    private boolean isInitialized;
    private String dictionariesFolder;
    private String modelsFolder;
    private boolean doModels, doDictionaries,
            readyForMultiThreading;
//    private boolean setParsingLevelAutomatically;
    private String parserPath;
    private int numThreads;

    public Context(final ContextConfiguration configuration,
                   final String modelsFolder, final String dictionariesFolder) {
        this(configuration, modelsFolder, dictionariesFolder, null);
    }

//    public Context(final InputFormat inputFormat, final List<OutputFormat> outputFormats,
//                   final String modelsFolder, final String dictionariesFolder, final String parserPath) {
//        this(inputFormat, outputFormats, modelsFolder, dictionariesFolder, parserPath, null, null, null);
//    }
//
//    public Context(final InputFormat inputFormat, final List<OutputFormat> outputFormats,
//                   final String modelsFolder, final String dictionariesFolder,
//                   final ParserTool parserTool, final ParserLanguage parserLanguage, final ParserLevel parserLevel) {
//        this(inputFormat, outputFormats, modelsFolder, dictionariesFolder, null, parserTool, parserLanguage, parserLevel);
//    }

    public Context(final ContextConfiguration configuration,
                   final String modelsFolder, final String dictionariesFolder, final String parserPath) {
        this.configuration = configuration;
        this.dictionariesFolder = dictionariesFolder;
        this.modelsFolder = modelsFolder;
        this.parserPath = parserPath;

        this.parsersTS = new LinkedBlockingQueue<>();
        this.dictionariesTS = new LinkedHashMap<>();
        this.modelsTS = new LinkedHashMap<>();
        this.sentenceSplittersTS = new LinkedBlockingQueue<>();

        this.readyForMultiThreading = false;
        this.isInitialized = false;

        this.doModels = modelsFolder != null;
        this.doDictionaries = dictionariesFolder != null;

//        this.setParsingLevelAutomatically = (parserLevel == null);
    }

    public ContextProcessors take() throws InterruptedException {
        Parser parser = parsersTS.take();
        SentenceSplitter splitter = sentenceSplittersTS.take();
        Map<String, CRFBase> contextModels = new LinkedHashMap<>();
        
        if (doModels) {
            for (MLModel model : modelsTS.values()) {
                contextModels.put(model.getModelName(), model.take());
            }
        }

        return new ContextProcessors(parser, splitter, contextModels);
    }

    public void put(ContextProcessors contextProcessors) throws InterruptedException {
        sentenceSplittersTS.put(contextProcessors.getSentenceSplitter());
        parsersTS.put(contextProcessors.getParser());

        for (MLModel model : modelsTS.values()) {
            model.put(contextProcessors.getCRF(model.getModelName()));
        }
    }

    public boolean addNewModel(String modelName, MLModel model) throws NejiException {
        try {
            model.initialize();
            if(readyForMultiThreading){
                model.addMultiThreadingSupport(numThreads);
            }
            modelsTS.put(model.getModelName(), model);
            logger.info("Model {}, group {}, was added to context.", modelName, model.getSemanticGroup());

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void removeModel(String modelName) {
        MLModel removedModel = modelsTS.remove(modelName);
        if (removedModel != null) {
            logger.info("Model {}, group {}, was removed from context.", modelName, removedModel.getSemanticGroup());
        }
    }

    // Models
    public List<MLModel> getModels() {
        return Lists.newArrayList(modelsTS.values());
    }

    public Set<Map.Entry<String, MLModel>> getModelPairs() {
        return modelsTS.entrySet();
    }

    public MLModel getModel(String modelName) {
        return modelsTS.get(modelName);
    }

    public boolean addNewDictionary(String dictionaryName, List<String> lines) {

        try {
            if (!dictionariesTS.containsKey(dictionaryName)) {

                Dictionary d = VariantMatcherLoader.loadDictionaryFromLines(lines);
                dictionariesTS.put(dictionaryName, d);
                logger.info("Dictionary {}, group {}, was added to context.", dictionaryName, d.getGroup());

                return true;
            }

        } catch (NejiException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public void removeDictionary(String dictionaryName) {
        Dictionary removedDictionary = dictionariesTS.remove(dictionaryName);
        if (removedDictionary != null) {
            logger.info("Dictionary {}, group {}, was removed from context.", dictionaryName, removedDictionary.getGroup());
        }
    }

    // Dictionaries
    public List<Dictionary> getDictionaries() {
        return Lists.newArrayList(dictionariesTS.values());
    }

    public Set<Map.Entry<String, Dictionary>> getDictionaryPairs() {
        return dictionariesTS.entrySet();
    }

    public ContextConfiguration getConfiguration() {
        return configuration;
    }

    public void initialize() throws NejiException {
        if (isInitialized) {
            return;
        }


        if (doModels) {
            String priorityFileName = modelsFolder + "_priority";
            MLModelsLoader ml;
            try {
                ml = new MLModelsLoader(Files.newInputStream(Paths.get(priorityFileName)));
            } catch (IOException ex) {
                throw new NejiException("There was a problem reading the models.", ex);
            }
            ml.load(new File(modelsFolder));

            // Get models
            modelsTS = ml.getModels();

            // Initialize models
            List<MLModel> modelList = Lists.newArrayList(modelsTS.values());
            for (MLModel model : modelList) {
                model.initialize();
            }

//            if (setParsingLevelAutomatically) {
//                this.parserLevel = getParserLevel(modelList);
//            }
        }

//        // set default parsing definitions
//        if (parserTool == null) {
//            parserTool = ParserTool.GDEP;
//        }
//
//        if (parserLanguage == null) {
//            parserLanguage = ParserLanguage.ENGLISH;
//        }
//
//        if (parserLevel == null) {
//            parserLevel = ParserLevel.TOKENIZATION;
//        }

        logger.info("Initializing parsers...");
        // Initialize Parser
        try {
            ParserTool tool = configuration.getParserTool();
            ParserLanguage lang = configuration.getParserLanguage();
            ParserLevel level = configuration.getParserLevel();
            Parser p = Parser.defaultParserFactory(tool, lang, level, parserPath);
            p.launch();
            parsersTS.put(p);
        } catch (Exception ex) {
            throw new NejiException("There was a problem loading the parser.", ex);
        }


        // Load dictionaries matchers
        if (doDictionaries) {
            logger.info("Loading dictionaries...");
            String priorityFileName = dictionariesFolder + "_priority";
            DictionariesLoader dl;
            try {
                dl = new DictionariesLoader(new FileInputStream(priorityFileName));
            } catch (FileNotFoundException ex) {
                throw new NejiException("There was a problem reading the dictionaries.", ex);
            }
            dl.load(new File(dictionariesFolder), true);
            dictionariesTS = dl.getDictionaries();
        }

        // Initialize sentence splitters
        try {
            SentenceSplitter ss = new LingpipeSentenceSplitter();
            sentenceSplittersTS.put(ss);
        } catch (Exception ex) {
            throw new NejiException("There was a problem loading the Sentence Splitters.", ex);
        }

        // Set initialized
        isInitialized = true;
    }

//    private ParserLevel getParserLevel(final List<MLModel> models) {
//        int[] counters = new int[4];
//        for (int i = 0; i < counters.length; i++) {
//            counters[i] = 0;
//        }
//        for (MLModel model : models) {
//            ModelConfig mc = model.getConfig();
//            if (mc.isLemma()) {
//                counters[0]++;
//            }
//            if (mc.isPos()) {
//                counters[1]++;
//            }
//            if (mc.isChunk()) {
//                counters[2]++;
//            }
//            if (mc.isNLP()) {
//                counters[3]++;
//            }
//        }
//
//        if (counters[3] > 0) {
//            return ParserLevel.DEPENDENCY;
//        } else if (counters[2] > 0) {
//            return ParserLevel.CHUNKING;
//        } else if (counters[1] > 0) {
//            return ParserLevel.POS;
//        } else if (counters[0] > 0) {
//            return ParserLevel.LEMMATIZATION;
//        } else {
//            return ParserLevel.TOKENIZATION;
//        }
//    }

    public void addMultiThreadingSupport(final int numThreads) throws NejiException {
        if (!isInitialized) {
            throw new RuntimeException("Context must be initialized before "
                    + "adding multi-threading support.");
        }
        if (readyForMultiThreading) {
            return;
        }

        // Parsers
        ParserTool tool = configuration.getParserTool();
        ParserLanguage lang = configuration.getParserLanguage();
        ParserLevel level = configuration.getParserLevel();
        for (int i = 1; i < numThreads; i++) {
            try {
                Parser p = Parser.defaultParserFactory(tool, lang, level, parserPath);
                p.launch();
                parsersTS.put(p);
            } catch (Exception ex) {
                throw new NejiException("There was a problem loading the parser.", ex);
            }
        }

        // Models
        if (doModels) {
            for (MLModel model : modelsTS.values()) {
                try {
                    model.addMultiThreadingSupport(numThreads);
                } catch (Exception ex) {
                    throw new NejiException("There was a problem loading the CRF models.", ex);
                }
            }
        }

        // Sentence Splitters
        for (int i = 1; i < numThreads; i++) {
            try {
                SentenceSplitter ss = new LingpipeSentenceSplitter();
                sentenceSplittersTS.put(ss);
            } catch (Exception ex) {
                throw new NejiException("There was a problem loading the Sentence Splitters.", ex);
            }
        }


        readyForMultiThreading = true;
        this.numThreads = numThreads;
    }

    public void terminate() throws NejiException {
        // Finalize parsers
        while (!parsersTS.isEmpty()) {
            try {
                Parser parser = parsersTS.take();
                parser.close();
                parser = null;
            } catch (InterruptedException ex) {
                throw new NejiException("There was a problem terminating the parsers.", ex);
            }
        }
        this.parsersTS = new LinkedBlockingQueue<>();

        // Dictionaries
        for (Map.Entry<String, Dictionary> e : dictionariesTS.entrySet()) {
            e.setValue(null);
            e = null;
        }
        this.dictionariesTS = new LinkedHashMap<>();

        // Models
        for (Map.Entry<String, MLModel> e : modelsTS.entrySet()) {
            e.setValue(null);
            e = null;
        }
        this.modelsTS = new LinkedHashMap<>();


        System.gc();
        isInitialized = false;
        readyForMultiThreading = false;
    }
}
