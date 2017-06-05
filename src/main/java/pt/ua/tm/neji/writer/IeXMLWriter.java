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

package pt.ua.tm.neji.writer;

import monq.jfa.DfaRun;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.NumericEntityEscaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationType;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.disambiguator.Disambiguator;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import pt.ua.tm.neji.core.corpus.Corpus;

/**
 * Writer that provides information following the IeXML inline format.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Tokens})
public class IeXMLWriter extends BaseWriter {

    private static Logger logger = LoggerFactory.getLogger(IeXMLWriter.class);

    private int startSentence;
    private int detail;
    private int nProcessedSentences;
    private static final Pattern CONTROL_CHARS = Pattern.compile("&(#[0-9a-fA-F]{1,2};)");
    private static final CharSequenceTranslator XML_ESCAPER =
            StringEscapeUtils.ESCAPE_XML.with(
                    NumericEntityEscaper.between(0, 8),
                    NumericEntityEscaper.between(10, 31)
            );
    private static final String PARENT_TAG = "sentences";


    public IeXMLWriter() throws NejiException {
        this(2);
    }

    public IeXMLWriter(int detail)throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_action, "s");
        super.addActionToXMLTag(end_action, "s");
        if (detail != 1 && detail != 2) {
            throw new RuntimeException("Nested annotations detail must be 1 or 2.");
        }

        this.detail = detail;
        this.startSentence = 0;
        this.nProcessedSentences = 0;
    }

    private Action start_action = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            startSentence = start;
        }
    };

    private Action end_action = new SentenceIteratorEndAction() {
        @Override
        public void execute(StringBuffer yytext, int start, Sentence nextSentence) {

            // Get sentence from document
            String sentence = yytext.substring(startSentence, start);
            int offset_id = sentence.indexOf('>') + 1;
            sentence = sentence.substring(offset_id);         
            
            // Disambiguate annotations by depth
            Disambiguator.discardByDepth(nextSentence, detail);

            // Get new sentence with annotations
            String newSentence = convertSentenceToXML(nextSentence, sentence);
            
            // Verify last sentece            
            Corpus corpus = nextSentence.getCorpus();
            if (nProcessedSentences == corpus.size()-1) {
                yytext.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><response><text>");
                yytext.append("</text>");
                yytext.append("</response>");
            }

            // Replace annotation in the XML document
            yytext.replace(startSentence + offset_id, start, newSentence);
                        
            nProcessedSentences++;
        }
    };

    private String escapeXML(String text) {
        String out = XML_ESCAPER.translate(text);
        out = CONTROL_CHARS.matcher(out).replaceAll("&amp;$1");
        return out;
    }

    private String convertSentenceToXML(final Sentence s, final String source) {
        StringBuilder sb = new StringBuilder();
        Annotation data;

        int lastEndSource = 0;

        List<TreeNode<Annotation>> nodes = s.getTree().build(1);

        List<TreeNode<Annotation>> processedNodes = new ArrayList<>();
        
        for (TreeNode<Annotation> node : nodes) {

            // Hack for annotation = root with nested
            if (processedNodes.contains(node)) {
                continue;
            }

            // NEW
            data = node.getData();
            if (node.equals(s.getTree().getRoot()) && data.getIDs().isEmpty()) {
                continue;
            }

            int startChar = s.getToken(data.getStartIndex()).getStart();
            int endChar = s.getToken(data.getEndIndex()).getEnd();

            // Copiar o que vai do último End Source, até a este Start Source
            try {
                String prev = source.substring(lastEndSource, startChar);
                if (prev.length() > 0) {
                    // Escape XML Tags
                    prev = escapeXML(prev);
                    sb.append(prev);
                }

                String annotation = source.substring(startChar, endChar + 1);
                boolean isMergedAnnotation = node.hasChildren();

                String ids;
                if (isMergedAnnotation) {
                    annotation = getMergedAnnotation(node, annotation);
                    ids = getMergedIDs(node);

                    // Hack
                    processedNodes.addAll(node.getChildren());

                } else {
                    annotation = escapeXML(annotation);
                    ids = data.getStringIDs();
                }

                sb.append("<e id=\"");
                sb.append(ids);
                sb.append("\">");
                sb.append(annotation);
                sb.append("</e>");

                lastEndSource = endChar + 1;
            } catch (Exception ex) {
                logger.error("ERROR:", ex);
                logger.error("Problem writing sentence to output: {}", s.getText());
                for (TreeNode<Annotation> n : nodes) {
                    Annotation an = node.getData();
                    logger.error("{} - {}", an.getStartIndex(), an.getEndIndex());
                    logger.error("ANNOTATION: {} --- {}", an.getText(), an.getIDs());
                }
            }


            // Hack
            processedNodes.add(node);
        }

        if (lastEndSource < source.length()) {
            String prev = source.substring(lastEndSource, source.length());
            // Escape XML Tags
            prev = escapeXML(prev);
            sb.append(prev);
        }
        
        return sb.toString();
    }

    private String getMergedIDs(TreeNode<Annotation> node) {

        int start, end;
        String range;

        HashMap<String, List<String>> idsMap = new HashMap<String, List<String>>();

        Annotation parentAnnotation = node.getData();
        if (node.hasChildren() && !parentAnnotation.getIDs().isEmpty()) {
            start = 1;
            end = parentAnnotation.getEndIndex() - parentAnnotation.getStartIndex() + 1;
            range = getRange(start, end);

            addIDtoMap(idsMap, range, parentAnnotation.getStringIDs());
        }

        // Children
        for (TreeNode<Annotation> child : node.getChildren()) {
            Annotation childAnnotation = child.getData();

            if (childAnnotation.getType().equals(AnnotationType.INTERSECTION)) {

                for (TreeNode<Annotation> grandChild : child.getChildren()) {
                    Annotation grandChildAnnotation = grandChild.getData();

                    if (grandChildAnnotation.getType().equals(AnnotationType.INTERSECTION)) {
                        continue;
                    }

                    start = grandChildAnnotation.getStartIndex() - parentAnnotation.getStartIndex() + 1;
                    end = grandChildAnnotation.getEndIndex() - parentAnnotation.getStartIndex() + 1;
                    range = getRange(start, end);

                    addIDtoMap(idsMap, range, grandChildAnnotation.getStringIDs());
                }

            } else {
                start = childAnnotation.getStartIndex() - parentAnnotation.getStartIndex() + 1;
                end = childAnnotation.getEndIndex() - parentAnnotation.getStartIndex() + 1;
                range = getRange(start, end);

                addIDtoMap(idsMap, range, childAnnotation.getStringIDs());
            }

//            start = childAnnotation.getStartIndex() - parentAnnotation.getStartIndex() + 1;
//            end = childAnnotation.getEndIndex() - parentAnnotation.getStartIndex() + 1;
//            range = getRange(start, end);
//
//            addIDtoMap(idsMap, range, childAnnotation.getStringIDs());
        }

        // Generate text
        StringBuilder sb = new StringBuilder();

        Iterator<String> it = idsMap.keySet().iterator();
        while (it.hasNext()) {
            range = it.next();
            List<String> ids = idsMap.get(range);
            sb.append("(");
            for (String id : ids) {
                sb.append(id);
                sb.append("|");
            }
            sb.setLength(sb.length() - 1);
            sb.append(")");
            sb.append(":");
            sb.append(range);
            sb.append("|");
        }
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    private String getRange(int start, int end) {
        StringBuilder range = new StringBuilder();
        range.append(start);
        if ((end - start) > 0) {
            range.append(",");
            range.append(end);
        }
        return range.toString();
    }

    private void addIDtoMap(HashMap<String, List<String>> map, String range, String ids) {
        if (ids.isEmpty()){
            return;
        }

        if (map.containsKey(range)) {
            map.get(range).add(ids);
        } else {
            List<String> listIDs = new ArrayList<String>();
            listIDs.add(ids);
            map.put(range, listIDs);
        }
    }

    private String getMergedAnnotation(TreeNode<Annotation> node, String annotationSourceText) {

        StringBuilder sb = new StringBuilder();

        Annotation data = node.getData();
        Sentence s = data.getSentence();

        // Tokens
        int lastEndSource = 0, startChar, endChar, startSource = 0, endSource = 0, countChars = 0, wordCounter = 1;
        int startOffset = s.getToken(data.getStartIndex()).getStart();

        for (int i = data.getStartIndex(); i <= data.getEndIndex(); i++, wordCounter++) {

            Token t = s.getToken(i);

            startChar = t.getStart() - startOffset;
            endChar = t.getEnd() - startOffset;
            
            for (int j = lastEndSource; (j < annotationSourceText.length()) && (countChars <= endChar); j++) {
                if (countChars == startChar) {
                    startSource = j;
                }
                if (countChars == endChar) {
                    endSource = j;
                }
                if (annotationSourceText.charAt(j) == ' ' || annotationSourceText.charAt(j) == '\t') {
                    sb.append(" ");
                }
                countChars++;
            }

            StringBuilder word = new StringBuilder();
            word.append("<w id=\"");
            word.append(wordCounter);
            word.append("\">");
            String text = annotationSourceText.substring(startSource, endSource + 1);
//            String escapeXML = StringEscapeUtils.escapeXml(text);
            String escapeXML = escapeXML(text);
            word.append(escapeXML);
            word.append("</w>");
            sb.append(word);

            lastEndSource = endSource + 1;
        }

        return sb.toString();
    }

    @Override
    public OutputFormat getFormat() {
        return OutputFormat.XML;
    }
}
