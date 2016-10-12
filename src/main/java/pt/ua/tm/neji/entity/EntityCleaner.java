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

package pt.ua.tm.neji.entity;

import monq.jfa.DfaRun;
import pt.ua.tm.neji.core.module.BaseTagger;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Module to clean entity annotations from input stream.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class EntityCleaner extends BaseTagger {

    private boolean inSentence;
    private boolean inEntity;
    private int startEntity;

    public EntityCleaner() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_action, "s");
        super.addActionToXMLTag(end_action, "s");
        super.addActionToXMLTag(start_entity, "e");
        super.addActionToXMLTag(end_entity, "e");

        this.inSentence = false;
        this.inEntity = false;
        this.startEntity = 0;
    }

    private Action start_action = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inSentence = true;
        }
    };

    private Action end_action = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inSentence = false;
        }
    };

    private Action start_entity = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            if (inSentence) {
                inEntity = true;
                startEntity = start;
            }
        }
    };

    private Action end_entity = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            if (inEntity) {

                String entity = yytext.substring(startEntity, start);

                int start_id = entity.indexOf("id=\"") + 4;
                int end_id = entity.indexOf("\">", start_id);
                String text = entity.substring(end_id + 2, entity.length());


                inEntity = false;
                yytext.replace(startEntity, start + 5, text);
            }
        }
    };
}
