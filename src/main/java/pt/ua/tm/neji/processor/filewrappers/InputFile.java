
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * The representation of an input document, encapsulating file data and format.
 * @author Tiago Nunes
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class InputFile extends CorpusFile {

    public InputFile(final Corpus corpus, final File file, final boolean compressed) {
        super(corpus, file, compressed);
        Validate.isTrue(file.isFile() && file.canRead());
    }

    public static InputStream newStreamForFile(File file, boolean compressed)
            throws IOException {
        InputStream is = new FileInputStream(file);
        if (compressed) {
            is = new GZIPInputStream(is);
        }
        return is;
    }

    public InputStream getInStream() throws IOException {
        return newStreamForFile(getFile(), isCompressed());
    }

}
