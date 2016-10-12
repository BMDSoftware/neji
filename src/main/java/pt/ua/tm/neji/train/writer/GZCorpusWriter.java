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

package pt.ua.tm.neji.train.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.exception.NejiException;

/**
 *
 * @author jeronimo
 */
@Requires({})
public class GZCorpusWriter extends BaseWriter {
    
    private static Logger logger = LoggerFactory.getLogger(ModelWriter.class);
    
    // Attributes
    private String path;
    
    /**
     * Constructor.
     * @param path directory to save the serialized corpus
     * @throws NejiException
     */
    public GZCorpusWriter(String path) throws NejiException {
        this();
        this.path = path;
    }
        
    /**
     * Default constructor.
     * @throws NejiException 
     */
    public GZCorpusWriter() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToRegex(text_action, ".+");
    }
    
    
    private BaseModule.DefaultAction text_action = new BaseModule.DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            
            // Get corpus
            Corpus corpus = getPipeline().getCorpus();
            
            // Get path
            String filePath = path + corpus.getIdentifier() + ".gz" + File.separator;
            File outputFile = new File(filePath);
            outputFile.getParentFile().mkdirs();
            
            //logger.info("Serializing corpus...");
            
            try {
                FileOutputStream fos = new FileOutputStream(outputFile);
                GZIPOutputStream gos = new GZIPOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(gos);
            
                oos.writeObject(corpus);
            
                oos.close();
                gos.close();
                fos.close();
            } catch (IOException ex) {
                System.out.println("Error: An error ocurred while serializing corpus in a .gz. " + ex.getMessage());
            }
            
            // Write someting at the output file required by the pipeline, To Change Probably
            yytext.replace(0, yytext.length(), "");            
        }
    };
    
    @Override
    public OutputFormat getFormat() {
        return OutputFormat.GZCORPUS;
    }
}
