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

package pt.ua.tm.neji.test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProteinToSpecies {

    private static ProteinToSpecies instance;
    private Multimap<String, String> map;
    private static Logger logger = LoggerFactory.getLogger(ProteinToSpecies.class);

    private ProteinToSpecies() {
//        String filePath = "/Users/david/Downloads/Uniprot2Entrez_selected";
        String filePath = "resources/mappers/Uniprot2Entrez_selected+genus";
        try {
            this.map = load(new FileInputStream(filePath));
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem loading the mapping file: " + filePath, ex);
        }
    }

    public synchronized Multimap<String, String> getMap() {
        return map;
    }

    public synchronized static ProteinToSpecies getInstance() {
        if (instance == null) {
            instance = new ProteinToSpecies();
        }
        return instance;
    }

    private synchronized static Multimap<String, String> load(final InputStream inputStream) {

        logger.info("Loading protein to species mapping...");
        Multimap<String, String> map = HashMultimap.create();

        try (
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(isr);
        ) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String uniprot = parts[0];
                String species = parts[2];

                if (species.contains("|")) {
                    parts = species.split("[|]");
                    String speciesSpecific = parts[0];
                    String speciesGenus = parts[1];

                    map.put(uniprot, speciesGenus);
                    map.put(uniprot, speciesSpecific);
                } else {
                    map.put(uniprot, species);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem loading the mapping file.", ex);
        }

        logger.info("Done!");

        return map;
    }
}
