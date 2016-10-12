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

package pt.ua.tm.neji.evaluation.craft.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextConfiguration;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.statistics.Statistics;

import java.util.ArrayList;
import java.util.List;

public class ProcessDocuments {

    private static Logger logger = LoggerFactory.getLogger(ProcessDocuments.class);

    public static void main(String... args) throws NejiException {

//        String inputFolder = "resources/corpus/bionlp2009/dev/";
//        String outputFolder = "resources/corpus/bionlp2009/dev/conll/";

//        String inputFolder = "resources/corpus/craft2/gold/";
        String inputFolder = "resources/corpus/craft2/silver/";

        String parserPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/tools/gdep/gdep_gimli";
        int numThreads = 6;

        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.add(OutputFormat.A1);


        Context context = new Context(new ContextConfiguration.Builder()
                .withInputFormat(InputFormat.RAW)
                .withOutputFormats(outputFormats)
                .build(), null, null, parserPath);

        FolderBatchExecutor batch = new FolderBatchExecutor(inputFolder, numThreads);
        try {
            batch.run(context);
        } catch (NejiException e) {
            logger.error("ERROR: ", e);
        }

        Statistics.getInstance().print();

    }
}
