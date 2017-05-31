
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Identifier for annotations.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Identifier implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Identifier.class);

    private String source;
    private String id;
    private String subgroup;
    private String group;

    public Identifier(String source, String id, String subgroup, String group) {
        this.source = source;
        this.id = id;
        this.subgroup = subgroup;
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSubGroup() {
        return subgroup;
    }

    public void setSubGroup(String subgroup) {
        this.subgroup = subgroup;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Identifier other = (Identifier) obj;
        if (( this.source == null ) ? ( other.source != null ) : !this.source.equals(other.source)) {
            return false;
        }
        if (( this.id == null ) ? ( other.id != null ) : !this.id.equals(other.id)) {
            return false;
        }
        if (( this.subgroup == null ) ? ( other.subgroup != null ) : !this.subgroup.equals(other.subgroup)) {
            return false;
        }
        if (( this.group == null ) ? ( other.group != null ) : !this.group.equals(other.group)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + ( this.source != null ? this.source.hashCode() : 0 );
        hash = 97 * hash + ( this.id != null ? this.id.hashCode() : 0 );
        hash = 97 * hash + ( this.subgroup != null ? this.subgroup.hashCode() : 0 );
        hash = 97 * hash + ( this.group != null ? this.group.hashCode() : 0 );
        return hash;
    }

    @Override
    public String toString() {
        return ( source == null ? "" : source ) + ":"
                + ( id == null ? "" : id ) + ":"
                + ( subgroup == null ? "" : subgroup ) + ":"
                + ( group == null ? "" : group );
    }
    
    public String toStringMinimized() {
        return ( source == null ? "" : source.toLowerCase() ) + ":"
                + ( id == null ? "" : id );
    }

    /**
     * Get a list of identifiers from a formated string.
     *
     * @param text The text that contains the identifiers. Each ID must follow
     * the format: source:identifier:subgroup:group. Various IDs are provided
     * following the format: ID1|ID2|ID3.
     * @return The list of identifiers.
     */
    public static List<Identifier> getIdentifiersFromText(String text) {
        List<Identifier> ids = new ArrayList<Identifier>();

        String[] textIDs = text.split("[|]");
        for (String textID : textIDs) {
            ids.add(getIdentifierFromText(textID));
        }

        return ids;
    }

    public static Identifier getIdentifierFromText(String text) {

        // Solve problems associated with IDs provided by LINNAEUS
        if (text.contains("species:ncbi")) {
            text = text.replaceAll("[\\\\?][\\d]+[\\\\.,][\\d]+", "");
            text = text.replaceAll("species:ncbi:(\\d+)", "NCBI:$1:T001:SPEC");
            text = text.replaceAll("SPECE-4", "SPEC");
        }


        String[] parts = text.split("[:]");
        String source, id, subgroup, group;

        if (parts.length != 4) {
            source = parts[0];
            id = parts[1];
            subgroup = null;
            group = null;
            /*throw new RuntimeException("There is a problem with the ID format."
                    + "It must contain 4 parts: source:id:subgroup:group."
                    + "Provided ID: " + text);*/
        } else {
            source = parts[0];
            id = parts[1];
            subgroup = parts[2];
            group = parts[3];
        }

        return new Identifier(source, id, subgroup, group);
    }
}
