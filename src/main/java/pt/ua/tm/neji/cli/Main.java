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

package pt.ua.tm.neji.cli;

import cc.mallet.util.MalletLogger;
import com.google.common.collect.Lists;
import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import pt.ua.tm.neji.batch.FileBatchExecutor;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextConfiguration;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.batch.BatchExecutor;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.logger.LoggingOutputStream;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.ml.MLModelsLoader;
import pt.ua.tm.neji.processor.FileProcessor;
import pt.ua.tm.neji.train.config.ModelConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Main application for the CLI tool.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Tiago Nunes
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 3.0
 * @since 1.0
 */
public class Main {

    /**
     * Help messages.
     */
    private static final String HEADER = "\nNeji: modular biomedical concept recognition made easy, fast and accessible.";
    private static final String USAGE
            = "-i <folder> -if [XML|RAW|BIOC|BC2] [-x <tags>] [-f <wildcard filter>] "
            + "-o <folder> -of [XML|NEJI|A1|CONLL|JSON|B64|BIOC|PIPE|PIPEXT|BC2] "
            + "[-p <folder>] "
            + "[-d <folder>] "
            + "[-m <folder>] "
            + "[-custom MODULE1-ARG1-ARG2|MODULE2-ARG1-ARG2|MODULE3-ARG1-ARG2] "
            + "[-ptool <PARSING_TOOL>] [-plang <PARSING_LANGUAGE>] [-plvl <PARSING_LEVEL>] "
            + "[-pcls <processor.class>] [-c] [-t <threads>] [-v]";
    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + "./neji.sh -i input -if XML -o output -of XML -x text -d folder -t 2\n"
            + "2: "
            + "./neji.sh -i input -if RAW -o output -of A1 -m folder -t 4\n"
            + "3: "
            + "./neji.sh -i input -if XML -o output -of XML -x text -d folder1 -m folder2 -c -t 6\n\n";
    private static final String FOOTER = "For more instructions, please visit http://bioinformatics.ua.pt/neji.";

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Print help message of the program.
     *
     * @param options Command line arguments.
     * @param msg     Message to be displayed.
     */
    private static void printHelp(final Options options, final String msg) {
        if (msg.length() != 0) {
            logger.error(msg);
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(150, "./neji.sh " + USAGE, HEADER, options, EXAMPLES + FOOTER);
    }

    public static void main(String[] args) {
//        installUncaughtExceptionHandler();

        int NUM_THREADS = Runtime.getRuntime().availableProcessors() - 1;
        NUM_THREADS = NUM_THREADS > 0 ? NUM_THREADS : 1;


        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information.");

        options.addOption("i", "input", true, "Folder with corpus files.");
        options.addOption("o", "output", true, "Folder to save the annotated corpus files.");
        options.addOption("f", "input-filter", true, "Wildcard to filter files in input folder");

        options.addOption("p", "parser", true, "Folder that contains the parsing tool.");

        Option o = new Option("m", "models", true, "Folder that contains the ML models.");
        o.setArgs(Integer.MAX_VALUE);
        options.addOption(o);

        options.addOption("d", "dictionaires", true, "Folder that contains the dictionaries.");

        options.addOption("if", "input-format", true, "BIOC, RAW or XML");
        o = new Option("of", "output-formats", true, "A1, B64, BIOC, CONLL, JSON, NEJI or XML");
        o.setArgs(Integer.MAX_VALUE);
        options.addOption(o);

        options.addOption("ptool", "parsing-tool", true, "GDEP or OPENNLP (GDEP is set by default)");

        options.addOption("plang", "parsing-language", true, "DANISH, DUTCH, ENGLISH, FRENCH, GERMAN, PORTUGUESE or SWEDISH (ENGLISH is set by default)");

        options.addOption("plvl", "parsing-level", true, "TOKENIZATION, POS, LEMMATIZATION, CHUNKING or DEPENDENCY (TOKENIZATION is set by default)");

        options.addOption("pcls", "processor-class", true, "Full name of pipeline processor class.");

        options.addOption("custom", "custom-modules", true, "Names of custom modules to be used in order, separated by pipes. If a specified module are not a reader or a writer, it will be executed after dictionary and model processing.");

        options.addOption("x", "xml-tags", true, "XML tags to be considered, separated by commas.");

        options.addOption("v", "verbose", false, "Verbose mode.");
        options.addOption("c", "compressed", false, "If files are compressed using GZip.");
        options.addOption("noids", "include-no-ids", false, "If annotations without IDs should be included.");
        options.addOption("t", "threads", true,
                "Number of threads. By default, if more than one core is available, it is the number of cores minus 1.");
        
        options.addOption("fp", "false-positives-filter", true, "File that contains the false positive terms.");
        options.addOption("gn", "semantic-groups-normalization", true, 
                "File that contains the semantic groups normalization terms.");
        
        CommandLine commandLine = null;
        try {
            // Parse the program arguments
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            logger.error("There was a problem processing the input arguments.", ex);
            return;
        }

        // Show help text
        if (commandLine.hasOption('h')) {
            printHelp(options, "");
            return;
        }

        // No options
        if (commandLine.getOptions().length == 0) {
            printHelp(options, "");
            return;
        }

        // Get corpus folder for input
        String folderCorpusIn = null;
        if (commandLine.hasOption('i')) {
            folderCorpusIn = commandLine.getOptionValue('i');
            File test = new File(folderCorpusIn);
            if (!test.isDirectory() || !test.canRead()) {
                logger.error("The specified path is not a folder or is not readable.");
                return;
            }
            folderCorpusIn = test.getAbsolutePath();
            folderCorpusIn += File.separator;
        } else {
            printHelp(options, "Please specify the input corpus folder.");
            return;
        }

        String inputFolderWildcard = null;
        if (commandLine.hasOption("f")) {
            inputFolderWildcard = commandLine.getOptionValue("f");
        }

        // Get Input format
        InputFormat inputFormat;
        if (commandLine.hasOption("if")) {
            inputFormat = InputFormat.valueOf(commandLine.getOptionValue("if"));
        } else {
            printHelp(options, "Please specify the input format.");
            return;
        }

        // Get corpus folder for output
        String folderCorpusOut = null;
        if (commandLine.hasOption('o')) {
            folderCorpusOut = commandLine.getOptionValue('o');
            File test = new File(folderCorpusOut);
            if (!test.isDirectory() || !test.canWrite()) {
                logger.error("The specified path is not a folder or is not writable.");
                return;
            }
            folderCorpusOut = test.getAbsolutePath();
            folderCorpusOut += File.separator;
        } else {
            printHelp(options, "Please specify the output corpus folder.");
            return;
        }


        // Get Output format
        List<OutputFormat> outputFormats = new ArrayList<>();
        if (commandLine.hasOption("of")) {
            String[] command = commandLine.getOptionValues("of");
            for (String s : command) {
                OutputFormat f = OutputFormat.valueOf(s);
                if (f.equals(OutputFormat.A1) || f.equals(OutputFormat.JSON) || f.equals(OutputFormat.NEJI)) {
                    if (inputFormat.equals(InputFormat.XML)) {
                        logger.error("XML input format only supports XML and CoNLL output formats, " +
                                "since other formats are based on character positions.");
                        return;
                    }
                }
                outputFormats.add(f);
            }
        } else {
            printHelp(options, "Please specify the output formats (in case of multiple formats, " +
                    "separate them with a \"\\|\").");
            return;
        }


        // Get XML tags
        String[] xmlTags = null;
        if (inputFormat.equals(InputFormat.XML)) {
            if (commandLine.hasOption("x")) {
                xmlTags = commandLine.getOptionValue("x").split(",");
            } else {
                printHelp(options, "Please specify XML tags to be used.");
                return;
            }
        }

        // Get models folder
        String modelsFolder = null;
        if (commandLine.hasOption('m')) {
            modelsFolder = commandLine.getOptionValue('m');

            File test = new File(modelsFolder);
            if (!test.isDirectory() || !test.canRead()) {
                logger.error("The specified models path is not a folder or is not readable.");
                return;
            }
            modelsFolder = test.getAbsolutePath();
            modelsFolder += File.separator;
        }

        // Get dictionaries folder
        String dictionariesFolder = null;
        if (commandLine.hasOption('d')) {
            dictionariesFolder = commandLine.getOptionValue('d');

            File test = new File(dictionariesFolder);
            if (!test.isDirectory() || !test.canRead()) {
                logger.error("The specified dictionaries path is not a folder or is not readable.");
                return;
            }
            dictionariesFolder = test.getAbsolutePath();
            dictionariesFolder += File.separator;
        }

        // Get parser folder
        String parserFolder = null;
        if (commandLine.hasOption("p")) {
            parserFolder = commandLine.getOptionValue("p");

            File test = new File(parserFolder);
            if (!test.isDirectory() || !test.canRead()) {
                logger.error("The specified parser path is not a folder or is not readable.");
                return;
            }
            parserFolder = test.getAbsolutePath();
            parserFolder += File.separator;
        }

        // Get processing modules
        String modulesCommandLine = "";
        if (commandLine.hasOption("custom")) {
            modulesCommandLine = commandLine.getOptionValue("custom");
        }

        // Get verbose mode
        boolean verbose = commandLine.hasOption('v');
        Constants.verbose = verbose;

        if (Constants.verbose) {
            MalletLogger.getGlobal().setLevel(Level.INFO);
            // Redirect sout
            LoggingOutputStream los = new LoggingOutputStream(LoggerFactory.getLogger("stdout"), false);
            System.setOut(new PrintStream(los, true));

            // Redirect serr
            los = new LoggingOutputStream(LoggerFactory.getLogger("sterr"), true);
            System.setErr(new PrintStream(los, true));
        } else {
            MalletLogger.getGlobal().setLevel(Level.OFF);
        }

        // Redirect JUL to SLF4
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // Get compressed mode
        boolean compressed = false;
        if (commandLine.hasOption('c')) {
            compressed = true;
        }

        // Get threads
        String threadsText = null;
        if (commandLine.hasOption('t')) {
            threadsText = commandLine.getOptionValue('t');
            NUM_THREADS = Integer.parseInt(threadsText);
            if (NUM_THREADS <= 0 || NUM_THREADS > 32) {
                logger.error("Illegal number of threads. Must be between 1 and 32.");
                return;
            }
        }

        // Load pipeline processor
        Class processor = FileProcessor.class;
        if (commandLine.hasOption("pcls")) {
            String processorName = commandLine.getOptionValue("pcls");
            try {
                processor = Class.forName(processorName);
            } catch (ClassNotFoundException ex) {
                logger.error("Could not load pipeline processor \"" + processorName + "\"");
                return;
            }
        }

        // Load parsing tool
        ParserTool parsingTool = ParserTool.GDEP;
        if (commandLine.hasOption("ptool")) {
            String parsingToolName = commandLine.getOptionValue("ptool");
            try {
                parsingTool = ParserTool.valueOf(parsingToolName);
            } catch (IllegalArgumentException ex) {
                logger.error("Invalid parsing tool \"" + parsingToolName + "\". "
                        + "Must be one of " + StringUtils.join(ParserTool.values(), ", "));
                return;
            }
        }

        // Load parsing language
        ParserLanguage parsingLanguage = ParserLanguage.ENGLISH;
        if (commandLine.hasOption("plang")) {
            String parsingLanguageName = commandLine.getOptionValue("plang");
            try {
                parsingLanguage = ParserLanguage.valueOf(parsingLanguageName);
            } catch (IllegalArgumentException ex) {
                logger.error("Invalid parsing language \"" + parsingLanguageName + "\". "
                        + "Must be one of " + StringUtils.join(ParserLanguage.values(), ", "));
                return;
            }
        }

        // Load parsing level
        ParserLevel parsingLevel = ParserLevel.TOKENIZATION;
        if (commandLine.hasOption("plvl")) {
            String parsingLevelName = commandLine.getOptionValue("plvl");
            try {
                parsingLevel = ParserLevel.valueOf(parsingLevelName);
            } catch (IllegalArgumentException ex) {
                logger.error("Invalid parsing level \"" + parsingLevelName + "\". "
                        + "Must be one of " + StringUtils.join(ParserLevel.values(), ", "));
                return;
            }
        } else {
            // Set model parsing level if ML will be used to annotate and no parsing level has been setted
            if (modelsFolder != null) {
                try {
                    parsingLevel = getModelsParsingLevel(modelsFolder);
                } catch (NejiException ex) {
                    logger.error("Could not load models parsing level.");
                    return;
                }
            }
        }

        // Get if annotations without ids should be included
        boolean includeAnnotationsWithoutIDs = false;
        if (commandLine.hasOption("noids")) {
            includeAnnotationsWithoutIDs = true;
        }
        
        // Get false positives filter
        byte[] fpByteArray = null;
        if (commandLine.hasOption("fp")) {
            String fpPath = commandLine.getOptionValue("fp");

            File test = new File(fpPath);
            if (!test.isFile() || !test.canRead()) {
                logger.error("The specified false positives path is not a file or is not readable.");
                return;
            }
            
            fpPath = test.getAbsolutePath();
            fpPath += File.separator;
            try {
                fpByteArray = IOUtils.toByteArray(
                        new FileInputStream(new File(fpPath)));
            } catch (IOException ex) {
                logger.error("There was a problem loading the false positives "
                        + "file.", ex);
                return;
            }
        }
        
        // Get semantic groups normalization
        byte[] groupsNormByteArray = null;
        if (commandLine.hasOption("gn")) {
            String gnPath = commandLine.getOptionValue("gn");

            File test = new File(gnPath);
            if (!test.isFile() || !test.canRead()) {
                logger.error("The specified semantic groups normalization path "
                        + "is not a file or is not readable.");
                return;
            }
            
            gnPath = test.getAbsolutePath();
            gnPath += File.separator;
            try {
                groupsNormByteArray = IOUtils.toByteArray(
                        new FileInputStream(new File(gnPath)));
            } catch (IOException ex) {
                logger.error("There was a problem loading the semantic groups "
                        + "normalization file.", ex);
                return;
            }
        }

        // Context is built through a descriptor first, so that the pipeline can be validated before any processing
        ContextConfiguration descriptor = null;
        try {
            descriptor = new ContextConfiguration.Builder()
                    .withInputFormat(inputFormat)
                    .withOutputFormats(outputFormats)
                    .withParserTool(parsingTool)
                    .withParserLanguage(parsingLanguage)
                    .withParserLevel(parsingLevel)
                    .parseCLI(modulesCommandLine)
                    .build();
            
            descriptor.setFalsePositives(fpByteArray);
            descriptor.setSemanticGroupsNormalization(groupsNormByteArray);
        } catch (NejiException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        boolean storeDocuments = false;

        Context context = new Context(
                descriptor,
                modelsFolder, // Models
                dictionariesFolder, // Dictionaries folder
                parserFolder // Parser folder
        );

        try {
            BatchExecutor batchExecutor = new FileBatchExecutor(folderCorpusIn, folderCorpusOut,
                    compressed, NUM_THREADS, inputFolderWildcard, storeDocuments, 
                    includeAnnotationsWithoutIDs);

            if (xmlTags == null) {
                batchExecutor.run(processor, context);
            } else {
                batchExecutor.run(processor, context, new Object[]{xmlTags});
            }

        } catch (Exception ex) {
            logger.error("There was a problem running the batch.", ex);
        }
    }

    private static void installUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable thrwbl) {
                if (thrwbl instanceof ThreadDeath) {
                    logger.warn("Ignoring uncaught ThreadDead exception.");
                    return;
                }
                logger.error("Uncaught exception on cli thread, aborting.", thrwbl);
                System.exit(0);
            }
        });
    }

    /**
     * Get the higher parsing level from a folder with one or more models.
     *
     * @param modelsFolderPath models folder path
     * @return parsing level
     */
    private static ParserLevel getModelsParsingLevel(String modelsFolderPath) throws NejiException {

        String priorityFileName = modelsFolderPath + "_priority";
        MLModelsLoader ml;
        ParserLevel parsingLevel = ParserLevel.TOKENIZATION;

        // Load models list
        try {
            ml = new MLModelsLoader(Files.newInputStream(Paths.get(priorityFileName)));
        } catch (IOException ex) {
            throw new NejiException("There was a problem reading the models.", ex);
        }

        ml.load(new File(modelsFolderPath));

        Map<String, MLModel> modelsTS = ml.getModels();
        List<MLModel> modelList = Lists.newArrayList(modelsTS.values());

        // Get higher models parsing level
        for (MLModel model : modelList) {
            ModelConfig mc = new ModelConfig(model.getConfigFile());

            if (mc.isNLP()) {
                parsingLevel = ParserLevel.DEPENDENCY;
                break;
            } else if (mc.isChunk()) {
                parsingLevel = ParserLevel.CHUNKING;
            } else if (mc.isPos()) {
                if (parsingLevel != ParserLevel.CHUNKING) {
                    parsingLevel = ParserLevel.POS;
                }
            } else if (mc.isLemma()) {
                if ((parsingLevel != ParserLevel.CHUNKING) && (parsingLevel != ParserLevel.POS)) {
                    parsingLevel = ParserLevel.LEMMATIZATION;
                }
            }
        }

        return parsingLevel;
    }
}
