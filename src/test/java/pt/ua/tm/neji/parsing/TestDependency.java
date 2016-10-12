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
import org.jgrapht.Graph;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.corpus.*;
import pt.ua.tm.neji.core.corpus.dependency.DependencyTag;
import pt.ua.tm.neji.core.corpus.dependency.LabeledEdge;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;

import java.io.IOException;
import java.util.List;

/**
 * Testing class for parsing at dependency level for GDep tool.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 */
public class TestDependency extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestDependency.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestDependency(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestDependency.class);
    }

    public void testGDEP() throws IOException, NejiException {
        Constants.verbose = true;
        int i, j;


        Variables[] variablesToTest = {
                Variables.str1.gdep,
                Variables.str2.gdep,
                Variables.str3.gdep,
                Variables.str4.gdep,
                Variables.str5.gdep
        };

        for(Variables v : variablesToTest){
            Corpus corpus = new Corpus();
            corpus.setText(v.text);
            List<Sentence> parsedSentences = Variables.parseWithDepParser(ParserLevel.DEPENDENCY, corpus, v.text);
            i = j = 0;
            for(Sentence s : parsedSentences){
                assertEquals(v.sentenceStart[j], s.getStart());
                assertEquals(v.sentenceEnd[j], s.getEnd());

                Graph<Token, LabeledEdge> dependencyList = s.getDependencyGraph();
                for(LabeledEdge edge : dependencyList.edgeSet()) {
                    Token t1 = (Token)edge.getV1();
                    Token t2 = (Token)edge.getV2();
                    DependencyTag tag = (DependencyTag)edge.getLabel();
                    assertEquals(v.dependencyToken1[i], t1.getText());
                    assertEquals(v.dependencyToken2[i], t2.getText());
                    assertEquals(v.dependencyTagName[i], tag.name());
                    assertEquals(v.dependencyOrdinal[i], tag.ordinal());

                    i++;
                }
                j++;
                s.clone(corpus);
            }
        }
    }
}

