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
package pt.ua.tm.neji.train.cli;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.ContextConfiguration;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.batch.BatchExecutor;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.batch.TrainBatchExecutor;
import pt.ua.tm.neji.train.config.ModelConfig;
import pt.ua.tm.neji.train.context.TrainContext;
import pt.ua.tm.neji.train.processor.TrainProcessor;
import pt.ua.tm.neji.train.util.A1Utils;
import pt.ua.tm.neji.train.util.A1Utils.A1Pairs;
import pt.ua.tm.neji.train.util.Utils;

/**
 *
 * @author jeronimo
 */
public class TrainMain {

    /**
     * Help Messages.
     */
    private static final String HEADER = "\nNeji: modular biomedical concept recognition made easy, fast and accessible.";
    private static final String USAGE
            = "-c <file|folder> "
            + "-a <file> -if [BC2|A1|JNLPBA|SERIALIZED] "
            + "-f <file> "
            + "-o <folder>"
            + "-m <name>"
            + "[-d <folder>]"
            + "[-t <threads>]"
            + "[-s <file>]";
    private static final String EXAMPLES = "\nUsage example:\n"
            + "1: "
            + "./nejiTrain.sh -c sentences -a annotations -if BC2 f features -o modelfolder/ -m modelname\n";
    private static final String FOOTER = "For more instructions, please visit http://bioinformatics.ua.pt/neji.";

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(TrainMain.class);

