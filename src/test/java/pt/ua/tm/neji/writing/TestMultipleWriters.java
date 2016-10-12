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

package pt.ua.tm.neji.writing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.writer.A1Writer;
import pt.ua.tm.neji.writer.BioCWriter;
import pt.ua.tm.neji.writer.CoNLLWriter;
import pt.ua.tm.neji.writer.JSONWriter;

import java.io.*;
import java.util.List;

/**
 * Testing class for writer modules of multiple formats in the same pipeline,
 * representing the same processed information in A1, JSON, Cand BioC formats.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestMultipleWriters extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestMultipleWriters.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestMultipleWriters(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestMultipleWriters.class);
    }

    public void test() throws IOException, NejiException {

        Pipeline p = new DefaultPipeline(Variables.corpus());
        String strXML = "<s id=\"test1\">" + p.getCorpus().getSentence(0).getText() + "</s>";

        String[] expected = new String[]{
                Variables.expectedA1,
                Variables.expectedJSON,
                Variables.expectedCoNLL,
                Variables.expectedBIOC
        };

        InputStream in = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
        p.add(new A1Writer(),
                new JSONWriter(),
                new CoNLLWriter(),
                new BioCWriter());

        List<OutputStream> out = p.run(in);
        assertEquals(4, out.size());

        for(int i = 0; i < expected.length; i++){
            assertEquals(expected[i], out.get(i).toString());
        }
    }
}
