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
package pt.ua.tm.neji.tree;

import java.io.Serializable;
import java.util.*;

public class Tree<T extends Comparable<? super T>> implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    public enum TreeTraversalOrderEnum {
        PRE_ORDER,
        POST_ORDER
    }

    private TreeNode<T> root;

    public Tree() {
        super();
    }

    public TreeNode<T> getRoot() {
        return this.root;
    }

    public void setRoot(TreeNode<T> root) {
        this.root = root;
    }

    public int getNumberOfNodes() {
        int numberOfNodes = 0;

        if (root != null) {
            numberOfNodes = auxiliaryGetNumberOfNodes(root) + 1; //1 for the root!
        }

        return numberOfNodes;
    }

    private int auxiliaryGetNumberOfNodes(TreeNode<T> node) {
        int numberOfNodes = node.getNumberOfChildren();

        for (TreeNode<T> child : node.getChildren()) {
            numberOfNodes += auxiliaryGetNumberOfNodes(child);
        }

        return numberOfNodes;
    }

    /**
     * Checks if any Node in the tree contains the specified data.
     *
     * @param dataToFind the data to be checked.
     * @return <tt>true</tt> if a Node contains the specified data or <tt>false</tt> if not.
     */
    public boolean exists(T dataToFind) {
        return (find(dataToFind) != null);
    }

    /**
     * Finds the Node that contains the specified data.
     *
     * @param dataToFind the data to be found.
     * @return the Node that contains the specified data.
     */
    public TreeNode<T> find(T dataToFind) {
        TreeNode<T> returnNode = null;

        if (root != null) {
            returnNode = auxiliaryFind(root, dataToFind);
        }

        return returnNode;
    }

    /**
     * Removes the Node that contains the specified data from the tree and moves its children nodes to the parent.
     *
     * @param dataToRemove the specified data to be removed.
     * @return the removed Node that contains the specified data, or <tt>null</tt> if the specified data does not exist in the tree
     */
    public TreeNode<T> removeNode(T dataToRemove) {
        TreeNode<T> returnNode = removeNodeAndChildren(dataToRemove);
        if(returnNode!=null) {
            if(returnNode.equals(root)){
                return returnNode;
            }
            Iterator<TreeNode<T>> iter = returnNode.getChildren().iterator();
            TreeNode<T> parent = returnNode.getParent();
            while(iter.hasNext()) {
                parent.addChild(iter.next());
            }
            return returnNode;
        }
        else
            return null;
    }

    /**
     * Removes the Node that contains the specified data and all of its children nodes from the tree.
     *
     * @param dataToRemove the specified data to be removed (along with its children nodes).
     * @return the removed Node that contains the specified data, or <tt>null</tt> if the specified data does not exist in the tree
     */
    public TreeNode<T> removeNodeAndChildren(T dataToRemove) {
        TreeNode<T> returnNode = find(dataToRemove);
        if(returnNode!=null) {
            if(returnNode.equals(root)){
                return returnNode;
            }
            TreeNode<T> parent = returnNode.getParent();
            int idx = parent.getChildren().indexOf(returnNode);
            parent.removeChildAt(idx);
            return returnNode;
        }
        else
            return null;
    }

    private TreeNode<T> auxiliaryFind(TreeNode<T> currentNode, T dataToFind) {
        TreeNode<T> returnNode = null;
        int i = 0;

        if (currentNode.getData().equals(dataToFind)) {
            returnNode = currentNode;
        } else if (currentNode.hasChildren()) {
            i = 0;
            while (returnNode == null && i < currentNode.getNumberOfChildren()) {
                returnNode = auxiliaryFind(currentNode.getChildAt(i), dataToFind);
                i++;
            }
        }

        return returnNode;
    }

    public boolean isEmpty() {
        return (root == null);
    }

    /**
     * Get T elements of tree of specific level.
     *
     * @param level Level of the elements to return.
     * @return List of T elements.
     */
    public List<T> buildData(final int level) {
        if (level < 0) {
            throw new RuntimeException("The tree level could not be a negative value.");
        }
        List<T> result = new ArrayList<T>();
        if (root != null) {
            buildData(level, 0, root, result);
        }
        return result;
    }

    private void buildData(int targetLevel, int currentLevel, TreeNode<T> node, List<T> result) {
        if (currentLevel == targetLevel) {
            result.add(node.getData());
        }
        for (TreeNode<T> child : node.getChildren()) {
            if (currentLevel < targetLevel) {
                buildData(targetLevel, currentLevel + 1, child, result);
            }
        }
    }

    /**
     * Get tree nodes of specific level.
     *
     * @param level Level of the elements to return.
     * @return List of tree nodes.
     */
    public List<TreeNode<T>> build(final int level) {
        if (level < 0) {
            throw new RuntimeException("The tree level could not be a negative value.");
        }
        List<TreeNode<T>> result = new ArrayList<TreeNode<T>>();
        if (root != null) {
            build(level, 0, root, result);
        }
        return result;
    }

    private void build(int targetLevel, int currentLevel, TreeNode<T> node, List<TreeNode<T>> result) {
        if (currentLevel <= targetLevel) {
            result.add(node);
        }

        for (TreeNode<T> child : node.getChildren()) {
            if (currentLevel < targetLevel) {
                build(targetLevel, currentLevel + 1, child, result);
            }
        }
    }

    public List<TreeNode<T>> build(TreeTraversalOrderEnum traversalOrder) {
        List<TreeNode<T>> returnList = null;

        if (root != null) {
            returnList = build(root, traversalOrder);
        }

        return returnList;
    }

    public List<TreeNode<T>> build(TreeNode<T> node, TreeTraversalOrderEnum traversalOrder) {
        List<TreeNode<T>> traversalResult = new ArrayList<TreeNode<T>>();

        if (traversalOrder == TreeTraversalOrderEnum.PRE_ORDER) {
            buildPreOrder(node, traversalResult);
        } else if (traversalOrder == TreeTraversalOrderEnum.POST_ORDER) {
            buildPostOrder(node, traversalResult);
        }
        return traversalResult;
    }

    private void buildPreOrder(TreeNode<T> node, List<TreeNode<T>> traversalResult) {
        traversalResult.add(node);
        for (TreeNode<T> child : node.getChildren()) {
            buildPreOrder(child, traversalResult);
        }
    }

    private void buildPostOrder(TreeNode<T> node, List<TreeNode<T>> traversalResult) {
        for (TreeNode<T> child : node.getChildren()) {
            buildPostOrder(child, traversalResult);
        }
        traversalResult.add(node);
    }

    /**
     * Build list o T elements following a specific traversal order.
     *
     * @param traversalOrder Tree traversal order to follow.
     * @param withRoot       Provide result with or without data from root none.
     * @return List of T elements.
     */
    public List<T> buildData(final TreeTraversalOrderEnum traversalOrder, final boolean withRoot) {
        List<T> result = new ArrayList<T>();
        List<TreeNode<T>> traversalResult = build(traversalOrder);
        for (TreeNode<T> node : traversalResult) {
            if (node.equals(root) && !withRoot) {
                continue;
            }
            result.add(node.getData());
        }
        return result;
    }

    public Map<TreeNode<T>, Integer> buildWithDepth(TreeTraversalOrderEnum traversalOrder) {
        Map<TreeNode<T>, Integer> returnMap = null;

        if (root != null) {
            returnMap = buildWithDepth(root, traversalOrder);
        }

        return returnMap;
    }

    public Map<TreeNode<T>, Integer> buildWithDepth(TreeNode<T> node, TreeTraversalOrderEnum traversalOrder) {
        Map<TreeNode<T>, Integer> traversalResult = new LinkedHashMap<TreeNode<T>, Integer>();

        if (traversalOrder == TreeTraversalOrderEnum.PRE_ORDER) {
            buildPreOrderWithDepth(node, traversalResult, 0);
        } else if (traversalOrder == TreeTraversalOrderEnum.POST_ORDER) {
            buildPostOrderWithDepth(node, traversalResult, 0);
        }

        return traversalResult;
    }

    private void buildPreOrderWithDepth(TreeNode<T> node, Map<TreeNode<T>, Integer> traversalResult, int depth) {
        traversalResult.put(node, depth);

        for (TreeNode<T> child : node.getChildren()) {
            buildPreOrderWithDepth(child, traversalResult, depth + 1);
        }
    }

    private void buildPostOrderWithDepth(TreeNode<T> node, Map<TreeNode<T>, Integer> traversalResult, int depth) {
        for (TreeNode<T> child : node.getChildren()) {
            buildPostOrderWithDepth(child, traversalResult, depth + 1);
        }

        traversalResult.put(node, depth);
    }

    /**
     * Build map of T elements with its specific level, following a specific traversal order.
     *
     * @param traversalOrder Tree traversal order to follow.
     * @param withRoot       Provide result with or without data from root none.
     * @return Map of T elements and its specific tree level.
     */
    public Map<T, Integer> buildDataWithDepth(final TreeTraversalOrderEnum traversalOrder, final boolean withRoot) {
        Map<T, Integer> result = new HashMap<T, Integer>();
        Map<TreeNode<T>, Integer> traversalResult = buildWithDepth(traversalOrder);
        for (TreeNode<T> node : traversalResult.keySet()) {
            if (node.equals(root) && !withRoot) {
                continue;
            }
            Integer depth = traversalResult.get(node);
            result.put(node.getData(), depth);
        }
        return result;
    }

    @Override
    public String toString() {
        String stringRepresentation = "";
        if (root != null) {
            stringRepresentation = build(TreeTraversalOrderEnum.PRE_ORDER).toString();
        }
        return stringRepresentation;
    }

    public String toStringWithDepth() {
        String stringRepresentation = "";
        if (root != null) {
            stringRepresentation = buildWithDepth(TreeTraversalOrderEnum.PRE_ORDER).toString();
        }
        return stringRepresentation;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Tree)) return false;
        @SuppressWarnings({"unchecked"})
        Tree<T> other = (Tree<T>) obj;
        return equals0(this.root, other.root);
    }

    private boolean equals0(TreeNode<T> thisNode, TreeNode<T> otherNode){
        if(thisNode.getData().equals(otherNode.getData()) &&
                thisNode.getChildren().size() == otherNode.getChildren().size()){
            if(thisNode.getChildren().size() > 0){

                Iterator<TreeNode<T>> iter1 = thisNode.getChildren().iterator();

                Iterator<TreeNode<T>> iter2 = otherNode.getChildren().iterator();

                while(iter1.hasNext() && iter2.hasNext()){
                    if(!equals0(iter1.next(), iter2.next())){
                        return false;
                    }
                }
                return true;
            } else {
                return true;
            }

        }
        return false;
    }
}
