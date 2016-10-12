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

package pt.ua.tm.neji.evaluation.craft;

import monq.jfa.ByteCharSource;
import monq.jfa.DfaRun;
import monq.jfa.Xml;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.evaluation.Concept;
import pt.ua.tm.neji.evaluation.ConceptList;
import pt.ua.tm.neji.exception.NejiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class XML2A1Module extends BaseModule {

    int annotationStart, annotationEnd;
    private ConceptList conceptList;
    private String group;
    private Map<String, String> map;
    private boolean inAnnotation;


    public XML2A1Module(ConceptList conceptList, final String group) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(annotation_start, "annotation");
        super.addActionToXMLTag(annotation_end, "annotation");
        super.addActionToXMLTag(span, "span");
        super.addActionToGoofedElement(spannedText, "spannedText");

//        try {
//            Nfa nfa = new Nfa(Nfa.NOTHING);
//            nfa.or(Xml.STag("annotation"), annotation_start);
//
//            nfa.or(Xml.EmptyElemTag("span"), span);
//
//            nfa.or(Xml.GoofedElement("spannedText"), spannedText);
//
//            nfa.or(Xml.ETag("annotation"), annotation_end);
//            setNFA(nfa, DfaRun.UNMATCHED_COPY);
//        } catch (ReSyntaxException ex) {
//            throw new NejiException(ex);
//        }

        this.conceptList = conceptList;
        this.group = group;
        this.inAnnotation = false;
        this.map = new HashMap<>();
    }

    private StartAction annotation_start = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inAnnotation = true;
        }
    };

    private EndAction annotation_end = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inAnnotation = false;
        }
    };

    private DefaultAction span = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            if (inAnnotation) {
                map = Xml.splitElement(yytext, start);
                annotationStart = Integer.parseInt(map.get("start"));
                annotationEnd = Integer.parseInt(map.get("end"));
            }
        }
    };

    private DefaultAction spannedText = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            if (inAnnotation) {
                String annotationText = yytext.substring(yytext.indexOf(">") + 1, yytext.lastIndexOf("<"));
                Concept concept = new Concept(annotationStart, annotationEnd, group, annotationText);
                if (!conceptList.contains(concept)
//                        && !annotationText.contains("...")
                        ) {
                    conceptList.add(concept);
                }
            }
        }
    };

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
