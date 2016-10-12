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
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.sentencesplitter.SentenceSplitter;

/**
 * Testing class for sentence splitting, used by parsing tools to detect Sentences.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestSentenceSplitter extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestSentenceSplitter.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestSentenceSplitter(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestSentenceSplitter.class);
    }

    public void test() {
        SentenceSplitter splitter = new LingpipeSentenceSplitter();
        String[] str = {
                "AA 12 - AA12 AA-12 (12AA) 12.-.",
                "going Now. please ? Pretty please? Maybe, listing Reasons: one; two; Three... done.. DONE",
                "test tEst.iNG t.Est . foo-bar - go . NOW!",
                "testing Now vs. testing Later"
        };
        String[] expectedSubstring = {
                "AA 12 - AA12 AA-12 (12AA) 12.-.",
                "going Now. please ?",
                "Pretty please?",
                "Maybe, listing Reasons: one; two; Three... done.. DONE",
                "test tEst.iNG t.Est . foo-bar - go .",
                "NOW!",
                "testing Now vs. testing Later"
        };
        int i = 0;
        for(String s : str) {
            int[][] ind = splitter.split(s);
            int k = 0;
            while(k < ind.length) {
                String temp = s.substring(ind[k][0], ind[k][1]);
                assertEquals(expectedSubstring[i++], temp);
                k++;
            }
        }
    }
}
