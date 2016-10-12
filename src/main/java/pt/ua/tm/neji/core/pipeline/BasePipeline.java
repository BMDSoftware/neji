
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Reader;
import pt.ua.tm.neji.core.module.Writer;
import pt.ua.tm.neji.exception.NejiException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract implementation of {@link Pipeline}, represented by a {@link Reader} attribute,
 * a list of processing {@link Module} and a list of {@link Writer}. This implementation
 * will deal with every operation related to building the pipeline, by adding modules to
 * the above mentioned data structures. It is recommended, although not required, that
 * every new Pipeline implementation should extend from this class, requiring the user
 * to simply implement the {@link Pipeline#run(InputStream)} method and to make sure that
 * it will run all of the added modules in the correct order (reader -> processors -> writers).
 *
 * When using the {@link Pipeline#add(Module, Module...)} method, if the specified {@link Module}
 * is a {@link Reader}, it will be pointed to the reader attribute, losing any other readers
 * that were added before. If the specified {@link Module} is a {@link Writer}, it will be added
 * to the writers list. If the specified {@link Module} is not a {@link Reader} nor a
 * {@link Writer}, it will be added to the processing modules list.
 *
 * In addition, the order of the added processing modules will largely affect the run process.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public abstract class BasePipeline implements Pipeline {

    private static Logger logger = LoggerFactory.getLogger(BasePipeline.class);

    private static final String NO_VALIDATOR_FOUND =
            "No pipeline validators were added to the pipeline. In order to perform pipeline validation, a " +
            "pipeline validator should be set prior to execution, using the method 'setValidator(PipelineValidator)'.";

    private Corpus corpus;
    private PipelineValidator validator;
    protected Reader reader;
    protected List<Module> processingList;
    protected List<Writer> writerList;
    private Multimap<Object, Object> storage;


    protected BasePipeline() {
        this(new Corpus());
    }

    protected BasePipeline(Corpus corpus) {
        this.corpus = corpus;
        this.validator = null;
        this.reader = null;
        this.processingList = new ArrayList<>();
        this.writerList = new ArrayList<>();
        this.storage = HashMultimap.create();
    }


    /**
     * Passes a validator for this pipeline's modules and insertion order, which will be executed
     * when the method {@link Pipeline#run(InputStream)} or {@link Pipeline#run(InputStream, List)}
     * is called.
     *
     * @param validator the validator to be used
     */
    protected final Pipeline setValidator(PipelineValidator validator){
        this.validator = validator;
        return this;
    }

    /**
     * Inserts the specified modules in the pipeline. If the specified {@link Module} is
     * a {@link Reader}, it will be pointed to the reader attribute, losing any other readers that
     * were added before. If the specified {@link Module} is a {@link Writer}, it will be added
     * to the writers list. If the specified {@link Module} is not a {@link Reader} nor a
     * {@link Writer}, it will be added to the processing modules list.
     * Afterwards, it will implicitly use the {@link Module#setPipeline(Pipeline)} method of the
     * specified module.
     *
     * In addition, while the order of the added writers will only affect the order of the
     * {@link OutputStream} list from the {@link Pipeline#run(InputStream)} method, the order
     * of the added processing modules will largely affect the run process.
     *
     * @param first the module to be inserted in the pipeline.
     * @param more other modules to be inserted in the pipeline.
     */
    @Override
    public Pipeline add(Module first, Module... more) {
        List<Module> modules = new ArrayList<>();
        modules.add(first);
        if (more != null) {
            modules.addAll(Arrays.asList(more));
        }

        for (Module m : modules) {
            if (m instanceof Reader) {
                reader = (Reader) m;

            } else if (m instanceof Writer) {
                writerList.add((Writer) m);

            } else {
                processingList.add(m);
            }

            m.setPipeline(this);
        }
        return this;
    }

    /**
     * Stores the specified data, identified by the specified key, as module data, allowing it
     * to be used by other modules in the same pipeline.
     */
    @Override
    public final Pipeline storeModuleData(Object key, Object data) {
        storage.put(key, data);
        return this;
    }

    /**
     * Returns the stored data identified by the specified key.
     * @param key the key that identifies the data to be returned
     * @return the stored data identified by the specified key.
     */
    @Override
    public final List<Object> getModuleData(Object key) {
        return new ArrayList<>(storage.get(key));
    }

    /**
     * Sets the specified corpus on this pipeline.
     * @param corpus the corpus to be set
     */
    @Override
    public final Pipeline setCorpus(Corpus corpus) {
        this.corpus = corpus;
        return this;
    }

    /**
     * Returns the corpus from this pipeline.
     * @return corpus from this pipeline
     */
    @Override
    public final Corpus getCorpus() {
        return corpus;
    }

    /**
     * Removes all of the stored module data from this pipeline.
     */
    @Override
    public final Pipeline clearStoredData(){
        this.storage.clear();
        return this;
    }

    /**
     * Removes all of the modules from this pipeline. The pipeline, including every
     * stored module data, will be empty after this call returns.
     */
    @Override
    public final Pipeline clear(){
        this.reader = null;
        this.processingList.clear();
        this.writerList.clear();
        return clearStoredData();
    }

    /**
     * Execution method that processes the specified {@link InputStream} using the added
     * modules and that writes the resulting output in a generated list of {@link OutputStream}.
     *
     * @param input the {@link InputStream} to be processed using the added modules
     * @return a list of {@link OutputStream} resulting from the processing
     * @throws NejiException if there was a problem with the run process or if the
     * initial validation process failed
     */
    @Override
    public final List<OutputStream> run(InputStream input) throws NejiException {
        if(validator==null) {
            logger.warn(NO_VALIDATOR_FOUND);
        } else {
            validator.validate();
        }
        return run_(input);
    }

    protected abstract List<OutputStream> run_(InputStream input) throws NejiException;

    /**
     * Execution method that processes the specified {@link InputStream} using the added
     * modules and that write the resulting output in the specified {@link OutputStream}
     * list.
     *
     * In addition, this method will perform an initial check to the modules required
     * and provided resources, as well as to the insertion order.
     * If a specific module is defined to require a resource that was not provided by
     * earlier modules in the pipeline, an exception is thrown.
     *
     * @param input the {@link InputStream} to be processed using the added modules
     * @param outputList the list of {@link OutputStream} where it will be written
     *                   the resulting output
     * @throws NejiException if there was a problem with the run process or if the
     *                       initial validation process failed
     */
    @Override
    public final void run(InputStream input, List<OutputStream> outputList) throws NejiException {
        if(validator==null) {
            logger.warn(NO_VALIDATOR_FOUND);
        } else {
            validator.validate();
        }
        run_(input, outputList);
    }

    protected abstract void run_(InputStream input, List<OutputStream> outputList) throws NejiException;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (reader != null) {
            sb.append(reader.getClass().getName());
        }

        for (Module m : processingList) {
            sb.append(", ");
            sb.append(m.getClass().getName());
        }

        for (Writer w : writerList) {
            sb.append(", ");
            sb.append(w.getClass().getName());
        }
        sb.append("]");
        return sb.toString();
    }
}