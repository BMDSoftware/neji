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

package pt.ua.tm.neji.util;

import org.apache.commons.lang3.StringUtils;
import pt.ua.tm.neji.util.lvg.LVG;
import pt.ua.tm.neji.util.stopwords.Stopwords;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 07/07/15.
 */
public class TSVWithLVG {

    public static void main(String... args) {

        String inputFileName = args[0];
        String outputFileName = args[1];

        // Set output
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(outputFileName);
        } catch (IOException e) {
            System.err.println(e);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            String line;

            while ((line = br.readLine()) != null) {

                String[] parts = line.split("\t");
                String id = parts[0];
                String[] names = parts[1].split("\\|");

                Set<String> ret = new HashSet<>();

                for (String n : names) {
                    Set<String> variations = LVG.getLVGNames(n);

                    String name = n.toLowerCase();
                    if (name.length() > 2 && !Stopwords.isStopword(name)) {
                        ret.add(name);
                    }

                    if (!variations.isEmpty()) {
                        ret.addAll(variations);
                    }
                }

                if (!ret.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(id);
                    sb.append("\t");
                    sb.append(StringUtils.join(ret, '|'));
                    sb.append("\n");

                    fileWriter.write(sb.toString());
                }
            }

        } catch (Exception ex) {
            System.err.println(ex);
            return;
        }


        try {
            fileWriter.close();
        } catch (IOException e) {
            System.err.println(e);
            return;
        }
    }
}
