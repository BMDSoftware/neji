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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;
import uk.ac.man.entitytagger.matching.matchers.ACIDMatcher;
import uk.ac.man.entitytagger.matching.matchers.RegexpMatcher;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 *
 * @author david
 */
public class TestRegexMatcher {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(LexEBIReader.class);

    public static void main(String[] args) {
        String text = "The present study was designed to test the hypothesis NOS-2 that inactivation of virtually any component within the pathway containing the BRCA1 and BRCA2 proteins would increase the risks for lymphomas and leukemias. In people who do not have BRCA1 or BRCA2 gene mutations, the encoded proteins prevent breast/ovarian cancer. However BRCA1 and BRCA2 proteins have multiple functions including participating in a pathway that mediates repair of DNA double strand breaks by error-free methods. Inactivation of BRCA1, BRCA2 or any other critical protein within this \"BRCA pathway\" due to a gene mutation should inactivate this error-free repair process. DNA fragments produced by double strand breaks are then left to non-specific processes that rejoin them without regard for preserving normal gene regulation or function, so rearrangements of DNA segments are more likely. These kinds of rearrangements are typically associated with some lymphomas and leukemias.";

//        String dictionaryFilePath = "/Volumes/data/Dropbox/phd/work/platform/code/neji/resources/lexicons/prge/lexebi_prge_human_preferred_regex.tsv";
        String dictionaryFilePath = "/Volumes/data/Dropbox/phd/work/platform/code/neji/resources/lexicons/prge/lexebi_prge_human_synonyms_regex.tsv";


        logger.info("Loading dictionary...");
        HashMap<String, Pattern> patterns = ACIDMatcher.loadPatterns(new File(dictionaryFilePath)).getA();
        Matcher m = new RegexpMatcher(patterns);
        
        
        logger.info(" done, loaded " + m.size() + " patterns.\n");

        logger.info("Performing matching...");
//        List<Mention> mentions = m.match(text, (Document) null);

        List<Mention> mentions = match(text, patterns);

        logger.info("Number of mentions: {}", mentions.size());

        for (Mention me : mentions) {
            logger.info("[{},{}]{} - {}", new Object[] {me.getStart(), me.getEnd(), me.getText(), me.getIdsToString()});
        }
    }

    private static List<Mention> match(String text, Map<String, Pattern> hashmap) {
        List<Mention> matches = new ArrayList<Mention>();
        Iterator<String> keys = hashmap.keySet().iterator();

        
        
        while (keys.hasNext()) {
            String key = keys.next();
            java.util.regex.Matcher m = hashmap.get(key).matcher(text);

            while (m.find()) {
                Mention match = new Mention(new String[]{key}, m.start(), m.end(), text.substring(m.start(), m.end()));
                matches.add(match);

//                if (doc != null) {
//                    match.setDocid(doc.getID());
//                }
//                if (Matcher.isValidMatch(text, match) && (doc == null || doc.isValid(m.start(), m.end()))) {
//                    matches.add(match);
//                }
            }
        }

        return matches;
    }
}
