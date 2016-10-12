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

package pt.ua.tm.neji.evaluation.ncbi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.evaluation.Concept;
import pt.ua.tm.neji.exception.NejiException;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 05/02/13
 * Time: 01:49
 * To change this template use File | Settings | File Templates.
 */
public class XML2A1Main {
    private static Logger logger = LoggerFactory.getLogger(XML2A1Main.class);

    public static void main(String... args) {
        String inputFile = "/Volumes/data/resources/corpora/NCBI_corpus/NCBI_corpus_testing.txt";
        String outputFolder = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/ncbi/gold/";

        try (
                FileInputStream fis = new FileInputStream(inputFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
        ) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String pmid = parts[0];

//                String tmp = text.toString().replaceAll("\\(\\s([A-Za-z0-9\\-]+)\\s\\)", "($1)");
//
//                text = new StringBuilder(tmp);

                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    String text = parts[i];
                    text = text.replaceAll("\\(\\s+", "(");
                    text = text.replaceAll("\\s+\\)", ")");
                    text = text.replaceAll("\\s+/\\s+", "/");
                    text = text.replaceAll("\\s+-\\s+", "-");
                    text = text.replaceAll("\\s+,\\s+", ", ");
                    sb.append(text);
                    sb.append(" ");
                }

//                String text = parts[1];

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(sb.toString().getBytes());
                baos.close();

                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

                XML2A1Module module = new XML2A1Module();
                module.process(bais);


                // TXT file
                FileOutputStream fos = new FileOutputStream(outputFolder + pmid + ".txt");
                fos.write(module.getText().toString().getBytes());
                fos.close();

                // A1 file
                fos = new FileOutputStream(outputFolder + pmid + ".a1");
                int counter = 1;
                for (Concept concept : module.getConceptList()) {
                    sb = new StringBuilder();

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

        } catch (IOException | NejiException ex) {
            logger.error("There was a problem reading the input file: " + inputFile, ex);
            return;
        }
    }
}
