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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.nlp;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import monq.jfa.DfaRun;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.*;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserSupport;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Module to perform Natural Language Processing, namely tokenization, lemmatization,
 * POS tagging, chunking and dependency parsing.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Passages})
@Provides({Resource.Sentences, Resource.DynamicNLP})
public class NLP extends BaseLoader implements DynamicNLP {

    /**
     * {@link Logger} to be used in the class.
     */
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(NLP.class);
    private final Parser parser;
    private final ParserLevel customLevel;
    private int startText;
    private int startTag;
    private int sentenceCounter;
    private boolean inText;
    int startIndex;

    public NLP(Parser parser) throws NejiException {
        this(parser, parser.getLevel());
    }

    public NLP(Parser parser, ParserLevel customLevel) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_text, "roi");
        super.addActionToXMLTag(end_text, "roi");
        this.parser = parser;
        this.customLevel = customLevel;
        sentenceCounter = 0;
        startText = 0;
        startTag = 0;
        inText = false;
        startIndex = 0;
    }

//    public NLP(final Corpus corpus, Parser parser) throws NejiException {
//        this(parser);
//        getPipeline().setCorpus(corpus);
//    }

    private Action start_text = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inText = true;
            startText = yytext.indexOf(">", start) + 1;
            startTag = start;
        }
    };

    private Action end_text = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int endText) {
            if (inText) {
                StringBuffer sb = new StringBuffer(yytext.substring(startText, endText));
                int regionSize = sb.length();

                Corpus corpus = getPipeline().getCorpus();

                int offset = 0;

                try {
                    if (!parser.isLaunched()) {
                        parser.launch();
                    }
                    List<Sentence> parsedSentences = parser.parseWithLevel(customLevel, corpus, sb.toString());

                    for(Sentence sentence : parsedSentences) {
                        int s = offset + sentence.getStart();
                        int e = offset + sentence.getEnd();

                        String prefix = "<s";
                        prefix += " id=\"" + sentenceCounter++ + "\"";
                        prefix += ">";

                        String suffix = "</s>";

                        String taggedSentence = prefix + sb.substring(s, e) + suffix;

                        sb.replace(s, e, taggedSentence);

                        offset += prefix.length() + suffix.length();                        

                        // Set sentence indexes
                        sentence.setStart(sentence.getStart() + startIndex);
                        sentence.setEnd(sentence.getEnd() + startIndex);
                        sentence.setOriginalStart(sentence.getOriginalStart() + startIndex);
                        sentence.setOriginalEnd(sentence.getOriginalEnd() + startIndex);
                    }

                } catch (NejiException | IOException ex) {
                    throw new RuntimeException("There was a problem parsing the sentence. Document: " +
                            corpus.getIdentifier() , ex);
                }

                int endTag = yytext.indexOf(">", endText) + 1;
                yytext.replace(startTag, endTag, sb.toString());
                
                startIndex += regionSize;
                
                // Count change lines
                int textIndex = startIndex;
                String text = corpus.getText();
                while((textIndex < text.length()) && (text.charAt(textIndex) == '\n')) {
                    startIndex++;
                    textIndex++;
                }
                
                if (corpus.size() > 0) {
                    corpus.getSentence(corpus.size() - 1).setOriginalEnd(startIndex);
                }
            }

            inText = false;
        }
    };

    @Override
    public Collection<ParserLevel> getLevels() {
        return ParserSupport.getEqualOrLowerSupportedLevels(parser.getTool(), parser.getLanguage(), customLevel);
    }
}
