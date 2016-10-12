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
package pt.ua.tm.neji.ml.postprocessing;

import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.tree.Tree;
import pt.ua.tm.neji.tree.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Perform parentheses post-processing.
 *
 * @author David Campos (<a
 *         href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Parentheses {

    public static void processTreeRemoving(final Sentence sentence) {

        List<TreeNode<Annotation>> nodes = sentence.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);

        for (TreeNode<Annotation> node : nodes) {
            if (node.equals(sentence.getTree().getRoot())) {
                continue;
            }
            Annotation annotation = node.getData();
            if (!isParanthesesBalanced(annotation)) {
                // Remove annotation
                TreeNode<Annotation> parent = node.getParent();
                parent.getChildren().remove(node);
            }
        }

    }

    /**
     * Process sentence by removing annotations with inconsistent number of
     * annotations.
     *
     * @param annotations The annotations to be processed.
     */
    public static List<Annotation> processRemoving(List<Annotation> annotations) {
        Annotation a;
        List<Annotation> result = new ArrayList<>(annotations);
        List<Integer> toRemoveIndexes = new ArrayList<>();

        for (int i = 0; i < annotations.size(); i++) {
            a = annotations.get(i);
            if (!isParanthesesBalanced(a)) {
                toRemoveIndexes.add(i);
            }
        }
        Collections.reverse(toRemoveIndexes);
        for(Integer i : toRemoveIndexes){
            result.remove(i.intValue());
        }

        return result;
    }

    /**
     * Process sentence by correcting annotations.
     *
     * @param annotations The annotations to be processed.
     */
    public static List<Annotation> processCorrecting(List<Annotation> annotations) {

        Annotation a;
        List<Annotation> result = new ArrayList<>(annotations);
        List<Integer> toRemoveIndexes = new ArrayList<>();

        for (int i = 0; i < annotations.size(); i++) {
            a = annotations.get(i);
            if (!isParanthesesBalanced(a) && !extendLeft(a)
                    && !extendRight(a) && !shrinkLeft(a) && !shrinkRight(a)) {
                toRemoveIndexes.add(i);
            }
        }
        Collections.reverse(toRemoveIndexes);
        for(Integer i : toRemoveIndexes){
            result.remove(i.intValue());
        }

        return result;
    }

    /**
     * Extend the annotation to the left side by adding tokens until the
     * previous parentheses.
     *
     * @param a The annotation to be extended.
     * @return <code>True</code> if the annotation was extended, and
     *         <code>False</code> otherwise.
     */
//    private static boolean extendLeft(Annotation a) {
//        Token t;
//        Annotation na;
//        for (int k = a.getStartIndex() - 1; k >= 0; k--) {
//            t = a.getSentence().getToken(k);
//            if (hasOpen(t) || hasClose(t)) {
//                na = Annotation.newAnnotationByTokenPositions(a.getSentence(), k, a.getEndIndex(), a.getScore());
//                if (isParanthesesBalanced(na)) {
//                    a.getSentence().removeAnnotation(a);
//                    a.getSentence().addAnnotation(na);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
    private static boolean extendLeft(Annotation a) {
        Token t;
        Annotation na;
        for (int k = a.getStartIndex() - 1; k >= 0; k--) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t) || hasClose(t)) {
                na = AnnotationImpl.newAnnotationByTokenPositions(a.getSentence(), k, a.getEndIndex(), a.getScore());
                if (isParanthesesBalanced(na)) {
                    a.getSentence().getTree().find(a).setData(na);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extend the annotation to the right side by adding tokens until the next
     * parentheses.
     *
     * @param a The annotation to be extended.
     * @return <code>True</code> if the annotation was extended, and
     *         <code>False</code> otherwise.
     */
//    private static boolean extendRight(Annotation a) {
//        Token t;
//        Annotation na;
//        for (int k = a.getEndIndex() + 1; k < a.getSentence().size(); k++) {
//            t = a.getSentence().getToken(k);
//            if (hasOpen(t) || hasClose(t)) {
//                na = Annotation.newAnnotationByTokenPositions(a.getSentence(), a.getStartIndex(), k, a.getScore());
//                if (isParanthesesBalanced(na)) {
//                    a.getSentence().getTree().find(a).setData(na);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
    private static boolean extendRight(Annotation a) {
        Token t;
        Annotation na;
        for (int k = a.getEndIndex() + 1; k < a.getSentence().size(); k++) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t) || hasClose(t)) {
                na = AnnotationImpl.newAnnotationByTokenPositions(a.getSentence(), a.getStartIndex(), k, a.getScore());
                if (isParanthesesBalanced(na)) {
                    a.getSentence().getTree().find(a).setData(na);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Shrink the annotation in the left side by removing tokens until the next
     * parentheses.
     *
     * @param a The annotation to be shrunk.
     * @return <code>True</code> if the annotation was shrunk, and
     *         <code>False</code> otherwise.
     */
//    private static boolean shrinkLeft(Annotation a) {
//        Token t;
//        Annotation na;
//        for (int k = a.getStartIndex(); k < a.getEndIndex(); k++) {
//            t = a.getSentence().getToken(k);
//            if (hasOpen(t) || hasClose(t)) {
//                na = Annotation.newAnnotationByTokenPositions(a.getSentence(), k + 1, a.getEndIndex(), a.getScore());
//                if (isParanthesesBalanced(na)) {
//                    a.getSentence().removeAnnotation(a);
//                    a.getSentence().addAnnotation(na);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
    private static boolean shrinkLeft(Annotation a) {
        Token t;
        Annotation na;
        for (int k = a.getStartIndex(); k < a.getEndIndex(); k++) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t) || hasClose(t)) {
                na = AnnotationImpl.newAnnotationByTokenPositions(a.getSentence(), k + 1, a.getEndIndex(), a.getScore());
                if (isParanthesesBalanced(na)) {
                    a.getSentence().getTree().find(a).setData(na);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Shrink the annotation in the right side by removing tokens until the
     * previous parentheses.
     *
     * @param a The annotation to be shrunk.
     * @return <code>True</code> if the annotation was shrunk, and
     *         <code>False</code> otherwise.
     */
//    private static boolean shrinkRight(Annotation a) {
//        Token t;
//        Annotation na;
//        for (int k = a.getEndIndex(); k > a.getStartIndex(); k--) {
//            t = a.getSentence().getToken(k);
//            if (hasOpen(t) || hasClose(t)) {
//                na = Annotation.newAnnotationByTokenPositions(a.getSentence(), a.getStartIndex(), k - 1, a.getScore());
//                if (isParanthesesBalanced(na)) {
//                    a.getSentence().removeAnnotation(a);
//                    a.getSentence().addAnnotation(na);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
    private static boolean shrinkRight(Annotation a) {
        Token t;
        Annotation na;
        for (int k = a.getEndIndex(); k > a.getStartIndex(); k--) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t) || hasClose(t)) {
                na = AnnotationImpl.newAnnotationByTokenPositions(a.getSentence(), a.getStartIndex(), k - 1, a.getScore());
                if (isParanthesesBalanced(na)) {
                    a.getSentence().getTree().find(a).setData(na);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the number of parentheses of an annotation is odd.
     *
     * @param a The annotation to be analyzed.
     * @return <code>True</code> if the annotation contains an odd number of
     *         parentheses, and
     *         <code>False</code> otherwise.
     */
    private static boolean isParanthesesBalanced(Annotation a) {
        int countOpen = 0, countClose = 0;
        Token t;
        for (int k = a.getStartIndex(); k <= a.getEndIndex(); k++) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t)) {
                countOpen++;
            }
            if (hasClose(t)) {
                countClose++;
            }
        }
        if (countOpen != countClose) {
            return false;
        }
        return true;
    }

    /**
     * Check if a token contains an open parentheses.
     *
     * @param t The token to be analyzed.
     * @return <code>True</code> if the annotation contains an open parentheses, and
     *         <code>False</code> otherwise.
     */
    private static boolean hasOpen(Token t) {
        String text = t.getText();
        if (text.contains("(")) {
            return true;
        }
        if (text.contains("[")) {
            return true;
        }
        if (text.contains("{")) {
            return true;
        }
        return false;
    }

    /**
     * Check if a token contains an close parentheses.
     *
     * @param t The token to be analyzed.
     * @return <code>True</code> if the annotation contains an close parentheses, and
     *         <code>False</code> otherwise.
     */
    private static boolean hasClose(Token t) {
        String text = t.getText();
        if (text.contains(")")) {
            return true;
        }
        if (text.contains("]")) {
            return true;
        }
        if (text.contains("}")) {
            return true;
        }
        return false;
    }
}
