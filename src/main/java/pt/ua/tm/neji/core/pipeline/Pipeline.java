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

import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.exception.NejiException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface that defines a pipeline.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public interface Pipeline {

    /**
     * Inserts the specified module in the pipeline. The order of the
     * added modules will largely affect the run process.
     *
     * @param first the module to be inserted in the pipeline.
     * @param more other modules to be inserted in the pipeline.
     */
    Pipeline add(Module first, Module... more);

    /**
     * Stores the specified data, identified by the specified key, as module data, allowing it
     * to be used by other modules in the same pipeline.
     */
    Pipeline storeModuleData(Object key, Object data);

    /**
     * Returns the stored data identified by the specified key.
     * @param key the key that identifies the data to be returned
     * @return the stored data identified by the specified key.
     */
    List<Object> getModuleData(Object key);

    /**
     * Sets the specified corpus on this pipeline.
     * @param corpus the corpus to be set
     */
    Pipeline setCorpus(Corpus corpus);

    /**
     * Returns the corpus from this pipeline.
     * @return corpus from this pipeline
     */
    Corpus getCorpus();

    /**
     * Removes all of the stored module data from this pipeline.
     */
    Pipeline clearStoredData();

    /**
     * Removes all of the modules from this pipeline. The pipeline, including every
     * stored module data, will be empty after this call returns.
     */
    Pipeline clear();

    /**
     * Run method to process the specified {@link InputStream} using the added
     * modules. The order of the added modules might largely affect the run process.
     *
     * @param input the InputStream to be processed using the added modules
     * @return a list of {@link OutputStream} resultant from the processing
     */
    List<OutputStream> run(InputStream input) throws NejiException;

    /**
     * Run method to process the specified {@link InputStream} using the added
     * modules and to write the resultant output in the specified {@link OutputStream}
     * list. The order of the added modules might largely affect the run process.
     *
     * @param input the {@link InputStream} to be processed using the added modules
     * @param outputList the list of {@link OutputStream} where it will be to written
     *                   the resultant output
     * @throws NejiException if there was a problem with the run process
     */
    void run(InputStream input, List<OutputStream> outputList) throws NejiException;

}
