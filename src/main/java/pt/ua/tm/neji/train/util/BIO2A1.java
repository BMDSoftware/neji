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
public class BIO2A1 {
    
    public static void main(String[] args) throws IOException {
        
        String dirStr = "Becalm/gpro/bio/maioria_intersect/";
        String out = "Becalm/gpro/a1/maioria_intersect/";
        
        File dir = new File(dirStr);
        
        // Iterate over files
        for (File file : dir.listFiles()) {
            
            List<String> newLines = new ArrayList();
            
            int start = -1;
            int end = -1;
            boolean flag = false;
            String text = "";
            int index = 1;
            
            // Read lines
            for (String line : FileUtils.readLines(file)) {
                if (line.trim().length() == 0) {
                    continue;
                }
                
                // Read line
                Ann ann = readLine(line);
                
                // Process labels
                if (!flag) {
                    if (ann.label.equals("B")) {
                        start = ann.start;
                        end = ann.end;
                        text = ann.text;
                        flag = true;
                    }
                } else {
                    if (ann.label.equals("O")) {
                        flag = false;
                        String ll = "T" + (index++) + "\tPRGE " + start + " " + end + "\t" + text;
                        newLines.add(ll);
                    } else if (ann.label.equals("I")) {
                        end = ann.end;
                        text += " " + ann.text;
                    } else if (ann.label.equals("B")) {
                        String ll = "T" + (index++) + "\tPRGE" + start + " " + end + "\t" + text;
                        newLines.add(ll);
                        
                        start = ann.start;
                        end = ann.end;
                        text = ann.text;
                        flag = true;
                    }
                }                
            }
            
            // Write to file
            FileUtils.writeLines(
                    new File(out, 
                    file.getName().substring(0, file.getName().indexOf(".")) + ".a1"), 
                    newLines);
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
