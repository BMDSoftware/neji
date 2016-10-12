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

package pt.ua.tm.neji.evaluation.arizona;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.evaluation.Concept;
import pt.ua.tm.neji.evaluation.ConceptList;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 04/02/13
 * Time: 22:40
 * To change this template use File | Settings | File Templates.
 */
public class Format2A1 {

    private static Logger logger = LoggerFactory.getLogger(Format2A1.class);

    public static void main(String... args) {
        String inputFile = "/Users/david/Downloads/AZDC_2009_09_10.txt";
        String outputFolder = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/arizona/gold/";

        Map<String, Entry> map = new HashMap<>();

        try (
                FileInputStream fis = new FileInputStream(inputFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
        ) {
            String line;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                // Skip first line
                if (counter == 0) {
                    counter++;
                    continue;
                }

                logger.info("{}", line);

                String[] parts = line.split("\t");

                String pmid = parts[1];
                String sentenceid = parts[2];
                String sentence = parts[3];
                String id = pmid + "_" + sentenceid;

                if (parts.length <= 8) {
                    if (!map.containsKey(id)) {
                        Entry entry = new Entry();
                        entry.sentence = sentence;
                        entry.conceptList = new ConceptList();

                        map.put(id, entry);
                    }
                    continue;
                }

                if (parts[8].trim().equals("No annotation") || parts[8].trim().equals("*")) {
                    if (!map.containsKey(id)) {
                        Entry entry = new Entry();
                        entry.sentence = sentence;
                        entry.conceptList = new ConceptList();

                        map.put(id, entry);
                    }
                    continue;
                }

                int start = Integer.parseInt(parts[4]);
                int end = Integer.parseInt(parts[5]);
                if (start == 0 && end == 0) { // Sentence without annotation
                    if (!map.containsKey(id)) {
                        Entry entry = new Entry();
                        entry.sentence = sentence;
                        entry.conceptList = new ConceptList();

                        map.put(id, entry);
                    }
                    continue;
                }


                String annotation = parts[7];

                Concept concept = new Concept(start, end, "DISO", annotation);

                List<Concept> conceptList;
                Entry entry;
                if (map.containsKey(id)) {
                    entry = map.get(id);
                    conceptList = entry.conceptList;
                    if (!conceptList.contains(concept)) {
                        conceptList.add(concept);
                    }
                } else {
                    conceptList = new ConceptList();
                    conceptList.add(concept);

                    entry = new Entry();
                    entry.sentence = sentence;
                    entry.conceptList = conceptList;
                }
                map.put(id, entry);
            }
        } catch (IOException ex) {
            logger.error("There was a problem reading the input file.", ex);
            return;
        }


        // Write files
        try {
            for (String id : map.keySet()) {
                Entry entry = map.get(id);

                // TXT file
                FileOutputStream fos = new FileOutputStream(outputFolder + id + ".txt");
                fos.write(entry.sentence.getBytes());
                fos.close();

                // A1 file
                fos = new FileOutputStream(outputFolder + id + ".a1");
                int counter = 1;
                for (Concept concept : entry.conceptList) {
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
            }
        } catch (IOException ex) {
            logger.error("There was a problem writing output files.", ex);
            return;
        }

        System.out.println("done");
    }

    static class Entry {
        String sentence;
        List<Concept> conceptList;
    }
}
