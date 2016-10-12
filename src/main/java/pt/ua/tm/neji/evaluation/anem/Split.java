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

package pt.ua.tm.neji.evaluation.anem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 07/02/13
 * Time: 16:11
 * To change this template use File | Settings | File Templates.
 */
public class Split {
    private static Logger logger = LoggerFactory.getLogger(Split.class);
    public static void main(String... args) {
        String listFilesPath = "/Volumes/data/resources/corpora/AnEM-1.0.4/test/test-files.list";
        String corpusFolder = "/Users/david/Downloads/AnEM_xAnnot/";

        String targetFolder = "/Users/david/Downloads/anem_craft/test/";

        try (
                FileInputStream fis = new FileInputStream(listFilesPath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
        ) {
            String line;

            while((line = br.readLine()) != null){
//                String inputTxtFile = corpusFolder + line + ".txt";
                String inputAnnotationFile = corpusFolder + line + ".ann";

//                String outputTxtFile = targetFolder + line + ".txt";
                String outputAnnotationFile = targetFolder + line + ".ann";

//                Files.copy(Paths.get(inputTxtFile), Paths.get(outputTxtFile));
                Files.copy(Paths.get(inputAnnotationFile), Paths.get(outputAnnotationFile));
            }
        } catch (IOException ex) {
            logger.error("There was a problem reading the input file.", ex);
            return;
        }
    }
}
