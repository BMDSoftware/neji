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

package pt.ua.tm.neji.train.reader;

import com.aliasi.util.Pair;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.core.Constants.LabelTag;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.core.module.BaseReader;
import pt.ua.tm.neji.core.module.DynamicNLP;
import pt.ua.tm.neji.core.module.Provides;
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
@Provides({Resource.Passages, Resource.Annotations})
public class JNLPBAReader extends BaseReader implements DynamicNLP {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(JNLPBAReader.class);
    
    // Attributes
    private GDepParser parser;
    private ParserLevel customLevel;
    
    /**
     * Constructor.
     * @param parser parser to use
     * @param customLevel parser level
     * @throws NejiException 
     */
    public JNLPBAReader(Parser parser, ParserLevel customLevel) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToTag(text_action, ".+");
        this.parser = (GDepParser) parser;
        this.customLevel = customLevel;
    }
    
    /**
     * Constructor.
     * @param parser parser to use
     * @throws NejiException
     */
    public JNLPBAReader(Parser parser) throws NejiException {
        this(parser, parser.getLevel());
    }
    
    private BaseModule.DefaultAction text_action = new BaseModule.DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {    
            
            StringBuilder corpusText = new StringBuilder();
            StringBuilder sentenceText = new StringBuilder();
            List<Pair<Integer, Integer>> splitPairList = new ArrayList<>();
            
            // Get corpus
            Corpus corpus = getPipeline().getCorpus(); 
            
            // Get entity list
            List<String> entityList = corpus.getEntity();
            
            // Reade tokens and build sentence and text
            InputStream is = new ByteArrayInputStream(yytext.toString().getBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            
            String tokenText;
            LabelTag label;
            String entity;
            Sentence sentence = new Sentence(corpus);
            int tokenIndex = 0;
            int tokenStart = 0;
            int tokenEnd;
            int sentenceStart = 0;
            int sentenceEnd = 0;
            int annotationStart = 0;
            boolean inAnnotation = false;
            boolean medlineHeaderVerify = true; // setted to false after first cycle
            
            try {

                while ((line = br.readLine()) != null) {
                    
                    // Verify medline header
                    if (medlineHeaderVerify) {
                        
                        medlineHeaderVerify = false; // Verify only in the beggining the document has the header
                        
                        if (line.startsWith("###MEDLINE:")) {
                            line = br.readLine(); // ignore this line, read next one
                            if (line.trim().length() == 0) {
                                continue; // ignore this line
                            }
                        }                        
                    }
                    
                    if (line.trim().length() == 0) { // end of sentence                       
                        
                        // Add text to corpusText
                        sentenceText.deleteCharAt(sentenceText.length() - 1);
                        sentenceEnd -= 1;
                        corpusText.append(sentenceText);
                        corpusText.append("\n");
                        
                        // Add sentence to corpus
                        sentence.setStart(sentenceStart);
                        sentence.setEnd(sentenceEnd);
                        corpus.addSentence(sentence);
                        splitPairList.add(new Pair<>(sentenceStart, sentenceEnd));
                        
                        // Reset sentence
                        sentence = new Sentence(corpus);
                        sentenceText = new StringBuilder();
                        sentenceStart = sentenceEnd + 1;
                        sentenceEnd = sentenceStart;
                        
                        tokenStart = 0;
                        tokenIndex = 0;
                        
                        continue;
                    }
                    
                    // Get parameters
                    String[] parts = line.split("\\s+");
                    tokenText = parts[0];
                    
                    if (parts[1].length() != 1) {
                        String[] parts2 = parts[1].split("-");
                        label = LabelTag.valueOf(parts2[0]);
                        entity = parts2[1];
                    } else {
                        label = LabelTag.valueOf(parts[1]);
                        entity = null;
                    }
                                     
                    // Build sentence text
                    sentenceText.append(tokenText);
                    sentenceText.append(" ");
                    
                    // Update tokenEnd and sentence end
                    tokenEnd = tokenStart + tokenText.length() - 1;
                    sentenceEnd += tokenText.length() + 1;
                    
                    // Add token to sentence
                    Token t = new Token(sentence, tokenStart, tokenEnd, tokenIndex);
                    sentence.addToken(t);
                    
                    // Verify annotation                    
                    if (!inAnnotation && label.equals(LabelTag.B) && ((entityList == null) || entityList.contains(entity.toLowerCase()))) { // new annotation
                        annotationStart = tokenIndex;
                        inAnnotation = true;
                    }
                    else if (inAnnotation && label.equals(LabelTag.B)) { // another annotation
                        // Add previous annotaion
                        Annotation a = AnnotationImpl.newAnnotationByTokenPositions(sentence, annotationStart, tokenIndex - 1, 1.0);
                        sentence.addAnnotationLabels(a);
                        
                        if ((entityList == null) || entityList.contains(entity.toLowerCase())) {
                            annotationStart = tokenIndex;
                        } else {
                            inAnnotation = false;
                        }                        
                    }
                    else if (inAnnotation && label.equals(LabelTag.O)) { // end of annotaion
                        // Add annotation
                        Annotation a = AnnotationImpl.newAnnotationByTokenPositions(sentence, annotationStart, tokenIndex - 1, 1.0);
                        sentence.addAnnotationLabels(a);
                        
                        inAnnotation = false;
                    }
                    
                    // Update token start and tokenIndex
                    tokenStart = tokenEnd + 2;
                    tokenIndex++;
                }
                
                // Set corpus text
                corpus.setText(corpusText.toString());
                
            } catch (IOException ex) {
                throw new RuntimeException("There was a problem reading the sentences file.", ex);
            }
            
            // Set <roi> and </roi> tags
            corpusText.insert(0, "<roi>");
            corpusText.append("</roi>");
            
            yytext.replace(start, yytext.length(), corpusText.toString());
            
            // Store data needed by NLP module in the pipeline
            getPipeline().clearStoredData();
            getPipeline().storeModuleData("SENTENCES_SPLIT_PAIR_LIST", splitPairList);
            
            // Store new GDep parser with white space tokenization (JNLPBA uses it)
            try {
                GDepParser newParser = new GDepParser(parser.getLanguage(), parser.getLevel(), null, true);
                getPipeline().storeModuleData("PARSER", newParser);
            } catch(Exception ex) {
                System.out.println("Error: creating a new parser in JNLPBA reader. " + ex.getMessage());
            }
            
        } 
    };  

    @Override
    public InputFormat getFormat() {
        return InputFormat.JNLPBA;
    }

    @Override
    public Collection<ParserLevel> getLevels() {
        return ParserSupport.getEqualOrLowerSupportedLevels(parser.getTool(), parser.getLanguage(), parser.getLevel());
    }   
}
