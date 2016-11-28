/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jeronimo
 */
public class PdfTextComparator {
    
    public static void main(String[] args) throws IOException {
        
        // Files directories
        File originalDir = new File("/home/jeronimo/Workspace/neji/extras/craft/txt_texts");
        File revisedDir = new File("/home/jeronimo/Workspace/neji/extras/craft/pdf_texts");
        
        // List of file original names
        String[] originalNames = originalDir.list();
        
        // Average
        double average = 0;
        
        for (String fileName : originalNames) {
            
            File originalFile = new File(originalDir, fileName);
            File revisedFile = new File(revisedDir, fileName);
            
            average += compare(originalFile, revisedFile);
        }
        
        average /= originalNames.length;
        
        System.out.println("\n-- Average: " + average);
        
        /*File originalDir = new File("/home/jeronimo/Workspace/neji/extras/txt_texts/11532192.txt");
        File revisedDir = new File("/home/jeronimo/Workspace/neji/extras/pdf_texts/11532192.txt");
        
        compare(originalDir, revisedDir);*/
    }
    
    private static double compare(File original, File revised) throws IOException {
        
        int contEqual = 0;
        int contTotal = 0;
        List<String> originalLines2match = new ArrayList();
        
        // Get lines from files
        List<String> originalLines = FileUtils.readLines(original);
        List<String> revisedLines = FileUtils.readLines(revised);
        
        // Get text from revised file (lower case, remove spaces, tabs and change lines)
        String revisedText = FileUtils.readFileToString(revised).toLowerCase();
                
        // Remove hifens
        revisedText = revisedText.replaceAll("[^a-zA-Z0-9,.]", "");
                
        // Iterate over original lines
        for (String line : originalLines) {
            if (line.trim().length() == 0) continue;
            contTotal++;
            
            // To lower case and remove spaces, tabs and change lines
            String oline = line.toLowerCase();
            
            // Remove duvidous characters {-:()\/}
            oline = oline.replaceAll("[^a-zA-Z0-9,.]", "");
            
            // Verification 1 - Line in text
            if (revisedText.contains(oline)) {
                contEqual++;
            } else {                
                // Save lines with no match
                originalLines2match.add(oline);
            }            
        }
              
        // Verification 2 - Match in two parts
        List<String> originalLines3match = new ArrayList();
        
        for (String oline : originalLines2match) {
            
            String firstPart = "";
            int i;
            
            // Extract first part
            for (i=0 ; i<oline.length() ; i++) {
                
                firstPart += oline.charAt(i);
                
                if (!revisedText.contains(firstPart)) {
                    break;
                }                
            }
            
            // Verify sencond part
            String secondPart = oline.substring(i);
            if (revisedText.contains(secondPart)) {
                contEqual++;
            } else {
                // Save lines with no match
                originalLines3match.add(oline);
            }
        }        
        
        // Verification 3 - Levenshetein distance
        for (String oline : originalLines3match) {
            
            boolean found = false;
            for (String line : revisedLines) {
                
                String rline = line.toLowerCase().replaceAll("[^a-zA-Z0-9,.]", "");
                
                // Calculate Levenshtein distance
                int distance = StringUtils.getLevenshteinDistance(oline, rline);
                
                int totalLenght;
                if (oline.length() > rline.length()) {
                    totalLenght = oline.length();
                } else {
                    totalLenght = rline.length();
                }
                
                double diff = ((double) distance / totalLenght) * 100;
                
                if (diff < 10) {
                    contEqual++;
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                //System.out.println(oline);
            }
        }
        
        // Calculate rate
        double rate = ((double) contEqual / contTotal) * 100;
        
        System.out.println("Approximate:");
        System.out.println("\tTotal: " + contTotal);
        System.out.println("\tEqual: " + contEqual);
        System.out.println("\t\tRate: " + rate);
        
        return rate;
    }
    
}
