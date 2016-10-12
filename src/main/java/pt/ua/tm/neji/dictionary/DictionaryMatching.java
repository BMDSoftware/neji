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

package pt.ua.tm.neji.dictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.train.config.Resources;
import pt.ua.tm.neji.exception.NejiException;
import uk.ac.man.entitytagger.Mention;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Class that contains all common dictionary matching operations
 * used by DictionaryHybrid and DictionaryTagger
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class DictionaryMatching {
    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private Logger logger = LoggerFactory.getLogger(DictionaryMatching.class);
    private Pattern stopwords;

    public DictionaryMatching() throws NejiException {
        try{
            stopwords = Resources.getStopwordsPattern();
        } catch(Exception e) {
            throw new NejiException(e);
        }
    }

    // Filter mentions to remove intersections (does not happen frequently)
    public List<Mention> removeList(List<Mention> mentions) {
        List<Mention> toRemove = new ArrayList<Mention>();
        for (int i = 0; i < mentions.size() - 1; i++) {
            for (int j = i + 1; j < mentions.size(); j++) {
                Mention m1 = mentions.get(i);
                Mention m2 = mentions.get(j);
                int size_m1 = m1.getText().length();
                int size_m2 = m2.getText().length();

                if (m1.getStart() >= m2.getStart() && m1.getStart() <= m2.getEnd()) {
                    if (size_m1 > size_m2) {
                        if (!toRemove.contains(m2)) {
                            toRemove.add(m2);
                        }
                    } else {
                        if (!toRemove.contains(m1)) {
                            toRemove.add(m1);
                        }
                    }
                } else if (m1.getEnd() >= m2.getStart() && m1.getEnd() <= m2.getEnd()) {
                    if (size_m1 > size_m2) {
                        if (!toRemove.contains(m2)) {
                            toRemove.add(m2);
                        }
                    } else {
                        if (!toRemove.contains(m1)) {
                            toRemove.add(m1);
                        }
                    }
                }
            }
        }
        return toRemove;
    }

    // Get pattern for stopwords recognition
//    Pattern getStopwordsPattern() {
//        return stopwords;
//    }

    // Discard stopwords recognition
    public boolean discardStopwords(Mention m) {
        return stopwords.matcher(m.getText()).matches();
    }
}
