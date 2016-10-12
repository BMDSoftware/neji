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

package pt.ua.tm.neji.util.obo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 06/07/15.
 */
public class OBOTerm {
    private String id;
    private Set<String> names;

    public OBOTerm() {
        this.id = null;
        this.names = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Set<String> getNames() {
        return names;
    }

    public void setNames(final Set<String> names) {
        this.names = names;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final OBOTerm term = (OBOTerm) o;

        if (id != null ? !id.equals(term.id) : term.id != null) return false;
        return !(names != null ? !names.equals(term.names) : term.names != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (names != null ? names.hashCode() : 0);
        return result;
    }
}
