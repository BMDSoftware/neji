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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import monq.jfa.DfaRun;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.config.ModelConfig;
import pt.ua.tm.neji.train.model.CRFModel;

/**
 *
 * @author jeronimo
 */
@Requires({Resource.Model})
public class ModelWriter extends BaseWriter {
    
    private static Logger logger = LoggerFactory.getLogger(ModelWriter.class);
    
    // Attributes
    private String dictionariesPath;
    private String mainPath;
    private List<String> entity;
    
    /**
     * Default constructor.
     * @throws NejiException
     */
    public ModelWriter() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToRegex(text_action, ".+");
        this.dictionariesPath = null;
    }
    
    /**
     * Constructor.
     * @param dictionariesPath path to the folder where are the dictionaries 
     * @param mainPath main path to write the model
     * @param entity model entity
     * @throws NejiException 
     */
    public ModelWriter(String dictionariesPath, String mainPath, List<String> entity) throws NejiException {
        this();
        this.dictionariesPath = dictionariesPath;
        this.mainPath = mainPath;
        this.entity = entity;
    }
    
    private DefaultAction text_action = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            
            // Get the crf model
            CRFModel model = (CRFModel) getPipeline().getModuleData("TRAINED_MODEL").get(0);
            
            // Get model name
            String modelName = (new File(mainPath)).getName();
            
            // Set main directory to save model data
            String mainDir = mainPath;
            String prgeDir = mainDir + File.separator + "model";
            
            // Create the needed folders
            createDirectories(mainDir, prgeDir);
            
            // Write the model
            try {
                model.write(new GZIPOutputStream(new FileOutputStream(prgeDir + File.separator + modelName + ".gz")));
            } catch (NejiException | IOException ex) {
                throw new RuntimeException("There was a problem writing the model.", ex);
            }
            
            // Create config file
            createConfigFile(prgeDir, model.getConfig(), modelName); 
            
            // Create prge file
            createPrgeFile(prgeDir, model.getParsing().name(), modelName, dictionariesPath);
            
            // Create prioriry file
            createPriorityFile(mainDir);
            
            // Create dictionaries folder and files (if needed)
            if (dictionariesPath != null) {
                createDictionariesFolder(prgeDir ,dictionariesPath);
            }
            
            // Write features at the output file required by the pipeline, To Change Probably
            yytext.replace(0, yytext.length(), model.getConfig().toString());
        }
    };
    
    @Override
    public OutputFormat getFormat() {
        return OutputFormat.MODEL;
    }
    
    /**
     * Creates the needed directories for the model.
     * @param path Path to the longest directory, in order to create all directories
     */
    private void createDirectories(String dirPath, String dirPath2) {
        
        // Create dir 1
        File dir = new File(dirPath);        
        dir.mkdir();
        
        // Clean dir
        try {
            FileUtils.cleanDirectory(dir);
        } catch(IOException ex) {
            throw new RuntimeException("There was a problem deleting the contents of a directory.", ex);
        }
        
        // Create dir 2
        dir = new File(dirPath2);
        dir.mkdir();
    }
    
    /**
     * Creates the priority file.
     * @param dir Path to directory where file will be created
     */
    private void createPriorityFile(String dir) {
        String priorityFilePath = dir + File.separator + "_priority";
        
        try {
            PrintWriter pwt = new PrintWriter(priorityFilePath);
            pwt.println("model" + File.separator + "model.properties");
            pwt.close();
        } catch(FileNotFoundException ex) {
            throw new RuntimeException("There was a problem writing the priority file.", ex);
        }
    }
    
    /**
     * Creates the configuration file.
     * @param dir Path to directory where file will be created
     * @param config Configuration of the model
     * @param modelName name of the model
     */
    private void createConfigFile(String dir, ModelConfig config, String modelName) {
        String configFilePath = dir + File.separator + modelName + ".config";
        
        try {
            FileOutputStream os = new FileOutputStream(configFilePath);
            config.write(os);
        } catch(FileNotFoundException ex) {
            throw new RuntimeException("There was a problem writing the priority file.", ex);
        }
    }
    
    /**
     * Creates the prge properties file.
     * @param dir Path to directory where file will be created
     * @param parsingDirection parsing direction
     * @param modelName name of the model
     */
    private void createPrgeFile(String dir, String parsingDirection, String modelName, String directoriesPath) {
        String prgeFilePath = dir + File.separator + "model.properties";
        
        try {
            PrintWriter pwt = new PrintWriter(prgeFilePath);
            pwt.println("file=" + modelName + ".gz");
            pwt.println("config=" + modelName + ".config");
            pwt.println("parsing=" + parsingDirection);
            pwt.print("group=");
            if (entity != null) {
                pwt.print(entity.get(0).toUpperCase());
                for (int i=1 ; i<entity.size() ; i++) {
                    pwt.print("," + entity.get(i).toUpperCase());
                }
                pwt.println();
            } else {
                pwt.println("NOCLASS");
            }
            if (directoriesPath != null) pwt.println("dictionaries=normalization" + File.separator);
            pwt.close();
        } catch(FileNotFoundException ex) {
            throw new RuntimeException("There was a problem writing the priority file.", ex);
        }
    }
    
    /**
     * Creates the dictionaries folder and files
     * @param upDir directory above the dictionaries folder
     * @param dictionariesPath path to the dictionaries folder
     */
    private void createDictionariesFolder(String upDir, String dictionariesPath) {
        String dictionariesDir = upDir + File.separator + "normalization";
        
        // Create dir
        File dir = new File(dictionariesDir);
        dir.mkdir();
        
        // Copy dictionaries and _priority files to the model
        try {
            FileUtils.copyDirectory(new File(dictionariesPath), dir);
        } catch(IOException ex) {
            throw new RuntimeException("There was a problem writing the priority file.", ex);
        }
    }
}
