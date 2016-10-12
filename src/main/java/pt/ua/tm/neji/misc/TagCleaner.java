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
import pt.ua.tm.neji.exception.NejiException;

/**
 * Module to remove XML and HTML tags.
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class TagCleaner extends BaseTagger {

    private static Logger logger = LoggerFactory.getLogger(TagCleaner.class);
    private int startTag;
    private int startContent;
    boolean inTag;

    public TagCleaner() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToTag(text_action, ".+");
    }
    
    public TagCleaner(String tag) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_action, tag);
        super.addActionToXMLTag(end_action, tag);
        this.startTag = 0;
        this.startContent = 0;
        this.inTag = false;
    }

    private Action text_action = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            int size = yytext.length();
            String text = yytext.toString();
            text = text.replaceAll("\\<.*?>", "");
            yytext.replace(start, size, text);
        }
    };

    private Action start_action = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inTag = true;
            startContent = yytext.indexOf(">", start) + 1;
            startTag = start;
        }
    };

    private Action end_action = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            String content = yytext.substring(startContent, start);
            yytext.replace(startTag, yytext.indexOf(">", start) + 1, content);
            inTag = false;
        }
    };
}
