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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.tm.neji.train.pipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import monq.jfa.CompileDfaException;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;
import monq.jfa.ReaderCharSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Reader;
import pt.ua.tm.neji.core.module.Writer;
import pt.ua.tm.neji.core.pipeline.BasePipeline;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.trainer.Trainer;

/**
 *
 * @author jeronimo
 */
public class TrainPipeline extends BasePipeline {
    
    private static Logger logger = LoggerFactory.getLogger(TrainPipeline.class);
    
    // Attributes
    protected Trainer trainer;
    
    /**
     * Constructor.
     */
    public TrainPipeline() {
        super();
        this.trainer = null;
        setValidator(new TrainPipelineValidator(this));
    }
    
    /**
     * Constructor.
     * @param corpus The corpus to process
     */
    public TrainPipeline(Corpus corpus) {
        super(corpus);
        this.trainer = null;
        setValidator(new TrainPipelineValidator(this));
    }
            
    @Override
    protected List<OutputStream> run_(InputStream sentencesInput) throws NejiException {
       
        try {
            final Nfa nfa = new Nfa(Nfa.NOTHING);
            DfaRun previous = nfa.compile(DfaRun.UNMATCHED_COPY).createRun();
            DfaRun inside;

            // Sets the sentences input stream in the specified InputStream
            ReaderCharSource cs = new ReaderCharSource(sentencesInput, "UTF-8");
            previous.setIn(cs);
            
            // Run Reader Module
            if (reader != null) {
                reader.compile();
                inside = reader.getRun();
                inside.setIn(previous);
                previous = inside;
            }
            
            // Run Processing Modules
            for (Module a : processingList) {
                a.compile();
                inside = a.getRun();
                inside.setIn(previous);
                previous = inside;
            }
            
            // Run Trainer Module
            if (trainer != null) {
                trainer.compile();
                inside = trainer.getRun();
                inside.setIn(previous);
                previous = inside;
            }
            
            // Run Writer module
            if (!writerList.isEmpty()) {
                Writer writer = writerList.get(0);
                writer.compile();
                inside = writer.getRun();
                inside.setIn(previous);
                previous = inside;
                filterOutput(new FileOutputStream("features.config"), previous);
            }           

            sentencesInput.close();
            
        } catch (IOException | CompileDfaException ex) {
            throw new NejiException(ex);
        }
        
        return null;
    }
    
    @Override
    protected void run_(final InputStream sentencesInput, final List<OutputStream> outputList) throws NejiException {
       
        // Output List will be ignored, in training
        this.run_(sentencesInput);
    }   
    
    private void filterOutput(OutputStream out, DfaRun previous) throws IOException {
        PrintStream ps = new PrintStream(out, false, "UTF-8");
        previous.filter(ps);
        ps.close();
        out.close();
    }
    
    public Reader getReader() {
        return reader;
    }

    public List<Module> getProcessingList() {
        return processingList;
    }

    public  List<Writer> getWriterList() {
        return writerList;
    }
    
    public Trainer getTrainer() {
        return trainer;
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
     * @return Pipeline pipeline with inserted modules
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

            } else if (m instanceof Trainer) {
                trainer = (Trainer) m;
            
            } else {
                processingList.add(m);
            }

            m.setPipeline(this);
        }
        return this;
    }
}
