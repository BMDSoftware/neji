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

package pt.ua.tm.neji.postprocessing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.*;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.tree.TreeNode;

import java.io.*;
import java.util.List;

/**
 * Testing class for abbreviation module, which looks for abbreviations that
 * correspond to an already annotated concept.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestAbbreviation extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestAbbreviation.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestAbbreviation(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestAbbreviation.class);
    }

    public void test() throws NejiException, IOException {
        final Corpus corpus = new Corpus();
        Annotation a;

        final String str1RAW = "Inhaled corticosteroids (IC) are the most commonly used controller medications prescribed for asthma.";
        final String str1XML = "<s>Inhaled corticosteroids (IC) are the most commonly used controller medications prescribed for asthma.</s>";

        final String str2RAW = "Ligase Transporter Member (LTP) and Lambda Type Polymerase (LTP)";
        final String str2XML = "<s>Ligase Transporter Member (LTP) and Lambda Type Polymerase (LTP)</s>";

        final String strRAW = str1RAW + "\n" + str2RAW;
        final String strXML = str1XML + str2XML;

        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), false).launch();
        corpus.setText(strRAW);
        List<Sentence> sentencesList = parser.parse(corpus, strRAW);
        Sentence s1 = sentencesList.get(0);
        Sentence s2 = sentencesList.get(1);
        parser.close();

        // add annotation "Inhaled corticosteroids" to sentence1
        a = AnnotationImpl.newAnnotationByTokenPositions(s1, 0, 1, 1.0);
        a.addID(Identifier.getIdentifierFromText("1:::PRGE"));
        s1.addAnnotationToTree(a);

        // add annotation "Ligase Transporter Member" to sentence2
        a = AnnotationImpl.newAnnotationByTokenPositions(s2, 0, 2, 1.0);
        a.addID(Identifier.getIdentifierFromText("1:::PRGE"));
        s2.addAnnotationToTree(a);

        // add annotation "Lambda Type Polymerase" to sentence2
        a = AnnotationImpl.newAnnotationByTokenPositions(s2, 7, 9, 1.0);
        a.addID(Identifier.getIdentifierFromText("2:::PRGE"));
        s2.addAnnotationToTree(a);

        // CASE 1: Testing abbreviation with a single annotation
        // EXPECTED RESULT: The initials will be annotated with the single annotation's ID
        List<TreeNode<Annotation>> sentenceTreeChildren = s1.getTree().getRoot().getChildren();
        assertEquals(1, sentenceTreeChildren.size());
        a = sentenceTreeChildren.get(0).getData();
        assertEquals("Inhaled corticosteroids", a.getText());
        assertEquals(0, a.getStartIndex());
        assertEquals(1, a.getEndIndex());

        corpus.addSentence(s1);
        Abbreviation ab = new Abbreviation();
        InputStream in = new ByteArrayInputStream(str1XML.getBytes("UTF-8"));
        new DefaultPipeline(corpus)
                .add(ab)
                .run(in);
        in.close();


        assertEquals(2, sentenceTreeChildren.size());
        a = sentenceTreeChildren.get(0).getData();
        assertEquals("Inhaled corticosteroids", a.getText());
        assertEquals(0, a.getStartIndex());
        assertEquals(1, a.getEndIndex());
        a = sentenceTreeChildren.get(1).getData();
        assertEquals("IC", a.getText());
        assertEquals(3, a.getStartIndex());
        assertEquals(3, a.getEndIndex());

        assertEquals(
                sentenceTreeChildren.get(0).getData().getStringIDs(),
                sentenceTreeChildren.get(1).getData().getStringIDs()
        );



        // CASE 2: Testing abbreviation with two annotations of equal initials
        // EXPECTED RESULT: Both initials will be annotated with the SECOND annotation's ID
        sentenceTreeChildren = s2.getTree().getRoot().getChildren();
        assertEquals(2, sentenceTreeChildren.size());
        a = sentenceTreeChildren.get(0).getData();
        assertEquals("Ligase Transporter Member", a.getText());
        assertEquals(0, a.getStartIndex());
        assertEquals(2, a.getEndIndex());
        a = sentenceTreeChildren.get(1).getData();
        assertEquals("Lambda Type Polymerase", a.getText());
        assertEquals(7, a.getStartIndex());
        assertEquals(9, a.getEndIndex());

        corpus.getSentences().remove(0);
        corpus.addSentence(s2);
        Abbreviation ab2 = new Abbreviation();
        InputStream in2 = new ByteArrayInputStream(str2XML.getBytes("UTF-8"));
        new DefaultPipeline(corpus)
                .add(ab2)
                .run(in2);
        in2.close();

        assertEquals(4, sentenceTreeChildren.size());
        a = sentenceTreeChildren.get(0).getData();
        assertEquals("Ligase Transporter Member", a.getText());
        assertEquals(0, a.getStartIndex());
        assertEquals(2, a.getEndIndex());
        a = sentenceTreeChildren.get(1).getData();
        assertEquals("LTP", a.getText());
        assertEquals(4, a.getStartIndex());
        assertEquals(4, a.getEndIndex());
        a = sentenceTreeChildren.get(2).getData();
        assertEquals("Lambda Type Polymerase", a.getText());
        assertEquals(7, a.getStartIndex());
        assertEquals(9, a.getEndIndex());
        a = sentenceTreeChildren.get(3).getData();
        assertEquals("LTP", a.getText());
        assertEquals(11, a.getStartIndex());
        assertEquals(11, a.getEndIndex());

        assertEquals(
                sentenceTreeChildren.get(1).getData().getStringIDs(),
                sentenceTreeChildren.get(2).getData().getStringIDs()
        );
        assertEquals(
                sentenceTreeChildren.get(2).getData().getStringIDs(),
                sentenceTreeChildren.get(3).getData().getStringIDs()
        );
    }
}

