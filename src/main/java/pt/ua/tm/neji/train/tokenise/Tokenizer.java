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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.tokenise;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;

/**
 *
 * @author david
 */
public class Tokenizer {

    GDepParser parser;

    public Tokenizer(boolean doWhiteSpaceTokenisation) throws NejiException {
        this.parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.CHUNKING, new LingpipeSentenceSplitter(), doWhiteSpaceTokenisation);
    }

    public void launch() throws NejiException {
        try {
            parser.launch();
        }
        catch (IOException ex) {
            throw new NejiException("There was a problem lauching the GDepTranslator Parser.", ex);
        }
    }

    public void terminate() {
        parser.close();
    }
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Tokenizer.class);

    public String[] tokenize(String text) throws NejiException {

        List<Object> result = parser.parse(text);

        String[] tokens = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {

            String[] parts = result.get(i).toString().split("\t");
            tokens[i] = parts[1];
        }
        return tokens;
    }

    public Sentence tokenize(Corpus c, String text) throws NejiException {
        Sentence s = new Sentence(c);
        List<Object> result = parser.parse(text);
        
        String t;
        int start = 0;
        Token token;
        for (int i = 0; i < result.size(); i++) {
            String[] parts = result.get(i).toString().split("\t");
            t = parts[1];
            token = new Token(s, start, ( start + t.length() - 1 ), i);
            s.addToken(token);
            start += t.length();            
            List<Object> results = new ArrayList<Object>();
        }

        return s;
    }
}
