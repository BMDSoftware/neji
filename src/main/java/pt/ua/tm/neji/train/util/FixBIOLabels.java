/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jeronimo
 */
public class FixBIOLabels {

    public static void main(String[] args) throws Exception {

        String mainDirStr = "Becalm/cemp/final/bio/base/";
        String a1DirStr = "Becalm/cemp/final/a1/cluster3/";
        String outDirStr = "Becalm/cemp/final/bio/cluster3/";

        File mainDir = new File(mainDirStr);
        File a1Dir = new File(a1DirStr);
        File outDir = new File(outDirStr);
        outDir.mkdir();
        FileUtils.cleanDirectory(outDir);

        for (File file : mainDir.listFiles()) {

            String filename = file.getName().substring(0, file.getName().indexOf("."));
            File a1File = new File(a1Dir,
                    filename + ".a1");

            // Get annotations
            List<Token> tokens = getTokens(file);
            List<Ann> anns = getAnnotations(a1File);
            Collections.sort(anns, new Comparator<Ann>() {
                public int compare(Ann a1, Ann a2) {
                    return Integer.compare(a1.start, a2.start);
                }
            });

            if (!anns.isEmpty()) {

                String tag = "O";
                Iterator<Ann> it = anns.iterator();
                Ann ann = it.next();
                boolean stop = false;

                // Fix tags                    
                for (Token t : tokens) {

                    /*if (filename.startsWith("CA2087883C")) {
                        System.out.println(ann.start);
                        System.out.println(t.text + " " + t.start);
                        System.out.println("");
                    }*/
                    // Match 
                    if (t.start >= ann.start && t.end <= ann.end) {

                        if (tag.equals("O")) {
                            t.label = "B";
                            tag = "I";
                        } else {
                            t.label = "I";
                        }

                    } else { // No match

                        if (tag.equals("I")) {
                            if (it.hasNext()) {
                                ann = it.next();
                            } else {
                                stop = true;
                                break;
                            }
                        }

                        tag = "O";
                    }

                    if (stop) {
                        break;
                    }
                }

                for (Token t : tokens) {

                    if (t.label.equals("L") || t.text == null) {
                        FileUtils.writeStringToFile(new File(outDir, filename + ".txt"), "\n", true);
                        continue;
                    }

                    String line = t.text + "\t" + t.start + "\t" + t.end + "\t" + t.label + "\n";
                    FileUtils.writeStringToFile(new File(outDir, filename + ".txt"), line, true);
                }

            } else {
                for (Token t : tokens) {

                    if (t.label.equals("L") || t.text == null) {
                        FileUtils.writeStringToFile(new File(outDir, filename + ".txt"), "\n", true);
                        continue;
                    }

                    String line = t.text + "\t" + t.start + "\t" + t.end + "\t" + "O" + "\n";
                    FileUtils.writeStringToFile(new File(outDir, filename + ".txt"), line, true);
                }
            }

        }

    }

    private static List<Ann> getAnnotations(File a1File) throws IOException {
        List<Ann> anns = new ArrayList();

        for (String line : IOUtils.readLines(new FileInputStream(a1File))) {

            if (line.trim().length() == 0) {
                continue;
            }

            if (!line.startsWith("T")) {
                continue;
            }

            String[] parts = line.split("\t");
            String[] parts2 = parts[1].split("\\s+");

            Ann ann = new Ann();
            ann.start = Integer.parseInt(parts2[1]);
            ann.end = Integer.parseInt(parts2[2]);

            anns.add(ann);
        }

        return anns;
    }

    private static List<Token> getTokens(File file) throws IOException {
        List<Token> tokens = new ArrayList();

        for (String line : IOUtils.readLines(new FileInputStream(file))) {

            if (line.trim().length() == 0) {
                Token tt = new Token();
                tt.label = "L";
                tokens.add(tt);
                continue;
            }

            String[] parts = line.split("\t");

            Token t = new Token();
            t.text = parts[0];
            t.start = Integer.parseInt(parts[1]);
            t.end = Integer.parseInt(parts[2]);
            t.label = "O";

            tokens.add(t);
        }

        return tokens;
    }

    private static class Ann {

        int start;
        int end;
    }

    private static class Token {

        String text;
        int start;
        int end;
        String label;
    }

}
