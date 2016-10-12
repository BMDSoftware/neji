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

package pt.ua.tm.neji.util.lvg;

import gov.nih.nlm.nls.lvg.Api.LvgCmdApi;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.util.stopwords.Stopwords;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 07/07/15.
 */
public class LVG {


    private static LvgCmdApi lvgApi;
    private static boolean isInit = false;

    private static void init() throws FileNotFoundException, NejiException {
        String lvgConfigFile = "/Users/david/Projects/lvg2015/data/config/lvg.properties";
//        lvgApi = new LvgCmdApi("-f:A -f:a -f:d -f:e -f:An -f:C -f:Ct -f:rs -f:i -f:is -f:o -f:P -f:s -f:S -f:Si -f:t -f:T -f:u", lvgConfigFile);
        lvgApi = new LvgCmdApi("-f:A -f:e -f:An -f:C -f:Ct -f:rs -f:i -f:is -f:o -f:P -f:s -f:S -f:Si -f:t -f:T -f:u", lvgConfigFile);
        lvgApi.GetLvgOutputOption().SetOutCategory(128);
        isInit = true;
    }

    public static Set<String> getLVGNames(final String n) throws Exception {
        if (!isInit) {
            init();
        }

        if (n.length() <= 2 || Stopwords.isStopword(n)) {
            return new HashSet<>();
        }

        Set<String> ret = new HashSet<>();
        String outputFromLvg = lvgApi.MutateToString(n.toLowerCase().trim());
        String[] lines = outputFromLvg.split("\n");

        for (String line : lines) {
            String[] parts = line.split("[|]");

            String name = parts[1].toLowerCase();
            name = name.trim();
            name = name.toLowerCase();

            if (name.length() > 2 && !Stopwords.isStopword(name)) {
                ret.add(name);
            }
        }

        return ret;
    }


}
