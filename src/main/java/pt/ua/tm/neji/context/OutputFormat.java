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

package pt.ua.tm.neji.context;

import java.util.List;
import pt.ua.tm.neji.core.module.Writer;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.writer.GZCorpusWriter;
import pt.ua.tm.neji.train.writer.ModelWriter;
import pt.ua.tm.neji.writer.*;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public enum OutputFormat {
    A1,
    NEJI,
    JSON,
    CONLL,
    XML,
    B64,
    BIOC,
    PIPE,
    PIPEXT,
    BC2,
    CUSTOM,
    MODEL, // Train writers
    GZCORPUS;

    public Writer instantiateDefaultWriter() throws NejiException {
        switch (this){
            case A1:        return new A1Writer();
            case CONLL:     return new CoNLLWriter();
            case JSON:      return new JSONWriter();
            case NEJI:      return new NejiWriter();
            case XML:       return new IeXMLWriter();
            case B64:       return new Base64Writer();
            case BIOC:      return new BioCWriter();
            case PIPE:      return new PipeWriter();
            case PIPEXT:    return new PipeExtendedWriter();
            case BC2:       return new BC2Writer();
            case CUSTOM:    return null; // it's the user's responsibility to provide the custom writer
            case MODEL:     return new ModelWriter();
            case GZCORPUS: return new GZCorpusWriter();
            default: throw new NejiException("Invalid output format " + this.name() + ": no default module.");
        }
    }
    
     public Writer instantiateTrainerWriter(String serializedPath, String dictionariesPath, String modelFolderPath, 
             List<String> entity) throws NejiException {
        switch (this) {
            case MODEL:    return new ModelWriter(dictionariesPath, modelFolderPath, entity);
            case GZCORPUS: return new GZCorpusWriter(serializedPath);
            default: throw new NejiException("Invalid output format " + this.name() + ": no default module.");
        }
    }
}
