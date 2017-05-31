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
public class MaioriaOneMatch {
    
    public static void main(String[] args) throws IOException {
        
        String cluster1Str = "BIO/gpro/annotations/bio/cluster1_x/";
        String cluster2Str = "BIO/gpro/annotations/bio/cluster2_x/";
        String cluster3Str = "BIO/gpro/annotations/bio/cluster3_x/";
        String out = "BIO/gpro/annotations/bio/maioria_one_match_x/";
        
        File cluster1 = new File(cluster1Str);
        File cluster2 = new File(cluster2Str);
        File cluster3 = new File(cluster3Str);
        
        // Iterate over files
        for (File file : cluster1.listFiles()) {
            
            List<String> newLines = new ArrayList();
            
            // Read lines
            List<String> lines1 = FileUtils.readLines(file);
            List<String> lines2 = FileUtils.readLines(new File(cluster2, file.getName()));
            List<String> lines3 = FileUtils.readLines(new File(cluster3, file.getName()));
            
            // Process lines
            for (int i=0 ; i<lines1.size() ; i++) {                            
                
                if (lines1.get(i).trim().length() == 0) {
                    newLines.add("");
                    continue;
                }
                
                // Read line
                Ann ann1 = readLine(lines1.get(i));
                Ann ann2 = readLine(lines2.get(i));
                Ann ann3 = readLine(lines3.get(i));
                
                // Process labels
                int count = 0;
                if (ann1.label.equals("X")) count++;
                if (ann2.label.equals("X")) count++;
                if (ann3.label.equals("X")) count++;
                
                if (count >= 1) {
                    ann1.label = "X";
                } else {
                    ann1.label = "O";
                }
                
                newLines.add(ann1.toString());
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
