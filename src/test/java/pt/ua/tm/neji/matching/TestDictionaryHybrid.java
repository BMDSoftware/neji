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
package pt.ua.tm.neji.matching;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.DictionaryHybrid;
import pt.ua.tm.neji.dictionary.VariantMatcherLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.reader.RawReader;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.writer.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Testing class for DictionaryHybrid module, which matches terms and concepts from
 * the provided dictionaries in the specified sentence.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestDictionaryHybrid extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestDictionaryHybrid.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestDictionaryHybrid(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestDictionaryHybrid.class);
    }

    public void test() throws NejiException, IOException {
        Constants.verbose = true;
        String str = "Inhaled corticosteroids (ICS) are the most commonly used " +
                "controller medications prescribed for asthma.";
        String strDOUBLE = str + "\n\n\n\n" + str.substring(30);

        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), false);
        parser.launch();

        testCase1(str, parser);
        testCase2(str, parser);
        testCase3(str, parser);
        testCase4(str, parser);
        testCase5(str, parser);
        testCase6(str, parser);
        testCase7(str, parser);
        testCase8(strDOUBLE, parser);

        parser.close();
    }

    private void testCase1(String str, Parser parser) throws NejiException, IOException {
        //Test Case 1: one entry of a word in a dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = new DefaultPipeline().
                add(new RawReader()).
                add(new NLP(parser)).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications").
                        load("5:::PRGE\tcorticosteroids").
                        getMatcher(), "PRGE"))).
                add(new NejiWriter()).
                run(in).get(0);

//        logger.info("Test Case 1:\n{}", out.toString());
        assertEquals("S1\t   0  102\tInhaled corticosteroids (ICS) are the most" +
                " commonly used controller medications prescribed for asthma.\n" +
                "\tT1\t   8   23\tcorticosteroids\t5:::PRGE\n" +
                "\tT2\t  68   79\tmedications\t3:::PRGE", out.toString());
    }

    private void testCase2(String str, Parser parser) throws NejiException, IOException {
        //Test Case 2: two entries of a word in the same dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = new DefaultPipeline().
                add(new RawReader()).
                add(new NLP(parser)).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications|medication").
                        load("4:::PRGE\tmedications").
                        getMatcher(), "PRGE"))).
                add(new NejiWriter()).
                run(in).get(0);

//        logger.info("Test Case 2:\n{}", out.toString());
        assertEquals("S1\t   0  102\tInhaled corticosteroids (ICS) are the most " +
                "commonly used controller medications prescribed for asthma.\n" +
                "\tT1\t  68   79\tmedications\t4:::PRGE|3:::PRGE", out.toString());
    }

    private void testCase3(String str, Parser parser) throws NejiException, IOException {
        //Test Case 3: two different words in the same dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = new DefaultPipeline().
                add(new RawReader()).
                add(new NLP(parser)).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications").
                        load("7:::PRGE\tasthma").
                        getMatcher(), "PRGE"))).
                add(new NejiWriter()).
                run(in).get(0);

//        logger.info("Test Case 3:\n{}", out.toString());
        assertEquals("S1\t   0  102\tInhaled corticosteroids (ICS) are the most " +
                "commonly used controller medications prescribed for asthma.\n" +
                "\tT1\t  68   79\tmedications\t3:::PRGE\n" +
                "\tT2\t  95  101\tasthma\t7:::PRGE", out.toString());
    }

    private void testCase4(String str, Parser parser) throws NejiException, IOException {
        //Test Case 4: two words in different dictionaries

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = new DefaultPipeline().
                add(new RawReader()).
                add(new NLP(parser)).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications").
                        getMatcher(), "PRGE"))).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("7:::PRGE\tasthma").
                        getMatcher(), "PRGE"))).
                add(new NejiWriter()).
                run(in).get(0);

//        logger.info("Test Case 4:\n{}", out.toString());
        assertEquals("S1\t   0  102\tInhaled corticosteroids (ICS) are the most " +
                "commonly used controller medications prescribed for asthma.\n" +
                "\tT1\t  68   79\tmedications\t3:::PRGE\n" +
                "\tT2\t  95  101\tasthma\t7:::PRGE", out.toString());
    }

    private void testCase5(String str, Parser parser) throws NejiException, IOException {
        //Test Case 5: two different words from different dictionaries with two entries in each dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = new DefaultPipeline().
                add(new RawReader()).
                add(new NLP(parser)).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications|medication").
                        load("4:::PRGE\tmedications").
                        getMatcher(), "PRGE"))).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("7:::PRGE\tasthma").
                        load("8:::PRGE\tasthmastic|asthma").
                        getMatcher(), "PRGE"))).
                add(new NejiWriter()).
                run(in).get(0);

//        logger.info("Test Case 5:\n{}", out.toString());
        assertEquals("S1\t   0  102\tInhaled corticosteroids (ICS) are the most " +
                "commonly used controller medications prescribed for asthma.\n" +
                "\tT1\t  68   79\tmedications\t4:::PRGE|3:::PRGE\n" +
                "\tT2\t  95  101\tasthma\t7:::PRGE|8:::PRGE", out.toString());
    }

    private void testCase6(String str, Parser parser) throws NejiException, IOException {
        //Test Case 6: two nested words in a dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = new DefaultPipeline().
                add(new RawReader()).
                add(new NLP(parser)).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("3:::PRGE\tinhaled|inhaled corticosteroids").
                        getMatcher(), "PRGE"))).
                add(new NejiWriter()).
                run(in).get(0);

//        logger.info("Test Case 6:\n{}", out.toString());
        assertEquals("S1\t   0  102\tInhaled corticosteroids (ICS) are the most " +
                "commonly used controller medications prescribed for asthma.\n" +
                "\tT1\t   0   23\tInhaled corticosteroids\t3:::PRGE", out.toString());
    }

    private void testCase7(String str, Parser parser) throws NejiException, IOException {
        //Test Case 7: two intersected words in a dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = new DefaultPipeline().
                add(new RawReader()).
                add(new NLP(parser)).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("3:::PRGE\tcontroller medications").
                        load("4:::PRGE\tmedications prescribed").
                        getMatcher(), "PRGE"))).
                add(new NejiWriter()).
                run(in).get(0);

//        logger.info("Test Case 7:\n{}", out.toString());
        assertEquals("S1\t   0  102\tInhaled corticosteroids (ICS) are the most " +
                "commonly used controller medications prescribed for asthma.\n" +
                "\tT1\t  68   90\tmedications prescribed\t4:::PRGE", out.toString());
    }

    private void testCase8(String str, Parser parser) throws NejiException, IOException {
        //Test Case 8: two words in a dictionary for two sentences

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = new DefaultPipeline().
                add(new RawReader()).
                add(new NLP(parser)).
                add(new DictionaryHybrid(new Dictionary(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications").
                        load("5:::PRGE\tcorticosteroids").
                        getMatcher(), "PRGE"))).
                add(new NejiWriter()).
                run(in).get(0);

//        logger.info("Test Case 8:\n{}", out.toString());
        assertEquals("S1\t   0  102\tInhaled corticosteroids (ICS) are the most " +
                "commonly used controller medications prescribed for asthma.\n" +
                "\tT1\t   8   23\tcorticosteroids\t5:::PRGE\n" +
                "\tT2\t  68   79\tmedications\t3:::PRGE\n" +
                "\n" +
                "S2\t 106  178\tare the most commonly used controller medications " +
                "prescribed for asthma.\n" +
                "\tT1\t 144  155\tmedications\t3:::PRGE", out.toString());
    }
}
