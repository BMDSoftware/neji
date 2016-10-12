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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

/**
 *
 * @author jeronimo
 */
public class A1ToPipe {
    
    public static void main (String[] args) throws FileNotFoundException, IOException {
        
        String a1Folder = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/test/a1/";
        String pipeFolder = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/test/gold_pipe/";
        
        File[] a1Files = (new File(a1Folder)).listFiles();
        File pipeFolderFile = new File(pipeFolder);
        
        for (File a1File : a1Files) {
            
            // Read file
            FileInputStream is = new FileInputStream(a1File);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            String fileName = a1File.getName().substring(0, a1File.getName().indexOf("."));
            
            PrintWriter pwt = new PrintWriter(pipeFolderFile.getAbsolutePath() + File.separator + fileName + ".pipe");
            
            String line;

            while ((line = br.readLine()) != null) {
                // Get parts
                String[] parts = line.split("\t");
                String[] parts2 = parts[1].split("\\s+");
                
                String start = parts2[1];
                String end = parts2[2];           

                // Write .pipe
                pwt.println(fileName + ".text" + "||" + "Disease_Disorder" + "||" + "CUI-less" + "||" + start + "||" + end);
            }

            pwt.close();
            br.close();
            is.close();            
        }
    }
    
}
