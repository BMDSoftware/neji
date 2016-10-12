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
package pt.ua.tm.neji.parser.gdep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Map the input and output data to run external executables.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class StreamGobbler implements Runnable {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);
    /**
     * Input data.
     */
    private InputStream is;
    /**
     * Output data.
     */
    private OutputStream os;

    /**
     * Constructor.
     * @param is Input data
     * @param os Output data
     */
    public StreamGobbler(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    /**
     * Map the input and output data.
     */
    public void run() {
        try {
            byte[] buffer = new byte[1 << 12];
            int c;
            while (( c = is.read(buffer) ) != -1) {
                os.write(buffer, 0, c);
                os.flush();
            }
        }
        catch (IOException ex) {
            //logger.error("There was a problem writing the output.", ex);
            return;
        }
    }
}
