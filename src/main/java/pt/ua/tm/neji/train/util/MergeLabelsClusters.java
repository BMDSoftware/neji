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
public class MergeLabelsClusters {
    
    public static void main(String[] args) throws Exception {
        
        File gold = new File("BIO/new/gold/");
        File cluster1 = new File("BIO/new/cluster1/");
        File cluster2 = new File("BIO/new/cluster2/");
        File cluster3 = new File("BIO/new/cluster3/");
        File out = new File("BIO/new/merge/");
        
        for (File file : gold.listFiles()) {
            
            List<String> merge = new ArrayList();
            
            String filename = file.getName();
            
            List<String> g = FileUtils.readLines(file);
            List<String> c1 = FileUtils.readLines(new File(cluster1, filename));
            List<String> c2 = FileUtils.readLines(new File(cluster2, filename));
            List<String> c3 = FileUtils.readLines(new File(cluster3, filename));
            
            for (int i=0 ; i<g.size() ; i++) {
                
                if (g.get(i).trim().length()==0) {
                    merge.add("");
                    continue;
                }
                
                StringBuilder sb = new StringBuilder();
                
                String[] g_parts = g.get(i).split("\t");
                
                
                sb.append(g_parts[0]);
                sb.append("\t");
                sb.append(g_parts[1]);
                sb.append("\t");
                sb.append(g_parts[2]);
                sb.append("\t");
                sb.append(g_parts[3]);
                sb.append("\t");
                sb.append(c1.get(i).split("\t")[3]);
                sb.append("\t");
                sb.append(c2.get(i).split("\t")[3]);
                sb.append("\t");
                sb.append(c3.get(i).split("\t")[3]);
                
                merge.add(sb.toString());
            }
            
            FileUtils.writeLines(new File(out, filename), merge);                        
        }
        
    }
    
}
