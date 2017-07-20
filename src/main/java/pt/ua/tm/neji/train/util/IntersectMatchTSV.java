/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;

/** 
 *
 * @author jeronimo
 */
public class IntersectMatchTSV {
    
    public static void main(String[] args) throws IOException {
        
        File file1 = new File("Becalm/cemp/final/tsv/cluster3_fps.tsv");
        File file2 = new File("Becalm/cemp/final/tsv/cluster3_fps.tsv");
        File outFile = new File("Becalm/FINAL_SUBMISSIONS/cemp/cluster3_fps_intersect.tsv");
        
        Map<String, Set<BCAnnotation>> map1 = new HashMap();
        Map<String, Set<BCAnnotation>> map2 = new HashMap();
        
        for (String line1 : FileUtils.readLines(file1)) {
            
            if (line1.startsWith("DOCUMENT_ID")) {
                continue;
            }
            
            BCAnnotation ann = BCAnnotation.readFromLine(line1);
            if (map1.containsKey(ann.id)) {
                map1.get(ann.id).add(ann);
            } else {
                Set<BCAnnotation> set = new HashSet();
                set.add(ann);
                map1.put(ann.id, set);
            }
        }
        
        for (String line2 : FileUtils.readLines(file2)) {
            
            if (line2.startsWith("DOCUMENT_ID")) {
                continue;
            }
            
            BCAnnotation ann = BCAnnotation.readFromLine(line2);
            if (map2.containsKey(ann.id)) {
                map2.get(ann.id).add(ann);
            } else {
                Set<BCAnnotation> set = new HashSet();
                set.add(ann);
                map2.put(ann.id, set);
            }
        }
        
        outFile.createNewFile();
        FileUtils.writeStringToFile(outFile, 
                "DOCUMENT_ID\tSECTION\tINIT\tEND\tSCORE\tANNOTATED_TEXT\tTYPE\tDATABASE_ID\n");
        
        for (String doc_id : map1.keySet()) {
            Set<BCAnnotation> set1 = map1.get(doc_id);
            Set<BCAnnotation> set2 = map2.get(doc_id);
            
            // Match them            
            Set<BCAnnotation> allAnns = new HashSet();
            allAnns.addAll(set1);
            if (set2 != null) {
                allAnns.addAll(set2);
            }
            Set<BCAnnotation> finalAnns = new HashSet();
                
            for (BCAnnotation a1 : new HashSet<BCAnnotation>(allAnns)) {                
                
                Set<BCAnnotation> intersections = new HashSet();
                
                for (BCAnnotation a2 : new HashSet<BCAnnotation>(allAnns)) { 
                    if (intersect(a1, a2)) {
                        if (!intersections.contains(a2)) {
                            intersections.add(a2);
                        }
                    }
                }

                if (intersections.size() > 0) {
                    BCAnnotation best = a1;
                    for (BCAnnotation a : intersections) {
                        if (a.score > best.score) {
                            best = a;
                        }
                    }
                    finalAnns.add(best);
                } else {
                    finalAnns.add(a1);
                }
            }
            
            for (BCAnnotation a : finalAnns) {
                FileUtils.writeStringToFile(outFile, a.toString() + "\n", true);
            } 
        }
    }

    public static boolean intersect(BCAnnotation a1, BCAnnotation a2) {
        
        int init1 = a1.start;
        int end1 = a1.end;
        
        int init2 = a2.start;
        int end2 = a2.end;
        
        if (a1.equals(a2) ||
                (init1 <= init2 && end1 >= end2) || // Nested 1
                (init2 <= init1 && end2 >= end1) || // Nested 2
                (init1 < init2 && end1 > init2) || // Intersection
                (init2 < init1 && end2 > init1) ||
                (init1 < end2 && end1 > end2) ||
                (init2 < end1 && end2 > end1) ) { 
            return true;
        }

        return false;
    }

    private static class BCAnnotation {

        String id;
        String type;
        int start;
        int end;
        String text;
        double score;
        String line;

        public BCAnnotation(String id, String type, int start, int end, 
                String text, double score, String line) {
            this.id = id;
            this.type = type;
            this.start = start;
            this.end = end;
            this.text = text;
            this.score = score;
            this.line = line;
        }

        public static BCAnnotation readFromLine(String line) {

            String[] parts = line.split("\t");

            String id = parts[0];
            String type = parts[1];
            int start = Integer.parseInt(parts[2]);
            int end = Integer.parseInt(parts[3]);
            double score = Double.parseDouble(parts[4]);
            String text = parts[5];

            return new BCAnnotation(id, type, start, end, text, score, line);
        }

        @Override
        public String toString() {
             return line;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj==null) return false;

            BCAnnotation other = (BCAnnotation) obj;

            if (id.equals(other.id) &&
                    type.equals(other.type) &&
                    start == other.start && 
                    end == other.end) {
                return true;
            }

            return false;
        }

        @Override
        public int hashCode() {
          return id.hashCode() + type.hashCode() + start + end;
      }
        
    }
    
}
