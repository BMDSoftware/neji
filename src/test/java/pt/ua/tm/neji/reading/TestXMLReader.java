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

package pt.ua.tm.neji.reading;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.reader.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Testing class for XML format reading module.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestXMLReader extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestXMLReader.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestXMLReader(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestXMLReader.class);
    }

    public void test() throws IOException, NejiException {
        Pipeline p = new DefaultPipeline();
        String str1 = "<tag>Inhaled corticosteroids (ICS) are the most commonly used controller medications prescribed for asthma.\n" +
                "The accuracy of the various NAT2 SNP genotype panels to infer NAT2 phenotype were as follows:</tag>" +
                "<colon> seven-SNP: 98.4%; tag-SNP: 77.7%; two-SNP: 96.1%; three-SNP: 92.2%; and four-SNP: 98.4%. </colon>";
        String str2 = "<abbreviation>UGT2B7<tag></abbreviation><tag> genetic polymorphisms are associated with the withdrawal symptoms " +
                "in methadone maintenance patients.</tag>";
        String[] xmlTags = {"tag", "abbreviation"};



        // Testing str1
        InputStream in = new ByteArrayInputStream(str1.getBytes("UTF-8"));
        OutputStream out = p.add(new XMLReader(xmlTags)).run(in).get(0);
        String s = out.toString();
//        logger.info(s);
        assertEquals("<tag><roi>Inhaled corticosteroids (ICS) are the most commonly used controller medications prescribed for asthma." +
                "</roi>\n<roi>The accuracy of the various NAT2 SNP genotype panels to infer NAT2 phenotype were as follows:</roi></tag>" +
                "<colon> seven-SNP: 98.4%; tag-SNP: 77.7%; two-SNP: 96.1%; three-SNP: 92.2%; and four-SNP: 98.4%. </colon>", s);
        in.close();
        out.close();



        // Testing str2
        in = new ByteArrayInputStream(str2.getBytes("UTF-8"));
        out = p.run(in).get(0);
        s = out.toString();
//        logger.info(s);
        assertEquals("<abbreviation>UGT2B7<tag><roi></roi></abbreviation><tag><roi> genetic polymorphisms are associated with the" +
                " withdrawal symptoms in methadone maintenance patients.</roi></tag>", s);
        in.close();
        out.close();
    }
}
