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

package pt.ua.tm.neji.parsing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.exception.NejiException;

import java.io.IOException;
import java.util.*;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 * @since 1.0
 */
public class TestDynamicParsing extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestDynamicParsing.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestDynamicParsing(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestDynamicParsing.class);
    }

    public void test() throws IOException, NejiException {
        Constants.verbose = true;
        StopWatch timer = new StopWatch();

        // create corpus
        Corpus corpus = new Corpus();
        corpus.setText(Variables.str1.gdep.text);
        
        // readies the parser
        Variables.parseWithDepParser(ParserLevel.TOKENIZATION, corpus, Variables.str1.gdep.text);

        // test if only tokenization was performed, no dependency features, lemmas, pos or chunks should exist
        timer.start();
        List<Sentence> sentences1 = Variables.parseWithDepParser(ParserLevel.TOKENIZATION, corpus, Variables.str1.gdep.text);
        timer.stop();
        logger.info("{}", sentences1.get(0).toExportFormat());
        logger.info("Tokenization took {}", timer.toString());
        timer.reset();

        // test if only lemmatization was performed, no dependency features, pos or chunks should exist
        timer.start();
        List<Sentence> sentences2 = Variables.parseWithDepParser(ParserLevel.LEMMATIZATION, corpus, Variables.str1.gdep.text);
        timer.stop();
        logger.info("{}", sentences2.get(0).toExportFormat());
        logger.info("Lemmatization took {}", timer.toString());
        timer.reset();


        // test if only pos was performed, no dependency features nor chunks should exist
        timer.start();
        List<Sentence> sentences3 = Variables.parseWithDepParser(ParserLevel.POS, corpus, Variables.str1.gdep.text);
        timer.stop();
        logger.info("{}", sentences3.get(0).toExportFormat());
        logger.info("POS took {}", timer.toString());
        timer.reset();


        // test if only chunking was performed, no dependency features should exist
        timer.start();
        List<Sentence> sentences4 = Variables.parseWithDepParser(ParserLevel.CHUNKING, corpus, Variables.str1.gdep.text);
        timer.stop();
        logger.info("{}", sentences4.get(0).toExportFormat());
        logger.info("Chunking took {}", timer.toString());
        timer.reset();


        // test if dependency parsing was performed
        timer.start();
        List<Sentence> sentences5 = Variables.parseWithDepParser(ParserLevel.DEPENDENCY, corpus, Variables.str1.gdep.text);
        timer.stop();
        logger.info("{}", sentences5.get(0).toExportFormat());
        logger.info("Dependency took {}", timer.toString());
        timer.reset();

    }
}
