/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;

/** 
 *
 * @author jeronimo
 */
public class IntersectMatch {
    
    public static void main(String[] args) throws IOException {
                
        File dir1 = new File("Becalm/cemp/final/a1/cluster3_fps/");
        File dir2 = new File("Becalm/cemp/final/a1/cluster12_fps_intersect/");
        File out = new File("Becalm/cemp/final/a1/cluster123_fps_intersect/");
        out.mkdir();
        FileUtils.cleanDirectory(out);
                 
        for (File file1 : dir1.listFiles()) {
            File file2 = new File(dir2, file1.getName());
            
            // Read annotations and its score
            List<Annotation> anns1 = new ArrayList();
            Annotation ann1 = null;
            
            for (String line : FileUtils.readLines(file1)) {
                if (line.startsWith("T")) {
                    ann1 = Annotation.readFromLine(line); 
                } else if (line.startsWith("#Score")) {
                    ann1.score = Double.parseDouble(line.split(" ", 2)[1]);
                    anns1.add(ann1);
                } else {
                    continue;
                }
            }
            
            List<Annotation> anns2 = new ArrayList();
            Annotation ann2 = null;
            
            for (String line : FileUtils.readLines(file2)) {
                if (line.startsWith("T")) {
                    ann2 = Annotation.readFromLine(line); 
                } else if (line.startsWith("#Score")) {
                    ann2.score = Double.parseDouble(line.split(" ", 2)[1]);
                    anns2.add(ann2);
                } else {
                    continue;
                }
            }

            // Match them            
            Set<Annotation> allAnns = new HashSet();
            allAnns.addAll(anns1);
            allAnns.addAll(anns2);
            Set<Annotation> finalAnns = new HashSet();
                
            for (Annotation a1 : new HashSet<Annotation>(allAnns)) {                
                
                Set<Annotation> intersections = new HashSet();
                
                for (Annotation a2 : new HashSet<Annotation>(allAnns)) { 
                    if (!a1.equals(a2) && intersect(a1, a2)) {
                        intersections.add(a2);
                    }
                }

                if (intersections.size() > 0) {
                    Annotation best = a1;
                    for (Annotation a : intersections) {
                        if (a.score > best.score) {
                            best = a;
                        }
                    }
                    finalAnns.add(best);
                } else {
                    finalAnns.add(a1);
                }
            }
            
            int index = 1;            
            File outFile = new File(out, file1.getName());
            outFile.createNewFile();
            
            for (Annotation a : finalAnns) {
                FileUtils.writeStringToFile(outFile, a.toString(index) + "\n", true);
                FileUtils.writeStringToFile(outFile, "#Score " + a.score + "\n", true);
                index++;
            } 
        }
    }

    public static boolean intersect(Annotation a1, Annotation a2) {
        
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
