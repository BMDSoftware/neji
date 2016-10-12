
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

import pt.ua.tm.neji.core.corpus.dependency.ChunkTag;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Class that represents a Chunk.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Chunk implements Serializable {

    private static final long serialVersionUID = 2L;
    private int index;
    private int start;
    private int end;
    private ChunkTag tag;
    private Sentence sentence;

    public Chunk(Sentence sentence, int index, int start, int end, ChunkTag tag) {
        this.sentence = sentence;
        this.index = index;
        this.start = start;
        this.end = end;
        this.tag = tag;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public ChunkTag getTag() {
        return tag;
    }

    public void setTag(ChunkTag tag) {
        this.tag = tag;
    }

    public Sentence getSentence() {
        return sentence;
    }

    public void setSentence(Sentence sentence) {
        this.sentence = sentence;
    }

    /**
     * Get BIO tag of the token in the chunk.
     * @param token The token that makes part of the chunk.
     * @return BIO tag of the token.
     */
    public String getBIOTag(final Token token) {
        if (token.getIndex() == getStart()) {
            if (getTag().equals(ChunkTag.O)){
                return "O";
            } else {
                return "B-" + getTag();
            }
        } else if (token.getIndex() > getStart() && token.getIndex() <= getEnd()) {
            if (getTag().equals(ChunkTag.O)){
                return "O";
            } else {
                return "I-" + getTag();
            }
        } else {
            throw new NoSuchElementException("The provided token is not part of the chunk.");
        }
    }

    @Override
    public String toString() {
        return "Chunk{" + "start=" + start + ", end=" + end + ", tag=" + tag + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chunk other = (Chunk) o;
        int thisSentenceStart = this.getSentence().getStart();
        int otherSentenceStart = other.getSentence().getStart();
        int thisSentenceEnd = this.getSentence().getEnd();
        int otherSentenceEnd = other.getSentence().getEnd();
        String thisSentenceText = this.getSentence().getText();
        String otherSentenceText = other.getSentence().getText();

        if (thisSentenceStart != otherSentenceStart) return false;
        if (thisSentenceEnd != otherSentenceEnd) return false;
        if (thisSentenceText == null || !thisSentenceText.equals(otherSentenceText)) return false;
        if (end != other.end) return false;
        if (index != other.index) return false;
        if (start != other.start) return false;
        if (tag != other.tag) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
