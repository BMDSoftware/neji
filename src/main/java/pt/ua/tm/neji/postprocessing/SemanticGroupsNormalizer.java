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
import java.util.HashMap;
import java.util.Map;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.core.module.BaseModule;
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
public class SemanticGroupsNormalizer extends BaseLoader {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(SemanticGroupsNormalizer.class);
    
    // Attributes
    private Map<String, String> groupsMap;

    /**
     * Constructor.
     * @param groupsNormalization input stream with the semantic groups normalization.
     * @throws NejiException 
     */
    public SemanticGroupsNormalizer(InputStream groupsNormalization) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToTag(text_action, ".+");
        this.groupsMap = loadGroupsNormalization(groupsNormalization);
    }
    
    /**
     * Constructor. Just used for validation purposes.
     */
    public SemanticGroupsNormalizer() {
        super(DfaRun.UNMATCHED_COPY);
    }
    
    private BaseModule.DefaultAction text_action = new BaseModule.DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {                

            // Get corpus
            Corpus corpus = getPipeline().getCorpus();
            
            // Normalize groups
            normalizeGroups(corpus);
        }        
    };

    /**
     * Load the semantic groups mapping from a file.
     * @param groupsIS semantic groups normalization file
     * @return semantic groups map
     */
    private Map<String, String> loadGroupsNormalization(InputStream groupsIS) {
        
        Map<String, String> map = new HashMap<>();
             
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(groupsIS));
                  
            String line = br.readLine();
            while(line != null){
                if (line.trim().length() == 0) {
                    continue;
                }
                
                // Get group and its normalization term
                String[] parts = line.split("\\|", 2);
                
                if (parts.length != 2) {
                    logger.warn("Invalid line in semantic groups normalization "
                            + "file. Each line should contain the group and the "
                            + "normalization term separated by a \"|\". Example: "
                            + "DISO|Disorders");
                    line = br.readLine();
                    continue;
                }
                
                String group = parts[0].toLowerCase();
                String norm = parts[1];
                map.put(group, norm);
                
                line = br.readLine();
            }
            
            br.close();
        } catch (IOException ex) {
            logger.error("A problem ocurred while loading the groups "
                    + "normalization terms from the file.", ex);
            System.exit(0);
        }
        
        return map;
    }
    
    /**
     * Normalize annotation groups.
     * @param corpus corpus
     */
    private void normalizeGroups(Corpus corpus) {
                
        if (groupsMap.isEmpty()) {
            return;
        }
        
        // Iterate over the sentences
        for (Sentence s : corpus.getSentences()) {
            
            // Iterate over the sentence annotations
            for (Annotation a : s.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, false)) {
                AnnotationImpl ai = (AnnotationImpl) a;
                
                // Iterate over the string ids
                for (Identifier id : ai.getIDs()) {
                    
                        String ann_group = id.getGroup().toLowerCase();

                        // Normalize groups
                        if (groupsMap.containsKey(ann_group)) {
                            id.setGroup(groupsMap.get(ann_group));
                        }
                    }
                }
            }
            
    }
    
}
