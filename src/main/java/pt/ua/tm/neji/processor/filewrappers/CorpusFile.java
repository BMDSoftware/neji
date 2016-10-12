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

package pt.ua.tm.neji.processor.filewrappers;

import org.apache.commons.lang.Validate;
import pt.ua.tm.neji.core.corpus.Corpus;

import java.io.File;

/**
 * Abstract class encapsulating properties common to {@link InputFile} and {@link OutputFile}.
 * @author Tiago Nunes
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public abstract class CorpusFile {

    private Corpus corpus;
    private final File file;
    private boolean compressed;
    
    public CorpusFile(final Corpus corpus, final File file, final boolean compressed) {
        Validate.notNull(file);
        this.corpus = corpus;
        this.file = file;
        this.compressed = compressed;
    }

    public Corpus getCorpus() {
        return corpus;
    }

    public final File getFile() {
        return file;
    }

    public boolean isCompressed() {
        return compressed;
    }    
}
