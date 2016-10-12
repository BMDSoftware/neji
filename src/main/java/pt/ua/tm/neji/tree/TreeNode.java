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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TreeNode<T extends Comparable<? super T>> implements Comparable<TreeNode<T>>, Serializable {
    private static final long serialVersionUID = 1L;
    private T data;
    private List<TreeNode<T>> children;
    private TreeNode<T> parent;
    boolean visited;

    public TreeNode() {
        super();
        children = new ArrayList<TreeNode<T>>();
        visited = false;
    }

    public TreeNode(T data) {
        this();
        setData(data);
    }

    public TreeNode<T> getParent() {
        return this.parent;
    }

    public List<TreeNode<T>> getChildren() {
        return this.children;
    }

    public int getNumberOfChildren() {
        return getChildren().size();
    }

    public boolean hasChildren() {
        return ( getNumberOfChildren() > 0 );
    }

    public void setChildren(List<TreeNode<T>> children) {
        for (TreeNode<T> child : children) {
            child.parent = this;
        }

        this.children = children;
    }

    public void addChild(TreeNode<T> child) {
        child.parent = this;

        int index = Collections.binarySearch(children, child);
        if (index < 0) {
            index = ~index;
        }
        children.add(index, child);
    }

    public void addChildAt(int index, TreeNode<T> child) throws IndexOutOfBoundsException {
        child.parent = this;
        children.add(index, child);

    }

    public void removeChildren() {
        this.children = new ArrayList<>();
    }

    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }

    public TreeNode<T> getChildAt(int index) throws IndexOutOfBoundsException {
        return children.get(index);
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String toString() {
        return getData().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TreeNode<?> other = (TreeNode<?>) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc) @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( data == null ) ? 0 : data.hashCode() );
        return result;
    }

    public String toStringVerbose() {
        String stringRepresentation = getData().toString() + ":[";

        for (TreeNode<T> node : getChildren()) {
            stringRepresentation += node.getData().toString() + ", ";
        }

        //Pattern.DOTALL causes ^ and $ to match. Otherwise it won't. It's retarded.
        Pattern pattern = Pattern.compile(", $", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(stringRepresentation);

        stringRepresentation = matcher.replaceFirst("");
        stringRepresentation += "]";

        return stringRepresentation;
    }

    @Override
    public int compareTo(TreeNode<T> t) {
        return data.compareTo(t.getData());
    }
}
