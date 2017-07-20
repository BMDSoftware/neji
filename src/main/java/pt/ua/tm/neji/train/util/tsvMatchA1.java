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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;

/** 
 *
 * @author jeronimo
 */
public class tsvMatchA1 {
    
    public static void main(String[] args) throws IOException {
        
        File tsvFile = new File("Becalm/cemp/final/tsv/cluster123_base.tsv");
        File a1Dir = new File("Becalm/cemp/final/a1/cluster123_fps_intersect/");
        File outFile = new File("Becalm/FINAL_SUBMISSIONS/cemp/intersect_fps.tsv");
        
        // Read TSV lines
        Map<String, Set<BCAnnotation>> mapTSV = new HashMap();
        
        for (String line1 : FileUtils.readLines(tsvFile)) {
            
            if (line1.startsWith("DOCUMENT_ID")) {
                continue;
            }
            
            BCAnnotation ann = BCAnnotation.readFromLine(line1);
            if (mapTSV.containsKey(ann.id)) {
                mapTSV.get(ann.id).add(ann);
            } else {
                Set<BCAnnotation> set = new HashSet();
                set.add(ann);
                mapTSV.put(ann.id, set);
            }
        }
        
        // Read annotations and its score
        Map<String, Set<Annotation>> mapA1 = new HashMap();
        
        for (File file1 : a1Dir.listFiles()) {
            
            // Read annotations and its score
            Set<Annotation> list = new HashSet();
            Annotation ann1 = null;
            
            for (String line : FileUtils.readLines(file1)) {
                if (line.startsWith("T")) {
                    ann1 = Annotation.readFromLine(line);
                    list.add(ann1);
                } else {
                    continue;
                }
            }
            
            String filename = file1.getName().substring(0, file1.getName().indexOf(".a1"));
            mapA1.put(filename, list);
        }
        
        // Final set
        Set<BCAnnotation> finalTSV = new LinkedHashSet();
        
        for (String doc_id : mapTSV.keySet()) {
        
            Set<BCAnnotation> tsvSet = mapTSV.get(doc_id);
            Set<Annotation> a1Set = mapA1.get(doc_id);
            
            for (BCAnnotation tsv : tsvSet) {
                for (Annotation a1 : a1Set) {
                    if (a1.text.equals(tsv.text)) {
                        finalTSV.add(tsv);
                        break;
                    }
                }
            }
            
        }
        
        outFile.createNewFile();
        FileUtils.writeStringToFile(outFile, 
                "DOCUMENT_ID\tSECTION\tINIT\tEND\tSCORE\tANNOTATED_TEXT\tTYPE\tDATABASE_ID\n");
                  
        for (BCAnnotation a : finalTSV) {
            FileUtils.writeStringToFile(outFile, a.toString() + "\n", true);
        } 
        
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
    
    public static class Annotation {
    
        String type;
        int start;
        int end;
        String text;
        double score;

        public Annotation(String type, int start, int end, String text) {
            this.type = type;
            this.start = start;
            this.end = end;
            this.text = text;
        }
    
        public Annotation(String type, int start, int end, String text, double score) {
            this.type = type;
            this.start = start;
            this.end = end;
            this.text = text;
            this.score = score;
        }
    
        public static Annotation readFromLine(String line) {

            String[] parts = line.split("\t", 3);

            String[] parts2 = parts[1].split(" ", 3);
            String type = parts2[0];
            int start = Integer.parseInt(parts2[1]);
            int end = Integer.parseInt(parts2[2]);
            String text = parts[2];

            return new Annotation(type, start, end, text);
        }
    
        public String toString(int index) {
             return "T" + index + "\t" + type + " " + start + " " + end + "\t" + text;
        }
    
        @Override
        public boolean equals(Object obj) {

            if (obj==null) return false;

            Annotation other = (Annotation) obj;

            if (start == other.start && end == other.end && score == other.score) {
                return true;
            }

            return false;
        }
        
        @Override
        public int hashCode() {
            return start + end + new Double(score).hashCode();
        }
    }
    
}
