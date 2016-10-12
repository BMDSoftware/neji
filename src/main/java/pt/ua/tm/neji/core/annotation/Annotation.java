
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

package pt.ua.tm.neji.core.annotation;

import pt.ua.tm.neji.core.corpus.Sentence;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a specific occurrence in a {@link Sentence} and allows information
 * regarding entities and their interactions to be organized and identified.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public interface Annotation extends Comparable<Annotation>, Serializable {

    /**
     * Provides access to the index of the first token that is inside the annotation.
     * @return the index of the first token that is inside the annotation
     */
    int getStartIndex();

    /**
     * Modifies the index of the first token that is inside the annotation with the
     * specified one.
     */
    void setStartIndex(int startIndex);

    /**
     * Provides access to the index of the last token that is inside the annotation.
     * @return the index of the last token that is inside the annotation
     */
    int getEndIndex();

    /**
     * Modifies the index of the last token that is inside the annotation with the
     * specified one.
     */
    void setEndIndex(int endIndex);

    /**
     * Provides access to the sentence where this annotation belongs to.
     * @return the sentence
     */
    Sentence getSentence();

    /**
     * Provides access to the confidence value.
     * @return the confidence value to generate the annotation
     */
    double getScore();

    /**
     * Returns the type of Annotation
     * @return the type of Annotation
     */
    AnnotationType getType();

    /**
     * Modifies the type of this Annotation
     * @param type the new type of this Annotation
     */
    void setType(AnnotationType type);

    /**
     * Returns a list of {@link Identifier} for this annotation.
     * @return a list of {@link Identifier} for this annotation
     */
    List<Identifier> getIDs();

    /**
     * Sets the specified {@link Identifier} as another Identifier for this Annotation
     * @param toAdd the specified {@link Identifier} that will become an
     *              Identifier for this Annotation
     */
    void addID(Identifier toAdd);

    /**
     * Sets the specified {@link Identifier} list as the new list of Identifiers
     * @param ids the new list of Identifiers for this Annotation
     */
    void setIDs(List<Identifier> ids);

    /**
     * Checks if the Identifiers of this annotation are all of the same group
     * @return <code>true</code> if the Identifiers in this annotation are all
     * part of the same group, and <code>false</code> otherwise.
     */
    boolean areIDsFromTheSameGroup();

    /**
     * Checks if the Identifiers of this annotation are all of the same specified group
     * @return <code>true</code> if the Identifiers in this annotation are all part of
     *         the same specified group, and <code>false</code> otherwise.
     */
    boolean areIDsFromTheSameGroup(String group);

    /**
     * Checks if this annotation was normalized or not.
     * @return if this annotation was normalized
     */
    boolean isNormalized();

    /**
     * Sets a new state to indicate that this annotation is (or not) normalized.
     * @param isNormalized the new normalization state for this annotation
     */
    void setNormalized(boolean isNormalized);

    /**
     * Returns a textual representation for every Identifier from this annotation.
     * @return a textual representation for every Identifier from this annotation
     */
    String getStringIDs();

    /**
     * Returns the text of this annotation based on the token indexes.
     * @return the text of this annotation
     */
    String getText();

    /**
     * Verifies if the specified annotation is contained in the current one.
     * In other words, verifies the search matching alignment.
     *
     * @param a the annotation that should be contained in the current one
     * @return <code>true</code> if the specified annotation is contained
     *         in the current one, and <code>false</code> otherwise.
     */
    boolean contains(Annotation a);

    /**
     * Verifies if the specified annotation is intersected with the current one.
     *
     * @param a the annotation that should be intersected with the current one.
     * @return <code>true</code> if the specified annotation is intersected
     *         in the current one, and <code>false</code> otherwise.
     */
    boolean intersection(Annotation a);

    /**
     * Verifies if the specified annotation is nested in the current one.
     *
     * @param a the annotation that should be nested in the current one.
     * @return <code>true</code> if the specified annotation is nested
     *         in the current one, and <code>false</code> otherwise.
     */
    boolean nested(Annotation a);

    /**
     * Compares this annotation with the specified one in terms of their position
     * in the sentence, comparing the start and end indexes.
     * @param t the other annotation to compare with this
     * @return the result of the comparison
     */
    @Override
    int compareTo(Annotation t);

    /**
     * Clones this Annotation and sets the resulting cloned annotation into the specified Sentence.
     * @param s the Sentence where the cloned Annotation will be set into
     * @return the cloned Annotation
     */
    Annotation clone(Sentence s);
}
