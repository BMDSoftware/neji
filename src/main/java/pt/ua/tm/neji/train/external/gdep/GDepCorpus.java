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
package pt.ua.tm.neji.train.external.gdep;

import java.io.*;
import java.util.ArrayList;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Represents the corpus provided by GDepTranslator parser after parsing the corpus.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class GDepCorpus {
    /**
     * The corpus is composed by a set of sentences.
     */
    private ArrayList<GDepSentence> corpus;

    /**
     * Constructor.
     */
    public GDepCorpus() {
        this.corpus = new ArrayList<>();
    }

    /**
     * Add sentence.
     * @param s GDepTranslator sentence to be added.
     */
    public void addSentence(final GDepSentence s) {
        corpus.add(s);
    }

    /**
     * Get specific sentence.
     * @param i The index of the sentence.
     * @return The sentence in the <code>i</code> position of the
     * {@link ArrayList}.
     */
    public GDepSentence getSentence(final int i) {
        return corpus.get(i);
    }

    /**
     * The number of sentences of the GDepTranslator corpus.
     * @return The number of sentences.
     */
    public int size() {
        return corpus.size();
    }
    
    /**
     * Write the GDepTranslator parsing result of the corpus in a file.
     * @param output The file to store the GDepTranslator corpus.
     * @throws NejiException Problem writing the file.
     */
    public void write(final OutputStream output) throws NejiException {
        try {
            //GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(file));
            for (GDepSentence s : corpus) {
                for (int i = 0; i < s.size(); i++) {
                    output.write(s.getToken(i).toString().getBytes());
                    output.write("\n".getBytes());
                }
                output.write("\n".getBytes());
            }
            output.close();
        }
        catch (IOException ex) {
            throw new NejiException("There was a problem writing the output file.", ex);
        }
    }

    /**
     * Load the GDepTranslator Output from a previously written file.
     * @param input File that contains the GDepTranslator corpus.
     * @throws NejiException Problem reading the file.
     */
    public void load(final InputStream input) throws NejiException {
        this.corpus = new ArrayList<>();

        try {
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader br = new BufferedReader(isr);
            String line;
            GDepSentence s = new GDepSentence(this);
            GDepToken gt;
            String[] parts;
            

            while (( line = br.readLine() ) != null) {
                if (line.equals("") || line.equals("\n")) {
                    if (s.size() > 0) {
                        this.addSentence(s);
                    }
                    s = new GDepSentence(this);
                } else {
                    parts = line.split("\t");
                    if (parts.length > 4)
                        gt = new GDepToken(parts[0], parts[1], parts[2], parts[3], Integer.valueOf(parts[4]), parts[5]);
                    else
                        gt = new GDepToken(parts[0], parts[1], parts[2], parts[3], null, null);
                    s.addToken(gt);
                }
            }

            br.close();
            input.close();
        }
        catch (IOException ex) {
            throw new NejiException("There was a problem reading the GDepTranslator file.", ex);
        }
    }
}
