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

package pt.ua.tm.neji.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class OverlappingEntry {

    private StatisticsEntry parent;
    private List<StatisticsEntry> children;
    private int occurrences;

    public OverlappingEntry(final StatisticsEntry parent) {
        this.parent = parent;
        this.children = new ArrayList<StatisticsEntry>();
        this.occurrences = 1;
    }

    public void addChild(final StatisticsEntry child) {
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    /**
     * Gets children.
     *
     * @return Value of children.
     */
    public List<StatisticsEntry> getChildren() {
        return children;
    }

    /**
     * Gets parent.
     *
     * @return Value of parent.
     */
    public StatisticsEntry getParent() {
        return parent;
    }

    /**
     * Gets occurrences.
     *
     * @return Value of occurrences.
     */
    public int getOccurrences() {
        return occurrences;
    }

    /**
     * Sets new occurrences.
     *
     * @param occurrences New value of occurrences.
     */
    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OverlappingEntry)) {
            return false;
        }

        OverlappingEntry that = (OverlappingEntry) o;

        if (children != null ? !children.equals(that.children) : that.children != null) {
            return false;
        }
        if (parent != null ? !parent.equals(that.parent) : that.parent != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(parent.getName());
        sb.append("\t");
        sb.append("(");
        sb.append(parent.getGroup());
        sb.append(")");
        sb.append("\t");
        sb.append("- ");
        sb.append(occurrences);
        sb.append("\n");

        for (StatisticsEntry se : children) {
            sb.append("\t");
            sb.append(se.getName());
            sb.append("\t");
            sb.append("(");
            sb.append(se.getGroup());
            sb.append(")");
            sb.append("\n");
        }

        return sb.toString();
    }

}
