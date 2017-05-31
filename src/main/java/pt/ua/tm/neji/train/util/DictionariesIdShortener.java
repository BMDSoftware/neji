/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jeronimo
 */
public class DictionariesIdShortener {
    
    public static void main(String[] args) throws IOException {
        
        String dirString = "UMLS_dicts/";
        File dir = new File(dirString);
        String outDirString = "UMLS_dicts_min/";
        File outDir = new File(outDirString);
        
        for (File file : dir.listFiles()) {
        
            List<String> newLines = new ArrayList();
            
            for (String line : FileUtils.readLines(file)) {
                if (line.trim().length() == 0 || !file.getName().endsWith(".tsv")) {
                    continue;
                }
                String[] parts1 = line.split("\t", 2);
                String[] parts2 = parts1[0].split(":");
                
                try {
                    newLines.add(parts2[0] + ":" + parts2[1] + "\t" + parts1[1]);
                } catch (Exception ex) {
                    System.out.println(file.getName());
                }
            }
            
            FileUtils.writeLines(new File(outDir, "UMLS_" + file.getName()), newLines);
        }
        
    }
    
}
