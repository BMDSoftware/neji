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

package pt.ua.tm.neji.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.train.util.FileUtil;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * NOT USED
 */
public class DictionariesEntries {

    private static Logger logger = LoggerFactory.getLogger(DictionariesEntries.class);
    public static void main(String... args) {

        String dictionariesFolder = "/Users/david/Downloads/dictionaries/";
        Map<String, HashSet<String>> dictionary = new HashMap<>();

        File[] files = new File(dictionariesFolder).listFiles(new FileUtil.Filter(new String[]{"tsv"}));

        for (File file : files) {
            try (
                    FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                String line;

                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\t");
                    String id = parts[0];
                    String[] names = parts[1].split("[|]");

                    HashSet<String> n;
                    if (dictionary.containsKey(id)){
                        n = dictionary.get(id);
                    } else {
                        n = new HashSet<>();
                    }
                    n.addAll(Arrays.asList(names));
                    dictionary.put(id, n);
                }
            } catch (IOException ex) {
                System.out.println("ERROR:");
            }
        }

        // Get number of names
        int numberOfNames = 0;
        int numberOfConcepts = 0;
        for (String id:dictionary.keySet()){
            numberOfNames += dictionary.get(id).size();
            numberOfConcepts++;
        }

        logger.info("Number of Concepts: {}", numberOfConcepts);
        logger.info("Number of Names: {}", numberOfNames);

    }
}
