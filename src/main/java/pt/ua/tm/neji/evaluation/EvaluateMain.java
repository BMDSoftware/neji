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

package pt.ua.tm.neji.evaluation;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.train.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Main evaluation class to test performance and precision of the obtained annotations.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class EvaluateMain {

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(EvaluateMain.class);

    private static String mappersFolderPath;

    private static enum TestType {
        all,
        exact,
        left,
        right,
        shared,
        overlap
    }

    /**
     * Print help message of the program.
     *
     * @param mainOptions Command line arguments.
     * @param msg     Message to be displayed.
     */
    private static void printHelp(final Options mainOptions, final String msg) {
        if (msg.length() != 0) {
            logger.error(msg);
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(150, "\t -g <folder> [-gf <wildcard filter>]\n\t-s <folder> [-sf <wildcard filter>] " +
                "\n\t-m <folder>\n\t-c \"<tests>\"\n\t-a \"<tests>\"" +
                "\nUsable tests syntax: "+Arrays.toString(TestType.values()).replaceAll("\\[", "").replaceAll("]", ""),
                "", mainOptions, "");
    }

    public static void main(String... args) {
        boolean performExactChunks = false,
                performLeftChunks = false,
                performRightChunks = false,
                performSharedChunks = false,
                performOverlapChunks = false,
                performExactAnn = false,
                performLeftAnn = false,
                performRightAnn = false,
                performSharedAnn = false,
                performOverlapAnn = false;

        CommandLineParser parser = new GnuParser();
        Options mainOptions = new Options();
        mainOptions.addOption("h", "help", false, "Print this usage information.");

        mainOptions.addOption("g", "gold", true, "Folder with gold files.");
        mainOptions.addOption("s", "silver", true, "Folder with silver files.");
        mainOptions.addOption("gf", "gold-filter", true, "Wildcard to filter files in gold folder");
        mainOptions.addOption("sf", "silver-filter", true, "Wildcard to filter files in silver folder");
        mainOptions.addOption("m", "mappers", true, "Folder with mapper files.");

        int numOfAvailableTests = TestType.values().length;
        Option o = new Option("tnorm", "test-normalization", true, "Set identifiers evaluations.");
        o.setArgs(numOfAvailableTests);
        mainOptions.addOption(o);
        o = new Option("tner", "test-ner", true, "Set chunk evaluations.");
        o.setArgs(numOfAvailableTests);
        mainOptions.addOption(o);

        CommandLine commandLine;
        try {
            // Parse the program arguments
            commandLine = parser.parse(mainOptions, args);
        } catch (ParseException ex) {
            logger.error("There was a problem processing the input arguments.", ex);
            return;
        }
        
        // Show help text
        if (commandLine.hasOption('h') || commandLine.getOptions().length == 0) {
            printHelp(mainOptions, "");
            return;
        }

        // Get gold folder
        String goldFolderPath;
        if (commandLine.hasOption('g')) {
            goldFolderPath = commandLine.getOptionValue('g');
        } else {
            printHelp(mainOptions, "Please specify the gold folder.");
            return;
        }
        
        File goldFolder = new File(goldFolderPath);
        if (!goldFolder.isDirectory() || !goldFolder.canRead()) {
            logger.error("The specified path is not a folder or is not readable.");
            return;
        }
        
        // Get gold folder filter
        String goldFolderWildcard = null;
        if (commandLine.hasOption("gf")) {
            goldFolderWildcard = commandLine.getOptionValue("gf");
        }

        // Get silver folder
        String silverFolderPath;
        if (commandLine.hasOption('s')) {
            silverFolderPath = commandLine.getOptionValue('s');
        } else {
            printHelp(mainOptions, "Please specify the silver folder.");
            return;
        }
        
        File silverFolder = new File(silverFolderPath);
        if (!silverFolder.isDirectory() || !silverFolder.canRead()) {
            logger.error("The specified path is not a folder or is not readable.");
            return;
        }
        
        // Get silver folder filter
        String silverFolderWildcard = null;
        if (commandLine.hasOption("sf")) {
            silverFolderWildcard = commandLine.getOptionValue("sf");
        }

        // Get mappers folder
        mappersFolderPath = null;
        if (commandLine.hasOption('m')) {
            mappersFolderPath = commandLine.getOptionValue('m');
            File mappersFolder = new File(mappersFolderPath);
            if (!mappersFolder.isDirectory() || !mappersFolder.canRead()) {
                logger.error("The specified path is not a folder or is not readable.");
                return;
            }
            mappersFolderPath = mappersFolder.getAbsolutePath() + File.separator;
        }

        // Get what evaluation tests to perform
        if (commandLine.hasOption("tnorm")) {
            for(String arg : commandLine.getOptionValues("tnorm")){
                try{
                    TestType type = TestType.valueOf(arg.toLowerCase());
                    switch (type){
                        case all:
                            performExactAnn = true;
                            performLeftAnn = true;
                            performRightAnn = true;
                            performSharedAnn = true;
                            performOverlapAnn = true;
                            break;
                        case exact:
                            performExactAnn = true;
                            break;
                        case left:
                            performLeftAnn = true;
                            break;
                        case right:
                            performRightAnn = true;
                            break;
                        case shared:
                            performSharedAnn = true;
                            break;
                        case overlap:
                            performOverlapAnn = true;
                            break;
                    }
                }catch (Exception ex){
                    logger.error("Invalid test name \""+arg+"\" at \"-tnorm\" command.");
                    return;
                }
            }
        }

        if (commandLine.hasOption("tner")) {
            for(String arg : commandLine.getOptionValues("tner")){
                try{
                    TestType type = TestType.valueOf(arg.toLowerCase());
                    switch (type){
                        case all:
                            performExactChunks = true;
                            performLeftChunks = true;
                            performRightChunks = true;
                            performSharedChunks = true;
                            performOverlapChunks = true;
                            break;
                        case exact:
                            performExactChunks = true;
                            break;
                        case left:
                            performLeftChunks = true;
                            break;
                        case right:
                            performRightChunks = true;
                            break;
                        case shared:
                            performSharedChunks = true;
                            break;
                        case overlap:
                            performOverlapChunks = true;
                            break;
                    }
                }catch (Exception ex){
                    logger.error("Invalid test name \""+arg+"\" at \"-tner\" command.");
                    return;
                }
            }
        }
        
        if(!performExactChunks && !performLeftChunks && !performRightChunks && !performSharedChunks && !performOverlapChunks &&
                !performExactAnn && !performLeftAnn && !performRightAnn && !performSharedAnn && !performOverlapAnn){
            logger.error("Please specify at least one evaluation test to perform.");
            return;
        }

//        String goldFolderPath = "resources/corpus/bionlp2011/dev/";
//        String silverFolderPath = "resources/corpus/bionlp2011/dev/silver/dictionaries/";
//        String silverFolderPath = "resources/corpus/bionlp2011/dev/silver/ml/";

        // CRAFT
//        String goldFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/craft/gold/";
//        String silverFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/craft/silver/";

//        args = new String[]{"resources/corpus/craft2/silver/","resources/corpus/craft2/silver/"};
//        args = new String[]{"resources/corpus/craft2/gold/","resources/corpus/craft2/silver/"};
//        args = new String[]{"resources/corpus/craft2/gold/","resources/temp/whatizit/craft/silver/a1/"};

        // CRAFT2
//        String goldFolderPath = args[0];
//        String silverFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/craft2/silver/";
//        String silverFolderPath = "/Volumes/data/Backups/2013-04-11_desktop/Downloads/whatizit/craft/ukpmc/a1/";
//        String silverFolderPath = args[1];
        //String silverFolderPath = "resources/temp/whatizit/craft/silver/a1/";
//        String silverFolderPath = "/Users/david/Downloads/whatizit/craft/ukpmc/a1";
//        String silverFolderPath = "/Users/david/Downloads/Craft1.0Annotations/";

        // ANEM COCOA
//        String goldFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/anem/test/gold/";
//        String silverFolderPath = "/Users/david/Downloads/anem_craft/test/";


        // MLEE
//        String goldFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/mlee/gold/";
//        String silverFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/mlee/silver/";

        // CellFinder
//        String goldFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/cellfinder/gold/";
//        String silverFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/cellfinder/silver/";

        // Arizona
//        String goldFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/arizona/gold/";
//        String silverFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/arizona/silver/";

        // NCBI Disease
//        String silverFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/ncbi/silver/";
//        String goldFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/ncbi/gold/";
//        String silverFolderPath = "/Users/david/Downloads/whatizit/ncbi/whatizitDiseaseUMLSDict/a1/";

        // ANEM
//        String goldFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/anem/test/gold/";
//        String silverFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/anem/test/silver/";

        // SCAI
//        String goldFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/scai/test/gold/";
//        String silverFolderPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/scai/test/silver/";

        // Get a lists of gold and silver files
        File[] goldFiles;
        if(goldFolderWildcard==null)
            goldFiles = goldFolder.listFiles();
        else
            goldFiles = goldFolder.listFiles(new FileUtil.Filter(new String[]{goldFolderWildcard}));

        File[] silverFiles;
        if(silverFolderWildcard==null)
            silverFiles = silverFolder.listFiles();
        else
            silverFiles = silverFolder.listFiles(new FileUtil.Filter(new String[]{silverFolderWildcard}));

        List<File> goldFilesList = Arrays.asList(goldFiles);
        List<File> goldFilesList2 = new ArrayList<>();
        for(File f : goldFilesList){
            if(!f.isDirectory())
                goldFilesList2.add(f);
        }
        Collections.sort(goldFilesList2);
        goldFiles = goldFilesList2.toArray(new File[goldFilesList2.size()]);
        
        List<File> silverFilesList = Arrays.asList(silverFiles);
        List<File> silverFilesList2 = new ArrayList<>();
        for(File f : silverFilesList){
            if(!f.isDirectory())
                silverFilesList2.add(f);
        }
        Collections.sort(silverFilesList2);
        silverFiles = silverFilesList2.toArray(new File[silverFilesList2.size()]);

        if (goldFiles.length != silverFiles.length) {
            throw new RuntimeException("Folders are not compatible (the number of files is not equal).");
        }

        // Perform tests
        if(performExactChunks || performExactAnn){
            logger.info("#####################");
            logger.info("EXACT");
            logger.info("#####################");
        }
        if(performExactChunks){
            logger.info("\tIdentifier matching: NONE");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Exact, IdentifierMatch.NONE);
        }
        if(performExactAnn){
            logger.info("\tIdentifier matching: EXACT");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Exact, IdentifierMatch.EXACT);
            logger.info("\tIdentifier matching: CONTAINS");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Exact, IdentifierMatch.CONTAIN);
        }

        if(performLeftChunks || performLeftAnn){
            logger.info("#####################");
            logger.info("LEFT");
            logger.info("#####################");
        }
        if(performLeftChunks){
            logger.info("\tIdentifier matching: NONE");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Left, IdentifierMatch.NONE);
        }
        if(performLeftAnn){
            logger.info("\tIdentifier matching: EXACT");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Left, IdentifierMatch.EXACT);
            logger.info("\tIdentifier matching: CONTAINS");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Left, IdentifierMatch.CONTAIN);
        }

        if(performRightChunks || performRightAnn){
            logger.info("#####################");
            logger.info("RIGHT");
            logger.info("#####################");
        }
        if(performRightChunks){
            logger.info("\tIdentifier matching: NONE");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Right, IdentifierMatch.NONE);
        }
        if(performRightAnn){
            logger.info("\tIdentifier matching: EXACT");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Right, IdentifierMatch.EXACT);
            logger.info("\tIdentifier matching: CONTAINS");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Right, IdentifierMatch.CONTAIN);
        }

        if(performSharedChunks || performSharedAnn){
            logger.info("#####################");
            logger.info("SHARED");
            logger.info("#####################");
        }
        if(performSharedChunks){
            logger.info("\tIdentifier matching: NONE");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Shared, IdentifierMatch.NONE);
        }
        if(performSharedAnn){
            logger.info("\tIdentifier matching: EXACT");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Shared, IdentifierMatch.EXACT);
            logger.info("\tIdentifier matching: CONTAINS");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Shared, IdentifierMatch.CONTAIN);
        }

        if(performOverlapChunks || performOverlapAnn){
            logger.info("#####################");
            logger.info("OVERLAP");
            logger.info("#####################");
        }
        if(performOverlapChunks){
            logger.info("\tIdentifier matching: NONE");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Overlap, IdentifierMatch.NONE);
        }
        if(performOverlapAnn){
            logger.info("\tIdentifier matching: EXACT");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Overlap, IdentifierMatch.EXACT);
            logger.info("\tIdentifier matching: CONTAINS");
            getCompleteEvaluator(goldFiles, silverFiles, CompleteEvaluator.EvaluationType.Overlap, IdentifierMatch.CONTAIN);
        }

        /*
        logger.info("Num. UNIPROTS: {}", CompleteEvaluator.numUNIPROTS);
        logger.info("Num. Uniprots mapped: {}", CompleteEvaluator.numUNIPROTSmapped);
        logger.info("Ratio: {}%", ((double)CompleteEvaluator.numUNIPROTSmapped/(double)CompleteEvaluator.numUNIPROTS)*100);
        logger.info("");
        logger.info("Num. PRGE concept names: {}", CompleteEvaluator.numPRGEnames);
        logger.info("Num. PRGE mapped: {}", CompleteEvaluator.numPRGEmapped);
        logger.info("Ratio: {}%", ((double)CompleteEvaluator.numPRGEmapped/(double)CompleteEvaluator.numPRGEnames)*100);

        logger.info("");
        logger.info("");
        logger.info("Num. Cell UMLS IDs: {}", CompleteEvaluator.numUMLSCL);
        logger.info("Num. Cell UMLS IDs mapped: {}", CompleteEvaluator.numUMLSCLmapped);
        logger.info("Ratio: {}%", ((double)CompleteEvaluator.numUMLSCLmapped/(double)CompleteEvaluator.numUMLSCL)*100);
        logger.info("");
        logger.info("Num. Cell concept names: {}", CompleteEvaluator.numCellNames);
        logger.info("Num. Cell concept names mapped: {}", CompleteEvaluator.numCellMapped);
        logger.info("Ratio: {}%", ((double)CompleteEvaluator.numCellMapped/(double)CompleteEvaluator.numCellNames)*100);

        logger.info("");
        logger.info("");
        logger.info("Total PROC_FUNC: {}", CompleteEvaluator.numPROCFUNC);
        logger.info("Total PROC_FUNC with IDs: {}", CompleteEvaluator.numPROCFUNCwithIDs);
        logger.info("Ratio: {}%", ((double)CompleteEvaluator.numPROCFUNCwithIDs/(double)CompleteEvaluator.numPROCFUNC)*100);
        logger.info("");
        logger.info("Num. PROC_FUNC UMLS IDs: {}", CompleteEvaluator.numUMLSPROCFUNC);
        logger.info("Num. PROC_FUNC UMLS IDs mapped: {}", CompleteEvaluator.numUMLSPROCFUNCmapped);
        logger.info("Ratio: {}%", ((double)CompleteEvaluator.numUMLSPROCFUNCmapped/(double)CompleteEvaluator.numUMLSPROCFUNC)*100);
        logger.info("");
        logger.info("Num. Cell concept names: {}", CompleteEvaluator.numPROCFUNCNames);
        logger.info("Num. Cell concept names mapped: {}", CompleteEvaluator.numPROCFUNCMapped);
        logger.info("Ratio: {}%", ((double)CompleteEvaluator.numPROCFUNCMapped/(double)CompleteEvaluator.numPROCFUNCNames)*100);
        */
    }

    private static CompleteEvaluator getCompleteEvaluator(File[] goldFiles, File[] silverFiles,
                                                          CompleteEvaluator.EvaluationType evaluationType, IdentifierMatch identifierMatch) {
        CompleteEvaluator evaluator = new CompleteEvaluator(mappersFolderPath);
        for (int i = 0; i < goldFiles.length; i++) {
            File goldFile = goldFiles[i];
            File silverFile = silverFiles[i];
            try (
                    FileInputStream goldFIS = new FileInputStream(goldFile);
                    FileInputStream silverFIS = new FileInputStream(silverFile)
            ) {
                evaluator.evaluate(goldFIS, silverFIS, evaluationType, identifierMatch);
            } catch (IOException ex) {
                throw new RuntimeException("There was a problem reading the files.", ex);
            }
        }

//        evaluator.print();
        evaluator.printToExcel();
        logger.info("");
//        evaluator.printEvaluationPerDocument("PRGE");
//        evaluator.printFNs();

//        evaluator.print();
//        evaluator.printFNs();
//        evaluator.printFPs();
        return evaluator;
    }

}
