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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextProcessors;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.load.A1Loader;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.processor.FileProcessor;
import pt.ua.tm.neji.processor.filewrappers.InputFile;
import pt.ua.tm.neji.reader.RawReader;
import pt.ua.tm.neji.sentence.SentenceTagger;
import pt.ua.tm.neji.statistics.StatisticsCollector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class DocumentProcessor extends FileProcessor {

    private static Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
    private InputFile a1Corpus;

    public DocumentProcessor(Context context, InputFile textCorpus, InputFile a1Corpus, boolean includeAnnotationsWithoutIDs) {
        super(context, textCorpus, includeAnnotationsWithoutIDs);
        this.a1Corpus = a1Corpus;
    }

    @Override
    public void run() {
        try {
            // Get processors
            ContextProcessors processors = getContext().take();
            Corpus corpus = getInputFile().getCorpus();
//            List<Pair> sentencesPositions = new ArrayList<>();

            Pipeline p = new DefaultPipeline(corpus);

            p.add(new RawReader());
            p.add(new SentenceTagger(processors.getSentenceSplitter()));
            p.add(new NLP(processors.getParser()));

//            p.add(new A2Loader(corpus, a1Corpus, a2Corpus));

            Map<String, Pattern> aggregate = new HashMap<>();
            aggregate.put("PRGE", Pattern.compile("(EntrezGene|PR)"));
            aggregate.put("PROC_FUNC", Pattern.compile("(GO_MF|GO_BP)"));

            aggregate.put("CELL", Pattern.compile("(CL)"));
            aggregate.put("COMP", Pattern.compile("(GO_CC)"));
            aggregate.put("CHED", Pattern.compile("(CHEBI)"));
            aggregate.put("SPEC", Pattern.compile("(NCBITaxon)"));

            p.add(new A1Loader(a1Corpus,
                    Pattern.compile("(sup|sub|italic|bold|underline|independent_continuant|taxonomic_rank|SO)"),
                    aggregate));

            // Change to proper writer
//            p.add(new CoNLLCustomWriter(corpus));
//            p.add(new LabelWriter(corpus));

            p.add(new StatisticsCollector());

            // Run processing pipeline
            p.run(getInputFile().getInStream(), getOutputFiles().getOutputStreamList());

            // Return processors
            getContext().put(processors);
        } catch (NejiException | InterruptedException | IOException e) {
            logger.error("ERROR:", e);
            return;
        }


    }
}
