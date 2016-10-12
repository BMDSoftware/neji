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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.writer;

import java.util.List;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationType;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree;
import pt.ua.tm.neji.tree.TreeNode;

/**
 *
 * @author jeronimo
 */
@Requires({Resource.Tokens, Resource.Annotations})
public class BC2Writer extends BaseWriter {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(BC2Writer.class);
    
    // Attributes
    StringBuilder content = new StringBuilder();

    public BC2Writer() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToGoofedElement(text_action, "s");
        super.setEofAction(eof_action);
        this.content = new StringBuilder();
    }

    public BC2Writer(final Corpus corpus) throws NejiException {
        this();
        getPipeline().setCorpus(corpus);
    }

    private Module.Action text_action = new BaseModule.SentenceIteratorDefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start, Sentence nextSentence) {

             // Get start and end of sentence
            int startSentence = yytext.indexOf("<s id=");
            int endSentence = yytext.lastIndexOf("</s>") + 4;

            int realStart = yytext.indexOf(">", startSentence) + 1;
            int realEnd = endSentence - 4;

            // Get sentence with XML tags
            String sentence = yytext.substring(realStart, realEnd);
            
            yytext.replace(startSentence, endSentence, sentence);
            
            // Get annotations in BC2 format
            StringBuilder sb = new StringBuilder();
            getAnnotations(nextSentence, sb);
            
            // Add sentence standoff format to final content
            content.append(sb.toString());

            // Remove processed input from input
            yytext.replace(0, endSentence, "");
        }
    };
    
    private EofAction eof_action = new EofAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            yytext.replace(0, yytext.length(), content.toString());
        }
    };

    private void getAnnotations(Sentence sentence, StringBuilder sb) {
        
        // Get tree nodes
        List<TreeNode<Annotation>> nodes = sentence.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);
                
        for (TreeNode<Annotation> node : nodes) {

            Annotation data = node.getData();

            // Skip intersection parents and root node without IDs
            if (data.getType().equals(AnnotationType.INTERSECTION) ||
                    (node.equals(sentence.getTree().getRoot()) && data.getIDs().isEmpty())) {
                continue;
            }

            int spacesNumber = 0; // spaces are ignored
            
            // Get start index of annotation in the sentence
            int annotationStart = sentence.getToken(data.getStartIndex()).getStart();
            
            for (int i=0 ; i<annotationStart ; i++) {
                char c = sentence.getText().charAt(i);
                if ((c == ' ') || (c == '\t') || (c == '\n')) {
                    spacesNumber++;
                }
            }
            
            annotationStart -= spacesNumber;
            
            // Get start index of annotation in the sentence
            int annotationEnd = sentence.getToken(data.getEndIndex()).getEnd();
            
            for (int i=annotationStart + spacesNumber ; i<annotationEnd ; i++) {
                char c = sentence.getText().charAt(i);
                if ((c == ' ') || (c == '\t') || (c == '\n')) {
                    spacesNumber++;
                }
            }
            
            annotationEnd -= spacesNumber;
            
            // Build annotaion line in BC2 format
            if (sentence.getId() != null) {
                sb.append(sentence.getId());
            } else {
                sb.append("S");
                sb.append(getPipeline().getCorpus().getSentences().indexOf(sentence) + 1);
            }
            sb.append("|");
            sb.append(annotationStart);
            sb.append(" ");
            sb.append(annotationEnd);
            sb.append("|");
            sb.append(data.getText());
            sb.append("\n");
        }        
    }
    
    @Override
    public OutputFormat getFormat() {
        return OutputFormat.BC2;
    }
}
