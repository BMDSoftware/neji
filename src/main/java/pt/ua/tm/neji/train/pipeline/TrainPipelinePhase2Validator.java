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

package pt.ua.tm.neji.train.pipeline;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.module.DynamicNLP;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Reader;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.core.module.Writer;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.pipeline.PipelineValidator;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.trainer.Trainer;
import pt.ua.tm.neji.tree.Tree;

/**
 *
 * @author jeronimo
 */
public class TrainPipelinePhase2Validator implements PipelineValidator {
        
    // Constants
    private static final String $module_name$ = "<MODULE_NAME>";
    private static final String $resource_name$ = "<RESOURCE_NAME>";

    private static final String NO_REQUIRES_MESSAGE
            = "The specified module '" + $module_name$ + "' must implement the \'Requires\' Java annotation.";

    private static final String NO_PROVIDES_MESSAGE
            = "The specified module '" + $module_name$ + "' must implement the \'Provides\' Java annotation.";

    private static final String REQUIRED_NOT_PROVIDED_MESSAGE = "The specified module '" + $module_name$
            + "' requires '" + $resource_name$ + "', which were not provided by earlier modules in the pipeline. "
            + "A module or a corpus that provides '" + $resource_name$ + "' must be added to the pipeline before "
            + "adding '" + $module_name$ + "'.";

    private static final String WAS_ALREADY_PROVIDED_MESSAGE = "The specified module '" + $module_name$
            + "' provides '" + $resource_name$ + "', which were already provided by earlier modules in the "
            + "pipeline. One pipeline can contain multiple modules that provide '"
            + Resource.Annotations.toString() + "' or '" + Resource.Relations.toString() + "', but can only "
            + "contain one module for each of the other resources.";

    private static final String INVALID_DYNAMIC_NLP_MESSAGE = "The specified module '" + $module_name$
            + "' that provides or requires '" + Resource.DynamicNLP + "' must implement the '"
            + DynamicNLP.class.getSimpleName() + "' interface, as well as its methods.";
        
    // Attributes
    private TrainPipelinePhase2 trainPipeline;
    
    /**
     * Constructor.
     */
    public TrainPipelinePhase2Validator(TrainPipelinePhase2 trainPipeline) {
        this.trainPipeline = trainPipeline;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws NejiException {
        Corpus corpus = trainPipeline.getCorpus();
        Reader reader = trainPipeline.getReader();
        List<Module> processingList = trainPipeline.getProcessingList();
        Trainer trainer = trainPipeline.getTrainer();
        List<Writer> writerList = trainPipeline.getWriterList();

        // The provides list that will be iteratively used for validation
        Set<Resource> providedList = new HashSet<>();
        Set<Resource> providedListFromCorpus =  getProvidedFromCorpus(corpus);

        Class<? extends Module> moduleClass;
        String moduleName;
        Provides provides;
        Requires requires;


        // Reader does not need the 'Requires' annotation, so check and add the provided features to the provides list
        if(reader != null) {
            moduleClass = reader.getClass();
            provides = moduleClass.getAnnotation(Provides.class);
            validateProvides(moduleClass.getSimpleName(), providedList, provides);
            addProvides(reader, providedList, provides.value());
        }

        for(Module m : processingList) {
            moduleClass = m.getClass();
            moduleName = moduleClass.getSimpleName();

            // Checks for each Module in the modules list if the 'Provides' list contains the Required features
            requires = moduleClass.getAnnotation(Requires.class);
            validateRequires(m, providedList, providedListFromCorpus, requires);


            // Once every 'Required' feature passes validation, check and add the provided features to the provides list
            provides = moduleClass.getAnnotation(Provides.class);
            validateProvides(moduleName, providedList, provides);
            addProvides(m, providedList, provides.value());
        }

        // For Trainer validate the required features with the provideds list and, check and add the provided features to
        // the provides list
        if (trainer != null) {
            moduleClass = trainer.getClass();
            moduleName = moduleClass.getName();
            
            // Check the Required features
            requires = moduleClass.getAnnotation(Requires.class);
            validateRequires(trainer, providedList, providedListFromCorpus, requires);
            
            // Check and add the provided features
            provides = moduleClass.getAnnotation(Provides.class);
            validateProvides(moduleName, providedList, provides);
            addProvides(trainer, providedList, provides.value());
        }
        
        // Writer does not need the 'Provides' annotation, so validate each required features with the provides list
        for(Writer w : writerList) {
            requires = w.getClass().getAnnotation(Requires.class);
            validateRequires(w, providedList, providedListFromCorpus, requires);
        }
    }
    
    private void validateProvides(String moduleName, Set<Resource> providedList, Provides p) throws NejiException {
        if (p==null) {
            throw new NejiException(NO_PROVIDES_MESSAGE
                    .replaceAll($module_name$, moduleName));
        }

        for(Resource f : p.value()) {
            if(providedList.contains(f) && !f.equals(Resource.Annotations) && !f.equals(Resource.Relations)) {
                throw new NejiException(WAS_ALREADY_PROVIDED_MESSAGE
                        .replaceAll($module_name$, moduleName)
                        .replaceAll($resource_name$, f.name()));
            }
        }
    }
    
    private Set<Resource> getProvidedFromCorpus(Corpus corpus) {

        final Set<Resource> providedList = new HashSet<>();
        // the following booleans are only used to avoid unnecessary checks and calls,
        // since the provided list doesn't need to check for duplicates
        boolean hasTokens = false,
                hasChunks = false,
                hasLemmas = false,
                hasPOS = false,
                hasDependencies = false,
                hasAnnotations = false,
                hasRelations = false;

        if (!corpus.getSentences().isEmpty()) {
            providedList.add(Resource.Sentences);

            for (Sentence s : corpus.getSentences()) {
                if (!s.getTokens().isEmpty()) {

                    if (!hasTokens) {
                        providedList.add(Resource.Tokens);
                        hasTokens = true;
                    }

                    for (Token t : s.getTokens()) {
                        if (!hasLemmas && t.getFeaturesMap().containsKey("LEMMA")) {
                            providedList.add(Resource.Lemmas);
                            hasLemmas = true;
                        }
                        if (!hasPOS && t.getFeaturesMap().containsKey("POS")) {
                            providedList.add(Resource.POS);
                            hasPOS = true;
                        }
                    }
                }

                if (!hasChunks && !s.getChunks().isEmpty()) {
                    providedList.add(Resource.Chunks);
                    hasChunks = true;
                }

                if (!hasDependencies && !s.getDependencyGraph().vertexSet().isEmpty()) {
                    providedList.add(Resource.Dependencies);
                    hasDependencies = true;
                }

                if (!hasAnnotations && !s.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, false).isEmpty()) {
                    providedList.add(Resource.Annotations);
                    hasAnnotations = true;
                }

                if (!hasRelations && !s.getRelations().isEmpty()) {
                    providedList.add(Resource.Relations);
                    hasRelations = true;
                }

            }
        }
        return providedList;
    }
    
