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
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Chunk;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.corpus.dependency.LabeledEdge;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree;
import pt.ua.tm.neji.tree.TreeNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pt.ua.tm.neji.core.Constants;

/**
 * Writer that provides information following the CoNLL format.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Tokens})
public class TestWriter extends BaseWriter {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(TestWriter.class);
    private int startSentence;

    public TestWriter() throws NejiException {
        super(DfaRun.UNMATCHED_DROP);
        super.addActionToXMLTag(start_action, "s");
        super.addActionToXMLTag(end_action, "s");
        super.setEofAction(eof_action);
        this.startSentence = 0;
    }

//    public CoNLLWriter(final Corpus corpus) throws NejiException {
//        this();
//        getPipeline().setCorpus(corpus);
//    }

    private Action start_action = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            startSentence = start;
        }
    };

    private Action end_action = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            yytext.delete(startSentence, start + 4);
        }
    };

    private EofAction eof_action = new EofAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            List<TreeNode<Annotation>> annotationNodes;
            StringBuilder sb = new StringBuilder();
            int offset = 0;

            for (Sentence s : getPipeline().getCorpus()) {

                boolean provideDPOutput = false;
                if (s.getDependencyGraph() != null) {
                    if(!s.getDependencyGraph().vertexSet().isEmpty()){
                        provideDPOutput = true;
                    }
                }

                annotationNodes = s.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);
                for (int i = 0; i < s.size(); i++) {
                    Token t = s.getToken(i);
             
                    // TOKEN
                    sb.append(t.getText());
                    sb.append("\t");
                    
                    // START
                    sb.append(offset + t.getStart());
                    sb.append("\t");
                    
                    // END
                    sb.append(offset + t.getEnd() + 1);
                    sb.append("\t");
                    
                    // LABEL
                    sb.append(t.getLabel());
                    sb.append("\t");

                    sb.append("\n");
                }
                
                sb.append("\n");
                
                offset += s.getText().length() + 1;
            }
            
            sb.append("\n");
            
            yytext.replace(0, yytext.length(), sb.toString());
        }
    };



    @Override
    public OutputFormat getFormat() {
        return OutputFormat.TEST_WRITER;
    }
}
