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

package pt.ua.tm.neji.core.module;

import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.FaAction;
import monq.jfa.Nfa;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Interface that defines a module.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public interface Module {

    /**
     * Sets a new pipeline where this module will be used.
     */
    void setPipeline(final Pipeline pipeline);

    /**
     * Returns the pipeline where this module is inserted in.
     * @return the pipeline where this module is inserted in.
     */
    Pipeline getPipeline();

    /**
     * Compiles the current module based on the added actions.
     *
     * @throws NejiException if there was a problem with the compile process
     */
    void compile() throws NejiException;

    /**
     * Returns the DFA for this module.
     * This module must be compiled with the "compile()" method in order to properly return a DFA.
     *
     * @return the DFA for this module
     * @throws NejiException if this module was not compiled prior to this method's call
     */
    Dfa getDFA() throws NejiException;

    /**
     * Returns the NFA for this module.
     * This module must be compiled with the "compile()" method in order to properly return a NFA.
     *
     * @return the NFA for this module
     * @throws NejiException if this module was not compiled prior to this method's call
     */
    Nfa getNFA() throws NejiException;

    /**
     * Returns the DFARun for this module.
     * This module must be compiled with the "compile()" method in order to properly return a DFARun.
     *
     * @return the DFARun for this module
     * @throws NejiException if this module was not compiled prior to this method's call
     */
    DfaRun getRun() throws NejiException;

    /**
     * Interface that defines an action, which encapsulates an {@link FaAction} to be used in this module's
     * NFA and re-implements the method {@link FaAction#invoke(StringBuffer, int, monq.jfa.DfaRun)} into a
     * more simplified "execute" method.
     */
    interface Action {

        /**
         * Method that must be implemented in each independent module with what actions are supposed
         * to be performed.
         * @param text the current text being processed in the pipeline
         * @param startIndex the index where the specified text being processed starts
         */
        void execute(StringBuffer text, int startIndex);

        /**
         * Returns the monq FaAction resulting from the implemented
         * {@link Action#execute(StringBuffer, int)} method.
         */
        FaAction getAction();
    }
}
