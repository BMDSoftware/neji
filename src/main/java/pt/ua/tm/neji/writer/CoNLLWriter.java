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

/**
 * Writer that provides information following the CoNLL format.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Tokens})
public class CoNLLWriter extends BaseWriter {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(CoNLLWriter.class);
    private int startSentence;

    public CoNLLWriter() throws NejiException {
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

                    // ID
                    sb.append(t.getIndex() + 1);
                    sb.append("\t");

                    // TEXT
                    sb.append(t.getText());
                    sb.append("\t");

                    if (!t.getFeature("LEMMA").isEmpty()) {
                        sb.append(t.getFeature("LEMMA").get(0));
                    } else {
                        sb.append("_");
                    }
                    sb.append("\t");

                    if (!s.getChunks().isEmpty()) {
                        Chunk chunk = s.getChunks().getTokenChunk(t);
                        sb.append(chunk.getBIOTag(t));
                    } else {
                        sb.append("_");
                    }
                    sb.append("\t");

                    if (!t.getFeature("POS").isEmpty()) {
                        sb.append(t.getFeature("POS").get(0));
                    } else {
                        sb.append("_");
                    }
                    sb.append("\t");

                    // FEATS
//                    sb.append(getAnnotationsAsFeatures(s.getTree(), annotationNodes, t));
                    sb.append(getAnnotationsAsFeaturesWithIdentifiers(s.getTree(), annotationNodes, t));
                    sb.append("\t");

                    // Get DP output from Dependency Graph
                    if (provideDPOutput) {
                        Graph<Token, LabeledEdge> graph = s.getDependencyGraph();
                        Set<LabeledEdge> edges = graph.edgesOf(t);

                        String depTag = "";
                        int depTok = 0;
                        boolean isRoot = true;

                        for (LabeledEdge edge : edges) {
                            if (graph.getEdgeSource(edge).equals(t)) {
                                depTag = edge.getLabel().toString();
                                depTok = graph.getEdgeTarget(edge).getIndex() + 1;
                                isRoot = false;
                                break;
                            }
                        }

                        if (isRoot) {
                            sb.append("0");
                            sb.append("\t");
                            sb.append("ROOT");
                        } else {
                            sb.append(depTok);
                            sb.append("\t");
                            sb.append(depTag);
                        }

                    } else {
                        sb.append("_");
                        sb.append("\t");
                        sb.append("_");
                    }


//                    if (!t.getFeature("DEP_TOK").isEmpty()) {
//                        sb.append(t.getFeature("DEP_TOK"));
//                    } else {
//                        sb.append("_");
//                    }
//                    sb.append("\t");
//
//                    if (!t.getFeature("DEP_TAG").isEmpty()) {
//                        sb.append(t.getFeature("DEP_TAG"));
//                    } else {
//                        sb.append("_");
//                    }


                    sb.append("\t");

                    // PHEAD
                    sb.append("_\t");

                    // PDEPREL
                    sb.append("_");

                    //sb.append(t.getLabel());
                    sb.append("\n");
                }
                sb.append("\n");
            }


            yytext.replace(0, yytext.length(), sb.toString());
        }
    };


    // Get concept annotations
    private static String getAnnotationsAsFeaturesWithIdentifiers(final Tree<Annotation> tree,
                                                                  final List<TreeNode<Annotation>> annotationNodes, final Token token) {
//        final Set<String> semGroups = new HashSet<>();
        final StringBuilder sb = new StringBuilder();

        for (final TreeNode<Annotation> node : annotationNodes) {
            Annotation data = node.getData();
            // Skip the root node (whole sentence)
            if (node.equals(tree.getRoot()) && data.getIDs().isEmpty()) {
                continue;
            }

            // Check if current node refers to our token
            if (token.getIndex() >= data.getStartIndex()
                    && token.getIndex() <= data.getEndIndex()) {

                for (final Identifier id : data.getIDs()) {
//                    semGroups.add(id.getGroup());
                    sb.append(id.toString());
                    sb.append("|");
                }
            }
        }

        // Build Semantic Groups string (separated by ;)
//        final StringBuilder sb = new StringBuilder();
//        for (final String group : semGroups) {
//            sb.append(group);
//            sb.append(";");
//        }

        if (sb.length() == 0) {
            sb.append("0");
        } else {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }


    // Get concept annotations
    private static String getAnnotationsAsFeatures(final Tree<Annotation> tree,
                                                   final List<TreeNode<Annotation>> annotationNodes, final Token token) {
        final Set<String> semGroups = new HashSet<>();

        for (final TreeNode<Annotation> node : annotationNodes) {
            // Skip the root node (whole sentence)
            if (node.equals(tree.getRoot()) && node.getData().getIDs().isEmpty()) {
                continue;
            }

            // Check if current node refers to our token
            if (token.getIndex() >= node.getData().getStartIndex()
                    && token.getIndex() <= node.getData().getEndIndex()) {

                for (final Identifier id : node.getData().getIDs()) {
                    semGroups.add(id.getGroup());
                }
            }
        }

        // Build Semantic Groups string (separated by ;)
        final StringBuilder sb = new StringBuilder();
        for (final String group : semGroups) {
            sb.append(group);
            sb.append(";");
        }

        if (sb.length() == 0) {
            sb.append("0");
        } else {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    @Override
    public OutputFormat getFormat() {
        return OutputFormat.CONLL;
    }
}
