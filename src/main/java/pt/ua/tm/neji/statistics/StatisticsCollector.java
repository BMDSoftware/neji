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

package pt.ua.tm.neji.statistics;

import monq.jfa.DfaRun;
import org.apache.commons.lang.StringUtils;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationType;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree;
import pt.ua.tm.neji.tree.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class StatisticsCollector extends BaseLoader {

    private int sentenceCounter;

    public StatisticsCollector() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToGoofedElement(text_action, "s");
        this.sentenceCounter = 0;
    }

//    public StatisticsCollector(final Corpus corpus) throws NejiException {
//        this();
//        getPipeline().setCorpus(corpus);
//    }

    private Action text_action = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            // Get start and end of sentence
            int startSentence = yytext.indexOf("<s id=");
            int endSentence = yytext.lastIndexOf("</s>") + 4;

            int realStart = yytext.indexOf(">", startSentence) + 1;
            int realEnd = endSentence - 4;

            // Get sentence without XML tags
            String sentenceText = yytext.substring(realStart, realEnd);

            Sentence s = getPipeline().getCorpus().getSentence(sentenceCounter);

//            List<TreeNode<DefaultAnnotation>> nodes = s.getTree().build(1);
            List<TreeNode<Annotation>> nodes = s.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);

            for (TreeNode<Annotation> node : nodes) {

                //Discard if it is root
                if (node.equals(s.getTree().getRoot())) {
                    continue;
                }
                Annotation a = node.getData();

                // Get annotation text from source
                int startSource = s.getToken(a.getStartIndex()).getStart();
                int endSource = s.getToken(a.getEndIndex()).getEnd() + 1;
                String annotationText = sentenceText.substring(startSource, endSource).toLowerCase();

                // Discard intersection
                if (a.getType().equals(AnnotationType.INTERSECTION)) {
                    StatisticsEntry se = new StatisticsEntry(annotationText, "INTERSECTED", 1);

                    OverlappingEntry oe = new OverlappingEntry(se);
                    addChildren(oe, node, sentenceText);

                    addEntry(Statistics.getInstance().getIntersected(), oe);
                    continue;
                }

                if (a.getType().equals(AnnotationType.NESTED)) {
                    StatisticsEntry se = new StatisticsEntry(annotationText,
                            StringUtils.join(getUniqueGroups(a.getIDs()), "|"), 1);

                    OverlappingEntry oe = new OverlappingEntry(se);
                    addChildren(oe, node, sentenceText);

                    addEntry(Statistics.getInstance().getNested(), oe);
                }

                StatisticsEntry se;
                if (a.areIDsFromTheSameGroup()) {
                    se = new StatisticsEntry(annotationText, a.getIDs().get(0).getGroup(), 1);
                    if (a.getIDs().size() > 1) {
                        // IDs ambiguity
                        addEntry(Statistics.getInstance().getAmbiguosID(), se);
                    }
                    addEntry(Statistics.getInstance().getAnnotations(), se);
                } else {
                    // Group ambiguity

                    // Get different groups
                    List<String> groups = getUniqueGroups(a.getIDs());
                    // Add to annotations
                    se = new StatisticsEntry(annotationText, StringUtils.join(groups, "|"), 1);
                    addEntry(Statistics.getInstance().getAmbiguosGroup(), se);
//                    addEntry(Statistics.getInstance().getAnnotations(), se);

                    for(String group:groups){
                        se = new StatisticsEntry(annotationText, group, 1);
                        addEntry(Statistics.getInstance().getAnnotations(), se);
                    }
                }
            }
            sentenceCounter++;
        }
    };

    private void addEntry(StatisticsVector v, StatisticsEntry se) {
        if (v.contains(se)) {
            se = v.get(v.indexOf(se));
            se.setOccurrences(se.getOccurrences() + 1);
        } else {
            se.setOccurrences(1);
            v.add(se);
        }
    }

    private void addEntry(OverlappingVector v, OverlappingEntry oe) {
        if (v.contains(oe)) {
            oe = v.get(v.indexOf(oe));
            oe.setOccurrences(oe.getOccurrences() + 1);
        } else {
            oe.setOccurrences(1);
            v.add(oe);
        }

    }

    private List<String> getUniqueGroups(List<Identifier> ids) {
        // Get different groups
        List<String> groups = new ArrayList<String>();
        for (Identifier id : ids) {
            if (!groups.contains(id.getGroup())) {
                groups.add(id.getGroup());
            }
        }
        return groups;
    }

    private void addChildren(OverlappingEntry oe, final TreeNode<Annotation> parent, final String sentenceText) {
        Sentence s = parent.getData().getSentence();

        for (TreeNode<Annotation> child : parent.getChildren()) {
            Annotation childData = child.getData();

            int startSourceChild = s.getToken(childData.getStartIndex()).getStart();
            int endSourceChild = s.getToken(childData.getEndIndex()).getEnd() + 1;
            String annotationTextChild = sentenceText.substring(startSourceChild,
                    endSourceChild).toLowerCase();

            StatisticsEntry seChild = new StatisticsEntry(annotationTextChild,
                    StringUtils.join(getUniqueGroups(childData.getIDs()), "|"), 1);

            oe.addChild(seChild);
        }
    }

}
