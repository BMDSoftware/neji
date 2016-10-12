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

import monq.jfa.CompileDfaException;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;
import monq.jfa.ReSyntaxException;
import monq.jfa.actions.Replace;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 28/03/13
 * Time: 00:43
 * To change this template use File | Settings | File Templates.
 */
public class Regex {

    public static String replace(String input, String oldText, String newText) {
        String output;
        try {
            Nfa nfa = new Nfa(oldText, new Replace(newText));

            DfaRun run = new DfaRun(nfa.compile(DfaRun.UNMATCHED_COPY));
            output = run.filter(input);
        } catch (ReSyntaxException | CompileDfaException | IOException ex) {
            throw new RuntimeException("There was a problem running the regex replacer.", ex);
        }

        return output;
    }

    public static String replace(String input, DfaRun dfaRun) {
        String output;
        try {
            output = dfaRun.filter(input);
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem running the regex replacer.", ex);
        }
        return output;
    }

    public static DfaRun getReplaceRun(final String oldText, final String newText) {
        DfaRun run;
        try {
            Nfa nfa = new Nfa(oldText, new Replace(newText));

            run = new DfaRun(nfa.compile(DfaRun.UNMATCHED_COPY));
        } catch (ReSyntaxException | CompileDfaException ex) {
            throw new RuntimeException("There was a problem running the regex replacer.", ex);
        }
        return run;
    }
}
