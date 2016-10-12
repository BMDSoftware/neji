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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.nlp;

import com.aliasi.util.Pair;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.core.module.DynamicNLP;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserSupport;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;

/**
 *
 * @author jeronimo
 */
@Requires({Resource.Passages})
@Provides({Resource.Sentences, Resource.DynamicNLP})
public class TrainNLP extends BaseLoader implements DynamicNLP {

    /**
     * {@link Logger} to be used in the class.
     */
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TrainNLP.class);
    
    // Attributes
    private GDepParser parser;
    private final ParserLevel customLevel;
    private int startText;
    private int startTag;
    private int sentenceCounter;
    private boolean inText;
    int startIndex;
    
    private int sentenceIndex;
    private Corpus labelledCorpus;
    private boolean newCorpusFlag;

    public TrainNLP(Parser parser) throws NejiException {
        this(parser, parser.getLevel());
    }

    public TrainNLP(Parser parser, ParserLevel customLevel) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_text, "roi");
        super.addActionToXMLTag(end_text, "roi");
        this.parser = (GDepParser) parser;
        this.customLevel = customLevel;
        sentenceCounter = 0;
        startText = 0;
        startTag = 0;
        inText = false;
        startIndex = 0;
        sentenceIndex = 0;      
        newCorpusFlag = true;
    }

    private Module.Action start_text = new BaseModule.StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inText = true;
            startText = yytext.indexOf(">", start) + 1;
            startTag = start;
        }
    };

    private Module.Action end_text = new BaseModule.EndAction() {
        @Override
        public void execute(StringBuffer yytext, int endText) {           
            
            if (inText) {
                StringBuffer sb = new StringBuffer(yytext.substring(startText, endText));
                int regionSize = sb.length();             
                
                // Get the corpus
                Corpus corpus;
                
                // If it is the first region to be processed, complete the corpus with
                // data provided from the reader.
                if (newCorpusFlag) {
                    labelledCorpus = getPipeline().getCorpus(); // Keep the previous corpus for later processing
                    
                    // Create the new corpus and initialize it
                    corpus = new Corpus(labelledCorpus.getFormat(), labelledCorpus.getEntity());
                    corpus.setIdentifier(labelledCorpus.getIdentifier());
                    corpus.setText(labelledCorpus.getText());
                    getPipeline().setCorpus(corpus);
                    
                    newCorpusFlag = false;
                } else { // Else just get the corpus to continue the regions processing
                    corpus = getPipeline().getCorpus();
                }

                int offset = 0;
                
                try {                                                                
                    // Get sentences split pair list, if it exists
                    List<Pair<Integer, Integer>> splitPairList = null;
                    if (!getPipeline().getModuleData("SENTENCES_SPLIT_PAIR_LIST").isEmpty()) {
                        splitPairList = (List<Pair<Integer, Integer>>) getPipeline().getModuleData("SENTENCES_SPLIT_PAIR_LIST").get(0);
                    }
                    
                    // Get new parser, if it exists
                    if (!getPipeline().getModuleData("PARSER").isEmpty()) {
                        parser = (GDepParser) getPipeline().getModuleData("PARSER").get(0);
                    }
                    
                    // Clean pipeline stored data
                    getPipeline().clearStoredData();
                    
                    // Launch parser
                    if (!parser.isLaunched()) {
                        parser.launch();
                    }
                                           
                    // Parse sentences
                    List<Sentence> parsedSentences = parser.parseWithLevel(customLevel, corpus, sb.toString(), splitPairList);
                    
                    for(int i = 0 ; i < parsedSentences.size() ; i++) {
                        
                        // Get the sentence from the new corpus and the previous one (from reader)
                        Sentence sentence = corpus.getSentence(i + sentenceIndex);
                        Sentence tmpSentence = labelledCorpus.getSentence(i + sentenceIndex);
                        
                        // Copy ids
                        sentence.setId(tmpSentence.getId());
                        
                        // Copy previously obtained BIO labels to the corpus
                        for (int j = 0 ; j < tmpSentence.size() ; j++) {
                            Token token = sentence.getToken(j);
                            Token tmpToken = tmpSentence.getToken(j);
                            token.setLabel(tmpToken.getLabel());
                        }
                                
                        // Set <s> and </s> tags
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
                    
                    // Update sentece index
                    sentenceIndex += parsedSentences.size();

                } catch (NejiException | IOException ex) {
                    throw new RuntimeException("There was a problem parsing the sentence.");
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
                
                corpus.getSentence(corpus.size() - 1).setOriginalEnd(startIndex);                  
            }
            
            inText = false;
        }
    };

    @Override
    public Collection<ParserLevel> getLevels() {
        return ParserSupport.getEqualOrLowerSupportedLevels(parser.getTool(), parser.getLanguage(), customLevel);
    }
}

