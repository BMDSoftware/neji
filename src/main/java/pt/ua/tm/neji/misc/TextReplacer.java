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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.module.BaseTagger;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Module to remove XML and HTML tags.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
@Requires({})
@Provides({})
public class TextReplacer extends BaseTagger {

    private static Logger logger = LoggerFactory.getLogger(TextReplacer.class);
//    private String newText;
//    private Automaton automaton;

//    private AbstractFaAction text = new AbstractFaAction() {
//        @Override
//        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
//            int size = yytext.length();
//            String text = yytext.toString();
//            text = text.replaceAll(regex.toString(), newText);
//
//            automaton.
//
//            yytext.replace(start, size, text);
//        }
//    };


    public TextReplacer(final String regex, final String newText) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addReplaceAction(regex, newText);
    }
}
