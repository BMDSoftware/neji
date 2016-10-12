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

package pt.ua.tm.neji.train.processor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextConfiguration;
import pt.ua.tm.neji.context.ContextProcessors;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Reader;
import pt.ua.tm.neji.core.module.Writer;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.core.processor.BaseProcessor;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.DictionaryHybrid;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.misc.DTDTagger;
import pt.ua.tm.neji.processor.FileProcessor;
import pt.ua.tm.neji.processor.filewrappers.InputFile;
import pt.ua.tm.neji.processor.filewrappers.OutputFile;
import pt.ua.tm.neji.train.context.TrainContext;
import pt.ua.tm.neji.train.dictionary.TrainDictionary;
import pt.ua.tm.neji.train.nlp.TrainNLP;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase1;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase2;
import pt.ua.tm.neji.train.trainer.DefaultTrainer;

/**
 *
 * @author jeronimo
 */
public class TrainProcessor extends BaseProcessor {
    
    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(FileProcessor.class);

    private InputFile sentencesInputFile;
    private InputFile annotationsInputFile;
    
    /**
     * Constructor.
     * @param context The context
     * @param sentencesInputFile The sentences input file
     */
    public TrainProcessor(Context context, InputFile sentencesInputFile) {
        super(context);
        setSentencesInputFile(sentencesInputFile);
        this.annotationsInputFile = null;
    }
    
    /**
     * Constructor.
     * @param context The context
     * @param sentencesInputFile The sentences input file
     * @param annotationsInputFile The annotations input file
     */
    public TrainProcessor(Context context, InputFile sentencesInputFile, InputFile annotationsInputFile) {
        this(context, sentencesInputFile);
        setAnnotationsInputFile(annotationsInputFile);
    }
    
    @Override
    public void run() {
                   
        // Get context and processors
        TrainContext context = (TrainContext) getContext();
        ContextProcessors cp = null;
        
        try {
            cp = context.take();
        } catch (InterruptedException ex) {
            String m = "There was a problem getting the context processors. Stream with the identifier "
                        + getSentencesInputFile().getCorpus().getIdentifier();
            throw new RuntimeException(m, ex);
        }
                        
        try {
            
            // Get corpus
            Corpus corpus = getSentencesInputFile().getCorpus();
            
            // Create pipeline
            Pipeline p;
            
            if (context.getPhase() == 1) { // phase 1
                
                p = new TrainPipelinePhase1(corpus);
                instantiateModulesPhase1(context.getDictionaries(), cp, context, p);
            }
            else { // phase 2
                
                p = new TrainPipelinePhase2();
                instantiateModulesPhase2(cp, context, p);
            }
            
            // Run pipeline
            p.run(sentencesInputFile.getInStream());

            // Return processors
            context.put(cp);
            p.clear();
            
        } catch (IOException | NejiException | InterruptedException ex) {
            
            // Return context processors to context
            try {
                context.put(cp);
            } catch (InterruptedException e) {
                String m = "There was a problem returning the context processors. Stream with the identifier "
                           + getSentencesInputFile().getCorpus().getIdentifier();;
                throw new RuntimeException(m, ex);
            }
            
            String m = "There was a problem annotating the stream with the identifier " + getSentencesInputFile().getCorpus().getIdentifier();
            throw new RuntimeException(m, ex);
        }
        
        logger.info("Done processing: {}", getSentencesInputFile().getCorpus().getIdentifier());
    }
    
    /**
     * Gets sentences input file.
     * @return The sentences input file
     */
    public final InputFile getSentencesInputFile() {
        return sentencesInputFile;
    }
    
    /**
     * Sets sentences input file.
     * @param sentencesInputFile The sentences input file.
     */
    public final void setSentencesInputFile(InputFile sentencesInputFile) {
        Validate.notNull(sentencesInputFile);
        this.sentencesInputFile = sentencesInputFile;
    }
    
    /**
     * Gets annotations input file.
     * @return The annotations input file
     */
    public final InputFile getAnnotationsInputFile() {
        return annotationsInputFile;
    }
    
    /**
     * Sets annotations input file.
     * @param annotationsInputFile The annotations input file.
     */
    public final void setAnnotationsInputFile(InputFile annotationsInputFile) {
        Validate.notNull(annotationsInputFile);
        this.annotationsInputFile = annotationsInputFile;
    }
    
    protected class OutputFileList extends ArrayList<OutputFile> {
        
        public OutputFileList() {
            super();
        }

        public List<OutputStream> getOutputStreamList() throws IOException {
            List<OutputStream> outputStreamList = new ArrayList<>();
            for(OutputFile off : this) {
                //outputStreamList.add(off.getOutStream());
            }
            return outputStreamList;
        }
    }
    
