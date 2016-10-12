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

package pt.ua.tm.neji.train.batch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.batch.FileBatchExecutor;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.core.Constants.LabelFormat;
import pt.ua.tm.neji.core.batch.BatchExecutor;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.processor.Processor;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.processor.filewrappers.InputFile;
import pt.ua.tm.neji.train.context.TrainContext;
import pt.ua.tm.neji.train.util.A1Utils;
import pt.ua.tm.neji.train.util.A1Utils.A1Pairs;

/**
 *
 * @author jeronimo
 */
public class TrainBatchExecutor extends BatchExecutor {
    
    // Attributes
    private static Logger logger = LoggerFactory.getLogger(FileBatchExecutor.class);
    private String inputSentencesFilePath;
    private String inputAnnotationsFilePath;
    private int numThreads;
    
    /**
     * Constructor. Phase 1
     * @param inputSentencesFilePath input sentences file path
     * @param inputAnnotationsFilePath input annotations file path
     * @param numThreads number of threads to use
     */
    public TrainBatchExecutor(final String inputSentencesFilePath, final String inputAnnotationsFilePath, int numThreads) {
        this.inputSentencesFilePath = inputSentencesFilePath;
        this.inputAnnotationsFilePath = inputAnnotationsFilePath;
        this.numThreads = numThreads;
    }
    
    /**
     * Constructor. Phase 2
     */
    public TrainBatchExecutor() {
        this(null, null, 1);
    }
    
    @Override
    public void run(Class<? extends Processor> processorCls, Context context, Object... args) throws NejiException {
                   
        StopWatch timer = new StopWatch();
        
        logger.info("Initializing context...");
        context.initialize();
        
        timer.start();
        
        if (((TrainContext) context).getPhase() == 1) {  // Phase 1            
            
            // If input format requires annotations
            if (context.getConfiguration().getInputFormat().equals(InputFormat.BC2)) { // File + Annotations formats
                processFiles(inputSentencesFilePath, inputAnnotationsFilePath, (TrainContext) context, processorCls, args);
            }
            else if (context.getConfiguration().getInputFormat().equals(InputFormat.A1)) { // Folder format
                processMultipleFiles(inputSentencesFilePath, numThreads, (TrainContext) context, processorCls, args);
            }
            else { // File formats 
                processFiles(inputSentencesFilePath, (TrainContext) context, processorCls, args);
            }
        }
        else { // Phase 2
                       
            // In this case inputSentencesFilePath contains the path to the corpus
            processFiles2((TrainContext) context, processorCls, args);
        }
                                                
        logger.info("Terminating context...");
        context.terminate();
        
        timer.stop();
        logger.info("Processed files in {}", timer.toString());
    }
    
    @Override
    public Collection<Corpus> getProcessedCorpora() {
        return null;
    }
    
    private void processFiles(final String inputSentencesFilePath, final String inputAnnotationsFilePath, 
                              TrainContext context, final Class<? extends Processor> processorCls, Object... args) {
        
        File sentencesFile = new File(inputSentencesFilePath);
        File annotationsFile = new File(inputAnnotationsFilePath);
        
        // Make corpus
        Corpus corpus = new Corpus(LabelFormat.BIO, context.getEntity());
        
        // Set corpus identifier
        corpus.setIdentifier(FilenameUtils.getBaseName(sentencesFile.getName()));
        
        // Make in/out corpus wrappers
        InputFile inputSentencesFile = new InputFile(corpus, sentencesFile, false);
        InputFile inputAnnotationsFile = new InputFile(corpus, annotationsFile, false);
        
        logger.info("");
        logger.info("Started processing...");
        
        Processor processor;
        try {
            processor = newProcessor(processorCls, context, inputSentencesFile, inputAnnotationsFile, args);
        } catch (NejiException ex) {
            String m = "There was a problem creating the processor";
            logger.error(m, ex);
            throw new RuntimeException(m, ex);
        }
        
        // Run processor
        processor.run();
    }
    
    private void processFiles(final String inputSentencesFilePath, TrainContext context,
                              final Class<? extends Processor> processorCls, Object... args) {
        
        File sentencesFile = new File(inputSentencesFilePath);
        
        // Make corpus
        Corpus corpus = new Corpus(LabelFormat.BIO, context.getEntity());
        
        // // Set corpus identifier
        corpus.setIdentifier(FilenameUtils.getBaseName(sentencesFile.getName()));
        
        // Make in/out corpus wrappers
        InputFile inputSentencesFile = new InputFile(corpus, sentencesFile, false);
        
        logger.info("");
        logger.info("Started processing...");
        
        Processor processor;
        try {
            processor = newProcessor(processorCls, context, inputSentencesFile, args);
        } catch (NejiException ex) {
            String m = "There was a problem creating the processor";
            logger.error(m, ex);
            throw new RuntimeException(m, ex);
        }
        
        // Run processor
        processor.run();
    }
    
    /**
     * Phase 2 process files
     * @param corpusPath
     * @param context
     * @param processorCls
     * @param args 
     */
    private void processFiles2(TrainContext context, final Class<? extends Processor> processorCls,
                              Object... args) {
                
        // Make corpus
        Corpus corpus = new Corpus(LabelFormat.BIO, context.getEntity());
        
        // Lets put it as the corpus path
        corpus.setIdentifier("");
        
        // Temporary file (to mantain the structure)
        File tmpFile = new File("tmp.txt");

        try {
            PrintWriter pwt = new PrintWriter(tmpFile);
            pwt.write(" ");
            pwt.close();
        } catch (IOException ex) {
            System.out.println("Error: An error ocurred while creating the temporary file. " + ex.getMessage());
        }
        
        // Make in/out corpus wrappers
        InputFile inputCorpusPathFile = new InputFile(corpus, tmpFile, false);
        
        logger.info("");
        logger.info("Started processing...");
        
        Processor processor;
        try {
            processor = newProcessor(processorCls, context, inputCorpusPathFile, args);
        } catch (NejiException ex) {
            String m = "There was a problem creating the processor";
            logger.error(m, ex);
            throw new RuntimeException(m, ex);
        }
        
        // Run processor
        processor.run();
    }
    
