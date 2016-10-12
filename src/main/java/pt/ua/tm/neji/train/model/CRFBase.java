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

package pt.ua.tm.neji.train.model;

import cc.mallet.fst.CRF;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants.Parsing;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.config.ModelConfig;

/**
 *
 * @author jeronimo
 */
public abstract class CRFBase implements ICRFBase {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(CRFBase.class);
    /**
     * Configuration of the model, including features and order.
     */
    private ModelConfig config;
    /**
     * The CRF model.
     */
    private CRF crf;
    /**
     * Parsing direction of the model.
     */
    private Parsing parsing;
    private Pipe pipe;
    private NoopTransducerTrainer transducer;

    /**
     * Constructor.
     *
     * @param config Model configuration.
     * @param parsing Parsing direction.
     */
    public CRFBase(final ModelConfig config, final Parsing parsing) {
        this.config = config;
        this.parsing = parsing;
        this.transducer = null;
    }

    /**
     * Constructor that loads the model from a file.
     *
     * @param config Model configuration.
     * @param parsing Parsing direction.
     * @param input File that contains the model.
     * @throws NejiException Problem loading the model from file.
     */
    public CRFBase(final ModelConfig config, final Parsing parsing, final InputStream input) throws NejiException {
        this.config = config;
        this.parsing = parsing;
        load(input);
    }

    /**
     * Get configuration of the model.
     *
     * @return The model configuration.
     */
    public ModelConfig getConfig() {
        return config;
    }

    /**
     * Get the {@link CRF} model.
     *
     * @return The CRF model.
     */
    public CRF getCRF() {
        return crf;
    }

    /**
     * Get the parsing direction of the model.
     *
     * @return Parsing direction.
     */
    public Parsing getParsing() {
        return parsing;
    }

    /**
     * Set the configuration of the model.
     *
     * @param config The new configuration.
     */
    public void setConfig(final ModelConfig config) {
        this.config = config;
    }

    /**
     * Set the CRF of the model.
     *
     * @param crf The new CRF.
     */
    public void setCRF(final CRF crf) {
        this.crf = crf;
        this.pipe = crf.getInputPipe();
        this.transducer = new NoopTransducerTrainer(crf);
    }
    
    /**
     * Set the parsing direction of the model.
     *
     * @param parsing The new parsing direction.
     */
    public void setParsing(final Parsing parsing) {
        this.parsing = parsing;
    }

    public abstract Pipe getFeaturePipe() throws NejiException;

    /**
     *
     * @param corpus
     * @throws NejiException
     */
    @Override
    public abstract void train(Corpus corpus) throws NejiException;

    /**
     * Implementation of the test capability, in order to provide feedback about
     * the performance of the model.
     *
     * @param corpus The corpus where the model should be tested.
     * @throws NejiException Problem testing the corpus.
     */
    @Override
    public void test(final Corpus corpus) throws NejiException {

        // Load test Data
        InstanceList testingData = corpus.toModelFormatTest(this);

        // Define Evaluator
        TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
                new InstanceList[]{testingData},
                new String[]{"test"}, corpus.getAllowedTags(), corpus.getAllowedTags()) {
        };

        // Evaluate
        evaluator.evaluateInstanceList(getTransducer(), testingData, "test");
    }

    @Override
    public NoopTransducerTrainer getTransducer() throws NejiException {
        
        if (crf == null) {
            throw new NejiException("CRF model must be loaded or trained before getting the transducer.");
        }
        
        if (transducer == null) {
            transducer = new NoopTransducerTrainer(crf);
        }
        return transducer;
    }

    /**
     * Load model from file.
     *
     * @param input The file that contains the model.
     * @throws NejiException Problem reading the input file.
     */
    private void load(InputStream input) throws NejiException {
        logger.info("Loading model...");
        CRF crf = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(input);
            crf = (CRF) ois.readObject();
            ois.close();
            input.close();
        }
        catch (ClassNotFoundException ex) {
            throw new NejiException("Provided model is not in CRF format.", ex);
        }
        catch (IOException ex) {
            throw new NejiException("There was a problem loading the CRF model.", ex);
        }
        this.crf = crf;
        this.transducer = new NoopTransducerTrainer(crf);
    }

    /**
     * Write the model into a file.
     *
     * @param output The file to store the model.
     * @throws NejiException Problem writing the output file.
     */
    public void write(final OutputStream output) throws NejiException {
        logger.info("Writing model...");
        try {
            ObjectOutputStream oos = new ObjectOutputStream(output);
            oos.writeObject(crf);
            oos.close();
        }
        catch (IOException ex) {
            throw new NejiException("There was a problem writing the model.", ex);
        }
    }
    
}
