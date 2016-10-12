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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author david
 */
public class DictionaryConvert {
    
    public static void main(String[] args) {
        
//        String input = "/Users/david/Dropbox/PhD/work/platform/code/neji/resources/lexicons/prge/geneProt70_hsa2_PreferredTerms.tsv";
//        String output = "/Users/david/Dropbox/PhD/work/platform/code/neji/resources/lexicons/prge/preferred.tsv";
        
        String input = "/Users/david/Dropbox/PhD/work/platform/code/neji/resources/lexicons/prge/geneProt70_hsa2_PRGE.tsv";
        String output = "/Users/david/Dropbox/PhD/work/platform/code/neji/resources/lexicons/prge/synonyms.tsv";
        
        HashMap<String, List<String>> dict = new HashMap<String, List<String>>();
        
        try {
            
            InputStreamReader isr = new InputStreamReader(new FileInputStream(input));
            BufferedReader br = new BufferedReader(isr);
            String line;
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("[\t]");
                
                String id = parts[0];
                String namesText = parts[1];
                
                if (namesText.equals("") || namesText.equals(" ")) {
                    continue;
                }
                
                String[] names = namesText.split("[|]");

                // Remove white spaces
                for (int i = 0; i < names.length; i++) {
                    names[i] = names[i].replaceAll("\\s+", "");
                }
                
                List<String> tmp;
                
                if (dict.containsKey(id)) {
                    tmp = dict.get(id);
                    
                    for (String name : names) {
                        if (!tmp.contains(name)) {
                            tmp.add(name);
                        }
                    }
                } else {
                    tmp = new ArrayList<String>();
                    
                    for (String name : names) {
                        if (!tmp.contains(name)) {
                            tmp.add(name);
                        }
                    }
                    
                    dict.put(id, tmp);
                }
            }
            isr.close();


            // Output
            FileOutputStream fos = new FileOutputStream(output);
            
            Iterator<String> it = dict.keySet().iterator();
            
            while (it.hasNext()) {
                String id = it.next();
                List<String> tmp = dict.get(id);
                
                if (tmp.isEmpty()) {
                    continue;
                }
                
                StringBuilder sb = new StringBuilder();
                for (String name : tmp) {
                    sb.append(name);
                    sb.append("|");
                }
                sb.setLength(sb.length() - 1);
                
                fos.write(id.getBytes());
                fos.write("\t".getBytes());
                fos.write(sb.toString().getBytes());
                fos.write("\n".getBytes());
            }
            
            fos.close();
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
