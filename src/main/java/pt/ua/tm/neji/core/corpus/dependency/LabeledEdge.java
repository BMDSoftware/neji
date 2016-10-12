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

package pt.ua.tm.neji.core.corpus.dependency;

import org.jgrapht.graph.DefaultEdge;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 12/03/13
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class LabeledEdge<V, T> extends DefaultEdge {
    private V v1;
    private V v2;
    private T label;

    public LabeledEdge(final V v1, final V v2, final T label) {
        this.v1 = v1;
        this.v2 = v2;
        this.label = label;
    }

    public V getV1() {
        return v1;
    }

    public V getV2() {
        return v2;
    }

    public T getLabel() {
        return label;
    }

    public String toString() {
        return v1.toString() + " - " + label.toString() + " - " + v2.toString();
    }
}
