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
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.corpus.*;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.*;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;

import java.io.IOException;
import java.util.List;

/**
 * Testing class for parsing at tokenization level for both GDep and OpenNLP tools.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 */
public class TestTokenization extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestTokenization.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestTokenization(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestTokenization.class);
    }

    public void testGDEP() throws IOException, NejiException {
        Constants.verbose = true;
        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), false).launch();
        int i, k, j;


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
            List<Sentence> parsedSentences = parser.parse(corpus, v.text);
            i = k = j = 0;
            for(Sentence s : parsedSentences){
                assertEquals(v.sentenceStart[j], s.getStart());
                assertEquals(v.sentenceEnd[j], s.getEnd());

                List<Token> tokenList = s.getTokens();
                for(Token token : tokenList) {                 
                    assertEquals(v.tokenStart[i], token.getStart());
                    assertEquals(v.tokenEnd[i], token.getEnd());
                    assertEquals(v.tokenText[i], token.getText());;
                    assertEquals(v.tokenLabel, token.getLabel());
                    assertEquals(k,token.getIndex());
                    i++;
                    k++;
                }
                k = 0;
                j++;
            }
        }

        parser.close();
    }

    public void testOPENNLP() throws IOException, NejiException {
        Parser parser = new OpenNLPParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION).launch();
        int i, k, j;


        Variables[] variablesToTest = {
                Variables.str1.opennlp,
                Variables.str2.opennlp,
                Variables.str3.opennlp,
                Variables.str4.opennlp,
                Variables.str5.opennlp
        };

        for(Variables v : variablesToTest) {
            Corpus corpus = new Corpus();
            corpus.setText(v.text);
            List<Sentence> parsedSentences = parser.parse(corpus, v.text);
            i = k = j = 0;
            for(Sentence s : parsedSentences) {
                assertEquals(v.sentenceStart[j], s.getStart());
                assertEquals(v.sentenceEnd[j], s.getEnd());

                List<Token> tokenList = s.getTokens();
                for(Token token : tokenList) {
                    assertEquals(v.tokenStart[i], token.getStart());
                    assertEquals(v.tokenEnd[i], token.getEnd());
                    assertEquals(v.tokenText[i], token.getText());
                    assertEquals(v.tokenLabel, token.getLabel());
                    assertEquals(k, token.getIndex());
                    i++;
                    k++;
                }
                k = 0;
                j++;
            }
        }

        parser.close();
    }
}
