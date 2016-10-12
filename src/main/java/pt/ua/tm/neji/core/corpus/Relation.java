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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.core.corpus;

import pt.ua.tm.neji.core.annotation.Annotation;

import java.io.Serializable;

/**
 * Class that represents a Relation between two concepts.
 * This relation can be defined by a {@link Token} or a boolean type.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Relation implements Serializable {

    private static final long serialVersionUID = 1L;
    private Sentence sentence;
    private Annotation concept1;
    private Annotation concept2;
    private Token interactor;
    private Boolean type;

    public Relation(final Sentence s, final Annotation concept1, final Annotation concept2, final Boolean type) {
        this(s, concept1, concept2, null, type);
    }

    public Relation(final Sentence s, final Annotation concept1, final Annotation concept2,
                    final Token interactor) {
        this(s, concept1, concept2, interactor, null);
    }

    public Relation(final Sentence s, final Annotation concept1, final Annotation concept2) {
        this(s, concept1, concept2, null, null);
    }

    public Relation(final Sentence s, final Annotation concept1, final Annotation concept2,
                    final Token interactor, final Boolean type) {

        this.sentence = s;
        this.concept1 = concept1;
        this.concept2 = concept2;
        this.interactor = interactor;
        this.type = type;
    }

    public Annotation getConcept1() {
        return concept1;
    }

    public void setConcept1(Annotation concept1) {
        this.concept1 = concept1;
    }

    public Annotation getConcept2() {
        return concept2;
    }

    public void setConcept2(Annotation concept2) {
        this.concept2 = concept2;
    }

    public Token getInteractor() {
        return interactor;
    }

    public void setInteractor(Token interactor) {
        this.interactor = interactor;
    }

    public Boolean getType() {
        return type;
    }

    public void setType(Boolean type) {
        this.type = type;
    }

    public Sentence getSentence() {
        return sentence;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Relation)) {
            return false;
        }

        Relation other = (Relation) o;
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
        if (concept1 == null || concept2 == null) {
            return false;
        }
        if (other.concept1 == null || other.concept2 == null) {
            return false;
        }
        if (concept1.equals(other.concept1) && concept2.equals(other.concept2)) {
            return true;
        }
        if (concept1.equals(other.concept2) && concept2.equals(other.concept1)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = sentence != null ? sentence.hashCode() : 0;
        result = 31 * result + (concept1 != null ? concept1.hashCode() : 0) + (concept2 != null ? concept2.hashCode() : 0);
//        result = 31 * result + (interactor != null ? interactor.hashCode() : 0);
//        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(concept1.toString() + ", " + concept2.toString());

        if(interactor!=null)
            sb.append(", interactor:" + interactor);

        if(type!=null)
            sb.append(", type:" + type);

        sb.append("}");
        return sb.toString();
    }
}
