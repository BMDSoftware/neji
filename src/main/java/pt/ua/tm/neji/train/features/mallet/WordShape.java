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
package pt.ua.tm.neji.train.features.mallet;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Add a feature that reflects the morphological shape of the token.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class WordShape extends Pipe implements Serializable {

    /**
     * Process each sentence to add the feature if necessary.
     * @param carrier Instance to be processed.
     * @return Instance with new features.
     */
    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();

        for (int i = 0; i < ts.size(); i++) {
            String typeI = "";
            String typeII = "";
            String typeIII = "";

            Token t = ts.get(i);
            char[] text = t.getText().toCharArray();

            boolean isDigitI = false;
            // 0 - Digit
            // 1 - Letter
            // 2 - Symbol
            int prev = -1;
            int current = -1;
            for (int k = 0; k < text.length; k++) {


                // Word Shape Type I
                if (Character.isDigit(text[k])) {
                    if (k == text.length - 1) {
                        typeI += "*";
                    } else {
                        isDigitI = true;
                    }
                } else {
                    if (isDigitI) {
                        typeI += "*";
                        isDigitI = false;
                        typeI += text[k];
                    } else {
                        typeI += text[k];
                    }
                }

                // Word Shape Type II
                if (Character.isDigit(text[k])) {
                    current = 0;
                } else if (Character.isLetter(text[k])) {
                    current = 1;
                } else {
                    current = 2;
                }

                if (( k == text.length - 1 ) && ( prev == current )) {
                    typeII += Int2Char(current);
                }

                if (( prev != current ) && ( prev != -1 )) {
                    typeII += Int2Char(prev);
                    if (k == text.length - 1) {
                        typeII += Int2Char(current);
                    }
                }

                prev = current;

                // Word Shape Type III
                if (Character.isLetter(text[k])) {
                    if (Character.isUpperCase(text[k])) {
                        typeIII += "A";
                    } else if (Character.isLowerCase(text[k])) {
                        typeIII += "a";
                    }
                } else if (Character.isDigit(text[k])) {
                    typeIII += "1";
                } else {
                    typeIII += "#";
                }
            }

            //if (!typeI.equals(""))
            t.setFeatureValue("WordShapeI=" + typeI, 1.0);

            //if (!typeII.equals(""))
            t.setFeatureValue("WordShapeII=" + typeII, 1.0);

            //if (!typeIII.equals(""))
            t.setFeatureValue("WordShapeIII=" + typeIII, 1.0);
        }
        return carrier;
    }

    /**
     * Convert an integer to the respective character representation.
     * Used to generate Word Shape Type II.
     * @param n The number.
     * @return The respective character.
     */
    private char Int2Char(int n) {
        if (n == 0) {
            return '*';
        }
        if (n == 1) {
            return 'a';
        }
        if (n == 2) {
            return '#';
        }

        return ' ';
    }
    
    // Serialization
    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
    }
}
