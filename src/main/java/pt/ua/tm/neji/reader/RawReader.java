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
 * Module to tag regions of interest from raw data.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Provides({Resource.Passages})
public class RawReader extends BaseReader {

    private static Logger logger = LoggerFactory.getLogger(RawReader.class);

    public RawReader() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToRegex(text_action, ".+");
    }

    private DefaultAction text_action = new DefaultAction(){
        @Override
        public void execute(StringBuffer yytext, int start) {            
            // Get corpus
            Corpus corpus = getPipeline().getCorpus();
            
            StringBuilder sb = new StringBuilder();
            sb.append("<roi>");

            String textWithoutProblems = solveEscaping(yytext.toString());
            
            // Set corpus text
            corpus.setText(textWithoutProblems);
            
            // Unescape XML Tags
            String unescapedText = unescapeXML(textWithoutProblems);


            sb.append(unescapedText);
            sb.append("</roi>");

            yytext.replace(start, yytext.length(), sb.toString());
        }
    };

    @Override
    public InputFormat getFormat() {
        return InputFormat.RAW;
    }
}
