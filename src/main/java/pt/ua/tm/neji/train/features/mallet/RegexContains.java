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


/**
 Add feature with value 1.0 if the entire token text matches the
 provided regular expression.

 @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
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
import java.util.regex.Pattern;


public class RegexContains extends Pipe implements Serializable {
    Pattern regex;
    String feature;

    public RegexContains(String featureName, Pattern regex) {
        this.feature = featureName;
        this.regex = regex;
    }

    // Too dangerous with both arguments having the same type
    //public RegexMatches (String regex, String feature) {
    //this (Pattern.compile (regex), feature);
    //}


    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();
        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            String s = t.getText();
            String conS = s;
            //dealing with ([a-z]+), ([a-z]+, [a-z]+), [a-z]+.
            if (conS.startsWith("("))
                conS = conS.substring(1);
            if (conS.endsWith(")") || conS.endsWith("."))
                conS = conS.substring(0, conS.length() - 1);

            if (regex.matcher(s).groupCount() > 1) {
                t.setFeatureValue(feature, 1.0);
            }
            if (conS.compareTo(s) != 0) {
                if (regex.matcher(s).groupCount() > 1)
                    t.setFeatureValue(feature, 1.0);
            }
        }
        return carrier;
    }


    // Serialization

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(regex);
        out.writeObject(feature);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        regex = (Pattern) in.readObject();
        feature = (String) in.readObject();
    }


}
