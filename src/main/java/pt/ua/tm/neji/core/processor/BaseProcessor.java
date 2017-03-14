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

package pt.ua.tm.neji.core.processor;

import org.apache.commons.lang.Validate;
import pt.ua.tm.neji.train.model.CRFBase;
import pt.ua.tm.neji.context.*;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Reader;
import pt.ua.tm.neji.core.module.Writer;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.DictionaryHybrid;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.misc.DTDTagger;
import pt.ua.tm.neji.ml.MLHybrid;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.reader.BioCReader;
import pt.ua.tm.neji.train.context.TrainContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pt.ua.tm.neji.disambiguator.Disambiguate;
import pt.ua.tm.neji.postprocessing.Abbreviation;
import pt.ua.tm.neji.postprocessing.FalsePositivesFilter;
import pt.ua.tm.neji.postprocessing.SemanticGroupsNormalizer;
import pt.ua.tm.neji.train.nlp.TrainNLP;
import pt.ua.tm.neji.train.reader.BC2Reader;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class BaseProcessor implements Processor {

    private Context context;

    protected BaseProcessor(Context context) {
        this.context = context;
    }

    @Override
    public final Context getContext() {
        return context;
    }

    @Override
    public final void setContext(Context context) {
        Validate.notNull(context);
        this.context = context;
    }

    protected final void instantiateModules(List<Dictionary> dictionaries,
                                            List<MLModel> models,
                                            ContextProcessors cp,
                                            Context c,
                                            Pipeline p,
                                            String[] xmlTags,
                                            boolean addAnnotationsWithoutIDs) throws NejiException {
        instantiateModules(
                dictionaries, models, cp.getParser().getLevel(), cp, c, p, 
                xmlTags, addAnnotationsWithoutIDs);
    }

    protected final void instantiateModules(List<Dictionary> dictionaries,
                                            List<MLModel> models,
                                            ParserLevel customLevel,
                                            ContextProcessors cp,
                                            Context c,
                                            Pipeline p,
                                            String[] xmlTags,
                                            boolean addAnnotationsWithoutIDs) throws NejiException {

        List<Module> moduleList = new ArrayList<>();
        fetchModulesFromConfig(cp.getParser(), customLevel, moduleList, xmlTags);
        
        int index = 2;

        // Dictionary matching
        for (Dictionary d : dictionaries) {
            DictionaryHybrid dtl = new DictionaryHybrid(d);
            moduleList.add(index++, dtl);
        }

        // Machine learning
        for (MLModel model : models) {
            // Take model
            CRFBase crf = cp.getCRF(model.getModelName());

            // Add ML recognizer to pipeline
            MLHybrid ml;
            if (model.hasNormalizationDictionaries()) {
                ml = new MLHybrid(crf, model.getSemanticGroup(),
                        model.getNormalizationDictionaries(), addAnnotationsWithoutIDs);
            } else {
                ml = new MLHybrid(crf, model.getSemanticGroup(), addAnnotationsWithoutIDs);
            }
            moduleList.add(index++, ml);
        }        
                        
        // Post-processing: Abbreviations        
        if (c.getConfiguration().getAbbreviations()) {
            Abbreviation abb = new Abbreviation();
            moduleList.add(index++, abb);            
        }
        
        // Post-processing: Disambiguation (remove nested annotations from the same group)
        if (c.getConfiguration().getDisambiguation()) {
            Disambiguate dis = new Disambiguate(true, false);
            moduleList.add(index++, dis);            
        }
        
        // Post-processing: False positives filter module        
        if (c.getConfiguration().getFalsePositivesStream() != null) {
            FalsePositivesFilter fpf = 
                    new FalsePositivesFilter(c.getConfiguration().getFalsePositivesStream());
            moduleList.add(index++, fpf);
        }
        
        // Post-processing: Semantic groups normalization       
        if (c.getConfiguration().getSemanticGroupsNormalizationStream() != null) {
            SemanticGroupsNormalizer sgn = 
                    new SemanticGroupsNormalizer(c.getConfiguration()
                            .getSemanticGroupsNormalizationStream());
            moduleList.add(index++, sgn);
        }

        // Add all of the modules to the pipeline
        for (Module m : moduleList) {
            p.add(m);
        }
        
//        p.add(new Abbreviation());
//
//        // Remove nested same group
//        p.add(new Disambiguate(true, false));


        // Protein species filtering experiment
//        p.add(new ProteinSpeciesFilter(ProteinToSpecies.getInstance().getMap(), "PRGE", "SPEC"));
    }    

    protected final void instantiateModulesFromGroups(List<Dictionary> dictionaries,
                                                      List<MLModel> models,
                                                      ContextProcessors cp,
                                                      Context c,
                                                      Pipeline p,
                                                      Map<String, Boolean> groups,
                                                      String[] xmlTags,
                                                      boolean addAnnotationsWithoutIDs) throws NejiException {
        instantiateModulesFromGroups(
                dictionaries, models, cp.getParser().getLevel(), cp, c, p, 
                groups, xmlTags, addAnnotationsWithoutIDs);
    }

    protected final void instantiateModulesFromGroups(List<Dictionary> dictionaries,
                                                      List<MLModel> models,
                                                      ParserLevel customLevel,
                                                      ContextProcessors cp,
                                                      Context c,
                                                      Pipeline p,
                                                      Map<String, Boolean> groups,
                                                      String[] xmlTags,
                                                      boolean addAnnotationsWithoutIDs) throws NejiException {
        List<Module> moduleList = new ArrayList<>();
        fetchModulesFromConfig(cp.getParser(), customLevel, moduleList, xmlTags);

        int index = 2;

        // Dictionaries
        for (Dictionary d : dictionaries) {
            if (groups.containsKey(d.getGroup())) {
                if (groups.get(d.getGroup())) {
                    DictionaryHybrid dtl = new DictionaryHybrid(d);
                    moduleList.add(index++, dtl);
                }
            }
        }

        // Machine learning
        for (MLModel model : models) {
            boolean addModel;
            if (!groups.containsKey(model.getSemanticGroup())) {
                addModel = false;
            } else {
                addModel = groups.get(model.getSemanticGroup());
            }

            if (addModel) {
                CRFBase crf = cp.getCRF(model.getModelName());
                moduleList.add(index++, new MLHybrid(crf, model.getSemanticGroup(),
                        model.getNormalizationDictionaries(), addAnnotationsWithoutIDs));
            }
        }
        
        // Post-processing: False positives filter module        
        if (c.getConfiguration().getFalsePositivesStream() != null) {
            FalsePositivesFilter fpf = 
                    new FalsePositivesFilter(c.getConfiguration().getFalsePositivesStream());
            moduleList.add(index++, fpf);
        }
        
        // Post-processing: Semantic groups normalization       
        if (c.getConfiguration().getSemanticGroupsNormalizationStream() != null) {
            SemanticGroupsNormalizer sgn = 
                    new SemanticGroupsNormalizer(c.getConfiguration()
                            .getSemanticGroupsNormalizationStream());
            moduleList.add(index++, sgn);
        }

        // Add all of the modules to the pipeline
        for (Module m : moduleList) {
            p.add(m);
        }

//        p.add(new Abbreviation());
//
//        // Remove nested same group
//        p.add(new Disambiguate(true, false));


        // Protein species filtering experiment
//        p.add(new ProteinSpeciesFilter(ProteinToSpecies.getInstance().getMap(), "PRGE", "SPEC"));

    }


    private void fetchModulesFromConfig(Parser parser,
                                        ParserLevel parserLevel,
                                        List<Module> moduleList,
                                        String[] xmlTags) throws NejiException {
        // Change previous s tags
//        moduleList.add(new TagReplacer("s", "§"));

        ContextConfiguration config = context.getConfiguration();

        // Change DocType of the document (DTD)
        if (config.getInputFormat().equals(InputFormat.XML)
                && config.getOutputFormats().contains(OutputFormat.XML)) {
            DTDTagger doc = new DTDTagger();
            moduleList.add(doc);
        }

        config.fetchCustomModules(moduleList, parser);
        
        Reader reader = null;
        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.addAll(config.getOutputFormats());
        for (Module m : moduleList) {

            if (m instanceof Reader) {
                reader = (Reader) m;

            } else if (m instanceof Writer) {
                OutputFormat f = ((Writer) m).getFormat();
                if (config.getOutputFormats().contains(f)) {
                    outputFormats.remove(f);
                }
            }
        }

        if (reader == null) {
            reader = config.getInputFormat().instantiateDefaultReader(parser, parserLevel, xmlTags);
        }
        moduleList.add(0, reader);

        // NLP
        if (reader instanceof BC2Reader) {
            TrainNLP nlp = new TrainNLP(parser, parserLevel);
            moduleList.add(1, nlp);
        }
        else if ((!(reader instanceof BioCReader)) && (!(context instanceof TrainContext))) {
            NLP nlp = new NLP(parser, parserLevel);
            moduleList.add(1, nlp);
        }

        // Instantiate writers
        for (OutputFormat f : outputFormats) {
            moduleList.add(f.instantiateDefaultWriter());
        }
    }
}
