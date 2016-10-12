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

package pt.ua.tm.neji.util.stopwords;

import pt.ua.tm.neji.train.config.Resources;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by david on 07/07/15.
 */
public class Stopwords {

    private static Pattern stopwords;
    private static boolean isInit = false;

    public static void init() {
        // Stopwords
        try {
            stopwords = Resources.getStopwordsPattern();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        isInit = true;
    }

    public static boolean isStopword(final String word) {
        if (!isInit) {
            init();
        }

        String w = word.trim();
        Matcher m = stopwords.matcher(w);
        if (!m.matches()) {
            return false;
        }
        System.out.println("Stopword: " + w);
        return true;
    }
}
