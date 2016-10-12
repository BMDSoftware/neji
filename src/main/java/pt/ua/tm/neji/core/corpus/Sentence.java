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

package pt.ua.tm.neji.core.corpus;

import java.io.Serializable;
import java.util.*;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants.LabelFormat;
import pt.ua.tm.neji.core.Constants.LabelTag;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.AnnotationType;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.dependency.LabeledEdge;
import pt.ua.tm.neji.tree.Tree;
import pt.ua.tm.neji.tree.TreeNode;

/**
 * Class that represents a Sentence.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class Sentence implements Iterable<Token>, Serializable {

    private static final long serialVersionUID = 4L;

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Sentence.class);

    /**
     * The corpus of the sentence.
     */
    private Corpus corpus;

    /**
     * The tokens of this sentence.
     */
    private List<Token> tokens;

    /**
     * The tree of the annotations associated with this sentence.
     */
    private Tree<Annotation> tree;

    /**
     * The relations between annotations in this sentence.
     */
    private List<Relation> relations;

    /**
     * The start index for this sentence in the document.
     */
    private int start;

    /**
     * The end index for this sentence in the document.
     */
    private int end;
    
    /**
     * The start original index for this sentence in the document.
     */
    private int originalStart;
    
    /**
     * The end original index for this sentence in the document.
     */
    private int originalEnd;

    /**
     * The dependencies between annotations in this sentence.
     */
    private UndirectedGraph<Token, LabeledEdge> dependencyGraph;

    /**
     * The chunks of the sentence.
     */
    private ChunkList chunks;

    /**
     * The unique identifier of the sentence. (used in model training)
     */
    private String id;

    /**
     * Constructor.
     *
     * @param c the corpus of the sentence
     */
    public Sentence(Corpus c) {
        this.corpus = c;
        this.tokens = new ArrayList<>();
        this.tree = new Tree<>();
        tree.setRoot(new TreeNode<>(AnnotationImpl.newAnnotationByTokenPositions(this, 0, 0, 1.0)));
        this.relations = new ArrayList<>();
        this.chunks = new ChunkList(this);
        this.dependencyGraph = new SimpleGraph<>(LabeledEdge.class);
        this.id = null;
    }

    /**
     * Gets the corpus of the sentence.
     * @return this sentence's corpus
     */
    public Corpus getCorpus() {
        return corpus;
    }

    /**
     * Sets a new corpus for this sentence.
     * @param corpus the new corpus index for this sentence
     */
    public void setCorpus(Corpus corpus) {
        this.corpus = corpus;
    }

    /**
     * Adds a new token to the sentence.
     * @param t the new token to be added
     */
    public void addToken(Token t) {
        tokens.add(t);
    }

    /**
     * Gets the token at the specified index.
     * @param i the index of the token to be returned
     * @return the token at the specified index
     */
    public Token getToken(final int i) {
        return tokens.get(i);
    }

    /**
     * Sets a new specific token at the specified index.
     * @param i the index of the token
     * @param t the new token to be set
     */
    public void setToken(final int i, final Token t) {
        tokens.set(i, t);
    }

    /**
     * Returns the list of tokens in this sentence.
     * @return a list of tokens
     */
    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    /**
     * Reverses the current order of the tokens in this sentence.
     */
    public void reverseTokens() {
        Collections.reverse(tokens);
    }

    /**
     * Gets the number of tokens in this sentence.
     * @return the number of tokens
     */
    public int size() {
        return tokens.size();
    }

    /**
     * Returns an iterator of the tokens in this sentence.
     * @return iterator of the tokens in this sentence
     */
    @Override
    public Iterator<Token> iterator() {
        return tokens.iterator();
    }

    /**
     * Returns the dependencies between annotations in this sentence.
     * @return a dependency graph representing the dependencies between annotations
     */
    public UndirectedGraph<Token, LabeledEdge> getDependencyGraph() {
        return dependencyGraph;
    }

    /**
     * Sets a new graph to represent the dependencies between annotations in this sentence.
     * @param dependencyGraph the new dependency graph
     */
    public void setDependencyGraph(final UndirectedGraph<Token, LabeledEdge> dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    /**
     * Returns the list of relations between annotations in this sentence.
     * @return a list of relations
     */
    public List<Relation> getRelations() {
        return Collections.unmodifiableList(relations);
    }

    /**
     * Adds a new relation to the list of relations.
     * @param relation the new relation to add to the list
     */
    public void addRelation(Relation relation) {
        relations.add(relation);
    }

    /**
     * Gets the relation at the specified index.
     * @param i the index of the relation to be returned
     * @return the relation at the specified index
     */
    public Relation getRelation(final int i) {
        return relations.get(i);
    }

    /**
     * Cleans the relation list.
     */
    public void cleanRelations() {
        relations = new ArrayList<>();
    }

    /**
     * Returns the list of chunks in this sentence.
     * @return a list of chunks
     */
    public ChunkList getChunks() {
        return chunks;
    }

    /**
     * Sets a new list of chunks to represent the chunks in this sentence.
     * @param chunks the new chunks list
     */
    public void setChunks(ChunkList chunks) {
        this.chunks = chunks;
    }

    /**
     * Gets the start index for this sentence in the document.
     * @return the start index for this sentence in the document
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets a new start index for this sentence in the document.
     * @param start the new start index for this sentence
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Gets the end index for this sentence in the document.
     * @return the end index for this sentence in the document
     */
    public int getEnd() {
        return end;
    }

    /**
     * Sets a new end index for this sentence in the document.
     * @param end the new end index for this sentence
     */
    public void setEnd(int end) {
        this.end = end;
    }
    
    /**
     * Gets the original start index for this sentence in the document.
     * @return the original start index for this sentence in the document
     */
    public int getOriginalStart() {
        return originalStart;
    }
    
    /**
     * Sets a new original start index for this sentence in the document.
     * @param originalStart the new original start index for
     */
    public void setOriginalStart(int originalStart) {
        this.originalStart = originalStart;
    }
    
    /**
     * Gets the original end index for this sentence in the document.
     * @return the original end index for this sentence in the document
     */
    public int getOriginalEnd() {
        return originalEnd;
    }
    
    /**
     * Sets a new original end index for this sentence in the document.
     * @param originalEnd the new original end index for
     */
    public void setOriginalEnd(int originalEnd) {
        this.originalEnd = originalEnd;
    }

    /**
     * Add an annotation to the tree. This implementation uses a protected method that assumes
     * the specified {@link Annotation} and the {@link Tree} annotations are instances of the
     * {@link pt.ua.tm.neji.core.annotation.AnnotationImpl} class. If the user wishes to use a
     * custom {@link Annotation} class, a custom class that extends from {@link Sentence}
     * should be implemented, and the protected method
     * {@link Sentence#addAnnotationToTree(TreeNode, Annotation)} should be overridden.
     * @param toAdd the annotation to be added to the concept tree
     */
    public void addAnnotationToTree(Annotation toAdd) {

        // Check tree root, and set it to the whole sentence
        Annotation rootData = tree.getRoot().getData();
        if (rootData.getStartIndex() == 0 && rootData.getEndIndex() == 0) {
            tree.getRoot().getData().setEndIndex(size() - 1);
        }

        addAnnotationToTree(tree.getRoot(), toAdd);
    }
    
    
    public void addAnnotationToTreeWithFeatures(Annotation toAdd, String featureName) {
        
        // Add annotation to tree
        addAnnotationToTree(toAdd);
        
        // Get 
        int startToken = toAdd.getStartIndex();
        int endToken = toAdd.getEndIndex();
        
        // Set B to the start token
        getToken(startToken).putFeature(featureName, LabelTag.B.name());
        
        // Set I to the end tokens
        for (int i = startToken + 1 ; i <= endToken ; i++) {
            getToken(i).putFeature(featureName, LabelTag.I.name());
        }
    }

    protected void addAnnotationToTree(TreeNode<Annotation> node, Annotation toAdd) {
        Annotation nodeData = node.getData();

        // Exact matching
        if (toAdd.equals(nodeData)) {

            for (Identifier newIdentifier : toAdd.getIDs()) {
                if (!nodeData.getIDs().contains(newIdentifier)) {
                    nodeData.addID(newIdentifier);
                }
            }
            return;
        }

        // New contains existent
        if (toAdd.contains(nodeData)) {
            TreeNode<Annotation> parent = node.getParent();

            // Remove child
            parent.removeChildAt(parent.getChildren().indexOf(node));

            // Add child to new node
            toAdd.setType(AnnotationType.NESTED);
            TreeNode<Annotation> toAddNode = new TreeNode<Annotation>(toAdd);
            toAddNode.addChild(node);

            // Add new node and nested to parent
            parent.addChild(toAddNode);
            return;
        }

        // Node is a leaf
        if (!node.hasChildren()) {
            // Existent contains New
            if (nodeData.contains(toAdd)) {
                nodeData.setType(AnnotationType.NESTED);
                node.addChild(new TreeNode<Annotation>(toAdd));
                return;
            }
        }

        // Existent contains new
        if (nodeData.contains(toAdd)) {

            // None of the childs contains it
            List<TreeNode<Annotation>> intersected = new ArrayList<TreeNode<Annotation>>();
            List<TreeNode<Annotation>> nested = new ArrayList<TreeNode<Annotation>>();

            boolean noneOfTheChildContainsIt = true;
            for (TreeNode<Annotation> child : node.getChildren()) {
                Annotation childData = child.getData();

                if (childData.contains(toAdd)) {
                    noneOfTheChildContainsIt = false;
                } else if (toAdd.contains(childData)) {
                    nested.add(child);
                } else if (toAdd.equals(childData)) {
                    // Exact matching
                    for (Identifier newIdentifier : toAdd.getIDs()) {
                        if (!childData.getIDs().contains(newIdentifier)) {
                            childData.addID(newIdentifier);
                        }
                    }
                    return;
                } else if (toAdd.intersection(childData)) {
                    intersected.add(child);
                }
            }


            if (noneOfTheChildContainsIt) {

                // Both intersection and nested
                if (!nested.isEmpty() && !intersected.isEmpty()) {


                    TreeNode<Annotation> parent = new TreeNode<>();

                    toAdd.setType(AnnotationType.NESTED);
                    TreeNode<Annotation> nodeToAdd = new TreeNode<>(toAdd);

                    parent.addChild(nodeToAdd);

                    int minStart = toAdd.getStartIndex();
                    int maxEnd = toAdd.getEndIndex();
                    for (TreeNode<Annotation> inter : intersected) {
                        // Remove child
                        node.removeChildAt(node.getChildren().indexOf(inter));
                        parent.addChild(inter);

                        minStart = (inter.getData().getStartIndex() < minStart) ? inter.getData().getStartIndex() : minStart;
                        maxEnd = (inter.getData().getEndIndex() > maxEnd) ? inter.getData().getEndIndex() : maxEnd;

                    }
                    Annotation parentData = AnnotationImpl.newAnnotationByTokenPositions(this, minStart, maxEnd, 1.0);
                    parentData.setType(AnnotationType.INTERSECTION);
                    parent.setData(parentData);


                    for (TreeNode<Annotation> nest : nested) {
                        // Remove child
                        node.removeChildAt(node.getChildren().indexOf(nest));

                        // Add child to new node
                        nodeToAdd.addChild(nest);
                    }

                    node.addChild(parent);
                    return;


                } else if (!intersected.isEmpty()) { // Check intersection


                    TreeNode<Annotation> parent = new TreeNode<>();

                    parent.addChild(new TreeNode<>(toAdd));

                    int minStart = toAdd.getStartIndex();
                    int maxEnd = toAdd.getEndIndex();
                    for (TreeNode<Annotation> inter : intersected) {
                        // Remove child
                        node.removeChildAt(node.getChildren().indexOf(inter));
                        parent.addChild(inter);

                        minStart = (inter.getData().getStartIndex() < minStart) ? inter.getData().getStartIndex() : minStart;
                        maxEnd = (inter.getData().getEndIndex() > maxEnd) ? inter.getData().getEndIndex() : maxEnd;

                    }
                    Annotation parentData = AnnotationImpl.newAnnotationByTokenPositions(this, minStart, maxEnd, 1.0);
                    parentData.setType(AnnotationType.INTERSECTION);
                    parent.setData(parentData);


                    node.addChild(parent);
                    return;
                } else if (!nested.isEmpty()) { // Check nested
                    TreeNode<Annotation> parent = node;

                    toAdd.setType(AnnotationType.NESTED);
                    TreeNode<Annotation> toAddNode = new TreeNode<>(toAdd);

                    for (TreeNode<Annotation> nest : nested) {
                        // Remove child
                        parent.removeChildAt(parent.getChildren().indexOf(nest));

                        // Add child to new node
                        toAddNode.addChild(nest);
                    }

                    // Add new node and nested to parent
                    parent.addChild(toAddNode);
                    return;
                } else {
                    nodeData.setType(AnnotationType.NESTED);
                    node.addChild(new TreeNode<>(toAdd));
                    return;
                }
            }

            for (TreeNode<Annotation> child : node.getChildren()) {
                addAnnotationToTree(child, toAdd);
            }
        }
    }


    /**
     * Removes the specified from the concept tree annotation and, if removeChildren
     * is set to <tt>true</tt>, removes all of the removed annotation's children,
     * or if removeChildren is set to <tt>false/tt>, moves all of the removed
     * annotation's children to the removed annotation's parent.
     *
     * @param dataToRemove the annotation to remove
     * @return the removed annotation, or <tt>null</tt> if the annotation to remove
     *         was not found in the concept tree or if it corresponded to the root
     *         annotation
     */
    public Annotation removeAnnotation(Annotation dataToRemove, boolean removeChildren) {
        if(removeChildren){
            TreeNode<Annotation> node = tree.removeNodeAndChildren(dataToRemove);
            return (node==null || node.equals(tree.getRoot())) ? null : node.getData();
        } else{
            TreeNode<Annotation> node = tree.removeNode(dataToRemove);
            return (node==null || node.equals(tree.getRoot())) ? null : node.getData();
        }
    }

    /**
     * Cleans the concept tree, only retaining the root annotation.
     */
    public void cleanAnnotationsTree() {
        tree.getRoot().removeChildren();
    }

    /**
     * Get the number of annotations associated with this sentence.
     * @return The number of annotations.
     */
    public int getNumberAnnotations() {
        return tree.getNumberOfNodes();
    }

    /**
     * Gets the concept tree.
     * @return the concept tree
     */
    public Tree<Annotation> getTree() {
        return tree;
    }

    /**
     * Gets the annotations from the concept tree, ordered by the traversal ordered and including (or not) the root annotation.
     * @param traversal the order of how to traverse the concept tree while obtaining the annotations
     * @param withRoot <tt>true</tt> if the list to be returned is to include the root annotation, <tt>false</tt> if not
     * @return a list of annotations from the concept tree
     */
    public List<Annotation> getTreeAnnotations(final Tree.TreeTraversalOrderEnum traversal, final boolean withRoot) {
        return tree.buildData(traversal, withRoot);
    }

    /**
     * Gets the annotations from the concept tree mapped with the depth where these annotations are found in the tree,
     * ordered by the traversal ordered and including (or not) the root annotation.
     * @param traversal the order of how to traverse the concept tree while obtaining the annotations
     * @param withRoot <tt>true</tt> if the list to be returned is to include the root annotation, <tt>false</tt> if not
     * @return a map of annotations with their depth from the concept tree
     */
    public Map<Annotation, Integer> getTreeAnnotationsWithDepth(final Tree.TreeTraversalOrderEnum traversal, final boolean withRoot) {
        return tree.buildDataWithDepth(traversal, withRoot);
    }

    /**
     * Gets the annotations from the concept tree that are at the specified depth.
     * @param depth the depth of the annotations to be returned
     * @return a list of annotations from the concept tree at the specified depth
     */
    public List<Annotation> getTreeAnnotations(final int depth) {
        return tree.buildData(depth);
    }

    /**
     * From the whole set of annotations, get the first annotation that is equal
     * to the one provided.
     *
     * @param a the annotation to test equality
     * @return The annotation that is equal to the one specified,
     *         or <code>null</code> if there are no annotations
     *         equal to the one specified
     */
    public Annotation containsExactAnnotation(final Annotation a) {
        for (Annotation an : tree.buildData(Tree.TreeTraversalOrderEnum.PRE_ORDER, false)) {
            if (an.equals(a)) {
                return an;
            }
        }
        return null;
    }

    /**
     * From the whole set of annotations, get the first annotation that contains
     * the one provided.
     *
     * @param a the annotation to test contention
     * @return The annotation that contains or is contained
     *         by the one specified or <code>null</code> if
     *         there are no annotations that contain or are
     *         contained to the one specified
     */
    public Annotation containsApproximateAnnotation(final Annotation a) {
        for (Annotation an : tree.buildData(Tree.TreeTraversalOrderEnum.PRE_ORDER, false)) {
            if (an.contains(a) || a.contains(an)) {
                return an;
            }
        }
        return null;
    }

    /**
     * Prints the annotations tree.
     */
    public void printTreeAnnotations() {
        printTreeAnnotations("", tree.getRoot());
    }

    /**
     * Print the annotations tree considering a start node and prefix for each
     * depth level.
     * @param prefix Prefix to be used on each level.
     * @param node   The node to print.
     */
    private void printTreeAnnotations(String prefix, TreeNode<Annotation> node) {
        logger.info("{}{}", prefix, node.getData().toString());

        for (TreeNode<Annotation> child : node.getChildren()) {
            printTreeAnnotations(prefix + "\t", child);
        }
    }

    /**
     * Provides the text of the sentence.
     * @return text of the sentence
     */
    public String getText() {
        return corpus.getText(start, end);
    }
    
    /**
     * Provides the original text of the sentence.
     * @return text of the original sentence
     */
    public String getOriginalText() {
        return corpus.getText(originalStart, originalEnd);
    }

    /**
     * Provides text representation of the sentence.
     * @return text representation of the sentence
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String s = "("+start+", "+end+"): '"+getText()+"', [";
        sb.append(s);
        for (Iterator<Token> iter = tokens.iterator(); iter.hasNext();) {
            sb.append(iter.next().getText());
            if(iter.hasNext())
                sb.append(", ");
        }
        return sb.append("]").toString().trim();
    }

    /**
     * Provides text representation of the sentence in an format to be exported and saved in a file.
     * @return String presenting the sentence.
     */
    public final String toExportFormat() {
        StringBuilder sb = new StringBuilder();

        Token t;
        for (int i = 0; i < tokens.size(); i++) {
            t = getToken(i);
            sb.append(t.getText());
            sb.append("\t");
            sb.append(t.featuresToString());
            sb.append("\t");
            sb.append(t.getLabel().toString());
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Clones this sentence onto the specified Corpus.
     * @param corpus the corpus of the new cloned sentence
     * @return the clone of this sentence
     */
    public Sentence clone(Corpus corpus) {

        // clone sentence data
        Sentence s = new Sentence(corpus);
        s.setStart(start);
        s.setEnd(end);
        s.setOriginalStart(originalStart);
        s.setOriginalEnd(originalEnd);

        // clone tokens
        s.tokens = new ArrayList<>(tokens.size());
        for(int i = 0; i<tokens.size(); i++) {
            s.tokens.add(null);
            s.setToken(i, getToken(i).clone(s));
        }

        // clone chunks
        ChunkList newCL = new ChunkList(s);
        for(Chunk c : chunks) {
            newCL.add(new Chunk(s, c.getIndex(), c.getStart(), c.getEnd(), c.getTag()));
        }
        s.setChunks(newCL);

        // clone annotations
        cloneAnnotations(s, getTree().getRoot(), s.getTree().getRoot());

        // clone relations
        for(Relation r : relations){
            s.addRelation(new Relation(s, r.getConcept1(), r.getConcept2(), r.getInteractor(), r.getType()));
        }

        // clone dependencies
        UndirectedGraph<Token, LabeledEdge> newDependencyGraph = new SimpleGraph<>(LabeledEdge.class);
        Set<Token> vertexSet = dependencyGraph.vertexSet();
        Set<LabeledEdge> edgeSet = dependencyGraph.edgeSet();
        for(Token t1 : vertexSet){
            for(Token t2 : s.getTokens())
                if(t1.getIndex() == t2.getIndex() && t1.getStart() == t2.getStart() && t1.getEnd() == t2.getEnd())
                    newDependencyGraph.addVertex(t2);
        }
        for(LabeledEdge e : edgeSet){
            Token t1 = (Token)e.getV1();
            Token t2 = (Token)e.getV2();
            for(Token t3 : s.getTokens()){
                if(t1.getIndex() == t3.getIndex() && t1.getStart() == t3.getStart() && t1.getEnd() == t3.getEnd()){
                    for(Token t4 : s.getTokens()){
                        if(t2.getIndex() == t4.getIndex() && t2.getStart() == t4.getStart() && t2.getEnd() == t4.getEnd()){
                            newDependencyGraph.addEdge(t3, t4, new LabeledEdge<>(t3, t4, e.getLabel()));
                            break;
                        }
                    }
                    break;
                }
            }
        }
        s.setDependencyGraph(newDependencyGraph);
        
        // clone id
        s.setId(id);

        return s;
    }

    /**
     * Creates a copy of a sentence annotations.
     * @param s sentence to copy
     * @param node annotations tree node to copy
     * @param newNode new node to create a copy
     */
    private void cloneAnnotations(Sentence s, TreeNode<Annotation> node, TreeNode<Annotation> newNode){
        newNode.setData(node.getData().clone(s));
        for(TreeNode<Annotation> child : node.getChildren()){
            TreeNode<Annotation> newChild = new TreeNode<>(null);
            cloneAnnotations(s, child, newChild);
            newNode.addChild(newChild);
        }
    }

    /**
     * Get the identifier for this sentence
     * @return this sentence ID.
     */
    public String getId() {
             return id;
    }
	 
    /**
      * Set a new identifier for this sentence
      * @param id the new identifier of the sentence.
      */
    public void setId(final String id) {
             this.id = id;
    }
    
    /**
     * Add annotation to sentence and update the labels of the tokens following
     * the desired format.
     * 
     * @param a Annotation to add to the sentence.
     */
    public void addAnnotationLabels(final Annotation a) {
        int start = a.getStartIndex();
        int end = a.getEndIndex();
        int length = end - start + 1;
        LabelFormat format = corpus.getFormat();

        for (int i = start; i <= end; i++) {
            if (i == start) { // First Token
                if (format.equals(LabelFormat.IO)) {
                    tokens.get(i).setLabel(LabelTag.I);
                } else if (format.equals(LabelFormat.BIO)) {
                    tokens.get(i).setLabel(LabelTag.B);
                } else if (format.equals(LabelFormat.BMEWO)) {
                    if (length > 1) {
                        tokens.get(i).setLabel(LabelTag.B);
                    } else {
                        tokens.get(i).setLabel(LabelTag.W);
                    }
                }
            } else if (i == end) { // Last Token
                if (format.equals(LabelFormat.BMEWO)) {
                    tokens.get(i).setLabel(LabelTag.E);
                } else {
                    tokens.get(i).setLabel(LabelTag.I);
                }
            } else { // Tokens in the Middle
                if (format.equals(LabelFormat.BMEWO)) {
                    tokens.get(i).setLabel(LabelTag.M);
                } else {
                    tokens.get(i).setLabel(LabelTag.I);
                }
            }
        }
    }

    /**
     * Compare two sentences.
     *
     * @param o the sentence to be compared with.
     * @return <tt>true</tt> if the two sentences are equal or <tt>false</tt> in case otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Sentence other = (Sentence) o;

        if (end != other.end)
            return false;
        if (start != other.start)
            return false;
        if (corpus != null ? !corpus.equals(other.corpus) : other.corpus != null)
            return false;
        if (chunks != null ? !chunks.equals(other.chunks) : other.chunks != null)
            return false;
        if (relations != null ? !relations.equals(other.relations) : other.relations != null)
            return false;
        if (tokens != null ? !tokens.equals(other.tokens) : other.tokens != null)
            return false;
        if (tree != null ? !tree.equals(other.tree) : other.tree != null)
            return false;
        if (id != null ? !id.equals(other.id) : other.id != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = corpus.hashCode();
        result = 31 * result + tokens.hashCode();
        result = 31 * result + tree.hashCode();
        result = 31 * result + start;
        result = 31 * result + end;
        return result;
    }
    
}
