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
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.dictionary.DictionaryTagger;
import pt.ua.tm.neji.dictionary.VariantMatcherLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Testing class for DictionaryTagger module, which matches terms and concepts from
 * the provided dictionaries and tags these matches in the output text.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestDictionaryTagger extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestDictionaryTagger.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestDictionaryTagger(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestDictionaryTagger.class);
    }

    public void test() throws NejiException, IOException {
        Pipeline p[] = new Pipeline[7];
        String str =
                "Inhaled corticosteroids (ICS) are the most commonly used " +
                        "controller medications prescribed for asthma.";
        String strXML = "<s id=\"test1\">" + str + "</s>";

        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), false)
                .launch();

        int pLength = p.length;
        for(int i = 0; i<pLength; i++) {
            Corpus corpus = new Corpus();
            parser.parse(corpus, str);
            p[i] = new DefaultPipeline(corpus).clear();
        }
        parser.close();

        testCase1(p[0], strXML);
        testCase2(p[1], strXML);
        testCase3(p[2], strXML);
        testCase4(p[3], strXML);
        testCase5(p[4], strXML);
        testCase6(p[5], strXML);
        testCase7(p[6], strXML);
    }

    private void testCase1(Pipeline p, String str) throws NejiException, IOException {
        //Test Case 1: one entry of a word in a dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = p.
                add(new DictionaryTagger(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications").
                        getMatcher())).
                run(in).get(0);

//        logger.info("Test Case 1:\n{}", out.toString());
        assertEquals("<s id=\"test1\">Inhaled corticosteroids (ICS) are the most " +
                "commonly used controller <e id=\"3:::PRGE\">medications</e> " +
                "prescribed for asthma.</s>", out.toString());
    }

    private void testCase2(Pipeline p, String str) throws NejiException, IOException {
        //Test Case 2: two entries of a word in the same dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = p.
                add(new DictionaryTagger(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications|medication").
                        load("4:::PRGE\tmedications").
                        getMatcher())).
                run(in).get(0);

//        logger.info("Test Case 2:\n{}", out.toString());
        assertEquals("<s id=\"test1\">Inhaled corticosteroids (ICS) are the most " +
                "commonly used controller <e id=\"4:::PRGE|3:::PRGE\">medications</e> " +
                "prescribed for asthma.</s>", out.toString());
    }

    private void testCase3(Pipeline p, String str) throws NejiException, IOException {
        //Test Case 3: two different words in the same dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = p.
                add(new DictionaryTagger(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications").
                        load("7:::PRGE\tasthma").
                        getMatcher())).
                run(in).get(0);

//        logger.info("Test Case 3:\n{}", out.toString());
        assertEquals("<s id=\"test1\">Inhaled corticosteroids (ICS) are the most " +
                "commonly used controller <e id=\"3:::PRGE\">medications</e> " +
                "prescribed for <e id=\"7:::PRGE\">asthma</e>.</s>", out.toString());
    }

    private void testCase4(Pipeline p, String str) throws NejiException, IOException {
        //Test Case 4: two words in different dictionaries

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = p.
                add(new DictionaryTagger(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications").
                        getMatcher())).
                add(new DictionaryTagger(new VariantMatcherLoader(true).
                        load("7:::PRGE\tasthma").
                        getMatcher())).
                run(in).get(0);

//        logger.info("Test Case 4:\n{}", out.toString());
        assertEquals("<s id=\"test1\">Inhaled corticosteroids (ICS) are the most " +
                "commonly used controller <e id=\"3:::PRGE\">medications</e> " +
                "prescribed for <e id=\"7:::PRGE\">asthma</e>.</s>", out.toString());
    }

    private void testCase5(Pipeline p, String str) throws NejiException, IOException {
        //Test Case 5: two different words from different dictionaries with two entries in each dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = p.
                add(new DictionaryTagger(new VariantMatcherLoader(true).
                        load("3:::PRGE\tmedications|medication").
                        load("4:::PRGE\tmedications").
                        getMatcher())).
                add(new DictionaryTagger(new VariantMatcherLoader(true).
                        load("7:::PRGE\tasthma").
                        load("8:::PRGE\tasthmastic|asthma").
                        getMatcher())).
                run(in).get(0);

//        logger.info("Test Case 5:\n{}", out.toString());
        assertEquals("<s id=\"test1\">Inhaled corticosteroids (ICS) are the most " +
                "commonly used controller <e id=\"4:::PRGE|3:::PRGE\">medications</e> " +
                "prescribed for <e id=\"7:::PRGE|8:::PRGE\">asthma</e>.</s>", out.toString());
    }

    private void testCase6(Pipeline p, String str) throws NejiException, IOException {
        //Test Case 6: two nested words in a dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = p.
                add(new DictionaryTagger(new VariantMatcherLoader(true).
                        load("3:::PRGE\tinhaled|inhaled corticosteroids").
                        getMatcher())).
                run(in).get(0);

//        logger.info("Test Case 6:\n{}", out.toString());
        assertEquals("<s id=\"test1\"><e id=\"3:::PRGE\">Inhaled corticosteroids</e> (ICS) are the most " +
                "commonly used controller medications prescribed for asthma.</s>", out.toString());
    }

    private void testCase7(Pipeline p, String str) throws NejiException, IOException {
        //Test Case 7: two intersected words in a dictionary

        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        OutputStream out = p.
                add(new DictionaryTagger(new VariantMatcherLoader(true).
                        load("3:::PRGE\tcontroller medications").
                        load("4:::PRGE\tmedications prescribed").
                        getMatcher())).
                run(in).get(0);

//        logger.info("Test Case 7:\n{}", out.toString());
        assertEquals("<s id=\"test1\">Inhaled corticosteroids (ICS) are the most " +
                "commonly used controller <e id=\"4:::PRGE\">medications " +
                "prescribed</e> for asthma.</s>", out.toString());
    }
}