    /**
     * Print help message of the program.
     *
     * @param options Command line arguments.
     * @param msg Message to be displayed.
     */
    private static void printHelp(final Options options, final String msg) {
        if (msg.length() != 0) {
            logger.error(msg);
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(150, "./neji.sh " + USAGE, HEADER, options, EXAMPLES + FOOTER);
    }

    public static void main(String[] args) {

        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information.");

        options.addOption("c", "sentences-input", true, "File with corpus sentences.");
        options.addOption("a", "annotations-input", true, "File with corpus annotations.");
        options.addOption("if", "input-format", true, "BC2, A1, JNLPBA or SERIALIZED.");
        options.addOption("f", "features", true, "Features.");
        options.addOption("o", "model output path", true, "Path to save the model.");
        options.addOption("m", "model name", true, "Name of the model.");
        options.addOption("d", "dictionaires", true, "Folder that contains the dictionaries.");
        options.addOption("t", "threads", true, "Number of threads. By default, is 1.");
        options.addOption("s", "serialize", true, "File to save the serialized corpus.");

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

        String fileSentencesIn = null;
        InputFormat inputFormat;
        String fileAnnotationsIn = null;
               
        // Get Input format        
        if (commandLine.hasOption("if")) {
            inputFormat = InputFormat.valueOf(commandLine.getOptionValue("if"));
        } else {
            printHelp(options, "Please specify the input format.");
            return;
        }
        
        // Get sentences file or folder for input        
        if (commandLine.hasOption('c')) {
            
            fileSentencesIn = commandLine.getOptionValue('c');
            File test = new File(fileSentencesIn);
            
            if (inputFormat.equals(InputFormat.BC2) || inputFormat.equals(InputFormat.SERIALIZED) || inputFormat.equals(InputFormat.JNLPBA)  ) { // File formats or SERIALIZED format            
            
                if (!test.isFile() || !test.canRead()) {
                    logger.error("The specified path is not a file or is not readable.");
                    return;
                }
                
            } else { // Folder formats
                
                if (!test.isDirectory() || !test.canRead()) {
                    logger.error("The specified path is not a folder or is not readable.");
                    return;
                }
                
                // Verify if corpus is not empty
                A1Pairs aiPairs = A1Utils.separateTextAnnotations(test.listFiles());
                if (aiPairs.getAnnotations().length == 0) {
                    String m = "The provided sentences directory does not "
                            + "contain annotations files.";
                    logger.error(m);
                    return;
                }
            }
            
            fileSentencesIn = test.getAbsolutePath();
            fileSentencesIn += File.separator;
            
        } else {
            printHelp(options, "Please specify the senteces file or folder.");
            return;
        }

        // Get annotations file for input
        if (inputFormat.equals(InputFormat.BC2) && commandLine.hasOption('a')) {
            fileAnnotationsIn = commandLine.getOptionValue('a');
            File test = new File(fileAnnotationsIn);
            if (test.isDirectory() || !test.canRead()) {
                logger.error("The specified path is not a file or is not readable.");
                return;
            }
            fileAnnotationsIn = test.getAbsolutePath();
            fileAnnotationsIn += File.separator;
        }         

        // Get model output path (path where model should be saved)
        String modelOutputPath;
        if (commandLine.hasOption("o")) {
            modelOutputPath = commandLine.getOptionValue('o');
            File test = new File(modelOutputPath);
            if (test.isFile() || !test.canRead()) {
                logger.error("The specified path is not a folder or is not readable.");
                return;
            }
            modelOutputPath = test.getAbsolutePath();
            modelOutputPath += File.separator;
        } else {
            printHelp(options, "Please specify the model output path.");
            return;
        }
        
        // Get model name (name for the model)
        String modelName;
        if (commandLine.hasOption('m')) {
            modelName = commandLine.getOptionValue('m');
        } else {
            printHelp(options, "Please specify the model name.");
            return;
        }
        if (modelName.contains(".gz")) modelName = modelName.substring(0, modelName.lastIndexOf(".gz"));
        String modelFolderPath = modelOutputPath + modelName + File.separator;
        
        // Get features
        String featuresFilePath;
        if (commandLine.hasOption('f')) {
            featuresFilePath = commandLine.getOptionValue('f');
            
            File test = new File(featuresFilePath);
            if (test.isDirectory() || !test.canRead()) {
                logger.error("The specified features file path is not a file or is not readable.");
                return;
            }
            featuresFilePath = test.getAbsolutePath();
            featuresFilePath += File.separator;
            
        } else {
            printHelp(options, "Please specify the model configuration file.");
            return;
        }
        
        // Read features file (contains parsing and entity)
        logger.info("Loading config...");
        ModelConfig config = new ModelConfig(featuresFilePath);
        
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
        
        // Get threads
        int numThreads = 1;
        if (commandLine.hasOption('t')) {
            String threadsText = commandLine.getOptionValue('t');
            numThreads = Integer.parseInt(threadsText);
            if (numThreads <= 0 || numThreads > 32) {
                logger.error("Illegal number of threads. Must be between 1 and 32.");
                return;
            }
        }
        
        // Get serialization info (file to save the serialized corpus)
        String serializedFilePath = null;
        if (commandLine.hasOption('s')) {
            serializedFilePath = commandLine.getOptionValue('s');
            
            File test = (new File(serializedFilePath)).getParentFile();
            if (test != null) {
                if (test.isFile() || !test.canRead()) {
                    logger.error("The specified serialize file path is not a folder or is not readable.");
                    return;
                }
                serializedFilePath = test.getAbsolutePath() + File.separator + (new File(serializedFilePath)).getName();
            }
            if (!serializedFilePath.endsWith(".gz")) serializedFilePath += ".gz";
        }

        // Parsing variables
        ParserTool parsingTool = ParserTool.GDEP;
        ParserLanguage parsingLanguage = ParserLanguage.ENGLISH;
        ParserLevel parsingLevel;
        
        // Get parsing
        if (config.isNLP()) parsingLevel = ParserLevel.DEPENDENCY;
        else if (config.isChunk()) parsingLevel = ParserLevel.CHUNKING;
        else if (config.isPos()) parsingLevel = ParserLevel.POS;
        else if (config.isLemma()) parsingLevel = ParserLevel.LEMMATIZATION;
        else parsingLevel = ParserLevel.TOKENIZATION;
                
        if (!inputFormat.equals(InputFormat.SERIALIZED)) {
        
            // -- Phase 1 (First pipeline) --

            // Set output formats
            List<OutputFormat> outputFormats = new ArrayList<>();
            outputFormats.add(OutputFormat.GZCORPUS);

            // Define context configuration
            ContextConfiguration descriptor = null;
            try {
                descriptor = new ContextConfiguration.Builder()
                        .withInputFormat(inputFormat)
                        .withOutputFormats(outputFormats)
                        .withParserTool(parsingTool)
                        .withParserLanguage(parsingLanguage)
                        .withParserLevel(parsingLevel)
                        .trainBuildPhase1();

            } catch (NejiException ex) {
                ex.printStackTrace();
                System.exit(1);
            }

            // Create context
            TrainContext context = new TrainContext(descriptor, config, dictionariesFolder, modelFolderPath, null, 1);

            try {
                BatchExecutor batchExecutor = new TrainBatchExecutor(fileSentencesIn, fileAnnotationsIn, numThreads);
                batchExecutor.run(TrainProcessor.class, context);
            } catch (Exception ex) {
                logger.error("There was a problem running the batch.", ex);
            }

            // -- Phase 2 (Merge corpus) --
            if (inputFormat.equals(InputFormat.A1)) { // Folder formats

                logger.info("Merging corpus...");

                try {
                    Utils.mergeGZCorpus(modelFolderPath, modelFolderPath + File.separator + modelName + ".gz");
                } catch (NejiException ex) {
                    System.out.println("Error: An error ocurred while merging the corpus. " + ex.getMessage());
                }
            }
        }
          
        // -- Phase 3 (Second pipeline) --
        
        // Set serialized corpus path
        String corpusLocationPath = null;
        if (inputFormat.equals(InputFormat.A1)) { // Folder formats
            corpusLocationPath = modelFolderPath + File.separator + modelName + ".gz";
        }
        else if (inputFormat.equals(InputFormat.BC2) || inputFormat.equals(InputFormat.JNLPBA)) { // File formats
            corpusLocationPath = modelFolderPath + File.separator + 
                    FilenameUtils.getBaseName((new File(fileSentencesIn)).getName()) + ".gz";
        }
        else { // Serialized format
            corpusLocationPath = fileSentencesIn;
        }
        
        // Save serialized corpus
        if (serializedFilePath != null)
        {
            logger.info("Saving serialized corpus...");
            File gzCorpusFile = new File(corpusLocationPath);
            File serializeFile = new File(serializedFilePath);
            
            try {            
                Files.copy(gzCorpusFile, serializeFile);
            }
            catch (IOException ex) {
                System.out.println("Error: There was a problem saving the serialized corpus. " + ex.getMessage());
            }
        }
        
        // Set output formats
        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.add(OutputFormat.MODEL);
        
        // Define context configuration
        ContextConfiguration descriptor = null;
        try {
            descriptor = new ContextConfiguration.Builder()
                    .withInputFormat(InputFormat.SERIALIZED)
                    .withOutputFormats(outputFormats)
                    .trainBuildPhase2();

        } catch (NejiException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
        // Create context
        TrainContext context = new TrainContext(descriptor, config, dictionariesFolder, corpusLocationPath,
                modelFolderPath, 2);
        
        try {
            BatchExecutor batchExecutor = new TrainBatchExecutor();
            batchExecutor.run(TrainProcessor.class, context);
        } catch (Exception ex) {
            logger.error("There was a problem running the batch.", ex);
        }
        
        // Delete corpus file
        if (!inputFormat.equals(InputFormat.SERIALIZED)) {
            (new File(corpusLocationPath)).delete();
        }
        
        // Delete tmp file (if it exists)
        (new File("tmp.txt")).delete();
        
    }
}
