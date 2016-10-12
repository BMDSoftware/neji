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

package pt.ua.tm.neji.core.module;

import monq.jfa.DfaRun;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.util.Char;
import pt.ua.tm.neji.util.Regex;
import pt.ua.tm.neji.util.XMLParsing;

import java.util.Arrays;

/**
 * Abstract class that integrates base functionalities of a {@link Reader} module.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public abstract class BaseReader extends BaseTagger implements Reader {
    private DfaRun roiRun, ampRun, nbsRun;

    public BaseReader(DfaRun.FailedMatchBehaviour failedBehaviour) throws NejiException {
        super(failedBehaviour);

        roiRun = Regex.getReplaceRun("\n", "</roi>\n<roi>");
        ampRun = Regex.getReplaceRun("&amp;#", "&#");
        String r = StringUtils.join(Arrays.asList(Char.getNonBreakingWhiteSpaces()), '|');
        nbsRun = Regex.getReplaceRun(r, " ");
    }

    public String solveEscaping(String text){
        // Solve escaping problems from MEDLINE
//            String textWithoutProblems = XMLParsing.solveXMLEscapingProblems(yytext.toString());
        return XMLParsing.solveXMLEscapingProblems(text, nbsRun, ampRun);
    }

    public String unescapeXML(String text){
        StringEscapeUtils.unescapeXml(text);

        // New lines, are new ROIs
//            unescapedText = unescapedText.replaceAll("\n", "</roi>\n<roi>");
//            unescapedText = Regex.replace(unescapedText, "\n", "</roi>\n<roi>");
        return Regex.replace(text, roiRun);
    }
}
