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
import pt.ua.tm.neji.ml.MLAnnotator;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.corpus.*;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.MLModel;

import java.util.List;

/**
 * Testing class for Annotator class, which loads a ML model and annotates the specified sentence.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestAnnotator extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestAnnotator.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestAnnotator(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestAnnotator.class);
    }

    public void test() {
        Constants.verbose = true;
        try {
            Sentence s = Variables.getCorpus2().getSentence(0);

            MLModel model = Variables.getModel();
            List<Annotation> annotationsML = MLAnnotator.annotate(s, model.getCrf());

            Annotation a1 = annotationsML.get(0);
            Annotation a2 = annotationsML.get(1);

//            logger.info(s.toString());
//            logger.info(a1.toString());
//            logger.info(a2.toString());

            assertEquals("(1,2): BRCA1 gene", a1.toString());
            assertEquals(s, a1.getSentence());
            assertEquals("BRCA1 gene", a1.getText());
            assertEquals(1, a1.getStartIndex());
            assertEquals(2, a1.getEndIndex());

            assertEquals("(4,4): P53", a2.toString());
            assertEquals(s, a2.getSentence());
            assertEquals("P53", a2.getText());
            assertEquals(4, a2.getStartIndex());
            assertEquals(4, a2.getEndIndex());

        } catch (NejiException e) {
            logger.error(e.toString());
        }
    }
}