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

package pt.ua.tm.neji.dictionary;

import uk.ac.man.entitytagger.matching.Matcher;

/**
 * Internal representation of a dictionary.
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Dictionary {
    private Matcher matcher;
    private String group;

    /**
     * Dictionary constructor.
     * @param matcher Value of matcher.
     * @param group Value of group.
     */
    public Dictionary(Matcher matcher, String group) {
        this.group = group;
        this.matcher = matcher;
    }

    /**
     * Gets group.
     *
     * @return Value of group.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets new group.
     *
     * @param group New value of group.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Sets new matcher.
     *
     * @param matcher New value of matcher.
     */
    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    /**
     * Gets matcher.
     *
     * @return Value of matcher.
     */
    public Matcher getMatcher() {
        return matcher;
    }
}
