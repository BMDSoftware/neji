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
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase1;
import pt.ua.tm.neji.train.reader.A1Reader;

/**
 *
 * @author jeronimo
 */
public class TestA1Reader extends TestCase {
    
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestA1Reader.class);
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestA1Reader(String testName){
        super(testName);
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestA1Reader.class);
    }
    
    public void test() throws IOException, NejiException {
        
        Pipeline p = new TrainPipelinePhase1();
        
        String str1Txt = "Expression of the Bcl-3 proto-oncogene suppresses p53 activation. ";
        String str1A1 = "T1	PRGE 14 38	the Bcl-3 proto-oncogene\n" +
                        "T2	PRGE 50 64	p53 activation";

        // Testing str1
        InputStream inTxt = new ByteArrayInputStream(str1Txt.getBytes("UTF-8"));
        InputStream inA1 = new ByteArrayInputStream(str1A1.getBytes("UTF-8"));
        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), false).launch();
        OutputStream out = p.add(new A1Reader(parser, null, inA1)).run(inTxt).get(0);
        String s = out.toString();
        
        inTxt.close();
        inA1.close();
        out.close();
                        
        // Verify <roi> tags
        assertEquals("<roi>Expression of the Bcl-3 proto-oncogene suppresses p53 activation. </roi>", s);
        
        // Verify annotaions
        assertEquals(p.getCorpus().getSentence(0).getToken(0).getLabel(), Constants.LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(1).getLabel(), Constants.LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(2).getLabel(), Constants.LabelTag.B);
        assertEquals(p.getCorpus().getSentence(0).getToken(3).getLabel(), Constants.LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(4).getLabel(), Constants.LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(5).getLabel(), Constants.LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(6).getLabel(), Constants.LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(7).getLabel(), Constants.LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(8).getLabel(), Constants.LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(9).getLabel(), Constants.LabelTag.O);
        assertEquals(p.getCorpus().getSentence(0).getToken(10).getLabel(), Constants.LabelTag.B);
        assertEquals(p.getCorpus().getSentence(0).getToken(11).getLabel(), Constants.LabelTag.I);
        assertEquals(p.getCorpus().getSentence(0).getToken(12).getLabel(), Constants.LabelTag.O);
    }
    
}
