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
package pt.ua.tm.neji.train.features.mallet;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import java.util.ArrayList;
import pt.ua.tm.neji.core.Constants.DictionaryType;
import pt.ua.tm.neji.train.config.ModelConfig;

/**
 * Parse input data and convert it into MALLET format.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Input2TokenSequence extends Pipe {

    /**
     * Model configuration that contains the features to be used.
     */
    private ModelConfig config;

    /**
     * Constructor.
     * @param config Model configuration.
     */
    public Input2TokenSequence(final ModelConfig config) {
        super(null, new LabelAlphabet());
        this.config = config;
    }

    /**
     * Extract the data and features from input data.
     * @param carrier Raw input data.
     * @return Processed instance with correct data and features.
     */
    @Override
    public Instance pipe(Instance carrier) {
        
        String sentenceLines = (String) carrier.getData();
        
        String[] tokens = sentenceLines.split("\n");
        TokenSequence data = new TokenSequence(tokens.length);
        LabelSequence target = new LabelSequence((LabelAlphabet) getTargetAlphabet(), tokens.length);
        StringBuffer source = new StringBuffer();        

        String text, lemma = "", pos = "", chunk = "", label;

        ArrayList<Token> newTokens = new ArrayList<Token>();
        ArrayList<String> newLabels = new ArrayList<String>();

        for (String t : tokens) {
            String[] features = t.split("\t");

            /*if (features.length != 6) {
                throw new IllegalStateException("Line \"" + t + "\" doesn't have 6 elements: Token, Lemma, POS, Chunk, Dict and Label.");
            }*/            
            
            text = features[0];
            if (config.isLemma())
                lemma = features[1];
            if (config.isPos())
                pos = features[2];
            if (config.isChunk())
                chunk = features[3];          
            label = features[features.length-1];

            // Numbers normalisation
            /*if (nm){
                Pattern num = Pattern.compile("[0-9]+");
                Matcher match = num.matcher(text);
                text = match.replaceAll("0");
                
                match = num.matcher(lemma);
                lemma = match.replaceAll("0");
            }*/
            Token token = new Token(text);

            if (config.isToken())
                token.setFeatureValue("WORD=" + text, 1.0);
            if (config.isLemma())
                token.setFeatureValue(lemma, 1.0);
            if (config.isPos())
                token.setFeatureValue(pos, 1.0);
            if (config.isChunk())
                token.setFeatureValue(chunk, 1.0);
            
            for (int i=4; i<features.length-1; i++){
                if (!config.isPrge() && features[i].contains("LEXICON=" + DictionaryType.PRGE))
                    continue;
                if (!config.isConcepts() && features[i].contains("LEXICON=" + DictionaryType.CONCEPT))
                    continue;
                if (!config.isVerbs() && features[i].contains("LEXICON=" + DictionaryType.VERB))
                    continue;
                if (!config.isNLP() && (
                        features[i].contains("SUB=") ||
                        features[i].contains("OBJ=") ||
                        features[i].contains("NMOD_OF=") ||
                        features[i].contains("NMOD_BY=") ||
                        features[i].contains("VMOD_OF=") ||
                        features[i].contains("VMOD_BY=")))
                    continue;
                
                token.setFeatureValue(features[i], 1.0);
            }
            
            newTokens.add(token);
            newLabels.add(label);


            source.append(text);
            source.append(" ");
        }

        // Invert direction
        /*if (config.isReverse()){
            Collections.reverse(newTokens);
            Collections.reverse(newLabels);
            source = source.reverse();
        }*/

        // Add Tokens to Data
        for (Token t:newTokens){
            StringBuilder sb = new StringBuilder(t.getText());
            /*if (config.isReverse())
                sb = sb.reverse();*/
            t.setText(sb.toString());
            data.add(t);
        }

        // Add labels to Target
        for (String l:newLabels)
            target.add(l);

        carrier.setData(data);
        carrier.setTarget(target);
        carrier.setSource(source);

        return carrier;
    }
}
