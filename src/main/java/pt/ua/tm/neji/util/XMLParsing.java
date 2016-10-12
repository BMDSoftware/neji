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

package pt.ua.tm.neji.util;

import monq.jfa.DfaRun;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for XML.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class XMLParsing {

    //    private static final DfaRun ampRun = Regex.getReplaceRun("&amp;#", "&#");
//    private static final DfaRun nbsRun = Regex.getReplaceRun("\u00A0", " ");
    private static Logger logger = LoggerFactory.getLogger(XMLParsing.class);

    public static String solveXMLEscapingProblems(String text, DfaRun nbsRun, DfaRun ampRun) {
        // Remove no breaking white spaces
        text = Regex.replace(text, nbsRun);
//        text = text.replaceAll("\u2002", " ");

        // Solve MEDLINE bug of HTML codes escaped twice
        text = StringEscapeUtils.unescapeXml(text);

        // Solve MEDLINE bug that escapes HTML4 hex codes
        text = Regex.replace(text, ampRun);

        return text;
    }

    public static String solveXMLEscapingProblems(String text) {
        // Remove no breaking white spaces
//        text = text.replaceAll("\\u00A0", " ");
        text = Regex.replace(text, "\u00A0", " ");
//        text = Regex.replace(text, nbsRun);

        // Solve MEDLINE bug that puts an XML tag with various lines
//        text = text.replaceAll("\n\\s+", " ");
//        text = text.replaceAll("\n", " ");

        // Solve MEDLINE bug of HTML codes escaped twice
        text = StringEscapeUtils.unescapeXml(text);

        // Solve MEDLINE bug that escapes HTML4 hex codes
//        text = text.replaceAll("&amp;#", "&#");
        text = Regex.replace(text, "&amp;#", "&#");
//        text = Regex.replace(text, ampRun);

        return text;
    }
}
