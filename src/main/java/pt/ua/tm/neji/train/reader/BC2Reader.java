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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import monq.jfa.DfaRun;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
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
public class BC2Reader extends BaseReader implements DynamicNLP {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(BC2Reader.class);
    
    // Attributes
    private InputStream inputAnnotations;
    private GDepParser parser;
    private ParserLevel customLevel;
    
    /**
     * Constructor.
     * @param parser parser to use
     * @param customLevel parser level
     * @throws NejiException 
     */
    public BC2Reader(Parser parser, ParserLevel customLevel) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToTag(text_action, ".+");
        this.parser = (GDepParser) parser;
        this.customLevel = customLevel;
        this.inputAnnotations = null;
    }
    
    /**
     * Constructor.
     * @param parser parser to use
     * @throws NejiException
     */
    public BC2Reader(Parser parser) throws NejiException {
        this(parser, parser.getLevel());
    }
    
    /** Constructor. 
     * @param parser parser to use
     * @param customLevel parser level
     * @param inputAnnotations file with annotations
     * @throws NejiException
     */
    public BC2Reader(Parser parser, ParserLevel customLevel, InputStream inputAnnotations) throws NejiException {
        this(parser, customLevel);
        this.inputAnnotations = inputAnnotations;
    }
    
    /**
     * Constructor.
     * @param parser parser to use
     * @param inputAnnotations file with annotations
     * @throws NejiException
     */
    public BC2Reader(Parser parser, InputStream inputAnnotations) throws NejiException {
        this(parser, parser.getLevel(), inputAnnotations);
    }
    
    private DefaultAction text_action = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {    
            
            // Get annotations
            MultiValueMap annotations = null;
            if (inputAnnotations != null) {

                //logger.info("Loading annotations...");

                try {
                    annotations = getAnnotations(inputAnnotations);                                    
                    inputAnnotations.close();
                } catch (NejiException | IOException ex) {
                    throw new RuntimeException("There was a problem reading the annotations.", ex);
                }
                
            }
            
            // Read sentences file
            Corpus corpus = getPipeline().getCorpus();            
            StringBuilder sb = new StringBuilder();
            List<Pair<Integer, Integer>> splitPairList = new ArrayList<>();
            List<String> sentencesIdList = new ArrayList<>();
            
            //logger.info("Loading sentences and IDs...");            
            
            InputStream is = new ByteArrayInputStream(yytext.toString().getBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int divider;
            String id;
            String sentenceText;
            int sentenceStart = 0;
            int sentenceEnd;
                        
            try {
                
                while ((line = br.readLine()) != null) {
                    divider = line.indexOf(" ");
                    id = line.substring(0, divider);
                    sentenceText = line.substring(divider + 1);
                    
                    // Build corpus text
                    sb.append(sentenceText);
                    sb.append("\n");
                    
                    // Add sentence id to the list
                    sentencesIdList.add(id);
                    
                    // Add sentence pair to the list
                    sentenceEnd = sentenceStart + sentenceText.length();
                    splitPairList.add(new Pair<>(sentenceStart, sentenceEnd));
                    sentenceStart = sentenceEnd + 1;
                }
                
            } catch (IOException ex) {
                throw new RuntimeException("There was a problem reading the sentences file.", ex);
            }
            
            // Set corpus text            
            corpus.setText(sb.toString());

            //logger.info("Loading tokens...");
            
            // Parse sentences and tokens (Just tokenization!)
            List<Sentence> parsedSentences = null;            
            try {
                // Tokenization parser
                GDepParser tokenizationParser = new GDepParser(parser.getLanguage(), ParserLevel.TOKENIZATION, null, false);
                
                // Launch parser
                if (!tokenizationParser.isLaunched()) {
                    tokenizationParser.launch();
                }
                
                // Parse sentences
                parsedSentences = tokenizationParser.parseWithLevel(ParserLevel.TOKENIZATION, corpus, sb.toString(), splitPairList);
            
                // Close parser
                tokenizationParser.close();
                
            } catch (IOException | NejiException ex) {
                throw new RuntimeException("There was a problem parsing the sentences and tokens.", ex);
            }      
       
            //System.out.println();
            //logger.info("Setting IDs and annotations...");
            
            // Set sentences ids and annotations
            int sentenceCounter = 0;
            for (Sentence sentence : parsedSentences) {
                
                // Set sentence id
                id = sentencesIdList.get(sentenceCounter++);
                sentence.setId(id);
                
                // Set sentence annotations
                if (annotations != null) {
                    Collection<BCAnnotation> a = annotations.getCollection(id);
                    setAnnotations(sentence, a);
                }
            }
            
            // Set <roi> and </roi> tags
            sb.insert(0, "<roi>");
            sb.append("</roi>");
            
            yytext.replace(start, yytext.length(), sb.toString());
            
            // Store data needed by NLP module in the pipeline
            getPipeline().clearStoredData();
            getPipeline().storeModuleData("SENTENCES_SPLIT_PAIR_LIST", splitPairList);
        }
    };
    
    private MultiValueMap getAnnotations(InputStream inputAnnotations) throws NejiException {
        
        MultiValueMap annotations = new MultiValueMap();
        
        try {
            InputStreamReader isr = new InputStreamReader(inputAnnotations);
            BufferedReader br = new BufferedReader(isr);
            String line;
            String parts[];
            int startChar;
            int endChar;
            String pos;
            String id;
            
            while ((line = br.readLine()) != null) {
                parts = line.split("[|]");                
                id = parts[0];
                
                pos = parts[1];
                parts = pos.split("\\s+");
                startChar = Integer.parseInt(parts[0]);
                endChar = Integer.parseInt(parts[1]);
                
                BCAnnotation a = new BCAnnotation(startChar, endChar);
                BCAnnotation other;
                
                // For annotations with the same start, use the largest one
                Collection<BCAnnotation> col;
                
                if ((col = annotations.getCollection(id)) != null) {
                    Iterator<BCAnnotation> it = col.iterator();
                    
                    while (it.hasNext()) {
                        other = it.next();
                        
                        if ((other.getStartChar() == a.getStartChar()) && (a.getEndChar() > other.getEndChar())) {
                            annotations.remove(id, other);
                            break;
                        }
                    }
                }
                
                annotations.put(id, a);
            }
        } catch (IOException ex) {
            throw new NejiException("There was a problem reading the annotations file.", ex);
        }
        
        return annotations;
    }
    
    private void setAnnotations(Sentence s, Collection<BCAnnotation> annotations) {
        
        if (annotations == null) {
            return;
        }
        
        // Sort annotations
        List<BCAnnotation> list = new ArrayList<>(annotations);
        Collections.sort(list, new BCAnnotationComparator());
        
        Annotation a;
        int spacesCounter = 0;
        int tokenIndex = 1;
        int previousTokenEndIndex;
        Token t = s.getToken(0);
        int firstIndex;
        int lastIndex = 0;
        boolean notFound;
        int lastIndexSpacesCounter = 0;
        
        for (BCAnnotation bca : annotations) {
            
            // Get first token of the annotation
            notFound = false;
            while (t.getStart() - spacesCounter != bca.getStartChar()) {               
                
                // Verify index
                if (tokenIndex >= s.size()) {
                    notFound = true;
                    break;
                }
                
                // Get next token
                previousTokenEndIndex = t.getEnd();             
                t = s.getToken(tokenIndex);
               
                // Count spaces since last token
                spacesCounter += t.getStart() - previousTokenEndIndex - 1;
                
                // Increment index
                tokenIndex++;
            }
            
            // Verify if the first token was found (if not go to the next annotation)
            if (notFound) {
                tokenIndex = lastIndex + 1;
                t = s.getToken(lastIndex);
                spacesCounter = lastIndexSpacesCounter;                
                continue;
            }
            
            firstIndex = tokenIndex - 1;
            
            // Get last token of the annotation
            notFound = false;
            while (t.getEnd() - spacesCounter < bca.getEndChar()) {               
                
                // Verify index
                if (tokenIndex >= s.size()) {
                    notFound = true;
                    break;
                }
                
                // Get next token
                previousTokenEndIndex = t.getEnd();
                t = s.getToken(tokenIndex);
               
                // Count spaces since last token
                spacesCounter += t.getStart() - previousTokenEndIndex - 1;     
                
                // Increment index
                tokenIndex++;  
            }
            
            // Verify if the first token was found (if not go to the next annotation)
            if (notFound) {
                tokenIndex = lastIndex + 1;
                t = s.getToken(lastIndex);
                spacesCounter = lastIndexSpacesCounter;
                continue;
            }
            
            lastIndex = tokenIndex - 1;
            lastIndexSpacesCounter = spacesCounter;      
            
            // Set annotation
            a = AnnotationImpl.newAnnotationByTokenPositions(s, firstIndex, lastIndex, 1.0);
            s.addAnnotationLabels(a);
        }
    }   

    @Override
    public InputFormat getFormat() {
        return InputFormat.BC2;
    }

    @Override
    public Collection<ParserLevel> getLevels() {
        return ParserSupport.getEqualOrLowerSupportedLevels(parser.getTool(), parser.getLanguage(), parser.getLevel());
    }
    
    /**
     * Class to read BioCreative Annotations.
     */
    private class BCAnnotation {

        /**
         * Index of the first char of the annotation.
         */
        private int startChar;
        
        /**
         * Index of the last char of the annotation.
         */
        private int endChar;

        /**
         * Constructor.
         *
         * @param startChar Index of the first char.
         * @param endChar Index of the last char.
         */
        public BCAnnotation(int startChar, int endChar) {
            this.startChar = startChar;
            this.endChar = endChar;
        }

        /**
         * Get the last char index.
         *
         * @return The index.
         */
        public int getEndChar() {
            return endChar;
        }

        /**
         * Get the first char index.
         *
         * @return The index.
         */
        public int getStartChar() {
            return startChar;
        }

        /**
         * Compare two BioCreative annotations.
         *
         * @param obj The {@link BCAnnotation} to be compared with.
         * @return
         * <code>True</code> if the two annotations are equal, and
         * <code>False</code> otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BCAnnotation other = (BCAnnotation) obj;
            if (this.startChar != other.startChar) {
                return false;
            }
            return true;
        }

        /**
         * Override the hashCode method to consider all the internal variables.
         *
         * @return Unique number for each BioCreative annotation.
         */
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + this.startChar;
            return hash;
        }

        /**
         * Provide text representation of the annotation.
         *
         * @return The text.
         */
        @Override
        public String toString() {
            return "(" + startChar + "," + endChar + ")";
        }
    }
    
    /**
     * Compare {@link BCAnnotation} objects.
     */
    private class BCAnnotationComparator implements Comparator<BCAnnotation> {

        /**
         * Compare BioCreative annotations.
         *
         * @param t The first BioCreative annotation.
         * @param t1 The second BioCreative annotation.
         * @return
         * <code>1</code> if the first annotation appears after or is larger
         * than the second one, and
         * <code>-1</code> otherwise.
         * <code>0</code> if the annotations are equal.
         */
        @Override
        public int compare(BCAnnotation t, BCAnnotation t1) {
            if (t.getStartChar() > t1.getStartChar()) {
                return 1;
            }
            if (t.getStartChar() < t1.getStartChar()) {
                return -1;
            }

            if (t.getEndChar() > t1.getEndChar()) {
                return 1;
            }
            if (t.getEndChar() < t1.getEndChar()) {
                return -1;
            }
            return 0;
        }
    }
}
