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

package pt.ua.tm.neji.reader;


import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.module.BaseReader;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Module to tag regions of interest from a XML document.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Provides({Resource.Passages, Resource.Tags})
public class XMLReader extends BaseReader {

    private static Logger logger = LoggerFactory.getLogger(XMLReader.class);
    private int startText;
    private StringBuilder originalTextSb;

    public XMLReader(String... xmlTags) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_action, xmlTags);
        super.addActionToXMLTag(end_action, xmlTags);
        startText = 0;
        originalTextSb = new StringBuilder();        
    }

    private StartAction start_action = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            startText = yytext.indexOf(">", start) + 1;
        }
    };

    private EndAction end_action = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {            
            StringBuilder sb = new StringBuilder();
            sb.append("<roi>");
            
            // Solve escaping problems from MEDLINE
//                String textWithoutProblems = XMLParsing.solveXMLEscapingProblems(yytext.substring(startText, start));
            String textWithoutProblems = solveEscaping(yytext.substring(startText, start));

            // Unescape XML Tags
            String unescapedText = unescapeXML(textWithoutProblems);

            sb.append(unescapedText);
            sb.append("</roi>");

            yytext.replace(startText, start, sb.toString());
            
            // Set corpus text
            Corpus corpus = getPipeline().getCorpus();
            originalTextSb.append(textWithoutProblems);
            originalTextSb.append('\n');
            corpus.setText(originalTextSb.toString());
        }
    };

    @Override
    public InputFormat getFormat() {
        return InputFormat.XML;
    }
}
