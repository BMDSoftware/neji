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
import pt.ua.tm.neji.train.reader.BC2Reader;

/**
 * Testing class for BC2 format reading module.
 * 
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>))
 * @version 1.0
 */
public class TestBC2Reader extends TestCase {
    
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestBC2Reader.class);
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestBC2Reader(String testName){
        super(testName);
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestBC2Reader.class);
    }
    
    public void test() throws IOException, NejiException {
        
        Pipeline p = new TrainPipelinePhase1();
        
        String str1Sentences = "P00001606T0076 Comparison with alkaline phosphatases and 5-nucleotidase";
        String str1Annotations = "P00001606T0076|14 33|alkaline phosphatases\n" +
                                 "P00001606T0076|37 50|5-nucleotidase";
        
        // Testing str1
        InputStream inSentences = new ByteArrayInputStream(str1Sentences.getBytes("UTF-8"));
        InputStream inAnnotations = new ByteArrayInputStream(str1Annotations.getBytes("UTF-8"));
        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), false).launch();
        OutputStream out = p.add(new BC2Reader(parser, inAnnotations)).run(inSentences).get(0);
        String s = out.toString();
        
        inSentences.close();
        inAnnotations.close();
        out.close();
        
        List<Pair<Integer, Integer>> splitPairList = (List<Pair<Integer, Integer>>) p.getModuleData("SENTENCES_SPLIT_PAIR_LIST").get(0);
                
        // Verify <roi> tags
        assertEquals("<roi>Comparison with alkaline phosphatases and 5-nucleotidase\n</roi>", s);
        
        // Verify split list (needed by NLP module)
        assertEquals(1, splitPairList.size());
        assertEquals(new Pair(0,56), splitPairList.get(0));
        
        // Verify annotaions
        assertEquals(p.getCorpus().getSentence(0).getToken(0).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(1).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(2).getLabel(), LabelTag.B);
        assertEquals(p.getCorpus().getSentence(0).getToken(3).getLabel(), LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(4).getLabel(), LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(5).getLabel(), LabelTag.B);
        assertEquals(p.getCorpus().getSentence(0).getToken(6).getLabel(), LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(7).getLabel(), LabelTag.I);
    }
}
