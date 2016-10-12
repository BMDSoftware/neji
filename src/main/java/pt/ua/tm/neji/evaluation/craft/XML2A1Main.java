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

package pt.ua.tm.neji.evaluation.craft;

import pt.ua.tm.neji.train.util.FileUtil;
import pt.ua.tm.neji.evaluation.Concept;
import pt.ua.tm.neji.evaluation.ConceptList;
import pt.ua.tm.neji.exception.NejiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 01/02/13
 * Time: 11:43
 * To change this template use File | Settings | File Templates.
 */
public class XML2A1Main {
    public static void main(String... args) {

        String annotationsFolder = "/Volumes/data/resources/corpora/craft-1.0/xml/";
        String outputFolder = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/corpus/craft2/gold/";

        String inputFolder = annotationsFolder + "entrezgene/";
        File[] files = new File(inputFolder).listFiles(new FileUtil.Filter(new String[]{"xml"}));

        String path;
        for (File file : files) {
            try {
                // Get annotations
                ConceptList conceptList = new ConceptList();

                // EntrezGene
                XML2A1Module module = new XML2A1Module(conceptList, "PRGE");
                module.process(new FileInputStream(file));

                // PR
//                path = file.getAbsolutePath().replace("entrezgene", "pr");
//                module.process(new FileInputStream(path));

                //Sequence Ontology
//                path = file.getAbsolutePath().replace("entrezgene", "so");
//                module = new XML2A1Module(conceptList, "PRGE");
//                module.process(new FileInputStream(path));

                // Chemicals
                path = file.getAbsolutePath().replace("entrezgene", "chebi");
                module = new XML2A1Module(conceptList, "CHED");
                module.process(new FileInputStream(path));

                // Biological processes and Molecular functions
                path = file.getAbsolutePath().replace("entrezgene", "go_bpmf");
                module = new XML2A1Module(conceptList, "PROC_FUNC");
                module.process(new FileInputStream(path));

                // Species
                path = file.getAbsolutePath().replace("entrezgene", "ncbitaxon");
                module = new XML2A1Module(conceptList, "SPEC");
                module.process(new FileInputStream(path));

                // Cell component
                path = file.getAbsolutePath().replace("entrezgene", "go_cc");
                module = new XML2A1Module(conceptList, "COMP");
                module.process(new FileInputStream(path));

                // Cell component
                path = file.getAbsolutePath().replace("entrezgene", "cl");
                module = new XML2A1Module(conceptList, "CELL");
                module.process(new FileInputStream(path));

                // Write A1
                String a1FileName = outputFolder + file.getName().substring(0, file.getName().indexOf(".")) + ".a1";
                FileOutputStream fos = new FileOutputStream(a1FileName);
                fos.write(getA1Output(conceptList).getBytes());
                fos.close();

            } catch (IOException | NejiException ex) {
                throw new RuntimeException("There was a problem processing the file: " + file.getAbsolutePath());
            }
        }
    }

    private static String getA1Output(final ConceptList conceptList) {
        StringBuilder sb = new StringBuilder();

        int counter = 0;
        for (Concept annotation : conceptList) {
            sb.append("T");
            sb.append(counter++);
            sb.append("\t");

            sb.append(annotation.getEntity());
            sb.append(" ");
            sb.append(annotation.getStart());
            sb.append(" ");
            sb.append(annotation.getEnd());
            sb.append("\t");

            sb.append(annotation.getText());
            sb.append("\n");
        }

        return sb.toString();
    }
}
