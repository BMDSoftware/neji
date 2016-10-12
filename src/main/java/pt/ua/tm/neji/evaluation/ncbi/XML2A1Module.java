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

package pt.ua.tm.neji.evaluation.ncbi;

import monq.jfa.ByteCharSource;
import monq.jfa.DfaRun;
import org.apache.commons.lang3.StringEscapeUtils;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.evaluation.Concept;
import pt.ua.tm.neji.evaluation.ConceptList;
import pt.ua.tm.neji.exception.NejiException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class XML2A1Module extends BaseModule {

    private StringBuilder text;
    private ConceptList conceptList;
    private int startTag, previousEnd;

    public XML2A1Module() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(category_start, "category=\"SpecificDisease\"", "category=\"DiseaseClass\"", "category=\"Modifier\"", "category=\"CompositeMention\"");
        super.addActionToXMLTag(category_end, "category");
        super.setEofAction(eof_action);


//        try {
//            Nfa nfa = new Nfa(Nfa.NOTHING);
//            nfa.or(Xml.STag("category=\"SpecificDisease\""), category_start);
//            nfa.or(Xml.STag("category=\"DiseaseClass\""), category_start);
//            nfa.or(Xml.STag("category=\"Modifier\""), category_start);
//            nfa.or(Xml.STag("category=\"CompositeMention\""), category_start);
//            nfa.or(Xml.ETag("category"), category_end);
//
//            setNFA(nfa, DfaRun.UNMATCHED_COPY, eof);
//        } catch (ReSyntaxException ex) {
//            throw new NejiException(ex);
//        }

        this.conceptList = new ConceptList();
        this.text = new StringBuilder();
        this.startTag = 0;
        this.previousEnd = 0;
    }

    private StartAction category_start = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            startTag = start;

            String previous = yytext.substring(previousEnd, start);
            previous = StringEscapeUtils.unescapeXml(previous);
            previous = previous.replaceAll("�+", "”");
            text.append(previous);
        }
    };

    //    private AbstractFaAction category_end = new AbstractFaAction() {
//        @Override
//        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
//
//            int startCategoryTag = yytext.indexOf("<");
//            text.append(yytext.substring(0, startCategoryTag));
//
//            String annotationTag = yytext.substring(startCategoryTag);
//
//            String category = annotationTag.substring(annotationTag.indexOf("\"") + 1, annotationTag.indexOf(">") - 1);
//
//            String annotation = annotationTag.substring(annotationTag.indexOf(">") + 1, annotationTag.lastIndexOf("<"));
//
//            int startAnnotation = text.length();
//            int endAnnotation = text.length() + annotation.length();
//
//            text.append(annotation);
//
////            Concept trigger = new Concept(startAnnotation, endAnnotation, category, annotation);
//            Concept trigger = new Concept(startAnnotation, endAnnotation, "DISO", annotation);
//            conceptList.add(trigger);
//        }
//    };

    private EndAction category_end = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {

            String annotationTag = yytext.substring(startTag, start + "category".length() + 3);

            String annotation = annotationTag.substring(annotationTag.indexOf(">") + 1, annotationTag.lastIndexOf("<"));
            annotation = StringEscapeUtils.unescapeXml(annotation);

            int startAnnotation = text.length();
            int endAnnotation = text.length() + annotation.length();

            text.append(annotation);

            Concept concept = new Concept(startAnnotation, endAnnotation, "DISO", annotation);
            conceptList.add(concept);


            previousEnd = start + "category".length() + 3;
        }
    };

    private EofAction eof_action = new EofAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            String previous = yytext.substring(previousEnd, start);
            previous = StringEscapeUtils.unescapeXml(previous);
            previous = previous.replaceAll("�+", "”");
            text.append(previous);
        }
    };

    public StringBuilder getText() {
        return text;
    }

    public ConceptList getConceptList() {
        return conceptList;
    }

    public void process(final InputStream in) throws NejiException {
        try {
            compile();
            DfaRun run = new DfaRun(getDFA());
            run.setIn(new ByteCharSource(in));
            run.filter();
        } catch (IOException ex) {
            throw new NejiException(ex);
        }
    }
}
