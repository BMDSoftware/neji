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

package pt.ua.tm.neji.context;

import pt.ua.tm.neji.train.model.CRFBase;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.sentencesplitter.SentenceSplitter;

import java.util.List;
import java.util.Map;

/**
 * Helper that provides access to {@link Context} processors, namely sentence splitters, parsers and ML models.
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class ContextProcessors {

    private Parser parser;
    private SentenceSplitter splitter;
    private Map<String, CRFBase> crfs;

    public ContextProcessors(final Parser parser, final SentenceSplitter splitter, final Map<String, CRFBase> crfs) {
        this.parser = parser;
        this.splitter = splitter;
        this.crfs = crfs;
    }

    public Parser getParser() {
        return parser;
    }

    public SentenceSplitter getSentenceSplitter() {
        return splitter;
    }

    public CRFBase getCRF(String modelName) {
        return crfs.get(modelName);
    }
}
