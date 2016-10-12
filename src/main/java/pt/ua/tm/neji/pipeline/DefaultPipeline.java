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

package pt.ua.tm.neji.pipeline;

import monq.jfa.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.module.*;
import pt.ua.tm.neji.core.pipeline.BasePipeline;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of a pipeline. The input stream is the input of the first module, and
 * the output of the first module is the input of the second module and so on, until the last
 * module provides the output to a storage resource specified by the user.
 *
 * This class extends from {@link BasePipeline}, which is represented by a {@link Reader} attribute,
 * a list of processing {@link Module} and a list of {@link Writer}.
 *
 * When using the {@link Pipeline#add(Module, Module...)} method, if the specified {@link Module} is a
 * {@link Reader}, it will be pointed to the reader attribute, losing any other readers that were
 * added before. If the specified {@link Module} is a {@link Writer}, it will be added to the writers
 * list. If the specified {@link Module} is not a {@link Reader} nor a {@link Writer}, it will be
 * added to the processing modules list.
 *
 * Take notice that using the {@link Module#setPipeline(Pipeline)} method, as
 * observed in the following code:
 * <pre>
 *     Pipeline pipeline = new DefaultPipeline();
 *     Module module = new A1Writer()
 *     module.setPipeline(pipeline);
 * </pre>
 * is NOT enough to make sure the pipeline adds the initialized module. Instead, the user must
 * ONLY use the {@link Pipeline#add(Module, Module...)} method, as observed in the following code:
 * <pre>
 *     Pipeline pipeline = new DefaultPipeline();
 *     Module module = new A1Writer();
 *     pipeline.add(module);
 * </pre>
 * This is because {@link Pipeline#add(Module, Module...)} will place the specified module in the
 * correct structure and implicitly use the {@link Module#setPipeline(Pipeline)} method afterwards.
 *
 * In addition, while the order of the added writers will only affect the order of the
 * {@link OutputStream} list from the {@link Pipeline#run(InputStream)} method, the order
 * of the added processing modules will largely affect the run process.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 3.0
 * @since 1.0
 */
public class DefaultPipeline extends BasePipeline {

    private static Logger logger = LoggerFactory.getLogger(DefaultPipeline.class);

    public DefaultPipeline() {
        super();
        setValidator(new DefaultPipelineValidator(this));
    }

    public DefaultPipeline(Corpus corpus) {
        super(corpus);
        setValidator(new DefaultPipelineValidator(this));
    }

    /**
     * Run method to process the specified {@link InputStream} using the added modules and
     * to return a list of {@link ByteArrayOutputStream} with the processed modules output
     * from different writers.
     *
     * The order of the added writers will only affect the order of the {@link OutputStream}
     * list. If there are no writers, the list will have one stream with the last processing
     * module's output. If there is only one writer module, the list will have one stream
     * with that writer's output.
     *
     * In addition, this method will perform an initial check to the modules required
     * and provided resources, suggested by Java annotations in each module class.
     * If a specific module is defined to require a resource that was not provided by
     * earlier modules in the pipeline, an exception is thrown.
     *
     * @param input the {@link InputStream} to be processed using the added modules
     * @return a list of {@link OutputStream}, whose amount of entries varies as follows:
     * <p>- if no writers were added, returns a single entry with the output from the previous
     * processing module;</p>
     * <p>- if only one Writer was added, returns a single entry with the output from said
     * writer;</p>
     * <p>- if two or more Writers were added, returns multiple outputs from said writers,
     * meaning that the number of entries is equal to the number of added writers.</p>
     * @throws NejiException if there was a problem with the run process or if the
     *                       initial validation process failed
     */
    @Override
    protected List<OutputStream> run_(InputStream input) throws NejiException {
        List<OutputStream> outList = new ArrayList<>();
        int i = 0;
        do{
            outList.add(new ByteArrayOutputStream());
            i++;
        }while(i<writerList.size());

        run_(input, outList);
        return outList;
    }

    /**
     * Run method to process the specified {@link InputStream} using the added
     * modules and to write the resultant output in the specified {@link OutputStream}
     * list.
     *
     * Since both the list of added writers and the specified {@link OutputStream} list
     * are used in sequential order, the order of the added writers and the order of
     * the specified {@link OutputStream} list will affect which stream will contain
     * which writer's output.
     *
     * In addition, this method will perform an initial check to the modules required
     * and provided resources, suggested by Java annotations {@link Provides} and
     * {@link Requires} in each module class.
     * If a specific module is defined to require a resource that was not provided by
     * earlier modules in the pipeline, an exception is thrown.
     *
     * @param input the {@link InputStream} to be processed using the added modules
     * @param outputList the list of {@link OutputStream} where the resultant output
     *                   will be written
     * @throws NejiException if there was a problem with the run process or if the
     *                       initial validation process failed
     */
    @Override
    protected void run_(final InputStream input, final List<OutputStream> outputList) throws NejiException {
        if (outputList == null || outputList.isEmpty()) {
            logger.error("The specified OutputStream list is null or is empty! " +
                    "Please specify a list with at least one OutputStream.", writerList.size());
            return;
        }
        try {
            final Nfa nfa = new Nfa(Nfa.NOTHING);
            DfaRun previous = nfa.compile(DfaRun.UNMATCHED_COPY).createRun();
            DfaRun inside;

            // sets in the specified InputStream
            ReaderCharSource cs = new ReaderCharSource(input, "UTF-8");
            previous.setIn(cs);

            // Run Reader Module
            if (reader != null) {
                reader.compile();
                inside = reader.getRun();
                inside.setIn(previous);
                previous = inside;
            }

            // Run Processing Modules
            for (Module a : processingList) {
                a.compile();
                inside = a.getRun();
                inside.setIn(previous);
                previous = inside;
            }

            // If no Writers were added, simply return the output from the previous module.
            if (writerList.size() == 0) {
                filterOutput(outputList.get(0), previous);
            }

            // If only one Writer was added, run it with the rest of the processing units
            else if (writerList.size() == 1) {
                Writer w = writerList.get(0);
                w.compile();
                inside = w.getRun();
                inside.setIn(previous);
                previous = inside;
                filterOutput(outputList.get(0), previous);
            }

            // If two or more Writers were added, create multiple clones of the processed
            // output, where each clone is processed individually by a different Writer from
            // the writerList
            else {
                StringBuffer sb = new StringBuffer();
                previous.filter(sb);

                int i = 0;
                for (final Writer w : writerList) {
                    w.compile();
                    if (i < outputList.size()) {

                        // converts the last module's output into a new DfaRun
                        DfaRun newDfa = nfa.compile(DfaRun.UNMATCHED_COPY).createRun();
                        newDfa.setIn(new CharSequenceCharSource(sb.toString()));

                        // processes the Writer "w" from writerList in the new DfaRun
                        inside = w.getRun();
                        inside.setIn(newDfa);
                        newDfa = inside;

                        // writes to an outputStream in the provided list the output of the new DfaRun
                        OutputStream newOut = outputList.get(i);
                        filterOutput(newOut, newDfa);
                        i++;
                    } else {
                        // if there are more writers than available OutputStreams
                        logger.warn("Only {} of {} writers were processed because the number " +
                                "of OutputStreams in the specified list is lower than the number of " +
                                "added Writers!", i, writerList.size());
                        break;
                    }
                }
            }

            input.close();
        } catch (IOException | CompileDfaException ex) {
            throw new NejiException(ex);
        }
    }

    private void filterOutput(OutputStream out, DfaRun previous) throws IOException {
        PrintStream ps = new PrintStream(out, false, "UTF-8");
        previous.filter(ps);
        ps.close();
        out.close();
    }


    // ------ Access methods for DefaultPipelineValidator ------

    final Reader getReader() {
        return reader;
    }

    final List<Module> getProcessingList() {
        return processingList;
    }

    final List<Writer> getWriterList() {
        return writerList;
    }

}
