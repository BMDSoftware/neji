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


import monq.jfa.DfaRun;
import pt.ua.tm.neji.core.corpus.Corpus;

/**
 * Abstract class that integrates base functionalities of a {@link Loader} module.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public abstract class BaseLoader extends BaseModule implements Loader {

    public BaseLoader(final DfaRun.FailedMatchBehaviour failedBehavior) {
        super(failedBehavior);
    }

//    public BaseLoader(final Corpus corpus, final DfaRun.FailedMatchBehaviour failedBehavior) {
//        super(corpus, failedBehavior);
//    }
}
