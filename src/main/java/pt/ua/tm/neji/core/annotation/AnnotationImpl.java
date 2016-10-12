
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

import org.apache.commons.lang.StringUtils;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Annotation} implementation that provides base functionality for annotations and
 * static factories for different types of positional indexes and textual representation.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>)
 * @version 2.0
 * @since 1.0
 */
public class AnnotationImpl
        implements Annotation, Comparable<Annotation>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The annotation is part of an sentence.
     */
    private Sentence sentence;

    /**
     * Index of the first token that is inside the annotation.
     */
    private int startIndex;

    /**
     * Index of the last token that is inside the annotation.
     */
    private int endIndex;

    /**
     * Confidence value to generate the annotation.
     */
    private double score;

    /**
     * Identifiers for this annotation.
     */
    private List<Identifier> ids;

    /**
     * Type of this annotation.
     */
    private AnnotationType type;

    /**
     * State of normalization for this annotation.
     */
    private boolean normalized;

    private AnnotationImpl(final Sentence s,
                           final int startIndex,
                           final int endIndex,
                           final double score) {
        this.sentence = s;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.score = score;
        this.ids = new ArrayList<>();
        this.type = AnnotationType.LEAF;
        this.normalized = false;
    }

    public static Annotation newAnnotationByTokenPositions(final Sentence s,
                                                           final int startIndex, final int endIndex, final double score) {
        return new AnnotationImpl(s, startIndex, endIndex, score);
    }

    public static Annotation newAnnotationByCharPositions(final Sentence s,
                                                          final int firstChar, final int lastChar, final double score) {
        int startToken = 0, endToken = 0;

        Token t;
        for (int i = 0; i < s.size(); i++) {
            t = s.getToken(i);

            if (firstChar >= t.getStart()) {
                startToken = i;
            }
            if (lastChar >= t.getEnd()) {
                endToken = i;
            }
        }

        if (endToken < startToken) {
            // Annotation doesn't fit tokenization!
            return null;
        } else {
            return new AnnotationImpl(s, startToken, endToken, score);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sentence getSentence() {
        return sentence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getScore() {
        return score;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setType(AnnotationType type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Identifier> getIDs(){
        return ids;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addID(Identifier toAdd) {
        ids.add(toAdd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIDs(List<Identifier> ids) {
        this.ids = ids;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean areIDsFromTheSameGroup() {
        if (ids.isEmpty()) {
            return true;
        } else {
            String group = ids.get(0).getGroup();
            return areIDsFromTheSameGroup(group);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean areIDsFromTheSameGroup(String group) {
        boolean sameGroup = true;
        for (int i = 0; i < ids.size(); i++) {
            if (!ids.get(i).getGroup().equals(group)) {
                sameGroup = false;
                break;
            }
        }
        return sameGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNormalized() {
        return normalized;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNormalized(boolean isNormalized) {
        this.normalized = isNormalized;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringIDs() {
        return StringUtils.join(ids, "|");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        int start = getSentence().getToken(getStartIndex()).getStart();
        int end = getSentence().getToken(getEndIndex()).getEnd()+1;
        return getSentence().getText().substring(start, end);
    }

    /**
     * Provides a textual representation of the annotation.
     * @return a {@link String} representing the annotation
     */
    @Override
    public String toString() {
        if (getStartIndex() < 0 || getEndIndex() < 0) {
            return "";
        }
        String s = "(" + getStartIndex() + "," + getEndIndex() + "): " + getText();
        String ids = getStringIDs();
        if(!ids.equals(""))
            s = s + " - " + ids;
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Annotation a) {
        if (this.getSentence() != a.getSentence() && ( this.getSentence() == null ||
                !this.getSentence().equals(a.getSentence()) )) {
            return false;
        }
        if (this.equals(a)){
            return false;
        }

        if (this.getStartIndex() <= a.getStartIndex() && this.getEndIndex() >= a.getEndIndex()) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean intersection(Annotation a) {
        if (this.getSentence() != a.getSentence() && ( this.getSentence() == null ||
                !this.getSentence().equals(a.getSentence()) )) {
            return false;
        }

        if (this.nested(a) || a.nested(this)) {
            return false;
        }

        if (this.getStartIndex() >= a.getStartIndex() &&
                this.getStartIndex() <= a.getEndIndex() && this.getEndIndex() > a.getEndIndex()) {
            return true;
        }

        if (this.getEndIndex() >= a.getStartIndex() &&
                this.getEndIndex() <= a.getEndIndex() && this.getStartIndex() < a.getStartIndex()) {
            return true;
        }

        if (a.getStartIndex() >= this.getStartIndex() &&
                a.getStartIndex() <= this.getEndIndex() && a.getEndIndex() > this.getEndIndex()) {
            return true;
        }

        if (a.getEndIndex() >= this.getStartIndex() &&
                a.getEndIndex() <= this.getEndIndex() && a.getStartIndex() < this.getStartIndex()) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean nested(Annotation a) {
        if (this.getSentence() != a.getSentence() && ( this.getSentence() == null ||
                !this.getSentence().equals(a.getSentence()) )) {
            return false;
        }
        if (this.equals(a)){
            return false;
        }
        if (( this.getStartIndex() >= a.getStartIndex() )
                && ( this.getStartIndex() <= a.getEndIndex() )
                && ( this.getEndIndex() >= a.getStartIndex() )
                && ( this.getEndIndex() <= a.getEndIndex() )) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Annotation a) {
        return new AnnotationComparator().compare(this, a);
    }

    /**
     * Checks the equality between this annotation and the specified one.
     *
     * @param obj the annotation to be compared with.
     * @return <code>true</code> if the two annotations are equal, and
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Annotation)) {
            return false;
        }
        final Annotation other = (Annotation) obj;
        int thisSentenceStart = this.getSentence().getStart();
        int otherSentenceStart = other.getSentence().getStart();
        int thisSentenceEnd = this.getSentence().getEnd();
        int otherSentenceEnd = other.getSentence().getEnd();
        String thisSentenceText = this.getSentence().getText();
        String otherSentenceText = other.getSentence().getText();

        if (thisSentenceStart != otherSentenceStart) {
            return false;
        }
        if (thisSentenceEnd != otherSentenceEnd) {
            return false;
        }
        if (thisSentenceText == null || !thisSentenceText.equals(otherSentenceText)) {
            return false;
        }
        if (this.getStartIndex() != other.getStartIndex()) {
            return false;
        }
        if (this.getEndIndex() != other.getEndIndex()) {
            return false;
        }
        return true;
    }

    /**
     * Override the hashCode method to consider all the internal variables.
     * @return unique number for each annotation
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + ( this.getSentence() != null ? this.getSentence().hashCode() : 0 );
        hash = 79 * hash + this.getStartIndex();
        hash = 79 * hash + this.getEndIndex();
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Annotation clone(Sentence s) {
        Annotation cloned = new AnnotationImpl(s, getStartIndex(), getEndIndex(), getScore());
        cloned.setType(getType());
        cloned.setNormalized(isNormalized());
        for(Identifier id : getIDs())
            cloned.addID(id);
        return cloned;
    }
}
