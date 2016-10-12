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

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexMatcher extends Matcher {

    private Map<String, Pattern> hashmap;

    public RegexMatcher(Map<String, Pattern> hashmap) {
        this.hashmap = hashmap;
    }

    @Override
    public List<Mention> match(String string) {
        List<Mention> matches = new ArrayList<Mention>();
        Iterator<String> keys = hashmap.keySet().iterator();

        while (keys.hasNext()) {
            String key = keys.next();
            java.util.regex.Matcher m = hashmap.get(key).matcher(string);

            while (m.find()) {
                Mention match = new Mention(new String[]{key}, m.start(), m.end(), string.substring(m.start(), m.end()));
                matches.add(match);
            }
        }

        return matches;
    }
    
    @Override
    public List<Mention> match(String string, Document dcmnt) {
        return match(string);
    }

    @Override
    public int size() {
        return hashmap.size();
    }

    
}
