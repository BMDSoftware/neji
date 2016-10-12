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

package pt.ua.tm.neji.annotation;

import java.io.File;
import java.io.IOException;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;

/**
 * Static class which contains variables and information shared between all annotation test classes.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Variables {
    private static MLModel model;
    private static Corpus corpus;
    private static Corpus corpus2;
    private static Corpus corpus3;
    static final String str = "human BRCA1 gene";
    static final String str2 = str + " and P53";
    static final String str3 = "human NAT2 SNP genotyping";

    public static MLModel getModel() throws NejiException {
        if(model==null){
            model = new MLModel("prge", new File("example/models/prge/prge.properties"));
            model.initialize();
        }
        return model;
    }

    static Corpus getCorpus() throws NejiException {
        if(corpus==null){
            corpus = new Corpus();
            corpus.setText(str);

            try {
                Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.CHUNKING, new LingpipeSentenceSplitter(), false).launch();
                parser.parse(corpus, str);
                parser.close();
            }catch (IOException ex){
                throw new NejiException(ex);
            }            
        }             
        return corpus;
    }

    static Corpus getCorpus2() throws NejiException {
        if(corpus2==null){
            corpus2 = new Corpus();
            corpus2.setText(str2);

            try {
                Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.CHUNKING, new LingpipeSentenceSplitter(), false).launch();
                parser.parse(corpus2, str2);
                parser.close();
            }catch (IOException ex){
                throw new NejiException(ex);
            }
        }
        return corpus2;
    }
    
    static Corpus getCorpus3() throws NejiException {
        if(corpus3==null){
            corpus3 = new Corpus();
            corpus3.setText(str3);

            try {
                Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.CHUNKING, new LingpipeSentenceSplitter(), false).launch();
                parser.parse(corpus3, str3);
                parser.close();
            }catch (IOException ex){
                throw new NejiException(ex);
            }
        }
        return corpus3;
    }
}
