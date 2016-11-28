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

import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.corpus.Corpus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * An output corpus.
 * @author Tiago Nunes
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class OutputFile extends CorpusFile {

    public OutputFile(final Corpus corpus, final File file, final boolean compressed) {
        super(corpus, file, compressed);
    }

    public static File newOutputFile(String outputFolder, String filename,
                                     OutputFormat format, boolean compressed) {
        if(format.equals(OutputFormat.BIOC)) {
            filename += ".xml";
        } else if (format.equals(OutputFormat.JSONPDF)) {
            filename += ".json";
        } else {
            filename += "."+format.toString().toLowerCase();
        }

        filename = String.format("%s%s", filename, compressed ? ".gz" : "");

        return new File(outputFolder, filename);
    }

    public OutputStream getOutStream() throws IOException {
        return newStreamForFile(getFile(), isCompressed());
    }

    private static OutputStream newStreamForFile(File file, boolean compressed)
            throws IOException {
        OutputStream os = new FileOutputStream(file);
        if (compressed) {
            os = new GZIPOutputStream(os);
        }
        return os;
    }

}
