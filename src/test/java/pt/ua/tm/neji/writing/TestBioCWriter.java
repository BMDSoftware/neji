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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.writer.BioCWriter;

/**
 * Testing class for BioC format writer module.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestBioCWriter extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestBioCWriter.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestBioCWriter(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestBioCWriter.class);
    }

    public void test() throws IOException, NejiException {

        Pipeline p = new DefaultPipeline(Variables.corpus());
        String strXML = "<s id=\"test1\">" + p.getCorpus().getSentence(0).getText() + "</s>";

        String expected = Variables.expectedBIOC;

        InputStream in = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
        p.add(new BioCWriter());
        OutputStream out = p.run(in).get(0);
        String outStr = out.toString();
        in.close();

//        logger.info("BioC Writer output: \n{}\n", outStr);
        assertEquals(expected, outStr);
    }
}
