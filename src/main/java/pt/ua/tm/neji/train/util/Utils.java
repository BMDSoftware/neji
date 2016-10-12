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
package pt.ua.tm.neji.train.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.exception.NejiException;

/**
 *
 * @author jeronimo
 */
public class Utils {
    
    // FileFilter for .gz files
    private static FileFilter gzfilefilter = new FileFilter() {
 
        public boolean accept(File file) {
            
            //if the file extension is .gz return true, else false
            if (file.getName().endsWith(".gz")) {
                return true;
            }
            return false;
        }
    };
    
    /**
     * Merge .gz corpus into one corpus.
     * @param folderPath Folder with serialized corpus
     * @param outputFilePath Output corpus
     */
    public static void mergeGZCorpus(String folderPath, String outputFilePath) throws NejiException {
        
        File folder = new File(folderPath);
        
        // Verify folder
        if (!folder.exists() || !folder.isDirectory() || !folder.canRead()) {
            System.out.println("The folder doesn't exist or is not readable.");
            return;
        }
        
        // Get all .gz files
        File[] files = folder.listFiles(gzfilefilter);
        
        // If any corpus, return
        if (files.length == 0) {
            return;
        }     
        
        // New corpus
        Corpus corpus = null;
        boolean firstCorpus = true;
        
        // Merge corpus
        for (File f : files) {
            
            // Get corpus from file
            Corpus c = deserializeCorpus(f);
            
            // Delete file
            f.delete();
            
            // Verify if it is the first corpus
            if (firstCorpus) {
                corpus = c;
                firstCorpus = false;                
                continue;
            }
            
            // Merge two corpus
            corpus.merge(c);
        }
        
        serializeCorpus(corpus, outputFilePath);
    }
    
    /**
     * Deserialize a corpus.
     * @param file file that contains the corpus
     * @return deserialized corpus
     */
    private static Corpus deserializeCorpus(File file) {
        Corpus corpus;    
        
        try {
            FileInputStream fis = new FileInputStream(file);
            GZIPInputStream gis = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gis);
            corpus = (Corpus) ois.readObject();
            ois.close();
            gis.close();
            fis.close();
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException("There was a problem deserializing the corpus.", ex);
        }
        
        return corpus;
    }
    
    /**
     * Serialize a corpus.
     * @param corpus corpus to serialize
     * @param filePath output path
     */
    private static void serializeCorpus(Corpus corpus, String filePath) {
        
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gos);
            oos.writeObject(corpus);
            oos.close();
            gos.close();
            fos.close();
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem serializing the corpus.", ex);
        }        
    }
    
}
