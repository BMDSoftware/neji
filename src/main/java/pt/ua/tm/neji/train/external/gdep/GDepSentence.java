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
package pt.ua.tm.neji.train.external.gdep;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GDepTranslator sentence.
 *
 * @author David Campos (<a
 * href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class GDepSentence {

    /**
     * The corpus of this sentence.
     */
    private GDepCorpus corpus;
    /**
     * A sentence is a set of tokens.
     */
    private ArrayList<GDepToken> sentence;

    /**
     * Constructor.
     *
     * @param corpus The corpus of the sentence.
     */
    public GDepSentence(final GDepCorpus corpus) {
        this.corpus = corpus;
        this.sentence = new ArrayList<GDepToken>();
    }

    public GDepSentence(final GDepCorpus corpus, List<Object> parsingResult) {
        this(corpus);
        
        GDepToken t;
        String token, lemma, pos, chunk, depTag;
        Integer depToken;
        String[] parts;
        
        for (int i = 0; i < parsingResult.size(); i++) {
            parts = parsingResult.get(i).toString().split("\t");
            
            token = null;
            lemma = null;
            pos = null;
            chunk = null;
            depToken = null;
            depTag = null;
            
            
            if (parts.length >= 3) { // Tokenization parsing
                token = parts[1];
            }
            if (parts.length >= 4) { // Lemmatization parsing
                lemma = parts[2];
            }
            if (parts.length >= 5) { // POS parsing
                pos = parts[3];
            }
            if (parts.length >= 6) { // Chunking parsing
                chunk = parts[3];
                pos = parts[4];
            }
            if (parts.length >= 8) { // Dependency Parsing
                depToken = Integer.valueOf(parts[6]) - 1;
                depTag = parts[7];
                
                pos = parts[4];
                chunk = parts[3];
            }
            
//            token = parts[1];
//            lemma = parts[2];
//            pos = parts[3];
//            chunk = parts[4];
//
//            depToken = null;
//            depTag = null;
//            if (parts.length > 6) {
//                depToken = Integer.valueOf(parts[6]) - 1;
//                depTag = parts[7];
//            }

            t = new GDepToken(token, lemma, pos, chunk, depToken, depTag);
            this.sentence.add(t);
        }
    }

    /**
     * Add token to sentence.
     *
     * @param token The token.
     */
    public void addToken(final GDepToken token) {
        sentence.add(token);
    }

    /**
     * Get specific token of the sentence.
     *
     * @param i The index of the token.
     * @return The GDepTranslator token.
     */
    public GDepToken getToken(final int i) {
        return sentence.get(i);
    }

    /**
     * Get the size of the sentence, the number of tokens.
     *
     * @return The number of tokens.
     */
    public int size() {
        return sentence.size();
    }

    /**
     * Get the GDepTranslator corpus of the GDepTranslator sentence.
     *
     * @return The GDepTranslator corpus.
     */
    public GDepCorpus getCorpus() {
        return corpus;
    }
}
