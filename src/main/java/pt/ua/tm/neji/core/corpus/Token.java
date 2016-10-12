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
package pt.ua.tm.neji.core.corpus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import pt.ua.tm.neji.core.Constants.LabelTag;
import pt.ua.tm.neji.train.external.gdep.GDepSentence;
import pt.ua.tm.neji.train.external.gdep.GDepToken;

/**
 * Class that represents a Token.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class Token implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The sentence to which this token is part of.
     */
    private Sentence sentence;

    /**
     * The index of the first character of the token. This counting discards
     * white spaces.
     */
    private int start;

    /**
     * The index of the last character of the token. This counting discards
     * white spaces.
     */
    private int end;

    /**
     * The index of the token in the sentence.
     */
    private int index;

    /**
     * The set of features of the token for each key (POS, LEMMA, etc...)
     */
    private Multimap<String, String> featuresMap;

    /**
     * The annotation label of the token.
     */
    private LabelTag label;

    /**
     * Constructor.
     *
     * @param s     The sentence.
     * @param start The first char index.
     * @param end   The last char index.
     * @param index The index in the sentence.
     */
    public Token(final Sentence s, final int start, final int end, final int index) {
        init(s, start, end, index);
    }
    
    /**
     * Constructor using the GDepTranslator Parsing result to build a new Token.
     *
     * @param s     The sentence to which the Token is associated with.
     * @param start The first char index.
     * @param index The index in the sentence.
     * @param gs    The GDepTranslator parsing result of sentence, since we can add features
     *              of the whole sentence to the token.
     */
    public Token(final Sentence s, final int start, final int index, final GDepSentence gs) {
        GDepToken gt = gs.getToken(index);

        // Init token
        int e = start + gt.getText().length() - 1;
        init(s, start, e, index);

        //Add features to Map
        featuresMap.put("LEMMA", gt.getLemma());
        featuresMap.put("POS", gt.getPOS());
        featuresMap.put("CHUNK", gt.getChunk());
        if (gt.getDepToken() == null) {
            featuresMap.put("DEP_TOK", null);
        } else {
            featuresMap.put("DEP_TOK", gt.getDepToken().toString());
        }
        featuresMap.put("DEP_TAG", gt.getDepTag());

        // Add Dependency features to Map
        if (gt.getDepTag() != null && gt.getDepToken() != null) {
            if (gt.getDepTag().equals("OBJ")) {
                featuresMap.put("OBJ", gs.getToken(gt.getDepToken()).getLemma());
            } else if (gt.getDepTag().equals("SUB")) {
                featuresMap.put("SUB", gs.getToken(gt.getDepToken()).getLemma());
            } else if (gt.getDepTag().equals("NMOD")) {
                featuresMap.put("NMOD_OF", gs.getToken(gt.getDepToken()).getLemma());
            }

            GDepToken gt2;
            for (int i = 0; i < gs.size(); i++) {
                gt2 = gs.getToken(i);
                if((gt2.getDepToken() == index) && gt2.getDepTag().equals("NMOD")) {
                    if (!featuresMap.containsEntry("NMOD_BY", gt2.getLemma())) {
                        featuresMap.put("NMOD_BY", gt2.getLemma());
                    }
                }
            }
        }
    }
    
    /**
     * Constructor used to load a token of a corpus stored in a file.
     *
     * @param s            The sentence to which the Token is associated with.
     * @param start        The first char index.
     * @param index        The index in the sentence.
     * @param exportFormat The token in the export format.
     */
    public Token(final Sentence s, final int start, final int index, final String exportFormat) {
        String[] parts = exportFormat.split("\\s+");
        String text = parts[0];
        String lemma = parts[1];
        String pos = parts[3];
        String chunk = parts[2];
        String label = parts[parts.length - 1];
        int e = start + text.length() - 1;

        // Init token
        init(s, start, e, index);
        this.label = LabelTag.valueOf(label);

        // Add features to Map
        featuresMap.put("LEMMA", lemma.substring(lemma.indexOf("=") + 1));
        featuresMap.put("POS", pos.substring(pos.indexOf("=") + 1));
        featuresMap.put("CHUNK", chunk.substring(chunk.indexOf("=") + 1));

        for (int i = 4; i < parts.length - 1; i++) {
            int separatorIndex = parts[i].indexOf("=");
            featuresMap.put(parts[i].substring(0, separatorIndex), parts[i].substring(separatorIndex + 1));
        }
    }
    
    /**
     * Default token initialization. Used to simplify the constructors.
     *
     * @param s     The sentence.
     * @param start The first char index.
     * @param end   The last char index.
     * @param index The index in the sentence.
     */
    private void init(final Sentence s, final int start, final int end, final int index) {
        this.sentence = s;
        this.start = start;
        this.end = end;
        this.index = index;
        this.label = LabelTag.O;        
        this.featuresMap = HashMultimap.create();
    }

    /**
     * Get a feature from this token features
     * @param key the designation of the feature
     * @return a list of features
     */
    public List<String> getFeature(String key) {
        return new ArrayList<>(featuresMap.get(key));
    }

    /**
     * Add a feature to tokens features.
     * @param key the feature designation
     * @param value the feature value
     */
    public void putFeature(String key, String value) {
        featuresMap.put(key, value);
    }

    /**
     * Gets token features.
     * @return The token features
     */
    public Multimap<String, String> getFeaturesMap() {
        return featuresMap;
    }

    /**
     * Sets token features.
     * @param featuresMap The token features
     */
    public void setFeaturesMap(Multimap<String, String> featuresMap) {
        this.featuresMap = featuresMap;
    }

    /**
     * Get the first char index.
     *
     * @return The char index.
     */
    public int getStart() {
        return start;
    }

    /**
     * Set the first char index.
     *
     * @param start The first char index.
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Get the last char index.
     *
     * @return The char index.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Set the last char index.
     *
     * @param end The last char index.
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Get the index of the token in the sentence.
     *
     * @return The token index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the annotation tag of the token.
     *
     * @return The tag.
     */
    public LabelTag getLabel() {
        return label;
    }

    /**
     * Set the annotation label of the token.
     *
     * @param label The new label of the token.
     */
    public void setLabel(final LabelTag label) {
        this.label = label;
    }

    /**
     * Get the sentence to which to token makes part of.
     *
     * @return The sentence.
     */
    public Sentence getSentence() {
        return sentence;
    }

    /**
     * Get the t of the token.
     *
     * @return The t.
     */
    public String getText() {
        return sentence.getText().substring(this.getStart(), this.getEnd() + 1);
    }

    /**
     * Get the number of features of the token.
     *
     * @return The number of features.
     */
    public int sizeFeatures() {
        return featuresMap.size();
    }

    /**
     * Get a text representation of the features of the token.
     *
     * @return Text with all the features.
     */
    public String featuresToString() {
        StringBuilder sb = new StringBuilder();
        for (String f : featuresMap.keySet()) {
            if (f.equals("DEP_TOK") || f.equals("DEP_TAG")) continue; // ignore DEP_TOK and DEP_TAG
            sb.append(f);
            sb.append("=");
            sb.append(getFeature(f).get(0));
            sb.append("\t");
        }
        return sb.toString().trim();
    }

    public Token clone(Sentence s) {
        Token newT = new Token(s, start, end, index);
        newT.setLabel(label);
        newT.setFeaturesMap(HashMultimap.create(featuresMap));
        return newT;
    }

    /**
     * Remove all token features.
     */
    public void removeFeatures() {
        featuresMap = HashMultimap.create();
    }

    /**
     * Provides text representation of the token.
     * @return text representation of the token
     */
    @Override
    public String toString() {
        return getText();
    }

    /**
     * Compare two tokens.
     * @param o the token to be compared with.
     * @return <tt>true</tt> if the two tokens are equal or <tt>false</tt> in case otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token other = (Token) o;
        int thisSentenceStart = this.getSentence().getStart();
        int otherSentenceStart = other.getSentence().getStart();
        int thisSentenceEnd = this.getSentence().getEnd();
        int otherSentenceEnd = other.getSentence().getEnd();
        String thisSentenceText = this.getSentence().getText();
        String otherSentenceText = other.getSentence().getText();

        if (thisSentenceStart != otherSentenceStart) return false;
        if (thisSentenceEnd != otherSentenceEnd) return false;
        if (thisSentenceText == null || !thisSentenceText.equals(otherSentenceText)) return false;
        if (index != other.index) return false;
        if (start != other.start) return false;
        if (end != other.end) return false;
        if (featuresMap != null ? !featuresMap.equals(other.featuresMap) : other.featuresMap != null) return false;
        if (label != other.label) return false;

        return true;
    }
    
    /**
     * Set dependency dependencyFeatures.
     */
    public void setDependencyFeatures () {
        
        if (getFeature("DEP_TOK").isEmpty() || getFeature("DEP_TAG").isEmpty()) {            
            return;
        }      
        
        Integer depToken = Integer.parseInt(getFeature("DEP_TOK").get(0));
        String depTag = getFeature("DEP_TAG").get(0);
        
        if (depToken < 0) {
            return;
        }
        
        // Add Dependency features
        String depTokenLemma = sentence.getToken(depToken).getFeature("LEMMA").get(0);
        if (depTag.equals("OBJ")) {
            featuresMap.put("OBJ", depTokenLemma);
        } else if (depTag.equals("SUB")) {
            featuresMap.put("SUB", depTokenLemma);
        } else if (depTag.equals("NMOD")) {
            featuresMap.put("NMOD_OF", depTokenLemma);
        }       
        
        // Add dependency features from other sentence tokens
        Integer dtoken;
        String dtag;
        String lemma;
        for (Token t : sentence.getTokens()) {
            
            // Verify if token has features DEP_TOK and DEP_TAG
            if (t.getFeature("DEP_TOK").isEmpty() || t.getFeature("DEP_TAG").isEmpty()) {
                continue;
            }
            
            // Get token DEP_TOK and DEP_TAG
            dtoken = Integer.parseInt(t.getFeature("DEP_TOK").get(0));
            dtag = t.getFeature("DEP_TAG").get(0);
            
            if ((dtoken == index) && depTag.equals("NMOD")) {
                
                // Verify if token already has this feature
                lemma = t.getFeature("LEMMA").get(0);
                if (!featuresMap.containsEntry("NMOD_BY", lemma)) {
                    featuresMap.put("NMOD_BY", lemma);
                }
            }
        }
    }
}