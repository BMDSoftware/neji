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

package pt.ua.tm.neji.writer.json;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON entry representation.
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class JSONEntry {

    private int id;
    private int start;
    private int end;
    private String text;
    private List<JSONTerm> terms;

    public JSONEntry(int id, int start, int end, String text) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.text = text;
        this.terms = new ArrayList<JSONTerm>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<JSONTerm> getTerms() {
        return terms;
    }

    public void setTerms(List<JSONTerm> terms) {
        this.terms = terms;
    }
    
    public void addTerm(JSONTerm term) {
        terms.add(term);
    }
    
}
