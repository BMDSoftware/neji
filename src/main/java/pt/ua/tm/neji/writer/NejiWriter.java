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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.TreeNode;

/**
 * Writer that provides information following the Neji format.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Tokens})
public class NejiWriter extends BaseWriter {

    private static Logger logger = LoggerFactory.getLogger(NejiWriter.class);

    int counter;
    private int offset;
    private StringBuilder content;

    public NejiWriter() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToGoofedElement(text_action, "s");
        super.setEofAction(eof_action);
        this.content = new StringBuilder();
        this.counter = 1;
        this.offset = 0;
    }

//    public NejiWriter(final Corpus corpus) throws NejiException {
//        this();
//        getPipeline().setCorpus(corpus);
//    }

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

            //Remove sentence tags and escape XML
//            sentence = sentence.replaceAll("\\<.*?>", "");
            //sentence = StringEscapeUtils.escapeXml(sentence);
            yytext.replace(startSentence, endSentence, sentence);

            // Get final start and end of sentence
            int startChar = offset + yytext.indexOf(sentence);
            int endChar = startChar + sentence.length();


            // Generate sentence on stand-off format
            StringBuilder sb = new StringBuilder();
            sb.append("S");
            sb.append(counter);
            counter++;
            sb.append("\t");

            sb.append(String.format("%4d", startChar));
            sb.append(" ");
            sb.append(String.format("%4d", endChar));
            sb.append("\t");

            sb.append(sentence);
            sb.append("\n");

            getAnnotations(nextSentence, sentence, sb, startChar);
            sb.append("\n");

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

    private void getAnnotations(Sentence s, String source, StringBuilder sb, int offset) {
        getAnnotations(s.getTree().getRoot(), source, "", sb, 0, 0, 1, offset);
    }

    private void getAnnotations(TreeNode<Annotation> node, String source, String prefix, StringBuilder sb, int level,
                                int counter, int subcounter, int offset) {
        Annotation data = node.getData();

        if (level != 0 || !data.getIDs().isEmpty()) {
            // Add result to StringBuilder
            sb.append(prefix);

            String termPrefix;

            if (level <= 1) {
                termPrefix = "T" + counter;
                sb.append(termPrefix);
            } else {
                termPrefix = "-" + subcounter;
                sb.append("-");
                sb.append(subcounter);
            }
            prefix += termPrefix;

            Sentence s = data.getSentence();

//            int startChar = Char.getCharPositionWithWhiteSpaces(source, s.getToken(data.getStartIndex()).getStart());
//            int endChar = Char.getCharPositionWithWhiteSpaces(source, s.getToken(data.getEndIndex()).getEnd());

            int startAnnotationInSentence = s.getToken(data.getStartIndex()).getStart();
            int endAnnotationInSentence = s.getToken(data.getEndIndex()).getEnd() + 1;

            int startChar = offset + startAnnotationInSentence;
            int endChar = offset + endAnnotationInSentence;

            sb.append("\t");
            sb.append(String.format("%4d", startChar));
            sb.append(" ");
            sb.append(String.format("%4d", endChar));

            sb.append("\t");
            sb.append(source.substring(startAnnotationInSentence, endAnnotationInSentence).trim());
            sb.append("\t");
            sb.append(data.getStringIDs());
            sb.append("\n");
        }


        int i = 0;
        for (TreeNode<Annotation> child : node.getChildren()) {
            getAnnotations(child, source, "\t" + prefix, sb, level + 1, ++counter, ++i, offset);
        }
    }

    @Override
    public OutputFormat getFormat() {
        return OutputFormat.NEJI;
    }
}
