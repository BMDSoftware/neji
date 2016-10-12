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

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class OverlappingVector extends Vector<OverlappingEntry> {
    public OverlappingVector() {
        super();
    }

    public int getOccurrences() {
        int sum = 0;
        for (OverlappingEntry se : this) {
            sum += se.getOccurrences();
        }
        return sum;
    }

    public int getUnique() {
        return size();
    }

    public void sort() {
        Collections.sort(this, new OverlappingEntryComparator());
    }

    @Override
    public Iterator<OverlappingEntry> iterator() {
        return super.iterator();
    }
}
