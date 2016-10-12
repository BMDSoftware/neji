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

package pt.ua.tm.neji.annotation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.train.model.CRFBase;
import pt.ua.tm.neji.core.corpus.*;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.VariantMatcherLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.MLHybrid;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.reader.RawReader;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Testing class for MLHybrid module, which uses the Annotator class and performs
 * concept recognition, abbreviation, parentheses and normalization.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestMLHybrid extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestMLHybrid.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestMLHybrid(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestMLHybrid.class);
    }

    public void test() throws Exception {
        Constants.verbose = true;
        Corpus c1 = Variables.getCorpus3();
        String group = "PRGE";

        Pipeline p = new DefaultPipeline(c1)
            .add(new RawReader())
            .add(new NLP(new GDepParser(ParserLanguage.ENGLISH, ParserLevel.CHUNKING, new LingpipeSentenceSplitter(), false)));
        OutputStream out = p.run(new ByteArrayInputStream(Variables.str3.getBytes())).get(0);

        Corpus c2 = (Corpus)c1.clone();
        Corpus c3 = (Corpus)c1.clone();
        Corpus c4 = (Corpus)c1.clone();
        Corpus c5 = (Corpus)c1.clone();

        MLModel model = Variables.getModel();

        testCase1(out.toString(), c1, group, model.getCrf());
        testCase2(out.toString(), c2, group, model.getCrf());
        testCase3(out.toString(), c3, group, model.getCrf());
        testCase4(out.toString(), c4, group, model.getCrf());
        testCase5(out.toString(), c5, group, model.getCrf());
    }

    private void testCase1(final String str, final Corpus c, final String group, final CRFBase mbase) throws NejiException {
        // test case 1: one matcher with one corresponding entry
        // resulting annotation must have the id of the entry (simple matching)

        final List<Dictionary> dict = new ArrayList<>();
        dict.add(new Dictionary(new VariantMatcherLoader(true)
                .load("1:::PRGE\tNAT2")
                .getMatcher(), group));
        new DefaultPipeline(c){{
            add(new MLHybrid(mbase, group, dict, true));
            run(new ByteArrayInputStream(str.getBytes()));
        }};
        //c.getSentence(0).printTreeAnnotations();
        
        assertEquals("1:::PRGE", c.getSentence(0).getTreeAnnotations(1).get(0).getStringIDs());
    }
        
    private void testCase2(final String str, final Corpus c, final String group, final CRFBase mbase) throws NejiException {
        // test case 2: one matcher with two entries with equal terms but different IDs
        // resulting annotation must have the ids from both entries (multiple ID matching)

        final List<Dictionary> dict = new ArrayList<>();
        dict.add(new Dictionary(new VariantMatcherLoader(true)
                .load("1:::PRGE\tNAT2")
                .load("2:::PRGE\tNAT2")
                .getMatcher(), group));
        new DefaultPipeline(c){{
            add(new MLHybrid(mbase, group, dict, true));
            run(new ByteArrayInputStream(str.getBytes()));
        }};
        //c.getSentence(0).printTreeAnnotations();

        assertEquals("1:::PRGE|2:::PRGE", c.getSentence(0).getTreeAnnotations(1).get(0).getStringIDs());
    }

    private void testCase3(final String str, final Corpus c, final String group, final CRFBase mbase) throws NejiException {
        // test case 3: one matcher with two corresponding entries, the first being larger than the second
        // resulting tree must only have the id from the largest entry (largest span matching)

        final List<Dictionary> dict = new ArrayList<>();
        dict.add(new Dictionary(new VariantMatcherLoader(true)
                .load("1:::PRGE\tNAT2")
                .load("2:::PRGE\thuman NAT2 SNP")
                .getMatcher(), group));
        new DefaultPipeline(c){{
            add(new MLHybrid(mbase, group, dict, true));
            run(new ByteArrayInputStream(str.getBytes()));
        }};
        //c.getSentence(0).printTreeAnnotations();

        assertEquals("2:::PRGE", c.getSentence(0).getTreeAnnotations(1).get(0).getStringIDs());
    }

    private void testCase4(final String str, final Corpus c, final String group, final CRFBase mbase) throws NejiException {
        // test case 4: two matchers and only one corresponding entry in the second matcher
        // resulting tree must have the id from the second dictionary (prioritized matching)

        final List<Dictionary> dict = new ArrayList<>();
        dict.add(new Dictionary(new VariantMatcherLoader(true)
                .load("1:::PRGE\tNormalization must not match here")
                .getMatcher(), group));
        dict.add(new Dictionary(new VariantMatcherLoader(true)
                .load("2:::PRGE\tNAT2")
                .getMatcher(), group));
        new DefaultPipeline(c){{
            add(new MLHybrid(mbase, group, dict, true));
            run(new ByteArrayInputStream(str.getBytes()));
        }};
        //c.getSentence(0).printTreeAnnotations();

        assertEquals("2:::PRGE", c.getSentence(0).getTreeAnnotations(1).get(0).getStringIDs());
    }

    private void testCase5(final String str, final Corpus c, final String group, final CRFBase mbase) throws NejiException {
        // test case 5: one matcher with one entry larger than the original string itself
        // resulting annotation must not have any ids (non-matching)

        final List<Dictionary> dict = new ArrayList<>();
        dict.add(new Dictionary(new VariantMatcherLoader(true)
                .load("1:::PRGE\tconsistent member of human NAT2 SNP genotyping lambda transport")
                .getMatcher(), group));
        new DefaultPipeline(c){{
            add(new MLHybrid(mbase, group, dict, true));
            run(new ByteArrayInputStream(str.getBytes()));
        }};
        //c.getSentence(0).printTreeAnnotations();

        assertEquals(":::PRGE", c.getSentence(0).getTreeAnnotations(1).get(0).getStringIDs());
    }
}

