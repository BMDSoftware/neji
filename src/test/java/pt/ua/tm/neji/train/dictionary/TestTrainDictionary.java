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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.dictionary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static junit.framework.Assert.assertEquals;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.VariantMatcherLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.train.nlp.TrainNLP;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase1;
import pt.ua.tm.neji.train.reader.A1Reader;

/**
 *
 * @author jeronimo
 */
public class TestTrainDictionary extends TestCase {
    
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestTrainDictionary.class);
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestTrainDictionary(String testName){
        super(testName);
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestTrainDictionary.class);
    }
    
    public void test() throws NejiException, IOException {
        Constants.verbose = true;
        String str1Txt = "Expression of the Bcl-3 proto-oncogene suppresses p53 activation. \n";
        String str1A1 = "";

        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), false);
        parser.launch();

        testCase1(str1Txt, str1A1, parser);
        testCase2(str1Txt, str1A1, parser);
        testCase3(str1Txt, str1A1, parser);

        parser.close();
    }
    
    private void testCase1(String strTxt, String strA1, Parser parser) throws NejiException, IOException {
        //Test Case 1: two different words in the same dictionary

        Pipeline p = new TrainPipelinePhase1();
        
        InputStream in = new ByteArrayInputStream(strTxt.getBytes("UTF-8"));
        InputStream an = new ByteArrayInputStream(strA1.getBytes("UTF-8"));
        OutputStream out = p.
                add(new A1Reader(parser, null, an)).
                add(new TrainNLP(parser)).
                add(new TrainDictionary(new Dictionary(new VariantMatcherLoader(true).
                        load("1:::PRGE\texpression").
                        load("2:::PRGE\tsuppresses").
                        getMatcher(), "DICTIONARY1"))).
                run(in).get(0);
        
        // Verify if dicionary features have been added to the two words in the dictionary and not in the others
        assertEquals(p.getCorpus().getSentence(0).getToken(0).featuresToString(), "DICTIONARY1=B");
        assertEquals(p.getCorpus().getSentence(0).getToken(9).featuresToString(), "DICTIONARY1=B");
        for (int i=1 ; i<=12 && i!=9 ; i++) {
            assertEquals(p.getCorpus().getSentence(0).getToken(i).featuresToString(), "");
        }
    }
    
    private void testCase2(String strTxt, String strA1, Parser parser) throws NejiException, IOException {
        //Test Case 2: same word in different dictionaries

        Pipeline p = new TrainPipelinePhase1();
        
        InputStream in = new ByteArrayInputStream(strTxt.getBytes("UTF-8"));
        InputStream an = new ByteArrayInputStream(strA1.getBytes("UTF-8"));
        OutputStream out = p.
                add(new A1Reader(parser, null, an)).
                add(new TrainNLP(parser)).
                add(new TrainDictionary(new Dictionary(new VariantMatcherLoader(true).
                        load("1:::PRGE\texpression").
                        getMatcher(), "DICTIONARY1"))).
                add(new TrainDictionary(new Dictionary(new VariantMatcherLoader(true).
                        load("2:::PRGE\texpression").
                        getMatcher(), "DICTIONARY2"))).
                run(in).get(0);
                
        // Verify if dicionary features have been added to the two words in the dictionary and not in the others
        assertEquals(p.getCorpus().getSentence(0).getToken(0).featuresToString(), "DICTIONARY2=B\tDICTIONARY1=B");
        for (int i=1 ; i<p.getCorpus().getSentence(0).size() ; i++) {
            assertEquals(p.getCorpus().getSentence(0).getToken(i).featuresToString(), "");
        }
    }
    
    private void testCase3(String strTxt, String strA1, Parser parser) throws NejiException, IOException {
        //Test Case 3: one expression with more than 1 word in the dictionary

        Pipeline p = new TrainPipelinePhase1();
        
        InputStream in = new ByteArrayInputStream(strTxt.getBytes("UTF-8"));
        InputStream an = new ByteArrayInputStream(strA1.getBytes("UTF-8"));
        OutputStream out = p.
                add(new A1Reader(parser, null, an)).
                add(new TrainNLP(parser)).
                add(new TrainDictionary(new Dictionary(new VariantMatcherLoader(true).
                        load("1:::PRGE\texpression of the").
                        getMatcher(), "DICTIONARY3"))).
                run(in).get(0);
        
        // Verify if dicionary features have been added to the two words in the dictionary and not in the others
        assertEquals(p.getCorpus().getSentence(0).getToken(0).featuresToString(), "DICTIONARY3=B");
        assertEquals(p.getCorpus().getSentence(0).getToken(1).featuresToString(), "DICTIONARY3=I");
        assertEquals(p.getCorpus().getSentence(0).getToken(2).featuresToString(), "DICTIONARY3=I");
        for (int i=3 ; i<p.getCorpus().getSentence(0).size() ; i++) {
            assertEquals(p.getCorpus().getSentence(0).getToken(i).featuresToString(), "");
        }
    }
}
