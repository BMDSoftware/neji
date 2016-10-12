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

package pt.ua.tm.neji.processor;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextProcessors;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.core.processor.BaseProcessor;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.processor.filewrappers.InputFile;
import pt.ua.tm.neji.processor.filewrappers.OutputFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract pipeline processor encapsulating common functionality.
 *
 * @author Tiago Nunes
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class FileProcessor extends BaseProcessor {

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(FileProcessor.class);

    private InputFile inputFile;
    private OutputFileList outputFileList;
    private String[] xmlTags;
    private final boolean addAnnotationsWithoutIDs;

    public FileProcessor(Context context, InputFile inputFile, boolean addAnnotationsWithoutIDs){
        super(context);
        setInputFile(inputFile);
        this.outputFileList = new OutputFileList();
        this.xmlTags = null;
        this.addAnnotationsWithoutIDs = addAnnotationsWithoutIDs;
    }

    public FileProcessor(Context context, InputFile inputFile, List<OutputFile> outputFiles, boolean addAnnotationsWithoutIDs) {
        this(context, inputFile, addAnnotationsWithoutIDs);
        this.outputFileList.addAll(outputFiles);
    }

    public FileProcessor(Context context, InputFile inputFile, List<OutputFile> outputFiles, boolean addAnnotationsWithoutIDs, String... xmlTags) {
        this(context, inputFile, outputFiles, addAnnotationsWithoutIDs);
        this.xmlTags = xmlTags;
    }

    @Override
    public void run() {

        // Get context and processors
        Context context = getContext();
        ContextProcessors cp;
        try {
            cp = context.take();
        } catch (InterruptedException ex) {
            String m = "There was a problem getting the context processors. Stream with the identifier "
                    + getInputFile().getCorpus().getIdentifier();
            throw new RuntimeException(m, ex);
        }


        try {
            // Get corpus
            Corpus corpus = getInputFile().getCorpus();

            // Create Pipeline
            Pipeline p = new DefaultPipeline(corpus);
            instantiateModules(context.getDictionaries(), context.getModels(), cp, p, xmlTags, addAnnotationsWithoutIDs);

            if (!context.getConfiguration().getOutputFormats().isEmpty()) {
//                p.add(new TextReplacer("&lt;§", "&lt;s"));
//                p.add(new TextReplacer("&lt;/§&gt;", "&lt;/s&gt;"));

                p.run(getInputFile().getInStream(), getOutputFiles().getOutputStreamList());

            } else {
//                p.add(new TextReplacer("&lt;§", "&lt;s"));
//                p.add(new TextReplacer("&lt;/§&gt;", "&lt;/s&gt;"));

                p.run(getInputFile().getInStream());
                logger.warn("Discarding processed output for file.");
            }

            // Return processors
            context.put(cp);
            p.clear();

        } catch (Exception ex) {

            // Return context processors to context
            try {
                context.put(cp);
            } catch (InterruptedException e) {
                String m = "There was a problem returning the context processors. Stream with the identifier "
                        + getInputFile().getCorpus().getIdentifier();
                throw new RuntimeException(m, ex);
            }

            String m = "There was a problem annotating the stream with the identifier " + getInputFile().getCorpus().getIdentifier();
            throw new RuntimeException(m, ex);
        }

        logger.info("Done processing: {}", getInputFile().getCorpus().getIdentifier());
    }

    public final InputFile getInputFile() {
        return inputFile;
    }

    public final OutputFileList getOutputFiles() {
        return outputFileList;
    }

    public final void setInputFile(InputFile inputPile) {
        Validate.notNull(inputPile);
        this.inputFile = inputPile;
    }

    protected class OutputFileList extends ArrayList<OutputFile> {
        
        public OutputFileList() {
            super();
        }

        public List<OutputStream> getOutputStreamList() throws IOException {
            List<OutputStream> outputStreamList = new ArrayList<>();
            for(OutputFile off : this) {
                outputStreamList.add(off.getOutStream());
            }
            return outputStreamList;
        }

//        public boolean hasOutputFormat(OutputFormat format) {
//            for(OutputFile off : this) {
//                if(off.getFormat().equals(format)){
//                    return true;
//                }
//            }
//            return false;
//        }
    }

}