    private void validateRequires(Module m,
                                  Set<Resource> providedList,
                                  Set<Resource> providedListFromCorpus,
                                  Requires r) throws NejiException {
        String moduleName = m.getClass().getSimpleName();
        if (r==null) {
            throw new NejiException(NO_REQUIRES_MESSAGE
                    .replaceAll($module_name$, moduleName));
        }

        for(Resource f : r.value()) {

            // if one of the 'Required' features is 'DynamicNLP', access the module's 'getLevels' method and
            // validates the obtained Resource levels with the provided list
            if(f.equals(Resource.DynamicNLP)){
                Collection<Resource> levels = getDynamicNLPLevels(m);
                for(Resource l : levels) {
                    if(!providedList.contains(l) && !providedListFromCorpus.contains(l)) {
                        throw new NejiException(REQUIRED_NOT_PROVIDED_MESSAGE
                                .replaceAll($module_name$, moduleName)
                                .replaceAll($resource_name$, l.name()));
                    }
                }

            } else if(!providedList.contains(f) && !providedListFromCorpus.contains(f)) {
                throw new NejiException(REQUIRED_NOT_PROVIDED_MESSAGE
                        .replaceAll($module_name$, moduleName)
                        .replaceAll($resource_name$, f.name()));
            }
        }
    }
    
    private void addProvides(Module m, Set<Resource> providedList, Resource[] values) throws NejiException {
        for(Resource f : values) {
            if(f.equals(Resource.DynamicNLP)) {
                Collection<Resource> levels = getDynamicNLPLevels(m);
                providedList.addAll(levels);
            } else {
                providedList.add(f);
            }
        }
    }
    
    private Collection<Resource> getDynamicNLPLevels(Module m) throws NejiException {
        if(Arrays.asList(m.getClass().getInterfaces()).contains(DynamicNLP.class)){
            DynamicNLP module = (DynamicNLP)m;
            Collection<ParserLevel> usedLevels = module.getLevels();

            Set<Resource> resources = new HashSet<>();
            for (ParserLevel lvl : usedLevels) {
                switch (lvl) {
                    case TOKENIZATION:  resources.add(Resource.Tokens); break;
                    case LEMMATIZATION: resources.add(Resource.Lemmas); break;
                    case CHUNKING:      resources.add(Resource.Chunks); break;
                    case POS:           resources.add(Resource.POS); break;
                    case DEPENDENCY:    resources.add(Resource.Dependencies); break;
                }
            }
            return resources;

        } else {
            throw new NejiException(INVALID_DYNAMIC_NLP_MESSAGE
                    .replaceAll($module_name$, m.getClass().getSimpleName()));
        }
    }
}
