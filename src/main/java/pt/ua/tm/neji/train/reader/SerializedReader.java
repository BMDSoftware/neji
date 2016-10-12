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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.tm.neji.train.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.core.module.BaseReader;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;

/**
 *
 * @author jeronimo
 */
@Provides({Resource.Passages, Resource.Sentences, Resource.Annotations, Resource.Relations, Resource.Tokens})
public class SerializedReader extends BaseReader {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(SerializedReader.class);

    // Attributes
    private String path;
    
    /**
     * Constructor.
     * @throws NejiException 
     */
    public SerializedReader() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToTag(text_action, ".+");
    }
    
    /**
     * Constructor.
     * @param path file to read
     * @throws NejiException 
     */
    public SerializedReader(String path) throws NejiException {
        this();
        this.path = path;
    }
    
    private BaseModule.DefaultAction text_action = new BaseModule.DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            
            // Get corpus
            Corpus corpus;
            
            //logger.info("Deserializing corpus...");
            
            // Get file
            File corpusFile = new File(path);
            
            // Deserialize corpus
            try {
                FileInputStream fis = new FileInputStream(corpusFile);
                GZIPInputStream gis = new GZIPInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(gis);
                corpus = (Corpus) ois.readObject();
                ois.close();
                gis.close();
                fis.close();
            } catch (IOException | ClassNotFoundException ex) {
                throw new RuntimeException("There was a problem deserializing the corpus.", ex);
            }
            
            // Set corpus identifier            
            String modelName = corpusFile.getParentFile().getName();
            corpus.setIdentifier(modelName);
            
            // Set corpus in the pipeline
            getPipeline().setCorpus(corpus);
            
            // Build sentences with tags <s> and </s>
            StringBuilder sb = new StringBuilder(corpus.getText());
            String prefix;
            String suffix = "</s>";
            String taggedSentence;
            int sentenceCounter = 0;
            int offset = 0;
            
            for (Sentence s : corpus.getSentences()) {
                
                // Build tagged sentence
                prefix = "<s";
                prefix += " id=\"" + sentenceCounter++ + "\"";
                prefix += ">";                
                taggedSentence = prefix + s.getText() + suffix;
                
                sb.replace(offset + s.getStart(), offset + s.getEnd(), taggedSentence);
                
                offset += prefix.length() + suffix.length();
            }
            
            yytext.replace(start, yytext.length(), sb.toString());                    
        }
    };

    @Override
    public InputFormat getFormat() {
        return InputFormat.SERIALIZED;
    }
}
