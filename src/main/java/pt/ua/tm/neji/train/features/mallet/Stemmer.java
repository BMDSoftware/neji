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
import java.io.Serializable;
import pt.ua.tm.neji.train.config.Resources;

/**
 * Add a stem feature of the token.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Stemmer extends Pipe implements Serializable {

    /**
     * The prefix to be added in the feature.
     */
    private String prefix;

    /**
     * Constructor.
     * @param p The prefix to be added.
     */
    public Stemmer(final String p) {
        this.prefix = p;
    }

    /**
     * Process each sentence to add the stem feature.
     * @param carrier Instance to be processed.
     * @return Instance with new features.
     */
    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();

        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);

            Resources.getStemmer().setCurrent(t.getText());
            Resources.getStemmer().stem();
            String stem = Resources.getStemmer().getCurrent();

            t.setFeatureValue(prefix + stem, 1.0);
            //t.setText(stem);
        }
        return carrier;
    }
}
