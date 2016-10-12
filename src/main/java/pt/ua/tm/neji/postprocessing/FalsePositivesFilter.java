/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.postprocessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree;

/**
 *
 * @author jeronimo
 */
@Requires({})
@Provides({Resource.Annotations})
public class FalsePositivesFilter extends BaseLoader {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(FalsePositivesFilter.class);
    
    // Attributes
    private List<String> fpList;

    /**
     * Constructor.
     * @param falsePositives input stream with the false positive terms.
     * @throws NejiException 
     */
    public FalsePositivesFilter(InputStream falsePositives) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToTag(text_action, ".+");
        this.fpList = loadFPList(falsePositives);
    }
    
    /**
     * Constructor. Just used for validation purposes.
     */
    public FalsePositivesFilter() {
        super(DfaRun.UNMATCHED_COPY);
    }
    
    private DefaultAction text_action = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {                

            // Get corpus
            Corpus corpus = getPipeline().getCorpus();
            
            // Filter annotations
            filterAnnotations(corpus);
        }        
    };

    /**
     * Load the false positive terms from a file.
     * @param fpFile false positives file
     * @return false positives list
     */
    private List<String> loadFPList(InputStream fpIS) {
        
        List<String> list = new ArrayList<>();
             
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(fpIS));
                  
            String line = br.readLine();
            while(line != null){
                if (line.trim().length() != 0) {
                    list.add(line.toLowerCase());
                }
                line = br.readLine();
            }
            
            br.close();
        } catch (IOException ex) {
            logger.error("A problem ocurred while loading the false "
                    + "positive terms from the FP file.", ex);
            System.exit(0);
        }
        
        return list;
    }
    
    /**
     * Filter annotations.
     * @param corpus corpus
     */
    private void filterAnnotations(Corpus corpus) {
        
        if (fpList.isEmpty()) {
            return;
        }
        
        // Iterate over the sentences
        for (Sentence s : corpus.getSentences()) {
            
            // Iterate over the sentence annotations
            for (Annotation a : s.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, false)) {
                AnnotationImpl ai = (AnnotationImpl) a;
                String text = ai.getText().toLowerCase();
                
                // If annotation matches any of the false positive terms then
                // remove it
                if (fpList.contains(text)) {
                    s.removeAnnotation(a, true);
                }
            }
        }
            
    }
    
}
