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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.writer;

import monq.jfa.DfaRun;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationType;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree.TreeTraversalOrderEnum;
import pt.ua.tm.neji.tree.TreeNode;

import java.util.ArrayList;
import java.util.List;
import pt.ua.tm.neji.context.OutputFormat;

/**
 * Writer that provides information following a pipe separated format.
 *
 * @author Tiago Nunes (<a href="mailto:tiago.nunes@ua.pt">tiago.nunes@ua.pt</a>))
 */
@Requires({Resource.Tokens, Resource.Annotations})
public class PipeWriter extends BaseWriter {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(PipeWriter.class);
    private static String SEPARATOR = "||";
    private int offset;
    private StringBuilder content;
    private int processedAnnotations;

    public PipeWriter() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToGoofedElement(text_action, "s");
        super.setEofAction(eof_action);
        this.content = new StringBuilder();
        this.offset = 0;
        this.processedAnnotations = 0;
    }

    public PipeWriter(final Corpus corpus) throws NejiException {
        this();
        getPipeline().setCorpus(corpus);
    }

    private Action text_action = new SentenceIteratorDefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start, Sentence nextSentence) {

            // Get start and end of sentence
            int startSentence = yytext.indexOf("<s id=");
            int endSentence = yytext.lastIndexOf("</s>") + 4;

            int realStart = yytext.indexOf(">", startSentence) + 1;
            int realEnd = endSentence - 4;

            // Get sentence with XML tags
            String sentence = yytext.substring(realStart, realEnd);

            //Disambiguator.disambiguate(s, 1, true, true);
            // Disambiguator.discardByDepth(nextSentence, 1);

            yytext.replace(startSentence, endSentence, sentence);

            // Get final start and end of sentence
            int startChar = offset + start;
            int endChar = startChar + sentence.length();

            // Generate sentence on stand-off format
            StringBuilder sb = new StringBuilder();

            // Add annotations text to StringBuilder
            processedAnnotations += getAnnotationsText(nextSentence, sentence, sb, processedAnnotations, startChar);

            // Add sentence standoff format to final content
            content.append(sb.toString());

            // Remove processed input from input
            yytext.replace(0, endSentence, "");

            offset = endChar;
        }
    };

    private EofAction eof_action = new EofAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            yytext.replace(0, yytext.length(), content.toString().trim());
        }
    };


    private int getAnnotationsText(Sentence s, String sentenceText, StringBuilder sb, int startCounting, int offset) {

        List<TreeNode<Annotation>> nodes = s.getTree().build(TreeTraversalOrderEnum.PRE_ORDER);

        int processed = 0;
        for (TreeNode<Annotation> node : nodes) {

            Annotation data = node.getData();

            // Skip intersection parents and root node without IDs
            if (data.getType().equals(AnnotationType.INTERSECTION) ||
                    (node.equals(s.getTree().getRoot()) && data.getIDs().isEmpty())) {
                continue;
            }

            int startAnnotationInSentence = s.getToken(data.getStartIndex()).getStart();
            int endAnnotationInSentence = s.getToken(data.getEndIndex()).getEnd() + 1;

            int startChar = offset + startAnnotationInSentence;
            int endChar = offset + endAnnotationInSentence;

            
            // Corpus Identifier
            String identifier = getPipeline().getCorpus().getIdentifier();
            if ((identifier == null) || (identifier.equals("<none>"))) {
                sb.append("Document");
            } else {
                sb.append(identifier);
                sb.append(".text");
            }
            sb.append(SEPARATOR);
            
            sb.append("Disease_Disorder");
            sb.append(SEPARATOR);

            sb.append(getCUIs(data.getIDs()));
            sb.append(SEPARATOR);
            sb.append(startChar);
            sb.append(SEPARATOR);
            sb.append(endChar);
            sb.append("\n");

            processed++;
        }
        return processed;
    }
    
    private static String getCUIs(List<Identifier> ids) {
        List<String> cuis = new ArrayList<>(ids.size());
        for (Identifier id: ids) {
            cuis.add(id.getID());
        }
        String cuisStr = StringUtils.join(cuis, ';');
        return cuisStr.isEmpty() ? "CUI-less" : cuisStr;
    }
    
    @Override
    public OutputFormat getFormat() {
        return OutputFormat.PIPE;
    }
}
