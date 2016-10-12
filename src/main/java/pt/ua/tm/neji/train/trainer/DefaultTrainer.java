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

package pt.ua.tm.neji.train.trainer;

import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants.Parsing;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.config.ModelConfig;
import pt.ua.tm.neji.train.model.CRFModel;

/**
 *
 * @author jeronimo
 */

@Requires({Resource.Tokens})
@Provides({Resource.Model})
public class DefaultTrainer extends BaseTrainer {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(DefaultTrainer.class);
    
    // Attributes
    private ModelConfig config;
    
    /**
     * Constructor.
     * @param config file with features to use at training.
     * @throws NejiException 
     */
    public DefaultTrainer(ModelConfig config) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToTag(text_action, ".+");
        this.config = config;
    }
    
    /**
     * Constructor. Just used for validation purposes.
     */
    public DefaultTrainer() {
        super(DfaRun.UNMATCHED_COPY);
    }
    
    private DefaultAction text_action = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {                

            // Get corpus
            Corpus corpus = getPipeline().getCorpus();
            
            // Deal with model and corpus direction    
            Parsing parsing = config.getParsing();
            if (!corpus.getParsing().equals(parsing)) {
                corpus.reverse();
            }
            
            // Create model
            CRFModel model = new CRFModel(config, parsing);
            
            // Train model
            try {                
                model.train(corpus);
            } catch (NejiException ex) {
                logger.error("Problem training the model.", ex);
                System.exit(0);
            }
            
            // Store data needed by writer module in the pipeline
            getPipeline().clearStoredData();
            getPipeline().storeModuleData("TRAINED_MODEL", model);
        }
    };
        
}
