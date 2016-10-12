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

import java.io.InputStream;
import pt.ua.tm.neji.core.module.Reader;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.reader.BioCReader;
import pt.ua.tm.neji.reader.RawReader;
import pt.ua.tm.neji.reader.XMLReader;
import pt.ua.tm.neji.train.reader.A1Reader;
import pt.ua.tm.neji.train.reader.BC2Reader;
import pt.ua.tm.neji.train.reader.JNLPBAReader;
import pt.ua.tm.neji.train.reader.SerializedReader;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public enum InputFormat {
    XML,
    RAW,
    BIOC,
    CUSTOM,
    
    // Training input formats
    BC2,
    A1,
    SERIALIZED,
    JNLPBA;
    
    public Reader instantiateDefaultReader(Parser p, ParserLevel level, String[] extra) throws NejiException {
        switch (this) {
            case RAW:       return new RawReader();
            case XML:       return new XMLReader(extra);
            case BIOC:      return new BioCReader(p, level);
            case CUSTOM:    return null; // it's the user's responsibility to provide the custom reader
            case BC2:       return new BC2Reader(p, level);
            case A1:        return new A1Reader(p, level);
            case SERIALIZED: return new SerializedReader();
            case JNLPBA:    return new JNLPBAReader(p, level);
            default: throw new NejiException("Invalid input format " + this.name() + ": no default module.");
        }
    }
    
    public Reader instantiateTrainerReader(Parser p, ParserLevel level, String path, InputStream annotations) throws NejiException {
        switch (this) {
            case BC2:       return new BC2Reader(p, level, annotations);
            case A1:        return new A1Reader(p, level, annotations);
            case SERIALIZED:  return new SerializedReader(path);
            case JNLPBA:    return new JNLPBAReader(p, level);
            default:        throw new NejiException("Invalid input format " + this.name() + ": no default module.");
        }
    }
}
