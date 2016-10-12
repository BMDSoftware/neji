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

package pt.ua.tm.neji.core.pipeline;

import pt.ua.tm.neji.core.module.*;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Validator class that handles validation process of inserted modules in the pipeline prior to its execution.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public interface PipelineValidator {

    /**
     * Performs a check to the {@link Requires} and {@link Provides} modules
     * in each module class. Insertion order of modules in the pipeline is also checked.
     *
     * An exception is thrown if:
     * <p>- the {@link Reader} module does not contain a {@link Provides} annotation;</p>
     * <p>- a processing module does not contain a {@link Provides} or a {@link Requires} annotation;</p>
     * <p>- a {@link Writer} module does not contain a {@link Requires} annotation;</p>
     * <p>- a specific module is defined to require a resource that was not provided by earlier modules
     * in the pipeline;</p>
     * <p>- a specific module is defined to provide an NLP resource that was already provided by earlier
     * modules in the pipeline (this excludes the {@link Resource#Annotations} or the
     * {@link Resource#Relations} resources).</p>
     *
     * @throws NejiException in case an invalid module or insertion order is found
     */
    void validate() throws NejiException;
}
