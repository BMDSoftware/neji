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
public class BIO2X {
    
    public static void main(String[] args) throws IOException {
        
        String dirStr = "BIO/gpro/annotations/bio/cluster3_pos/";
        String out = "BIO/gpro/annotations/bio/cluster3_pos_x/";
        
        File dir = new File(dirStr);
        
        // Iterate over files
        for (File file : dir.listFiles()) {
            
            List<String> newLines = new ArrayList();
            
            // Read lines
            for (String line : FileUtils.readLines(file)) {
                if (line.trim().length() == 0) {
                    newLines.add("");
                    continue;
                }
                
                // Read line
                Ann ann = readLine(line);
                
                // Process labels
                if (ann.label.equals("B") || ann.label.equals("I")) {
                    ann.label = "X";
                }
                
                newLines.add(ann.toString());
            }
            
            // Write to file
            FileUtils.writeLines(new File(out, file.getName()), newLines);
        }
        
    }
    
    public static Ann readLine(String line) {
        
        String[] parts = line.split("\t");
        
        return new Ann(parts[0], Integer.parseInt(parts[1]), 
                Integer.parseInt(parts[2]), parts[3]);
    }
    
    private static class Ann {        
        String text;
        int start;
        int end;
        String label;

        public Ann(String text, int start, int end, String label) {
            this.text = text;
            this.start = start;
            this.end = end;
            this.label = label;
        }
        
        @Override
        public String toString() {
            return text + "\t" + start + "\t" + end + "\t" + label;
        }
    }
}
