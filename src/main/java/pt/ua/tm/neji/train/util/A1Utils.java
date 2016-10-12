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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jeronimo
 */
public class A1Utils {
    
    /**
     * Separates .txt files from .a1 files, storing .txt at files array and .a1 at annotations array.
     * @param files Files to separate, will be used to return text files (.txt)
     * @return a pair with txt files and a1 files
     */
    public static A1Pairs separateTextAnnotations(File[] files) {
        
        List<File> a1Files = new ArrayList<>();
        List<File> txtFiles = new ArrayList<>();
        
        // Separate a1 files from txt files
        for (File a1File : files)
        {
            if (a1File.getName().endsWith(".a1")) {
                File txtFile = new File(a1File.getAbsolutePath().substring(0, a1File.getAbsolutePath().length() - 3) + ".txt");
                if (txtFile.exists()) {
                    a1Files.add(a1File);
                    txtFiles.add(txtFile);
                }
            }
        }
        
        // Return files
        return (new A1Pairs(txtFiles.toArray(new File[txtFiles.size()]), a1Files.toArray(new File[a1Files.size()])));
    }
       
    /**
     * Auxiliary class with all A1 pairs (.txt and .a1).
     */
    public static class A1Pairs {
        
        // Attributes
        private File[] files;
        private File[] annotations;
        
        // Constructor
        public A1Pairs(File[] files, File[] annotions) {
            this.files = files;
            this.annotations = annotions;
        }
        
        public File[] getFiles() {
            return files;
        }
        
        public File[] getAnnotations() {
            return annotations;
        }
    }    
}
