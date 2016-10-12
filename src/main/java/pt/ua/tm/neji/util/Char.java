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

package pt.ua.tm.neji.util;

/**
 * Utility class for characters.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Char {

    public static int getNumNonWhiteSpaceChars(String s) {
        assert (s != null);
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ') {
                count++;
            }
        }
        return count;
    }

    public static Character[] getNonBreakingWhiteSpaces() {
        return new Character[]{'\u00A0', '\u2000', '\u2001', '\u2002', '\u2003', '\u2004',
                '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200A', '\u200B',
                '\u202F', '\u205F', '\uFEFF', '\u2028'};
    }
}
