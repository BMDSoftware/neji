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

import pt.ua.tm.neji.core.parser.ParserLevel;

import java.util.Collection;

/**
 * DynamicNLP is a supporting interface for the {@link Resource#DynamicNLP} annotation, which forces
 * classes that have that resource in the {@link Provides} or the {@link Requires} annotation to implement
 * the methods in this interface before being able to reach runtime execution.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public interface DynamicNLP {
    /**
     * Returns a Collection of {@link ParserLevel} that are to be provided or required by the implementing module.
     * @return a Collection of {@link ParserLevel} provided or required by the implementing module during runtime.
     */
    Collection<ParserLevel> getLevels();
}
