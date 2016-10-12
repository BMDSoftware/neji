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

package pt.ua.tm.neji.evaluation.scai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.evaluation.Concept;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 06/02/13
 * Time: 13:21
 * To change this template use File | Settings | File Templates.
 */
public class CoNLL2A1 {

    private static Logger logger = LoggerFactory.getLogger(CoNLL2A1.class);

    public static void main(String... args) {
        String inputFile = "/Users/david/Downloads/chemicals-test-corpus-27-04-2009-v3.iob";
        String outputFolder = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/scai/test/gold/";

//        String inputFile = "/Users/david/Downloads/train-27-04-2009-v2.iob";
//        String outputFolder = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/scai/train/gold/";

        int labelIndex = 4;


        Pattern pattern = Pattern.compile("###\\s+([0-9]+)");


        try (
                FileInputStream fis = new FileInputStream(inputFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
        ) {
            String line, pmid = "";

            StringBuilder text = new StringBuilder();
            List<Concept> concepts = new ArrayList<>();
            int lastEnd = 0;

            List<String> lines = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }


            for (int i = 0; i < lines.size(); i++) {
                line = lines.get(i);
                Matcher matcher = pattern.matcher(line);

                if (line.equals("")) {

                    // TXT file
                    FileOutputStream fos = new FileOutputStream(outputFolder + pmid + ".txt");
                    fos.write(text.toString().getBytes());
                    fos.close();

                    // A1 file
                    fos = new FileOutputStream(outputFolder + pmid + ".a1");
                    int counter = 1;
                    for (Concept concept : concepts) {
                        StringBuilder sb = new StringBuilder();

                        sb.append("T");
                        sb.append(counter);
                        sb.append("\t");

                        sb.append(concept.getEntity());
                        sb.append(" ");
                        sb.append(concept.getStart());
                        sb.append(" ");
                        sb.append(concept.getEnd());
                        sb.append("\t");

                        sb.append(concept.getText());
                        sb.append("\n");

                        fos.write(sb.toString().getBytes());
                        counter++;
                    }
                    fos.close();

                } else if (line.contains("### ")) { // New file
                    pmid = line.substring(4);

                    // Restart
                    text = new StringBuilder();
                    concepts = new ArrayList<>();
                    lastEnd = 0;
                } else {
                    logger.info("{}", line);

                    String[] parts = line.split("\t");
                    String label = parts[labelIndex].substring(1);
                    int end = Integer.parseInt(parts[2]);
                    int start = Integer.parseInt(parts[1]);

                    if (label.contains("B-")) {
                        int startAnnotation = start;
                        int endAnnotation = end;

                        String annotationText = parts[3];
//                        StringBuilder annotationText = new StringBuilder();

                        processLine(parts, text, lastEnd);
                        lastEnd = end;
//                        processLine(parts, annotationText, lastEnd);

                        for (int j = i + 1; j < lines.size(); j++) {
                            line = lines.get(j);
                            parts = line.split("\t");
                            label = parts[labelIndex].substring(1);
                            end = Integer.parseInt(parts[2]);
                            if (label.contains("I-")) {
                                processLine(parts, text, lastEnd);
                                endAnnotation = end;
                                i++;
//                                processLine(parts, annotationText, lastEnd);
                            } else {
                                break;
                            }
                            lastEnd = end;
                        }

//                        Concept concept = new Concept(startAnnotation, endAnnotation - 1, label.substring(2), annotationText);
//                        Concept concept = new Concept(startAnnotation, endAnnotation, "CHED", annotationText);
                        Concept concept = new Concept(startAnnotation, endAnnotation, "CHED", annotationText.toString().trim());
                        concepts.add(concept);

                    } else { // O
                        processLine(parts, text, lastEnd);
                    }

                    lastEnd = end;
                }
            }
        } catch (IOException ex) {
            logger.error("There was a problem reading the input file.", ex);
            return;
        }
    }

    private static void processLine(String[] parts, StringBuilder text, int lastEnd) {
        String token = parts[0];
        int start = Integer.parseInt(parts[1]);
        for (int i = lastEnd; i < start; i++) {
            text.append(" ");
        }
        text.append(token);
    }

}