    // For training purposes
    protected final void instantiateModules(List<Dictionary> dictionaries,
                                            ContextProcessors cp,
                                            TrainContext c,
                                            Pipeline p) throws NejiException, IOException {

        List<Module> moduleList = new ArrayList<>();
        fetchModulesFromConfig(cp.getParser(), cp.getParser().getLevel(), moduleList, null, c);
        
        int index = 1;
                
        // Dictionary matching
        for (Dictionary d : dictionaries) {
            DictionaryHybrid dtl = new DictionaryHybrid(d);
            moduleList.add(index++, dtl);
        }
        
        // Train module
        moduleList.add(index++, new DefaultTrainer(c.getFeatures()));
        
        // Add all of the modules to the pipeline
        for (Module m : moduleList) {
            p.add(m);
        }
    }
    
    /**
     * Instantiate modules for phase 1 of training (first pipeline)
     * @param dictionaries dictionaries list
     * @param cp context processors
     * @param c context
     * @param p pipeline
     * @throws NejiException
     * @throws IOException 
     */
    protected final void instantiateModulesPhase1(List<Dictionary> dictionaries,
                                            ContextProcessors cp,
                                            TrainContext c,
                                            Pipeline p) throws NejiException, IOException {

        List<Module> moduleList = new ArrayList<>();
        fetchModulesFromConfig(cp.getParser(), cp.getParser().getLevel(), moduleList, null, c);
        
        int index = 1;
        
        // NLP module
        moduleList.add(index++, new TrainNLP(cp.getParser(), cp.getParser().getLevel()));
                
        // Dictionary matching module
        for (Dictionary d : dictionaries) {
            TrainDictionary dtl = new TrainDictionary(d);
            moduleList.add(index++, dtl);
        }
        
        // Add all of the modules to the pipeline
        for (Module m : moduleList) {
            p.add(m);
        }
    }
    
    /**
     * Instantiate modules for phase 2 of training (second pipeline)
     * @param cp context processors
     * @param c context
     * @param p pipeline
     * @throws NejiException
     * @throws IOException 
     */
    protected final void instantiateModulesPhase2(ContextProcessors cp,
                                                  TrainContext c,
                                                  Pipeline p) throws NejiException, IOException {

        List<Module> moduleList = new ArrayList<>();
        fetchModulesFromConfig(cp.getParser(), cp.getParser().getLevel(), moduleList, null, c);
        
        int index = 1;
        
        // Add all of the modules to the pipeline
        for (Module m : moduleList) {
            p.add(m);
        }
        
        // Train module
        moduleList.add(index++, new DefaultTrainer(c.getFeatures()));
        
        // Add all of the modules to the pipeline
        for (Module m : moduleList) {
            p.add(m);
        }
    }
    
    private void fetchModulesFromConfig(Parser parser,
                                        ParserLevel parserLevel,
                                        List<Module> moduleList,
                                        String[] xmlTags,
                                        TrainContext c) throws NejiException, IOException {

        ContextConfiguration config = c.getConfiguration();

        // Change DocType of the document (DTD)
        if (config.getInputFormat().equals(InputFormat.XML)
                && config.getOutputFormats().contains(OutputFormat.XML)) {
            DTDTagger doc = new DTDTagger();
            moduleList.add(doc);
        }

        config.fetchCustomModules(moduleList, parser);
        
        Reader reader = null;
        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.addAll(config.getOutputFormats());
        for (Module m : moduleList) {

            if (m instanceof Reader) {
                reader = (Reader) m;

            } else if (m instanceof Writer) {
                OutputFormat f = ((Writer) m).getFormat();
                if (config.getOutputFormats().contains(f)) {
                    outputFormats.remove(f);
                }
            }
        }

        if (reader == null) {
            if (config.getInputFormat().equals(InputFormat.SERIALIZED)) {
                reader = config.getInputFormat().instantiateTrainerReader(null, null, c.getSerializedCorpusPath(), null);
            } else if (getAnnotationsInputFile() == null) {
                reader = config.getInputFormat().instantiateDefaultReader(parser, parserLevel, xmlTags);
            } else {
                reader = config.getInputFormat().instantiateTrainerReader(parser, parserLevel, c.getSerializedCorpusPath(), getAnnotationsInputFile().getInStream());
            }
        }
        moduleList.add(0, reader);

        // Instantiate writers
        for (OutputFormat f : outputFormats) {
            moduleList.add(f.instantiateTrainerWriter(c.getSerializedCorpusPath(), c.getDictionariesPath(),
                    c.getModelPath(), c.getEntity()));
        }
    }
}
