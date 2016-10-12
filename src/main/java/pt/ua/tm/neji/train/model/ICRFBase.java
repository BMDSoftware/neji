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

import cc.mallet.fst.NoopTransducerTrainer;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.exception.NejiException;

/**
 *
 * @author jeronimo
 */
public interface ICRFBase {
    
    /**
     * Train the model.
     * @param corpus Corpus to train the model..
     * @throws NejiException problems regarding the train procedures.
     */
    void train(Corpus corpus) throws NejiException;
    /**
     * Test the model.
     * @param corpus Corpus to test the model.
     * @throws NejiException Problems regarding the files and model.
     */
    void test(Corpus corpus) throws NejiException;
    
    /**
     * Get transducer trainer
     * @return Trainer ready to annotate documents.
     */
    NoopTransducerTrainer getTransducer() throws NejiException;
    
}
