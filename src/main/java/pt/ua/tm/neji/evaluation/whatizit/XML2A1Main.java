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

package pt.ua.tm.neji.evaluation.whatizit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.train.util.FileUtil;
import pt.ua.tm.neji.evaluation.Concept;
import pt.ua.tm.neji.exception.NejiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class XML2A1Main {
    private static Logger logger = LoggerFactory.getLogger(XML2A1Main.class);

    public static void main(String... args) {
//        String inputFolder = "/Users/david/Downloads/whatizit/craft/ukpmc/xml/";
//        String outputFolder = "/Users/david/Downloads/whatizit/craft/ukpmc/a1/";

//        String inputFolder = "/Users/david/Downloads/whatizit/ncbi/whatizitDiseaseUMLSDict/xml/";
//        String outputFolder = "/Users/david/Downloads/whatizit/ncbi/whatizitDiseaseUMLSDict/a1/";

        String inputFolder = "/Volumes/data/Backups/2013-04-11_desktop/Downloads/whatizit/craft/ukpmc/xml/";
        String outputFolder = "/Volumes/data/Backups/2013-04-11_desktop/Downloads/whatizit/craft/ukpmc/a1/";


        File[] files = new File(inputFolder).listFiles(new FileUtil.Filter(new String[]{"xml"}));

        for (File file : files) {
            try {
                XML2A1Module module = new XML2A1Module();
                module.process(new FileInputStream(file));


                // TXT file
                FileOutputStream fos = new FileOutputStream(outputFolder + file.getName().replace(".xml", ".txt"));
                fos.write(module.getText().toString().getBytes());
                fos.close();

                // A1 file
                fos = new FileOutputStream(outputFolder + file.getName().replace(".xml", ".a1"));
                int counter = 1;
                for (Concept concept : module.getConceptList()) {
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

                    // MLAnnotator notes
                    String prefix = "#" + (counter);
                    sb.append(prefix);
                    sb.append("\tIdentifiers ");
                    sb.append("T");
                    sb.append(counter);
                    sb.append("\t");
                    // Get ids for that group
                    sb.append(StringUtils.join(concept.getIdentifiers(),"|"));
                    sb.append("\n");

                    fos.write(sb.toString().getBytes());
                    counter++;
                }

                fos.close();
            } catch (IOException | NejiException ex) {
                logger.error("ERROR:", ex);
                return;
            }

        }
    }
}
