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
 * Add a feature that reflects the number of uppercase characters on the token.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class NumberOfCap extends Pipe implements Serializable {

    /**
     * Process each sentence to add the feature if necessary.
     * @param carrier Instance to be processed.
     * @return Instance with new features.
     */
    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();

        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            char[] text = t.getText().toCharArray();

            int numCap = 0;
            for (int k = 0; k < text.length; k++) {
                if (Character.isUpperCase(text[k])) {
                    numCap++;
                }
            }

            if (numCap == 1) {
                t.setFeatureValue("SingleCap", 1.0);
            } else if (numCap == 2) {
                t.setFeatureValue("TwoCap", 1.0);
            } else if (numCap == 3) {
                t.setFeatureValue("ThreeCap", 1.0);
            } else if (numCap >= 4) {
                t.setFeatureValue("MoreCap", 1.0);
            }
        }
        return carrier;
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
