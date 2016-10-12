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

package pt.ua.tm.neji.evaluation.craft.statistics;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.train.util.FileUtil;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.core.batch.BatchExecutor;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.processor.Processor;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.processor.filewrappers.InputFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Batch pipeline processors executor for a folder of documents, with support for concurrent
 * execution of multiple pipeline processors.
 *
 * @author David Campos
 * @version 1.0
 * @since 1.0
 */
public class FolderBatchExecutor extends BatchExecutor {

    private static Logger logger = LoggerFactory.getLogger(FolderBatchExecutor.class);
    private Collection<Corpus> processedCorpora;
    private String inputFolderPath;
    private int numThreads;

    public FolderBatchExecutor(final String inputFolderPath, final int numThreads) {
        this.inputFolderPath = inputFolderPath;
        this.numThreads = numThreads;
        this.processedCorpora = new ArrayList<>();
    }

    @Override
    public void run(Class<? extends Processor> processorClass, Context context, Object... objects) throws NejiException {
        throw new NotImplementedException("Not implemented. Use void run(Context context) instead.");
    }

    public void run(final Context context) throws NejiException {
        logger.info("Initializing context...");
        context.initialize();
        logger.info("Installing multi-threading support...");
        context.addMultiThreadingSupport(numThreads);

        ExecutorService executor;

        logger.info("Starting thread pool with support for {} threads...", numThreads);
        executor = Executors.newFixedThreadPool(numThreads);

        StopWatch timer = new StopWatch();
        timer.start();

        File inputFolder = new File(inputFolderPath);
        File[] files = inputFolder.listFiles(new FileUtil.Filter(new String[]{"txt"}));

        for (File file : files) {
//            File a1File = new File(file.getAbsolutePath().replaceAll(".txt", ".ann"));
            File a1File = new File(file.getAbsolutePath().replaceAll(".txt", ".a1"));
            Processor processor = getDocumentProcessor(file, a1File, context);

            // Process entry
            executor.execute(processor);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Stopped thread pool.");

        logger.info("Terminating context...");
        context.terminate();

        timer.stop();
        logger.info("Processed {} files in {}", processedCorpora.size(), timer.toString());
    }

    @Override
    public Collection<Corpus> getProcessedCorpora() {
        return processedCorpora;
    }

    private Processor getDocumentProcessor(final File inputTextFile, final File a1File, Context context) {
        Processor processor = null;

        InputFile rawFormat = new InputFile(new Corpus(), inputTextFile, false);
        InputFile a1Format = new InputFile(new Corpus(), a1File, false);

        processedCorpora.add(rawFormat.getCorpus());

        processor = new DocumentProcessor(context, rawFormat, a1Format, false);

        return processor;
    }
}
