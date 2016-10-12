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

package pt.ua.tm.neji.batch;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.batch.BatchExecutor;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.processor.Processor;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.processor.filewrappers.InputFile;
import pt.ua.tm.neji.processor.filewrappers.OutputFile;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Batch pipeline processors executor, with support for concurrent execution of multiple pipeline processors.
 *
 * @author David Campos
 * @author Tiago Nunes
 * @version 1.0
 * @since 1.0
 */
public class FileBatchExecutor extends BatchExecutor {

    private static Logger logger = LoggerFactory.getLogger(FileBatchExecutor.class);
    private String inputFolderPath, outputFolderPath, inputWildcardFilter;
    private int numThreads;
    private boolean compressed;
    private int filesProcessed;
    private Collection<Corpus> processedCorpora;
    private boolean storeDocuments;
    private final boolean addAnnotationsWithoutIDs;


    public FileBatchExecutor(final String inputFolderPath, final String outputFolderPath,
                             final boolean compressed, final int numThreads, final String inputWildcardFilter,
                             final boolean storeDocuments, final boolean addAnnotationsWithoutIDs) {
        this.inputFolderPath = inputFolderPath;
        this.outputFolderPath = outputFolderPath;
        this.inputWildcardFilter = inputWildcardFilter;
        this.compressed = compressed;
        this.numThreads = numThreads;
        this.filesProcessed = 0;
        this.processedCorpora = new ArrayList<>();
        this.storeDocuments = storeDocuments;
        this.addAnnotationsWithoutIDs = addAnnotationsWithoutIDs;
    }

    public FileBatchExecutor(final String inputFolderPath, final String outputFolderPath,
                             final boolean compressed, final int numThreads, final boolean storeDocuments,
                             final boolean addAnnotationsWithoutIDs) {
        this(inputFolderPath, outputFolderPath, compressed, numThreads, null, storeDocuments, addAnnotationsWithoutIDs);
    }

    private static FileFilter newFileFilter(String wildcardFilter, boolean compressed) {
        List<String> wildcards = new ArrayList<>();

        if (StringUtils.isNotBlank(wildcardFilter)) {
            wildcards.add(wildcardFilter);
        }
        if (compressed) {
            wildcards.add("*.gz");
        }
        if (wildcards.isEmpty()) {
            wildcards.add("*");
        }

        return new AndFileFilter(new WildcardFileFilter(wildcards), HiddenFileFilter.VISIBLE);
    }

    @Override
    public void run(Class<? extends Processor> processorCls, Context context, Object... args) throws NejiException {
//        System.setProperty("file.encoding", "UTF-8");

        logger.info("Initializing context...");
        context.initialize();
        logger.info("Installing multi-threading support...");
        context.addMultiThreadingSupport(numThreads);

//        try {
//        logger.info("Starting thread pool with support for {} threads...", numThreads);
//            executor = Executors.newFixedThreadPool(numThreads, new ProcessorThreadFactory());

        StopWatch timer = new StopWatch();
        timer.start();

//            CorpusDirWalker walker = new CorpusDirWalker(processorCls, context,
//                    inputWildcardFilter, compressed, storeDocuments, args);
//
//        // Store processed corpora
//            walker.processFiles();
//
//            executor.shutdown();
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        filesProcessed = processFiles(inputFolderPath, inputWildcardFilter, outputFolderPath, numThreads, context, processorCls, args);

        logger.info("Stopped thread pool.");

        logger.info("Terminating context...");
        context.terminate();

        timer.stop();
        logger.info("Processed {} files in {}", filesProcessed, timer.toString());
//        } catch (IOException | InterruptedException ex) {
//            throw new NejiException("Problem processing pipeline.", ex);
//        }
    }

    @Override
    public Collection<Corpus> getProcessedCorpora() {
        return processedCorpora;
//        return null;
    }

