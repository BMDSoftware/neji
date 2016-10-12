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
package pt.ua.tm.neji.ml.postprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.postprocessing.ExtractAbbreviations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Perform abbreviation resolution post-processing.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Abbreviation {
    
    private static Logger logger = LoggerFactory.getLogger(Abbreviation.class);
    
    public static void process(Sentence s, List<Annotation> annotationsML) {
        ExtractAbbreviations extractor = new ExtractAbbreviations();
        String text = s.getText();

        // Get short and long form pairs
        HashMap<String, String> pairs = extractor.extractAbbrPairs(text);        
        Set<String> acronyms = pairs.keySet();
        Iterator<String> it = acronyms.iterator();

        // Deal with short and long forms
        Annotation shortAnnotation, longAnnotation, tmp;
        String shortText, longText;
        while (it.hasNext()) {
            
            shortText = it.next();
            longText = pairs.get(shortText);
            
            shortAnnotation = getAnnotationFromText(s, shortText);
            longAnnotation  = getAnnotationFromText(s, longText);
            
            if(shortAnnotation == null || longAnnotation == null) {

                if(Constants.verbose){
                    logger.info("============================================================");

                    logger.info("SENTENCE: {}", s.getText());
                    logger.info("SENTENCE: {}", s.toExportFormat());
                    logger.info("SHORT: {}", shortText);
                    logger.info("LONG: {}", longText);

                    logger.info("============================================================\n\n\n");
                }

                continue;
            }
            
            if (containsExactAnnotation(shortAnnotation, annotationsML) != null
                    || containsApproximateAnnotation(shortAnnotation, annotationsML) != null
                    || containsExactAnnotation(longAnnotation, annotationsML) != null
                    || containsApproximateAnnotation(longAnnotation, annotationsML) != null) {

                // Add short if it does not exist
                if (containsExactAnnotation(shortAnnotation, annotationsML) == null
                        && containsApproximateAnnotation(shortAnnotation, annotationsML) == null) {
                    annotationsML.add(shortAnnotation);
                }

                // Long form does not exist, add
                if (containsExactAnnotation(longAnnotation, annotationsML) == null
                        && containsApproximateAnnotation(longAnnotation, annotationsML) == null) {
                    annotationsML.add(longAnnotation);
                } // Partial match of long form annotations
                else if (containsExactAnnotation(longAnnotation, annotationsML) == null
                        && ( tmp = containsApproximateAnnotation(longAnnotation, annotationsML) ) != null) {
//                    s.removeAnnotation(tmp);
                    annotationsML.remove(tmp);
//                    s.addAnnotation(longAnnotation);
                    annotationsML.add(longAnnotation);
                }
            }
        }
        
    }

    /**
     * From the whole set of annotations, get the first annotation that is equal
     * to the one provided.
     *
     * @param a The annotation.
     * @return The annotation that is equal to the one provided by argument,
     *         or <code>null</code> otherwise.
     */
    private static Annotation containsExactAnnotation(final Annotation a, final List<Annotation> annotationsML) {
        for (Annotation an : annotationsML) {
            if (an.equals(a)) {
                return an;
            }
        }
        return null;
    }

    /**
     * From the whole set of annotations, get the first annotation that contains
     * the one provided.
     *
     * @param a The annotation.
     * @return The annotation that contains to the one provided by argument,
     *         or <code>null</code> otherwise.
     */
    private static Annotation containsApproximateAnnotation(final Annotation a, final List<Annotation> annotationsML) {
        for (Annotation an : annotationsML) {
            if (an.contains(a) || a.contains(an)) {
                return an;
            }
        }
        return null;
    }

    /**
     * Get the annotation from text.
     *
     * @param s The sentence that contains the annotation.
     * @param text The text of the annotation.
     * @return The annotation that reflect the input text.
     */
    private static Annotation getAnnotationFromText(final Sentence s, final String text) {
        String[] tokens = text.split(" ");
        
        int start;
        int end;
        int count;
        for (int i = 0; i < s.size(); i++) {
            if (s.getToken(i).getText().equals(tokens[0])) {
                end = start = i;
                count = 1;
                for (int j = start + 1; j < s.size() && count < tokens.length && s.getToken(j).getText().equals(tokens[count]); j++) {
                    end++;
                    count++;
                }
                return AnnotationImpl.newAnnotationByTokenPositions(s, start, end, 0.0);
            }
        }
        return null;
    }
}
