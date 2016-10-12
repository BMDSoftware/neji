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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 *
 * @author jeronimo
 */
public class ChemnerToBC2 {
    
    public static void main (String[] args) throws IOException {
        
        //String filePath = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/chemner_data/CHEMDNER_TRAIN_V01/chemdner_ann_training_13-07-31.txt";
        //String destinationPath = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/train/chemdner_ann_training_13-07-31";
        
        String filePath = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/train/training_annotations.txt";
        String destinationPath = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/train/bc2/training_annotations";
        
        // Verify if file exists
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            System.out.println("File doesn't exist or can't be read.");
            System.exit(0);
        }
        
        // Read file
        FileInputStream is = new FileInputStream(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        
        // Convert file to BC2
        PrintWriter pwt = new PrintWriter(destinationPath);
        String line;
        
        while ((line = br.readLine()) != null) {
            
            String[] parts = line.split("\t");
            
            String id = parts[0];
            String start = parts[2];
            String end = parts[3];            
            String text = parts[4];
            
            pwt.println(id + "|" + start + " " + end + "|" + text);
        }
        
        pwt.close();
        br.close();
        is.close();
    }
    
}
