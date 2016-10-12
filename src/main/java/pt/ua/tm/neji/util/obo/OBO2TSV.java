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

package pt.ua.tm.neji.util.obo;

import org.apache.commons.lang3.StringEscapeUtils;
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
 * Created by david on 24/06/15.
 */
public class OBO2TSV {


    public static void main(String... args) {

        String inputFileName = args[0];
        String outputFileName = args[1];
        String semanticGroup = args[2];


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

            OBOTerm term = new OBOTerm();
            boolean inTerm = false;

            while ((line = br.readLine()) != null) {
                // process the line.
                if (line.contains("[Term]")) {
                    if (inTerm && term.getId() != null && !term.getNames().isEmpty()) {
                        writeTerm(fileWriter, term, semanticGroup);
                    }
                    term = new OBOTerm();
                    inTerm = true;
                } else if (line.contains("[Typedef]")) {
                    inTerm = false;
                } else if (line.contains("alt_id:")) {
                    continue;
                } else if (line.contains("ref:")) {
                    continue;
                } else if (line.contains("xref:")) {
                    continue;
                } else if (line.contains("def:")) {
                    continue;
                } else if (line.contains("id:")) {
                    if (!inTerm) continue;
                    String id = line.substring(3).trim();
                    term.setId(id);
                } else if (line.contains("name:")) {
                    if (!inTerm) continue;
                    String name = line.substring(5).trim();
                    name = format(name);
                    term.getNames().add(name);
                } else if (line.contains("synonym:")) {
                    if (!inTerm) continue;
                    String synonym = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"')).trim();
                    synonym = format(synonym);
                    term.getNames().add(synonym);
                }
            }
            writeTerm(fileWriter, term, semanticGroup);
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

    private static void writeTerm(final FileWriter fileWriter, final OBOTerm term, final String semanticGroup) throws Exception {
        Set<String> names = new HashSet<>();
        for (String n : term.getNames()) {
            Set<String> variations = LVG.getLVGNames(n);

            String name = n.toLowerCase();
            if (name.length() > 2 && !Stopwords.isStopword(name)) {
                names.add(name);
            }
            if (!variations.isEmpty()) {
                names.addAll(variations);
            }
        }

//        System.out.println(term.getId());
//        System.out.println(StringUtils.join(names, '|'));

        if (!names.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(term.getId());
            sb.append("::");
            sb.append(semanticGroup);
            sb.append("\t");
            sb.append(StringUtils.join(names, '|'));
            sb.append("\n");

            fileWriter.write(sb.toString());
        }
    }

    private static String format(final String in) {
        String name = in;
        name = name.replaceAll("_", "");
        name = name.replaceAll("\\s+\\((Japanese|Spanish)\\)", "");
        name = name.replaceAll("(&#[0-9]+)", "$1;");
        name = StringEscapeUtils.unescapeXml(name);
        name = name.toLowerCase();
        return name;
    }

}
