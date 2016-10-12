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

package pt.ua.tm.neji.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.batch.FileBatchExecutor;
import pt.ua.tm.neji.context.*;
import pt.ua.tm.neji.core.batch.BatchExecutor;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.DictionaryHybrid;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.MLHybrid;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.processor.FileProcessor;
import pt.ua.tm.neji.processor.filewrappers.InputFile;
import pt.ua.tm.neji.processor.filewrappers.OutputFile;
import pt.ua.tm.neji.reader.XMLReader;
import pt.ua.tm.neji.sentence.SentenceTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test a pipeline processor.
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class TestProcessor extends FileProcessor {
    /** {@link org.slf4j.Logger} to be used in the class. */
    private static Logger logger = LoggerFactory.getLogger(TestProcessor.class);

    public TestProcessor(Context context, InputFile input, List<OutputFile> outputFileList, boolean annotationsWithoutIds) {
        super(context, input, outputFileList, annotationsWithoutIds);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's <code>run</code> method to be called in that separately executing thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may execute any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            Context context = getContext();
            ContextProcessors cp = context.take(); // Take parser, sentence splitter and CRF
            Corpus corpus = getInputFile().getCorpus(); // Create corpus to store data
            DefaultPipeline p = new DefaultPipeline(corpus); // Create pipeline
            p.add(new XMLReader(new String[]{"ArticleTitle", "AbstractText"})); // Reader
            p.add(new SentenceTagger(cp.getSentenceSplitter())); // Sentence tagger
            p.add(new NLP(cp.getParser())); // NLP
            for (Dictionary d : context.getDictionaries()) { // Dictionary matching
                p.add(new DictionaryHybrid(d));
            }
            for (MLModel model : context.getModels()) { // Machine learning
                p.add(new MLHybrid(model, cp.getCRF(model.getModelName()), false));
            }
            for (OutputFormat f : context.getConfiguration().getOutputFormats()) {
                p.add(f.instantiateDefaultWriter());
            }
            p.run(getInputFile().getInStream(), getOutputFiles().getOutputStreamList()); //Run pipeline
            context.put(cp); // Return parser, sentence splitter and CRF
        } catch (NejiException | InterruptedException | IOException ex) {
            throw new RuntimeException("There was a problem running the pipeline.", ex);
        }
    }

    public static void main(String... args) throws NejiException {

        // Input and output resources
        String inputFolder = "input/";
        String outputFolder = "output/";
        String dictionariesFolder = "resources/dictionaries/";
        String modelsFolder = "resources/models/";

        // required Input format
        InputFormat inputFormat = InputFormat.XML;

        // required Output formats
        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.add(OutputFormat.XML);

        // Create context
        Context context = new Context(new ContextConfiguration.Builder()
                .withInputFormat(inputFormat)
                .withOutputFormats(outputFormats)
                .build(), modelsFolder, dictionariesFolder, null);

        // Run batch
        try {
            boolean areFilesCompressed = true;
            int numThreads = 6;
            BatchExecutor batchExecutor = new FileBatchExecutor(inputFolder, outputFolder,
                    areFilesCompressed, numThreads, false, false);
            Class p = TestProcessor.class;
            batchExecutor.run(p, context);
        } catch (Exception ex) {
            logger.error("There was a problem processing the files.", ex);
        }

    }
}
