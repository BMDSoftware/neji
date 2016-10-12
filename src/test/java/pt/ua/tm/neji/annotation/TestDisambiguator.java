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
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.*;
import pt.ua.tm.neji.disambiguator.Disambiguator;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree;

import java.io.*;
import java.util.List;

/*
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestDisambiguator extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestDisambiguator.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestDisambiguator(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestDisambiguator.class);
    }

    public void test() throws NejiException, IOException {
        Corpus corpus = Variables.getCorpus2();

        Sentence s1 = corpus.getSentence(0);
        Sentence s2 = s1.clone(corpus);
        corpus.addSentence(s2);
        Annotation a;

        // adds annotations to the sentences's trees
        for(Sentence s : corpus.getSentences()) {

            a = AnnotationImpl.newAnnotationByTokenPositions(s, 0, 2, 1.0);
            a.addID(Identifier.getIdentifierFromText("2:::DISO"));
            s.addAnnotationToTree(a); // human BRCA1 gene

            a = AnnotationImpl.newAnnotationByTokenPositions(s, 1, 2, 1.0);
            a.addID(Identifier.getIdentifierFromText("3:::PRGE"));
            s.addAnnotationToTree(a); // BRCA1 gene

            a = AnnotationImpl.newAnnotationByTokenPositions(s, 1, 1, 1.0);
            a.addID(Identifier.getIdentifierFromText("3:::PRGE"));
            s.addAnnotationToTree(a); // BRCA1

            a = AnnotationImpl.newAnnotationByTokenPositions(s, 1, 4, 1.0);
            a.addID(Identifier.getIdentifierFromText("4:::DISO"));
            s.addAnnotationToTree(a); // BRCA1 gene and P53

            a = AnnotationImpl.newAnnotationByTokenPositions(s, 4, 4, 1.0);
            a.addID(Identifier.getIdentifierFromText("5:::DISO"));
            s.addAnnotationToTree(a); // P53
        }

        // original tree without disambiguation
        List<Annotation> list = s1.getTree().buildData(Tree.TreeTraversalOrderEnum.PRE_ORDER, false);
        assertEquals(5, list.size());
        a = list.get(0);
        assertEquals("human BRCA1 gene", a.getText());
        assertEquals(0, a.getStartIndex());
        assertEquals(2, a.getEndIndex());
        a = list.get(1);
        assertEquals("BRCA1 gene", a.getText());
        assertEquals(1, a.getStartIndex());
        assertEquals(2, a.getEndIndex());
        a = list.get(2);
        assertEquals("BRCA1", a.getText());
        assertEquals(1, a.getStartIndex());
        assertEquals(1, a.getEndIndex());
        a = list.get(3);
        assertEquals("BRCA1 gene and P53", a.getText());
        assertEquals(1, a.getStartIndex());
        assertEquals(4, a.getEndIndex());
        a = list.get(4);
        assertEquals("P53", a.getText());
        assertEquals(4, a.getStartIndex());
        assertEquals(4, a.getEndIndex());
        assertEquals(7, s1.getTree().getNumberOfNodes());


        // tests case 1: disambiguate annotations nested in the same group
        Disambiguator.discardNestedSameGroup(s1);
        list = s1.getTree().buildData(Tree.TreeTraversalOrderEnum.PRE_ORDER, false);
        assertEquals(3, list.size());
        a = list.get(0);
        assertEquals("human BRCA1 gene", a.getText());
        assertEquals(0, a.getStartIndex());
        assertEquals(2, a.getEndIndex());
        a = list.get(1);
        assertEquals("BRCA1 gene", a.getText());
        assertEquals(1, a.getStartIndex());
        assertEquals(2, a.getEndIndex());
        a = list.get(2);
        assertEquals("BRCA1 gene and P53", a.getText());
        assertEquals(1, a.getStartIndex());
        assertEquals(4, a.getEndIndex());
        assertEquals(5, s1.getTree().getNumberOfNodes());


        // test case 2: disambiguate annotations by depth
        Disambiguator.discardByDepth(s2, 2);
        list = s2.getTree().buildData(Tree.TreeTraversalOrderEnum.PRE_ORDER, false);
        assertEquals(2, list.size());
        a = list.get(0);
        assertEquals("human BRCA1 gene", a.getText());
        assertEquals(0, a.getStartIndex());
        assertEquals(2, a.getEndIndex());
        a = list.get(1);
        assertEquals("BRCA1 gene and P53", a.getText());
        assertEquals(1, a.getStartIndex());
        assertEquals(4, a.getEndIndex());
        assertEquals(5, s1.getTree().getNumberOfNodes());


        // test case 3: disambiguate annotations of the same group by priority
//        Disambiguator.discardSameGroupByPriority(s3);
//        logger.info("CASE 3: disambiguate annotations of the same group by priority\n{}\n", s3.getTree().toStringWithDepth());

    }
}