    private int processFiles(final String inputFolderPath, final String inputWildcardFilter,
                              final String outputFolderPath, final int numThreads, Context context,
                              final Class<? extends Processor> processorCls, Object... args) {

        int filesProcessed = 0;
        File inputFolder = new File(inputFolderPath);
        FileFilter fileFilter = newFileFilter(inputWildcardFilter, compressed);
        File[] files = inputFolder.listFiles(fileFilter);

        logger.info("Starting thread pool with support for {} threads...", numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        LinkedList<Future> futures = new LinkedList<>();


        for (File file : files) {

            // Make corpus, output file
            Corpus corpus = new Corpus();

            // By default, the corpus identifier is the file name
            corpus.setIdentifier(FilenameUtils.getBaseName(file.getName()));


            // Make in/out corpus wrappers
            InputFile inputFile = new InputFile(corpus, file, compressed);
            List<OutputFile> outputFiles = new ArrayList<>();
            for(OutputFormat outputFormat : context.getConfiguration().getOutputFormats()){
                File outFile = OutputFile.newOutputFile(
                        outputFolderPath, FilenameUtils.getBaseName(FilenameUtils.getBaseName(file.getName())),
                        outputFormat, compressed);
                outputFiles.add(new OutputFile(corpus, outFile, compressed));
            }

            if (storeDocuments) {
                processedCorpora.add(corpus);
            }

            Processor processor;
            try {
                processor = newProcessor(processorCls, context, inputFile, outputFiles, addAnnotationsWithoutIDs, args);
            } catch (NejiException ex) {
                String m = "There was a problem creating the processor of the file: " + file.getAbsolutePath();
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

//    private class WildcardFileNameFilter implements FilenameFilter {
//
//        private Pattern pattern;
//
//        public WildcardFileNameFilter(final Pattern pattern) {
//            this.pattern = pattern;
//        }
//
//        @Override
//        public boolean accept(File dir, String name) {
//            return pattern.matcher(name).matches();
//        }
//    }
//
//    /**
//     * Walks the input corpus directory and processes files matching filters using a given pipeline processor.
//     */
//    private class CorpusDirWalker extends DirectoryWalker<Corpus> {
//
//        private final Class<Processor> processorCls;
//        private final Context context;
//        private Object[] args;
//        private boolean storeDocuments;
//
//        public CorpusDirWalker(Class<Processor> processorCls,
//                               Context context, String inputWildcardFilter, boolean compressed, boolean storeDocuments, Object... args) {
//            super(newFileFilter(inputWildcardFilter, compressed), 1);
//
//            this.processorCls = processorCls;
//            this.context = context;
//            this.storeDocuments = storeDocuments;
//            this.args = args;
//        }
//
////        /**
////         * Walks corpus directory and processes all matched files.
////         *
////         * @return collection of processed Corpus.
////         */
////        public Collection<Corpus> processFiles() throws IOException {
////            Collection<Corpus> processed = new ArrayList<Corpus>();
////
////            walk(new File(inputFolderPath), processed);
////
////            return processed;
////        }
//
//        public void processFiles() throws IOException {
//            walk(new File(inputFolderPath), null);
//        }
//
//        /**
//         * Log walked directory name.
//         */
//        @Override
//        protected boolean handleDirectory(File directory, int depth, Collection<Corpus> results) throws IOException {
//            logger.info("Walking \"{}\"", directory.getAbsolutePath());
//            return true;
//        }
//
//        /**
//         * Process file on pipeline.
//         */
//        @Override
//        protected void handleFile(File file, int depth, Collection<Corpus> results) throws IOException {
//            // Make corpus, output file
//            Corpus corpus = new Corpus();
//
//            // By default, the corpus identifier is the file name
//            corpus.setIdentifier(FilenameUtils.getBaseName(file.getName()));
//
//            File outFile = OutputFileFormat.newOutputFile(
//                    outputFolderPath, FilenameUtils.getBaseName(FilenameUtils.getBaseName(file.getName())),
//                    outputFormat, compressed);
//
//            // Make in/out corpus wrappers
//            InputFileFormat inputPile = new InputFileFormat(file, inputFormat, compressed, corpus);
//            OutputFileFormat outputPile = new OutputFileFormat(outFile, outputFormat, compressed, corpus);
//
//            try {
//                Processor processor = newProcessor(processorCls, context, inputPile, outputPile, args);
//
////                logger.info("Processing \"{}\"...", file.getAbsolutePath());
//                executor.execute(processor);
////                executor.submit(processor);
//
////                if (storeDocuments) {
////                    results.add(corpus);
////                }
//
//                filesProcessed += 1;
//            } catch (NejiException ex) {
//                logger.error("Error processing file \"" + file.getAbsolutePath() + "\"", ex);
//                throw new RuntimeException("Error processing file \"" + file.getAbsolutePath() + "\"", ex);
//            }
//        }
//    }
//
//    private class ProcessorThreadFactory implements ThreadFactory {
//        @Override
//        public Thread newThread(Runnable r) {
//            Thread t = new Thread(r);
//            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//                @Override
//                public void uncaughtException(Thread t, Throwable e) {
//                    LoggerFactory.getLogger(t.getName()).error(e.getMessage(), e);
//                }
//            });
//
//            return t;
//        }
//    }
}
