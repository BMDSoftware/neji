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
package pt.ua.tm.neji.train.reading;

import com.aliasi.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants.LabelTag;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase1;
import pt.ua.tm.neji.train.reader.JNLPBAReader;

/**
 *
 * @author jeronimo
 */
public class TestJNLPBAReader extends TestCase {
    
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestJNLPBAReader.class);
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestJNLPBAReader(String testName){
        super(testName);
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestJNLPBAReader.class);
    }
    
    public void test() throws IOException, NejiException {
        
        Pipeline p = new TrainPipelinePhase1();
        
        String str1Sentences = "IL-2	B-DNA\n" +
                               "gene	I-DNA\n" +
                               "expression	O\n" +
                               "and	O\n" +
                               "NF-kappa	B-protein\n" +
                               "B	I-protein\n" +
                               "activation	O\n" +
                               "through	O\n" +
                               "CD28	B-protein\n" +
                               "requires	O\n" +
                               "reactive	O\n" +
                               "oxygen	O\n" +
                               "production	O\n" +
                               "by	O\n" +
                               "5-lipoxygenase	B-protein\n" +
                               ".	O\n" +
                               "\n" +
                               "Activation	O\n" +
                               "of	O\n" +
                               "the	O\n" +
                               "CD28	B-protein\n" +
                               "surface	I-protein\n" +
                               "receptor	I-protein\n" +
                               "provides	O\n" +
                               "a	O\n" +
                               "major	O\n" +
                               "costimulatory	O\n" +
                               "signal	O\n" +
                               "for	O\n" +
                               "T	O\n" +
                               "cell	O\n" +
                               "activation	O\n" +
                               "resulting	O\n" +
                               "in	O\n" +
                               "enhanced	O\n" +
                               "production	O\n" +
                               "of	O\n" +
                               "interleukin-2	B-protein\n" +
                               "(	O\n" +
                               "IL-2	B-protein\n" +
                               ")	O\n" +
                               "and	O\n" +
                               "cell	O\n" +
                               "proliferation	O\n" +
                               ".	O\n" +
                               "\n";
        
        // Testing str1
        InputStream inSentences = new ByteArrayInputStream(str1Sentences.getBytes("UTF-8"));
        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), true).launch();
        OutputStream out = p.add(new JNLPBAReader(parser)).run(inSentences).get(0);
        String s = out.toString();
        
        inSentences.close();
        out.close();
        
        List<Pair<Integer, Integer>> splitPairList = (List<Pair<Integer, Integer>>) p.getModuleData("SENTENCES_SPLIT_PAIR_LIST").get(0);
                    
        // Verify <roi> tags
        assertEquals("<roi>IL-2 gene expression and NF-kappa B activation through CD28 requires reactive oxygen production by 5-lipoxygenase .\n" +
                     "Activation of the CD28 surface receptor provides a major costimulatory signal for T cell activation resulting in enhanced production of interleukin-2 ( IL-2 ) and cell proliferation .\n</roi>", s);
                
        // Verify split list (needed by NLP module)
        assertEquals(2, splitPairList.size());
        assertEquals(new Pair(0,115), splitPairList.get(0));
        assertEquals(new Pair(116,299), splitPairList.get(1));
        
        // Verify annotaions
        assertEquals(p.getCorpus().getSentence(0).getToken(0).getLabel(), LabelTag.B);
        assertEquals(p.getCorpus().getSentence(0).getToken(1).getLabel(), LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(2).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(3).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(4).getLabel(), LabelTag.B);
        assertEquals(p.getCorpus().getSentence(0).getToken(5).getLabel(), LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(6).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(7).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(8).getLabel(), LabelTag.B);
        assertEquals(p.getCorpus().getSentence(0).getToken(9).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(10).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(11).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(12).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(13).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(14).getLabel(), LabelTag.B);
        assertEquals(p.getCorpus().getSentence(0).getToken(15).getLabel(), LabelTag.O);
    
        assertEquals(p.getCorpus().getSentence(1).getToken(0).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(1).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(2).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(3).getLabel(), LabelTag.B);
        assertEquals(p.getCorpus().getSentence(1).getToken(4).getLabel(), LabelTag.I);
        assertEquals(p.getCorpus().getSentence(1).getToken(5).getLabel(), LabelTag.I);
        assertEquals(p.getCorpus().getSentence(1).getToken(6).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(7).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(8).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(9).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(10).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(11).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(12).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(13).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(14).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(15).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(16).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(17).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(18).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(19).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(20).getLabel(), LabelTag.B);
        assertEquals(p.getCorpus().getSentence(1).getToken(21).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(22).getLabel(), LabelTag.B);
        assertEquals(p.getCorpus().getSentence(1).getToken(23).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(24).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(25).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(26).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(1).getToken(27).getLabel(), LabelTag.O);
    }
    
}
