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
 * Add a feature to tokens that contain Uppercase and Lowercase letters.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class MixCase extends Pipe implements Serializable {

    /**
     * Process each sentence to add the feature if necessary.
     * @param carrier Instance to be processed.
     * @return Instance with new features.
     */
    public Instance pipe(Instance carrier) {
        /*TokenSequence ts = (TokenSequence) carrier.getData();
        
        for (int i = 0; i < ts.size(); i++) {
        Token t = ts.get(i);
        if (isMixCased(t.getText()))
        t.setFeatureValue("MixCase", 1.0);           
        }
        return carrier;*/
        TokenSequence ts = (TokenSequence) carrier.getData();

        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            char[] text = t.getText().toCharArray();

            boolean hasCap = false;
            boolean hasLow = false;
            for (int k = 0; k < text.length; k++) {
                if (Character.isLowerCase(text[k])) {
                    hasLow = true;
                } else if (Character.isUpperCase(text[k])) {
                    hasCap = true;
                }
                if (hasLow && hasCap) {
                    break;
                }
            }

            if (hasLow && hasCap) {
                t.setFeatureValue("MixCase", 1.0);
            }
        }
        return carrier;
    }

    /**
     * Check if a text contains both lowercase and uppercase characters.
     * @param t Text to check.
     * @return true if the text is mix cased, and false otherwise.
     */
    public static boolean isMixCased(final String t) {
        if (!t.toUpperCase().equals(t) && !t.toLowerCase().equals(t)) {
            return true;
        }
        return false;
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
