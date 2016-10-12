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

package pt.ua.tm.neji.evaluation.whatizit;

import monq.jfa.ByteCharSource;
import monq.jfa.DfaRun;
import monq.jfa.Xml;
import org.apache.commons.lang3.StringEscapeUtils;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.evaluation.Concept;
import pt.ua.tm.neji.evaluation.ConceptList;
import pt.ua.tm.neji.exception.NejiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class XML2A1Module extends BaseModule {

    private Map<String, String> map;
    private StringBuilder text;
    private ConceptList conceptList;
    private int startTag;
    private int previousEnd;
    Pattern idsPattern = Pattern.compile("ids=\"(.+?)\"");

    private static final String tagSPEC = "z:species";
    private static final String tagPRGE = "z:uniprot";
    private static final String tagGO = "z:go";
    private static final String tagCHED = "z:chebi";
    private static final String tagDISO = "z:e";

    public XML2A1Module() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(category_start, tagSPEC, tagPRGE, tagCHED, tagGO, tagDISO);
        super.addActionToXMLTag(spec_end, tagSPEC);
        super.addActionToXMLTag(prge_end, tagPRGE);
        super.addActionToXMLTag(ched_end, tagCHED);
        super.addActionToXMLTag(go_end, tagGO);
        super.addActionToXMLTag(diso_end, tagDISO);
        super.setEofAction(eof_action);

//        try {
//            Nfa nfa = new Nfa(Nfa.NOTHING);
//
////            nfa.or(Xml.ETag("z:species"), category_end);
////            nfa.or(Xml.ETag("z:species"), category_end);
////            nfa.or(Xml.ETag("z:chebi"), category_end);
////            nfa.or(Xml.ETag("z:disease"), category_end);
////            nfa.or(Xml.ETag("z:e"), category_end);
////            nfa.or(Xml.ETag("z:uniprot"), category_end);
//
//            nfa.or(Xml.STag(tagSPEC), category_start);
//            nfa.or(Xml.ETag(tagSPEC), spec_end);
//
//            nfa.or(Xml.STag(tagPRGE), category_start);
//            nfa.or(Xml.ETag(tagPRGE), prge_end);
//
//            nfa.or(Xml.STag(tagCHED), category_start);
//            nfa.or(Xml.ETag(tagCHED), ched_end);
//
//            nfa.or(Xml.STag(tagGO), category_start);
//            nfa.or(Xml.ETag(tagGO), go_end);
//
//            nfa.or(Xml.STag(tagDISO), category_start);
//            nfa.or(Xml.ETag(tagDISO), diso_end);
//
//            setNFA(nfa, DfaRun.UNMATCHED_COPY, eof);
//        } catch (ReSyntaxException ex) {
//            throw new NejiException(ex);
//        }

        this.conceptList = new ConceptList();
        this.text = new StringBuilder();
        this.map = new HashMap<>();
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


    // NOTE: in the EndAction, runner.collect is set to false... if this module
    // does not work, try swapping the Action class to StartAction

    private EndAction spec_end = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            addTrigger(yytext, start, tagSPEC, "SPEC", true);
        }
    };


    private EndAction prge_end = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            addTrigger(yytext, start, tagPRGE, "PRGE", true);
        }
    };


    private EndAction ched_end = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            addTrigger(yytext, start, tagCHED, "CHED", true);
        }
    };


    private EndAction diso_end = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            addTrigger(yytext, start, tagDISO, "DISO", true);
        }
    };


    private EndAction go_end = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            String annotationTag = yytext.substring(startTag, start + tagGO.length() + 3);

            String annotation = annotationTag.substring(annotationTag.indexOf(">") + 1, annotationTag.lastIndexOf("<"));
            annotation = StringEscapeUtils.unescapeXml(annotation);

            int startAnnotation = text.length();
            int endAnnotation = text.length() + annotation.length();

            text.append(annotation);

            map = Xml.splitElement(yytext, startTag);
            String onto = map.get("onto");
            String group;
            if (onto.equals("cellular_component")) {
                group = "COMP";
            } else if (onto.equals("biological_process") || onto.equals("molecular_function")) {
                group = "PROC_FUNC";
            } else {
                throw new RuntimeException("Non recognized group!");
            }
            Concept concept = new Concept(startAnnotation, endAnnotation, group, annotation);
            concept.getIdentifiers().addAll(getIDs(annotationTag, group));
            conceptList.add(concept);


//            yytext.replace(startTag, start+7, annotation);

            previousEnd = start + tagGO.length() + 3;
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

    private List<String> getIDs(final String annotationTag, final String concept) {
        Matcher matcher = idsPattern.matcher(annotationTag);

//        System.out.println(annotationTag);

        matcher.find();
        String idsText = matcher.group(1);
        String[] ids = idsText.split(",");

        List<String> idsList = new ArrayList<>();
        for (String id : ids) {

            if (concept.equals("PRGE")) {
                id = "UNIPROT:" + id;
            } else if (concept.equals("SPEC")) {
                id = "NCBI:" + id;
            } else if (concept.equals("CHED")) {
                id = "CHEBI:" + id;
            }

            idsList.add(id);
        }

        return idsList;
    }

    private void addTrigger(StringBuffer yytext, int start, String tag, String group, boolean add) {
        String annotationTag = yytext.substring(startTag, start + tag.length() + 3);

        String annotation = annotationTag.substring(annotationTag.indexOf(">") + 1, annotationTag.lastIndexOf("<"));
        annotation = StringEscapeUtils.unescapeXml(annotation);

        int startAnnotation = text.length();
        int endAnnotation = text.length() + annotation.length();

        text.append(annotation);

        if (add) {
            Concept concept = new Concept(startAnnotation, endAnnotation, group, annotation);
            concept.getIdentifiers().addAll(getIDs(annotationTag, group));
            conceptList.add(concept);
        }


        previousEnd = start + tag.length() + 3;
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
