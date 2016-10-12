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

package pt.ua.tm.neji.annotation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.*;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.parsing.TestDependency;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.tree.Tree;
import pt.ua.tm.neji.tree.TreeNode;

import java.io.IOException;
import java.util.*;

/**
 * Testing class for annotation's tree operations.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestTree extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestDependency.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestTree(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestTree.class);
    }

    public void testTree() throws IOException, NejiException {
        Constants.verbose = true;
        Corpus corpus = new Corpus();
        String str = "AA 12 - AA12 AA-12 (12AA) 12.-.";
        corpus.setText(str);

        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), false).launch();
        parser.parse(corpus, str);
        parser.close();
        Sentence s = corpus.getSentence(0);


        Annotation a;
        Integer counter = 0;
        int[] startIndex = {15, 19, 16, 15, 4, 4, 8, 15, 4, 8, 15, 4, 8, 4, 19};
        int[] endIndex   = {15, 19, 17, 16, 4, 6, 8, 15, 6, 8, 15, 6, 8, 4, 19};
        String expectedTreeToString = "[(0,13): AA 12 - AA12 AA-12 (12AA) 12.-., (4,6): AA-12 - 5:5:5:5|8:8:8:8|11:11:11:11, (4,4): AA - 4:4:4:4, (8,8): 12AA - 6:6:6:6|9:9:9:9]";
        String expectedTreeToStringWithDepth = "{(0,13): AA 12 - AA12 AA-12 (12AA) 12.-.=0, (4,6): AA-12 - 5:5:5:5|8:8:8:8|11:11:11:11=1, (4,4): AA - 4:4:4:4=2, (8,8): 12AA - 6:6:6:6|9:9:9:9=1}";
        String expectedTreeAnnotations0 = "[(0,13): AA 12 - AA12 AA-12 (12AA) 12.-.]";
        String expectedTreeAnnotations1 = "[(4,6): AA-12 - 5:5:5:5|8:8:8:8|11:11:11:11, (8,8): 12AA - 6:6:6:6|9:9:9:9]";
        String expectedTreeAnnotations2 = "[(4,4): AA - 4:4:4:4]";
        int[] expectedNoChildren = {2, 1, 0};
        int k = 0;


        // Testing Insertion
        for(; counter<12; counter++) {
            a = AnnotationImpl.newAnnotationByTokenPositions(s, startIndex[counter], endIndex[counter], 1.0);
            a.addID(new Identifier(counter.toString(), counter.toString(),counter.toString(), counter.toString()));
            s.addAnnotationToTree(a);
        }
        Tree<Annotation> tree = s.getTree();
//        logger.info("printTreeAnnotations representation");
//        s.printTreeAnnotations();
//        logger.info("toString representation:\n\t\t{}", tree.toString());
//        logger.info("toStringWithDepth representation:\n\t\t{}", tree.toStringWithDepth());
//        logger.info("Nodes at depth {}: {}", 0, Arrays.toString(s.getTreeAnnotations(0).toArray()));
//        logger.info("Nodes at depth {}: {}", 1, Arrays.toString(s.getTreeAnnotations(1).toArray()));
//        logger.info("Nodes at depth {}: {}", 2, Arrays.toString(s.getTreeAnnotations(2).toArray()));
//        logger.info("Amount of annotations: {}", counter);
//        logger.info("Root: {}", tree.getRoot().getData().toString());
//        logger.info("No. Children: {}", tree.getRoot().getNumberOfChildren());
//        for (TreeNode<Annotation> child : tree.getRoot().getChildren()) {
//            logger.info("No. Children: {}", child.getNumberOfChildren());
//        }
//        logger.info("Number of Nodes: {}", tree.getNumberOfNodes());
        assertEquals(expectedTreeToString, tree.toString());
        assertEquals(expectedTreeToStringWithDepth, tree.toStringWithDepth());
        assertEquals(expectedTreeAnnotations0, Arrays.toString(s.getTreeAnnotations(0).toArray()));
        assertEquals(expectedTreeAnnotations1, Arrays.toString(s.getTreeAnnotations(1).toArray()));
        assertEquals(expectedTreeAnnotations2, Arrays.toString(s.getTreeAnnotations(2).toArray()));
        assertTrue(counter == 12);
        assertEquals("(0,13): AA 12 - AA12 AA-12 (12AA) 12.-.", tree.getRoot().getData().toString());
        assertEquals(expectedNoChildren[k++], tree.getRoot().getNumberOfChildren());
        for (TreeNode<Annotation> child : tree.getRoot().getChildren()) {
            assertEquals(expectedNoChildren[k++], child.getNumberOfChildren());
        }
        assertTrue(tree.getNumberOfNodes() == 4);


        // Testing Traversal (pre-order)
//        logger.info("Pre-order map: ");
        Iterator<Map.Entry<Annotation, Integer>> iter =
                s.getTreeAnnotationsWithDepth(Tree.TreeTraversalOrderEnum.PRE_ORDER, true).entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry e = iter.next();
//            logger.info(" {} = {}", e.getValue(), e.getKey());
            switch(e.getValue().toString()) {
                case "0":
                    assertEquals("(0,13): AA 12 - AA12 AA-12 (12AA) 12.-.", e.getKey().toString());
                    break;
                case "1":
                    try {
                        assertEquals("(8,8): 12AA - 6:6:6:6|9:9:9:9", e.getKey().toString());
                    } catch (junit.framework.ComparisonFailure f) {
                        assertEquals("(4,6): AA-12 - 5:5:5:5|8:8:8:8|11:11:11:11", e.getKey().toString());
                    }
                    break;
                case "2":
                    assertEquals("(4,4): AA - 4:4:4:4", e.getKey().toString());
                    break;
            }
        }


        // Testing Traversal (post-order)
//        logger.info("Post-order map: ");
        iter = s.getTreeAnnotationsWithDepth(Tree.TreeTraversalOrderEnum.POST_ORDER, true).entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry e = iter.next();
//            logger.info(" {} = {}", e.getValue(), e.getKey());
            switch(e.getValue().toString()) {
                case "0":
                    assertEquals("(0,13): AA 12 - AA12 AA-12 (12AA) 12.-.", e.getKey().toString());
                    break;
                case "1":
                    try {
                        assertEquals("(8,8): 12AA - 6:6:6:6|9:9:9:9", e.getKey().toString());
                    } catch (junit.framework.ComparisonFailure f) {
                        assertEquals("(4,6): AA-12 - 5:5:5:5|8:8:8:8|11:11:11:11", e.getKey().toString());
                    }
                    break;
                case "2":
                    assertEquals("(4,4): AA - 4:4:4:4", e.getKey().toString());
                    break;
            }
        }

        a = AnnotationImpl.newAnnotationByTokenPositions(s, 4, 6, 1.0);
        a.addID(new Identifier(counter.toString(), counter.toString(),counter.toString(), counter.toString()));


        // Testing removal
//        logger.info("Removing node (4,6) and moving children to parent");
        TreeNode<Annotation> child = tree.find(a).getChildAt(0);
        tree.removeNode(a);
//        s.printTreeAnnotations();
        assertTrue(tree.find(a)==null);
        assertTrue(tree.find(child.getData())!=null);
//        logger.info("Removing node (4,6) and children");
        s.addAnnotationToTree(a); // resetting removed annotation for additional testing
        child = tree.find(a).getChildAt(0);
        tree.removeNodeAndChildren(a);
//        s.printTreeAnnotations();
        assertTrue(tree.find(a)==null);
        assertTrue(tree.find(child.getData())==null);


        // Testing Find method and Root changes
//        logger.info("Changing root");
        s.addAnnotationToTree(a); // resetting removed annotation for additional testing
        s.addAnnotationToTree(child.getData()); // resetting removed annotation for additional testing
        assertEquals(new TreeNode<>(a), tree.find(a));
        TreeNode<Annotation> prevRoot = tree.getRoot();
        TreeNode<Annotation> node = tree.find(a);
        tree.setRoot(node);
//        s.printTreeAnnotations();
        assertFalse(tree.getRoot().equals(prevRoot));
        assertEquals(node, tree.getRoot());
        assertEquals(node, tree.find(a));


        // Testing clean
//        logger.info("Cleaning");
        s.cleanAnnotationsTree();
//        s.printTreeAnnotations();
        assertEquals(0, tree.getRoot().getNumberOfChildren());
        assertEquals(new ArrayList<TreeNode<Annotation>>(), tree.getRoot().getChildren());
        assertEquals("[(4,6): AA-12 - 12:12:12:12]", tree.toString());
        assertEquals("{(4,6): AA-12 - 12:12:12:12=0}", tree.toStringWithDepth());
        assertEquals("[(4,6): AA-12 - 12:12:12:12]", Arrays.toString(s.getTreeAnnotations(0).toArray()));
        assertEquals("[]", Arrays.toString(s.getTreeAnnotations(1).toArray()));
        assertEquals("[]", Arrays.toString(s.getTreeAnnotations(2).toArray()));
        assertEquals("(4,6): AA-12 - 12:12:12:12", tree.getRoot().getData().toString());
        assertTrue(tree.getNumberOfNodes() == 1);
    }
}