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

import pt.ua.tm.neji.exception.NejiException;
import uk.ac.man.entitytagger.matching.Matcher;
import uk.ac.man.entitytagger.matching.matchers.VariantDictionaryMatcher;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Class that loads String lines and converts them into a dictionary matcher using the VariantDictionaryMatcher class.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class VariantMatcherLoader {

    private Map<String, Set<String>> map;

    private boolean ignoreCase;

    private boolean wasLoaded;


    public VariantMatcherLoader(boolean ignoreCase) {
        this.map = new HashMap<>();
        this.ignoreCase = ignoreCase;
        this.wasLoaded = false;
    }

    /**
     * Loads a single String line and recognizes terms and IDs to be matched.
     * The specified line must be in the TSV format, by separating IDs from associated terms with a
     * tab ("\t") and separate terms from each other with a pipe ("|").
     *
     * This method will NOT create the matcher immediately, which allows the user to create a matcher from
     * multiple lines by calling this method multiple times. To create and return the matcher based on the
     * loaded lines, the user must call the 'getMatchers()' method.
     *
     * @param line the line to be loaded
     */
    public VariantMatcherLoader load(String line) {
        Pattern tabPattern = Pattern.compile("\t");
        Pattern pipePattern = Pattern.compile("\\|");

        String[] fields = tabPattern.split(line);

        if (ignoreCase)
            fields[1] = fields[1].toLowerCase();

        String[] names  = pipePattern.split(fields[1]);
        for (String n : names){
            if (!map.containsKey(n))
                map.put(n, new HashSet<String>());
            map.get(n).add(fields[0]);
        }

        wasLoaded = true;
        return this;
    }

    /**
     * Returns the matcher for every loaded String. Before using this method, the user must call the 'load'
     * method at least once.
     *
     * @return a matcher of all the loaded String lines
     * @throws NejiException if the 'load' method was not used at least once prior to calling this method
     */
    public Matcher getMatcher() throws NejiException {
        if(!wasLoaded)
            throw new NejiException(
                    "Nothing was loaded prior to calling this method! " +
                    "The 'load' method must be used at least once.");

        String[] terms = new String[map.size()];

        int i = 0;
        for (String term : map.keySet()){
            terms[i++] = term;
        }

        Arrays.sort(terms);
        String[][] termToIdsMap = new String[terms.length][];
        for (int k = 0; k < terms.length; k++) {
            termToIdsMap[k] = map.get(terms[k]).toArray(new String[0]);
        }

        return new VariantDictionaryMatcher(termToIdsMap, terms, true);
    }

    public static Dictionary loadDictionaryFromLines(List<String> lines) throws NejiException {
        VariantMatcherLoader matcherLoader = new VariantMatcherLoader(true);

        String line = lines.get(0);
        String[] parts = line.split("\t");
        String[] fields = parts[0].split(":");
        String group = fields[3];

        for (String l : lines) {
            matcherLoader.load(l);
        }

        return new Dictionary(matcherLoader.getMatcher(), group);
    }
}