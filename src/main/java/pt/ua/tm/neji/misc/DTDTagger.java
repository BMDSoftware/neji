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

package pt.ua.tm.neji.misc;

import monq.jfa.DfaRun;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.module.BaseTagger;
import pt.ua.tm.neji.exception.NejiException;

import java.util.logging.Logger;

/**
 * Module to replace the DTD header.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class DTDTagger extends BaseTagger {

    /**
     * {@link Logger} to be used in the class.
     */
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(DTDTagger.class);
    private boolean inDocType;
    private int startDocType;
    private String header;

    /**
     * Constructor that sets the DTD header as a default one.
     */
    public DTDTagger() throws NejiException{
        this("<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2011//EN\" \"http://bioinformatics.ua.pt/support/gimli/pubmed/gimli.dtd\">");
    }

    /**
     * Constructor that sets the DTD header as the specified string.
     * @param header the header to be used by this module
     */
    public DTDTagger(String header) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToTag(start_action, "<\\!DOCTYPE PubmedArticleSet");
        super.addActionToTag(end_action, ">");
        this.header = header;
        inDocType = false;
        startDocType = 0;

//        try {
//            Nfa nfa = new Nfa(Nfa.NOTHING);
//            nfa.or("<\\!DOCTYPE PubmedArticleSet", start_doctype);
//            nfa.or(">", end_doctype);
//            setNFA(nfa, DfaRun.UNMATCHED_COPY);
//        } catch (ReSyntaxException ex) {
//            throw new NejiException(ex);
//        }
    }

    private Action start_action = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inDocType = true;
            startDocType = start;
        }
    };

    private Action end_action = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            if (inDocType) {
                int end = yytext.indexOf(">", start) + 1;

                yytext.replace(startDocType, end, header);

                inDocType = false;
            }
        }
    };
}
