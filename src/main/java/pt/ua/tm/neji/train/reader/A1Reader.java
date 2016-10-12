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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import monq.jfa.DfaRun;
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
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;

/**
 *
 * @author jeronimo
 */
@Provides({Resource.Passages, Resource.Annotations})
public class A1Reader extends BaseReader implements DynamicNLP {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(A1Reader.class);
    
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
    public A1Reader(Parser parser, ParserLevel customLevel) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToTag(text_action, ".+");
        this.parser = (GDepParser) parser;
        this.customLevel = customLevel;
        this.inputAnnotations = null;
    }
    
    public A1Reader(Parser parser, ParserLevel customLevel, InputStream inputAnnotations) throws NejiException {
        this(parser, customLevel);
        this.inputAnnotations = inputAnnotations;
    }
    
    private DefaultAction text_action = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {    
            
            // Set corpus text
            Corpus corpus = getPipeline().getCorpus();
            corpus.setText(yytext.toString());
            
            // Get annotations
            List<A1Annotation> annotations = null;
            if (inputAnnotations != null) {

                //logger.info("Loading annotations...");

                try {
                    annotations = getAnnotations(inputAnnotations, corpus.getEntity());
                    inputAnnotations.close();
                } catch (NejiException | IOException ex) {
                    throw new RuntimeException("There was a problem reading the annotations.", ex);
                }
            }
            
            //logger.info("Loading sentences and tokens...");  
            
            // Parse sentences and tokens
            List<Sentence> parsedSentences = new ArrayList<>();
            String[] regions = yytext.toString().split("\\r+|\\n+");
            StringBuilder sb = new StringBuilder();
            int startIndex = 0;
            
            try {
                // Tokenization parser
                GDepParser tokenizationParser = new GDepParser(parser.getLanguage(), ParserLevel.TOKENIZATION,
                                                               new LingpipeSentenceSplitter(), false);
                
                // Launch parser
                if (!tokenizationParser.isLaunched()) {
                    tokenizationParser.launch();
                }
                
                for (String region : regions) {
                    
                    // Parse sentences
                    List<Sentence> regionParsedSentences = tokenizationParser.parseWithLevel(ParserLevel.TOKENIZATION, corpus, region);
                    
                    for(Sentence sentence : regionParsedSentences) {                        
                        // Set sentence indexes
                        sentence.setStart(sentence.getStart() + startIndex);
                        sentence.setEnd(sentence.getEnd() + startIndex);
                        sentence.setOriginalStart(sentence.getOriginalStart() + startIndex);
                        sentence.setOriginalEnd(sentence.getOriginalEnd() + startIndex);                        
                    }
                    
                    parsedSentences.addAll(regionParsedSentences);
                    startIndex += region.length();
                    
                    // Count change lines
                    int textIndex = startIndex;
                    String text = corpus.getText();
                    while((textIndex < text.length()) && (text.charAt(textIndex) == '\n')) {
                        startIndex++;
                        textIndex++;
                    }
                    
                    // Set <roi> tags
                    sb.append("<roi>");
                    sb.append(region);
                    sb.append("</roi>");
                }
            
                // Close parser
                tokenizationParser.close();
                
            } catch (IOException | NejiException ex) {
                throw new RuntimeException("There was a problem parsing the sentences and tokens.", ex);
            }
                        
            //System.out.println();
            //logger.info("Setting annotations...");
            
            // Set sentences annotations
            setAnnotations(parsedSentences, annotations, corpus.getText());
            
            // Set <roi> and </roi> tags            
            yytext.replace(start, yytext.length(), sb.toString());
        }
    };
    
    /**
     * Gets A1 annotations from an input stream.
     * @param inputAnnotations annotations from .a1 file
     * @param entitiesList entities to match
     * @return list with A1 annotations.
     * @throws NejiException
     */
    private List<A1Annotation> getAnnotations(InputStream inputAnnotations, List<String> entitiesList) throws NejiException {
        
        List<A1Annotation> annotations = new ArrayList<>();
        
        try {
            InputStreamReader isr = new InputStreamReader(inputAnnotations);
            BufferedReader br = new BufferedReader(isr);
            String line;
            String parts[];
            int startChar;
            int endChar;
            String entity;
            
            while ((line = br.readLine()) != null) {
                
                // Ignore lines that don't correspond to entity annotations (T),
                // like note annotations (#) and normalization annotations (N)
                if (!line.startsWith("T")) {
                    continue;
                }                
                
                // example format: T0	DISO 122 134	Hypertension
                parts = line.split("\\s+");
                entity = parts[1];
                startChar = Integer.parseInt(parts[2]);
                endChar = Integer.parseInt(parts[3]);
                
                // Verify if it matches the corpus entity
                if ((entitiesList != null) && !entitiesList.contains(entity.toLowerCase())) {
                    continue; // go to next one
                }
                
                A1Annotation a = new A1Annotation(entity, startChar, endChar);
                
                // For annotations with the same start, use the largest one
                int otherIndex = annotations.indexOf(a);
                if (otherIndex != -1)
                {
                    A1Annotation other = annotations.get(otherIndex);
                    if (a.getEndChar() > other.getEndChar()) {
                        annotations.remove(other);
                        annotations.add(a);
                    }
                } else {
                    annotations.add(a);
                }
            }
        } catch (IOException ex) {
            throw new NejiException("There was a problem reading the annotations file.", ex);
        }
        
        return annotations;
    }
    
    /**
     * 
     * @param s sentences list
     * @param annotations annotations list
     */
    private void setAnnotations(List<Sentence> sentences, List<A1Annotation> annotations, String corpusText) {
        
        if (annotations == null) {
            return;
        }

        int previousTextLength = 0;
        
        for (Sentence s : sentences) {

            Annotation a;
            int tokenIndex;
            Token t;
            int firstIndex;
            int lastIndex = 0;
            boolean notFound;
            
            // Sentece annotations list
            List<A1Annotation> sentenceAnnotations = new ArrayList<>();
            for (A1Annotation ann : annotations) {
                if (ann.getStartChar() < s.getEnd()) {
                    sentenceAnnotations.add(ann);
                }
            }
            
            annotations.removeAll(sentenceAnnotations);
            
            for (A1Annotation a1a : sentenceAnnotations) {
                
                tokenIndex = 1;
                t = s.getToken(0);

                //System.out.println("Vou procurar: " + a1a.toString());
                //(new Scanner(System.in)).nextLine();
                
                // Get first token of the annotation
                notFound = false;
                while (t.getStart() + previousTextLength != a1a.getStartChar()) {
                    
                    //System.out.println("Start: " + (t.getStart() + previousTextLength) + " " + a1a.getStartChar());
                    //(new Scanner(System.in)).nextLine();

                    // Verify index
                    if (tokenIndex >= s.size()) {
                        //*System.out.println("buh");
                        notFound = true;
                        break;
                    }
                    
                    // Get next token           
                    t = s.getToken(tokenIndex);

                    // Increment index
                    tokenIndex++;
                }
                
                // Verify if the first token was found (if not go to the next annotation)
                if (notFound) {
                    //System.out.println("*** Eu entrei no primeiro! ***");
                    continue;
                }

                firstIndex = tokenIndex - 1;
                //System.out.println("Encontrei o start: " + s.getToken(tokenIndex-1).getText());
                //(new Scanner(System.in)).nextLine();
                                
                // Get last token of the annotation
                notFound = false;
                while (t.getEnd() + previousTextLength < a1a.getEndChar() - 1) {

                    //System.out.println("End: " + (t.getStart() + previousTextLength) + " " + a1a.getStartChar());
                    
                    // Verify index
                    if (tokenIndex >= s.size()) {
                        notFound = true;
                        break;
                    }

                    // Get next token
                    t = s.getToken(tokenIndex);

                    // Increment index
                    tokenIndex++;
                }

                // Verify if the first token was found (if not go to the next annotation)
                if (notFound) {
                    //System.out.println("*** Eu entrei no utlimo! ***");
                    continue;
                }

                lastIndex = tokenIndex - 1;
                //System.out.println("Encontrei o end: " + s.getToken(tokenIndex-1).getText());
                //System.out.println("FirstIndex=" + firstIndex + " <--> LastIndex=" + lastIndex);
                //(new Scanner(System.in)).nextLine();
                
                // Set annotation
                a = AnnotationImpl.newAnnotationByTokenPositions(s, firstIndex, lastIndex, 1.0);
                s.addAnnotationLabels(a);
            }
            
            // Update
            previousTextLength += s.getText().length();
            for (int i = s.getEnd() ; i < corpusText.length() ; i++) {
                char c = corpusText.charAt(i);
                if ((c == '\n') || (c == ' ') || (c == '\t') || (c == '\r')) {
                    previousTextLength++;
                } else {
                    break;
                }
            }           
        }
    }
    
    /**
     * Class to read A1 Annotations.
     */
    private class A1Annotation implements Comparable<A1Annotation>{

        /**
         * Entity of the annotation.
         */
        private String entity;
        
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
         * @param entity Entity of the annotation.
         * @param startChar Index of the first char.
         * @param endChar Index of the last char.
         */
        public A1Annotation(String entity, int startChar, int endChar) {
            this.entity = entity;
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
         * Get the annotation entity.
         *
         * @return The entity.
         */
        public String getEntity() {
            return entity;
        }

        /**
         * Compare two A1 annotations.
         *
         * @param obj The {@link A1Annotation} to be compared with.
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
            final A1Annotation other = (A1Annotation) obj;
            if (this.startChar != other.startChar) {
                return false;
            }
            return true;
        }

        /**
         * Override the hashCode method to consider all the internal variables.
         *
         * @return Unique number for each A1 annotation.
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
            return entity + " (" + startChar + "," + endChar + ")";
        }

        @Override
        public int compareTo(A1Annotation other) {
            if (this.startChar < other.getStartChar()) {
                return -1;
            }
            
            return 1;
        }
    }
    
    @Override
    public InputFormat getFormat() {
        return InputFormat.A1;
    }

    @Override
    public Collection<ParserLevel> getLevels() {
        return ParserSupport.getEqualOrLowerSupportedLevels(parser.getTool(), parser.getLanguage(), parser.getLevel());
    }
}
