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
import static junit.framework.Assert.assertEquals;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.writer.BC2Writer;

/**
 *
 * @author jeronimo
 */
public class TestBC2Writer extends TestCase {
    
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestBC2Writer.class);

    /**
     * Create the test case.
     * @param testName 
     */
    public TestBC2Writer(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestBC2Writer.class);
    }

    public void test() throws IOException, NejiException {

        Pipeline p = new DefaultPipeline(Variables.corpus());
        String strXML = "<s id=\"test1\">" + p.getCorpus().getSentence(0).getText() + "</s>";

        String expected = Variables.expectedBC2;

        InputStream in = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
        p.add(new BC2Writer());
        OutputStream out = p.run(in).get(0);
        String outStr = out.toString();
        in.close();
        
        assertEquals(expected, outStr);
    }    
}
