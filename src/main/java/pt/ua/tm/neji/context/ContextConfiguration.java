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

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Primitives;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import martin.common.Pair;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.ModuleLookup;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Reader;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.core.pipeline.PipelineValidator;
import pt.ua.tm.neji.dictionary.DictionaryHybrid;
import pt.ua.tm.neji.disambiguator.Disambiguate;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.pipeline.DefaultPipelineValidator;
import pt.ua.tm.neji.postprocessing.Abbreviation;
import pt.ua.tm.neji.train.nlp.TrainNLP;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase1;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase1Validator;
import pt.ua.tm.neji.train.trainer.DefaultTrainer;

/**
 * Utility class that holds pipeline modules, parser parameters and document
 * formats to handle conversion into JSON and back, which in turn allows
 * these to be persisted or transferred between different endpoints.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class ContextConfiguration {

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(ContextConfiguration.class);

    private final InputFormat inputFormat;
    private final List<OutputFormat> outputFormats;
    private final List<String> moduleNames;
    private final Map<String, Collection<Pair<String>>> moduleParams;
    private final ParserTool parserTools;
    private final ParserLanguage parserLanguage;
    private final ParserLevel parserLevel;
    
    private byte[] falsePositives;
    private byte[] semanticGroupsNormalization;

    private ContextConfiguration(final InputFormat inputFormat,
                                 final List<OutputFormat> outputFormats,
                                 final List<String> moduleNames,
                                 final Map<String, Collection<Pair<String>>> moduleParams,
                                 final ParserTool parserTools,
                                 final ParserLanguage parserLanguage,
                                 final ParserLevel parserLevel) {

        this(inputFormat, outputFormats, moduleNames, moduleParams, parserTools,
                parserLanguage, parserLevel, null, null);
    }
    
    private ContextConfiguration(final InputFormat inputFormat,
                                 final List<OutputFormat> outputFormats,
                                 final List<String> moduleNames,
                                 final Map<String, Collection<Pair<String>>> moduleParams,
                                 final ParserTool parserTools,
                                 final ParserLanguage parserLanguage,
                                 final ParserLevel parserLevel,
                                 final byte[] falsePositives,
                                 final byte[] semanticGroupsNormalization) {

        this.inputFormat = inputFormat;
        this.outputFormats = outputFormats;
        this.moduleNames = moduleNames;
        this.moduleParams = moduleParams;
        this.parserTools = parserTools;
        this.parserLanguage = parserLanguage;
        this.parserLevel = parserLevel;
        
        this.falsePositives = falsePositives;
        this.semanticGroupsNormalization = semanticGroupsNormalization;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();

        String inputFormatJson = gson.toJson(inputFormat);
        String outputFormatsJson = gson.toJson(outputFormats);
        String moduleNamesJson = gson.toJson(moduleNames);
        String paramsJson = gson.toJson(moduleParams);

        List<String> parserStrings = new ArrayList<>();
        parserStrings.add(parserTools.name());
        parserStrings.add(parserLanguage.name());
        parserStrings.add(parserLevel.name());
        String parserArgsJson = gson.toJson(parserStrings);

        return  inputFormatJson + "|" + outputFormatsJson + "|" +
                moduleNamesJson + "|" + paramsJson + "|" + parserArgsJson;
    }

    public InputFormat getInputFormat() {
        return inputFormat;
    }

    public List<OutputFormat> getOutputFormats() {
        return outputFormats;
    }

    public ParserTool getParserTool(){
        return parserTools;
    }

    public ParserLanguage getParserLanguage(){
        return parserLanguage;
    }

    public ParserLevel getParserLevel() {
        return parserLevel;
    }
    
    /**
     * Get false positives byte array.
     * @return The false positives byte array
     */
    public byte[] getFalsePositives() {
        return falsePositives;
    }
    
    /**
     * Get false positives stream.
     * @return The false positives stream
     */
    public InputStream getFalsePositivesStream() {
        if (falsePositives == null) {
            return null;
        }
        
        return new ByteArrayInputStream(falsePositives);
    }
    
    /**
     * Set false positives.
     * @param falsePositives The false positives byte array
     */
    public void setFalsePositives(byte[] falsePositives) {
        this.falsePositives = falsePositives;
    }
    
    /** Get semantic groups normalization byte array.
     * @return The semantic groups normalization byte array
     */
    public byte[] getSemanticGroupsNormalization() {
        return semanticGroupsNormalization;
    }
    
    /**
     * Get semantic groups normalization stream.
     * @return The false positives stream
     */
    public InputStream getSemanticGroupsNormalizationStream() {
        if (semanticGroupsNormalization == null) {
            return null;
        }
        
        return new ByteArrayInputStream(semanticGroupsNormalization);
    }
    
    /**
     * Set semantic groups normalization.
     * @param semanticGroupsNormalization The semantic groups normalization byte array
     */
    public void setSemanticGroupsNormalization(byte[] semanticGroupsNormalization) {
        this.semanticGroupsNormalization = semanticGroupsNormalization;
    }

    public int fetchCustomModules(final List<Module> moduleList, Parser parser) {
        int indexAfterReaders = 1;
        int i = 0;

        try{
            for(String moduleName : moduleNames) {
                
                Module m;
                Collection<Pair<String>> moduleParams = this.moduleParams.get(moduleName);

                if (moduleParams != null) {
                    m = invokeModuleWithParams(moduleName, moduleParams, parser);

                } else {
                    m = (Module) Class.forName(moduleName).newInstance();

                }
                moduleList.add(m);
                if(m instanceof Reader){
                    indexAfterReaders = i + 1;
                }

                i++;
            }
        } catch (ReflectiveOperationException ex) {
            logger.error("Error fetching modules", ex);
        }

        return indexAfterReaders;
    }

    private static Module invokeModuleWithParams(final String moduleName,
                                                 final Collection<Pair<String>> moduleParams,
                                                 final Parser parser) throws ReflectiveOperationException {

        List<Class<?>> classes = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        for (Pair<String> pair : moduleParams) {

            Class paramClass = Class.forName(pair.getX());

            if (paramClass == Parser.class) {
                if(parser==null) {
                    logger.warn("Module that required a parser in constructor was skipped during ContextDescriptor fetching: no parser was provided.");
                    continue;
                } else {
                    objects.add(parser);
                }

            } else if (paramClass.isEnum()) {
                Enum paramEnum = Enum.valueOf(paramClass, pair.getY());
                objects.add(paramEnum);

            } else {
                objects.add(ConstructorUtils.invokeConstructor(paramClass, pair.getY()));

                if (Primitives.isWrapperType(paramClass)) {
                    paramClass = Primitives.unwrap(paramClass);
                }
            }
            classes.add(paramClass);
        }

        return  (Module) ConstructorUtils.invokeConstructor(
                Class.forName(moduleName),
                objects.toArray(),
                classes.toArray(new Class[classes.size()]));
    }

    public static final class Builder {

        // Variables that define a Context and the pipeline modules
        private final AtomicReference<InputFormat> inputFormat;
        private final List<OutputFormat> outputFormats;
        private final List<String> moduleNames;
        private final Multimap<String, Pair<String>> moduleParams;
        private final AtomicReference<ParserTool> atomicTool;
        private final AtomicReference<ParserLanguage> atomicLanguage;
        private final AtomicReference<ParserLevel> atomicLevel;
        private final AtomicReference<String> atomicParserPath;

        // A pipeline is built right away to perform validation when building
        // the configuration, providing immediate feedback to the user in
        // case it fails.
        private final List<Module> modulesForValidation;


        public Builder(){
            inputFormat = new AtomicReference<>();
            outputFormats = new ArrayList<>();
            moduleNames = new ArrayList<>();
            moduleParams = ArrayListMultimap.create();
            atomicTool = new AtomicReference<>(ParserTool.GDEP);
            atomicLanguage = new AtomicReference<>(ParserLanguage.ENGLISH);
            atomicLevel = new AtomicReference<>(ParserLevel.TOKENIZATION);
            modulesForValidation = new ArrayList<>();
            atomicParserPath = new AtomicReference<>();
        }

        public ContextConfiguration build() throws NejiException {

            final InputFormat inputFormat2 = inputFormat.get();

            if (inputFormat2 == null) {
                throw new NejiException("An input format must be set before parsing the command line modules.");
            }

            if (outputFormats.isEmpty()) {
                throw new NejiException("One or more output formats must be set before parsing the command line modules.");
            }

            Parser placeholderParser = Parser.defaultParserFactory(
                    atomicTool.get(), atomicLanguage.get(), atomicLevel.get(), atomicParserPath.get());
            if (!inputFormat2.equals(InputFormat.CUSTOM)) {
                modulesForValidation.add(inputFormat2.instantiateDefaultReader(placeholderParser, 
                        placeholderParser.getLevel(), new String[0], null, null));
            }

            // adds placeholder NLP and Abbreviation to fulfill some requirements during validation
            int index = 0;
            if (inputFormat2.equals(InputFormat.BC2)) {
                modulesForValidation.add(0, new TrainNLP(placeholderParser));
                index = 1;
            }
            else if (!(inputFormat2.equals(InputFormat.BIOC) || inputFormat2.equals(InputFormat.PDF)) 
                    && !moduleNames.contains(NLP.class.getName())) {
                modulesForValidation.add(0, new NLP(placeholderParser));
                index = 1;
            }
            modulesForValidation.add(index, new Abbreviation());


            for (OutputFormat outputFormat : outputFormats) {
                if (!outputFormat.equals(OutputFormat.CUSTOM))
                    modulesForValidation.add(outputFormat.instantiateDefaultWriter());
            }

            // validate specified modules
            DefaultPipeline p = new DefaultPipeline();
            PipelineValidator validator = new DefaultPipelineValidator(p);
            for (Module m : modulesForValidation) {
                p.add(m);
            }
            validator.validate();

            return new ContextConfiguration(
                    inputFormat.get(),                  // input format
                    outputFormats,                      // output format
                    moduleNames, moduleParams.asMap(),  // pipeline modules and args
                    atomicTool.get(),                   // parser tool
                    atomicLanguage.get(),               // parser language
                    atomicLevel.get());                 // parser level
        }
        
        public ContextConfiguration trainBuildPhase1() throws NejiException {

            final InputFormat inputFormat2 = inputFormat.get();

            // Veify if there are an input format
            if (inputFormat2 == null) {
                throw new NejiException("An input format must be set before parsing the command line modules.");
            }

            // Verify if there are an output format
            if (outputFormats.isEmpty()) {
                throw new NejiException("One output formats must be set before parsing the command line modules.");
            }

            // Add reader to modules for validation
            Parser placeholderParser = Parser.defaultParserFactory(
                    atomicTool.get(), atomicLanguage.get(), atomicLevel.get(), atomicParserPath.get());            
            if (!inputFormat2.equals(InputFormat.CUSTOM)) {
                modulesForValidation.add(inputFormat2.instantiateDefaultReader(placeholderParser, 
                        placeholderParser.getLevel(), new String[0], null, null));
            }
            
            // Add writer to modules for validation
            for (OutputFormat outputFormat : outputFormats) {
                if (!outputFormat.equals(OutputFormat.CUSTOM)) {
                    modulesForValidation.add(outputFormat.instantiateDefaultWriter());
                }
            }

            // Validate specified modules
            TrainPipelinePhase1 p = new TrainPipelinePhase1();
            PipelineValidator validator = new TrainPipelinePhase1Validator(p);
            for (Module m : modulesForValidation) {
                p.add(m);
            }
            validator.validate();

            return new ContextConfiguration(
                    inputFormat.get(),                  // input format
                    outputFormats,                      // output format
                    moduleNames, moduleParams.asMap(),  // pipeline modules and args
                    atomicTool.get(),                   // parser tool
                    atomicLanguage.get(),               // parser language
                    atomicLevel.get());                 // parser level
        }
        
        public ContextConfiguration trainBuildPhase2() throws NejiException {

            final InputFormat inputFormat2 = inputFormat.get();

            // Veify if there are an input format
            if (inputFormat2 == null) {
                throw new NejiException("An input format must be set before parsing the command line modules.");
            }

            // Verify if there are an output format
            if (outputFormats.isEmpty()) {
                throw new NejiException("One output formats must be set before parsing the command line modules.");
            }
            
            // Add reader to modules for validation
            Parser placeholderParser = Parser.defaultParserFactory(
                    atomicTool.get(), atomicLanguage.get(), atomicLevel.get(), atomicParserPath.get());            
            if (!inputFormat2.equals(InputFormat.CUSTOM)) {
                modulesForValidation.add(inputFormat2.instantiateDefaultReader(placeholderParser, 
                        placeholderParser.getLevel(), new String[0], null, null));
            }
            
                        
            // Add trainer to modules for validation
            modulesForValidation.add(new DefaultTrainer());
            
            // Add writer to modules for validation
            for (OutputFormat outputFormat : outputFormats) {
                if (!outputFormat.equals(OutputFormat.CUSTOM)) {
                    modulesForValidation.add(outputFormat.instantiateDefaultWriter());
                }
            }

            // Validate specified modules
            TrainPipelinePhase1 p = new TrainPipelinePhase1();
            PipelineValidator validator = new TrainPipelinePhase1Validator(p);
            for (Module m : modulesForValidation) {
                p.add(m);
            }
            validator.validate();

            return new ContextConfiguration(
                    inputFormat.get(),                  // input format
                    outputFormats,                      // output format
                    moduleNames, moduleParams.asMap(),  // pipeline modules and args
                    atomicTool.get(),                   // parser tool
                    atomicLanguage.get(),               // parser language
                    atomicLevel.get());                 // parser level
        }

        public Builder withInputFormat(final InputFormat inputFormat) {
            this.inputFormat.set(inputFormat);
            return this;
        }

        public Builder withOutputFormats(final List<OutputFormat> outputFormats) {
            this.outputFormats.addAll(outputFormats);
            return this;
        }

        public Builder withParserTool(final ParserTool parserTool) {
            this.atomicTool.set(parserTool);
            return this;
        }

        public Builder withParserLanguage(final ParserLanguage parserLanguage) {
            this.atomicLanguage.set(parserLanguage);
            return this;
        }

        public Builder withParserLevel(final ParserLevel parserLevel) {
            this.atomicLevel.set(parserLevel);
            return this;
        }

        public Builder withParserPath(final String parserPath) {
            this.atomicParserPath.set(parserPath);
            return this;
        }

        /**
         * Parses a JSON string and obtains organized descriptions of modules to be
         * instantiated and used in the future.
         */
        public Builder parseJson(String jsonText) throws NejiException {
            try {
                Gson gson = new Gson();
                Pattern pipePattern = Pattern.compile("\\|");

                String[] split = pipePattern.split(jsonText);

                InputFormat aux1 = gson.fromJson(split[0], new TypeToken<InputFormat>() {}.getType());
                inputFormat.set(aux1);

                List<OutputFormat> aux2 = gson.fromJson(split[1], new TypeToken<List<OutputFormat>>() {}.getType());
                outputFormats.addAll(aux2);

                List<String> aux3 = gson.fromJson(split[2], new TypeToken<List<String>>() {}.getType());
                moduleNames.addAll(aux3);

                Map<String, Collection<Pair<String>>> aux4 = gson.fromJson(split[3],
                        new TypeToken<Map<String, Collection<Pair<String>>>>() {}.getType());
                for(Map.Entry<String, Collection<Pair<String>>> entry : aux4.entrySet()) {
                    moduleParams.putAll(entry.getKey(), entry.getValue());
                }

                List<String> parserStrings =  gson.fromJson(split[4], new TypeToken<List<String>>() {}.getType());
                atomicTool.set(ParserTool.valueOf(parserStrings.get(0)));
                atomicLanguage.set(ParserLanguage.valueOf(parserStrings.get(1)));
                atomicLevel.set(ParserLevel.valueOf(parserStrings.get(2)));

                return this;

            } catch (JsonSyntaxException ex) {
                throw new NejiException(ex);
            }
        }

        /**
         * Parses a list of command-line segments and obtains organized descriptions of
         * modules to be instantiated and used in the future.
         */
        public Builder parseCLI(String modulesCommandLine) throws NejiException {
            if(Strings.isNullOrEmpty(modulesCommandLine)) {
                return this; // default post-processing modules
            }

            final List<String> modulesCommandSegments =
                    Lists.newArrayList(Pattern.compile("\\|").split(modulesCommandLine));

            for (int k = 0; k < modulesCommandSegments.size(); k++) {
                String moduleName = modulesCommandSegments.get(k).toLowerCase();
                List<String> moduleArgs = new ArrayList<>();

                if (moduleName.contains("-")) {
                    String[] segmentParts = moduleName.split("-");
                    moduleName = segmentParts[0];

                    int size = segmentParts.length;
                    for (int i = 1; i < size; i++) {
                        moduleArgs.add(segmentParts[i]);
                    }
                }

                Class<? extends Module> moduleClass = null;

                for (Class<? extends Module> m : ModuleLookup.getAllModuleClasses()) {
                    if (m.getSimpleName().toLowerCase().contains(moduleName)) {
                        moduleClass = m;
                        break;
                    }
                }

                // if class is null, it is not a valid module
                if (moduleClass == null) {
                    throw new NejiException("Invalid command line segment, index " + k + ": '" + moduleName + "' does not exist.");
                }

                try {
                    resolveForValidation(moduleClass, moduleArgs);
                } catch (NejiException ex) {
                    throw new NejiException("Invalid command line segment, index " + k + ": " + ex.getMessage());
                }
            }

            return this;
        }


        private void resolveForValidation(final Class<? extends Module> moduleClass,
                                          final List<String> moduleArgs) throws NejiException {
            Module moduleObject;
            List<Pair<String>> params = new ArrayList<>();

            try {
                try {
                    // tests if module has a constructor that requires a Parser
                    moduleClass.getConstructor(Parser.class);

                    // since module has such a constructor:
                    Parser parser = Parser.defaultParserFactory(atomicTool.get(), atomicLanguage.get(), atomicLevel.get(), null);
                    moduleObject = ConstructorUtils.invokeConstructor(moduleClass, parser);
                    params.add(new Pair<>(Parser.class.getName(), ""));
                    atomicTool.set(parser.getTool());
                    atomicLanguage.set(parser.getLanguage());
                    atomicLevel.set(parser.getLevel());

                } catch (ReflectiveOperationException ex) {
                    // since module does not have a constructor that requires a Parser

                    if (moduleClass == Disambiguate.class) {
                        moduleObject = new Disambiguate(true, false);
                        params.add(new Pair<>(Boolean.class.getName(), Boolean.toString(true)));
                        params.add(new Pair<>(Boolean.class.getName(), Boolean.toString(false)));

                    } else if (false) {
                        /// TODO: FILL IN OTHER MODULE'S DEFAULT DEFINITIONS HERE


                    } else {
                        // no more specific module definitions, assume default constructor
                        moduleObject = moduleClass.newInstance();
                    }
                }

            }catch (InstantiationException|IllegalAccessException ex){
                throw new NejiException(ex);
            }

            modulesForValidation.add(moduleObject);
//            if (moduleObject instanceof Reader) {
//                inputFormat.set(((Reader) moduleObject).getInputFormat());
//            } else if (moduleObject instanceof Writer) {
//                outputFormats.add(((Writer) moduleObject).getOutputFormat());
            if(moduleObject instanceof NLP){
                modulesForValidation.add(new DictionaryHybrid(null)); // just to provide Annotations during validation
            }

            moduleNames.add(moduleClass.getName());
            for (Pair<String> param : params) {
                moduleParams.put(moduleClass.getName(), param);
            }
        }

    }
}
