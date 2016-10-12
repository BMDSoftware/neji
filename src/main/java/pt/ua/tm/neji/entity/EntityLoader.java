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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.util.Char;

import java.util.List;

/**
 * Module to load entity annotations from the input stream to the internal {@link Corpus}.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class EntityLoader extends BaseLoader {

    /** {@link Logger} to be used in the class. */
    private static Logger logger = LoggerFactory.getLogger(EntityLoader.class);
    private int sentence;
    private boolean inSentence;
    private boolean inEntity;
    private int startEntity;
    private int startSentence;
    private int previousNumChars;

    public EntityLoader() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_sentence, "s");
        super.addActionToXMLTag(end_sentence, "s");
        super.addActionToXMLTag(start_entity, "e");
        super.addActionToXMLTag(end_entity, "e");

        this.sentence = 0;
        this.inSentence = false;
        this.inEntity = false;
        this.startEntity = 0;
        this.startSentence = 0;
        this.previousNumChars = 0;
    }

//    public EntityLoader(final Corpus corpus) throws NejiException {
//        this();
//        getPipeline().setCorpus(corpus);
//    }

    private Action start_sentence = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inSentence = true;
            startSentence = yytext.indexOf(">", start) + 1;
            previousNumChars = 0;
        }
    };

    private Action end_sentence = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inSentence = false;
            sentence++;
        }
    };

    private Action start_entity = new StartAction() {

        @Override
        public void execute(StringBuffer yytext, int start) {
            if (inSentence) {
                //logger.info("ENTITY: {}", yytext);

                /*
                 * map.clear(); Xml.splitElement(map, yytext, start); String
                 * entity = map.get(">");
                 *
                 * logger.info("ENTITY: {}", entity);
                 */

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

                //logger.info("ENTITY: {}", entity);

                int start_id = entity.indexOf("id=\"") + 4;
                int end_id = entity.indexOf("\">", start_id);
                String id = entity.substring(start_id, end_id);

                //logger.info("ID: {}", id);

                String text = entity.substring(end_id + 2, entity.length());
                //logger.info("TEXT: {}", text);

                String sentenceBeforeEntity = yytext.substring(startSentence, startEntity);
                //logger.info("BEFORE ENTITY: {}", sentenceBeforeEntity);

                int numChars = Char.getNumNonWhiteSpaceChars(sentenceBeforeEntity);

                int startEntityChars = previousNumChars + numChars;
                int endEntityChars = previousNumChars + numChars + Char.getNumNonWhiteSpaceChars(text) - 1;

                //logger.info("[{} - {}]", startEntityChars, endEntityChars);

                //String entityType = id.substring(id.lastIndexOf(":") + 1);
                //addAnnotation(corpus.getSentence(sentence), startEntityChars, endEntityChars, entityType, id);

                Sentence s = getPipeline().getCorpus().getSentence(sentence);
                Annotation a = AnnotationImpl.newAnnotationByCharPositions(s, startEntityChars, endEntityChars, 1.0);

                if (a != null) {
                    List<Identifier> ids = Identifier.getIdentifiersFromText(id);
                    a.setIDs(ids);
                    s.addAnnotationToTree(a);
                }

                //logger.info("");

                inEntity = false;
                startSentence = start + 4;
                previousNumChars = endEntityChars + 1;
            }
        }
    };
}