    // For multiple files processing
    private int processMultipleFiles(final String inputFolderPath, final int numThreads, TrainContext context,
                              final Class<? extends Processor> processorCls, Object... args) {
        
        int filesProcessed = 0;
        
        // Getting folder files
        File inputFolder = new File(inputFolderPath);
        File[] files = inputFolder.listFiles();
        File[] annotations = null;
        boolean hasAnnotationFiles = false;
        
        // Determine if format is A1, to separate .txt and .a1 files, before processing
        if (context.getConfiguration().getInputFormat().equals(InputFormat.A1)) {
            A1Pairs a1Pairs = A1Utils.separateTextAnnotations(files);
            files = a1Pairs.getFiles();
            annotations = a1Pairs.getAnnotations();
            hasAnnotationFiles = true;
        }

        // Multi-threading 
        try {
            logger.info("Installing multi-threading support...");
            context.addMultiThreadingSupport(numThreads);
        } catch (NejiException ex) {
            String m = "There was a problem installing multi-threading support.";
            logger.error(m, ex);
            throw new RuntimeException(m, ex);
        }
        
        // Start thread pool
        logger.info("Starting thread pool with support for {} threads...", numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        LinkedList<Future> futures = new LinkedList<>();

        // Iterate over files
        for (int i = 0 ; i < files.length ; i++) {
            
            // Make corpus, output file
            Corpus corpus = new Corpus(LabelFormat.BIO, context.getEntity());

            // By default, the corpus identifier is the file name
            corpus.setIdentifier(FilenameUtils.getBaseName(files[i].getName()));

            // Make in/out corpus wrappers
            InputFile inputSentencesFile = new InputFile(corpus, files[i], false);
            InputFile inputAnnotationsFile = null;
            
            // Verify ih has annotation files
            if (hasAnnotationFiles) {
                inputAnnotationsFile = new InputFile(corpus, annotations[i], false);
            }           

            Processor processor;
            try {
                if (!hasAnnotationFiles) processor = newProcessor(processorCls, context, inputSentencesFile, args);
                else processor = newProcessor(processorCls, context, inputSentencesFile, inputAnnotationsFile, args);
            } catch (NejiException ex) {
                String m = "There was a problem creating the processor of the file: " + files[i].getAbsolutePath();
                logger.error(m, ex);
                throw new RuntimeException(m, ex);
            }

            Future submit = executor.submit(processor);
            futures.add(submit);
        }

        logger.info("");
        logger.info("{} file(s) to process.", futures.size());
        logger.info("Started processing...");


        Iterator<Future> it = futures.iterator();
        while (it.hasNext()) {
            Future future = it.next();
            try {
                Object o = future.get();
                future = null;
                it.remove();
                filesProcessed++;
            } catch (ExecutionException | InterruptedException ex) {
                String m = "There was a problem running the processor.";
                logger.error(m, ex);
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            String m = "There was a problem executing the processing tasks.";
            logger.error(m, ex);
            throw new RuntimeException(m, ex);
        }

        return filesProcessed;
    }
    
    protected static <A, B, C> Processor newProcessor(final Class<? extends Processor> processorCls,
                                                   final Context context,
                                                   final A sentencesInput,
                                                   final B annotationsInput,                                                  
                                                   final Object... args) throws NejiException {
                
        Validate.notNull(processorCls);
        Validate.notNull(context);
        Validate.notNull(sentencesInput);
        Validate.notNull(annotationsInput);

        int numberArgs = 3 + (args != null ? args.length : 0);
        List<Object> values = new ArrayList<>(numberArgs);
        values.add(context);
        values.add(sentencesInput);
        values.add(annotationsInput);

        List<Class> types = new ArrayList<>(numberArgs);
        types.add(context.getClass());
        types.add(sentencesInput.getClass());
        types.add(annotationsInput.getClass());        

        if (args != null) {
            for (Object arg : args) {
                values.add(arg);
                types.add(arg.getClass());
            }
        }
        
        try {
            return (Processor) ConstructorUtils.invokeConstructor(
                    processorCls, values.toArray(), types.toArray(new Class[types.size()]));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            logger.error("Error creating new pipeline processor.", ex);
            throw new NejiException("Error creating new pipeline processor.", ex);
        }
    }
    
    protected static <A, C> Processor newProcessor(final Class<? extends Processor> processorCls,
                                                   final Context context,
                                                   final A sentencesInput,                                                
                                                   final Object... args) throws NejiException {
        
        Validate.notNull(processorCls);
        Validate.notNull(context);
        Validate.notNull(sentencesInput);
        
        int numberArgs = 2 + (args != null ? args.length : 0);
        List<Object> values = new ArrayList<>(numberArgs);
        values.add(context);
        values.add(sentencesInput);

        List<Class> types = new ArrayList<>(numberArgs);
        types.add(context.getClass());
        types.add(sentencesInput.getClass());

        if (args != null) {
            for (Object arg : args) {
                values.add(arg);
                types.add(arg.getClass());
            }
        }
        
        try {
            return (Processor) ConstructorUtils.invokeConstructor(
                    processorCls, values.toArray(), types.toArray(new Class[types.size()]));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            logger.error("Error creating new pipeline processor.", ex);
            throw new NejiException("Error creating new pipeline processor.", ex);
        }
    }   
}
