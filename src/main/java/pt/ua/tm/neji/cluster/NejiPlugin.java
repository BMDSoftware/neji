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

package pt.ua.tm.neji.cluster;

import pt.ua.tm.neji.batch.FileBatchExecutor;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextConfiguration;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.batch.BatchExecutor;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.processor.FileProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class NejiPlugin {

    private Context context;

    public void init(final File workDir) {

        String workdirPath = workDir.getPath() + File.separator;

        String modelsPath = null;
        String dictionariesPath = "resources/dictionaries/";
        String parserPath = "resources/tools/gdep";

        try {
            InputFormat inputFormat = InputFormat.RAW;

            List<OutputFormat> outputFormats = new ArrayList<>();
            outputFormats.add(OutputFormat.A1);

            context = new Context(new ContextConfiguration.Builder()
                    .withInputFormat(inputFormat)
                    .withOutputFormats(outputFormats)
                    .build(), modelsPath, dictionariesPath, parserPath);
            context.initialize();
        } catch (NejiException ex) {
            throw new RuntimeException("There was a problem loading the annotation context.", ex);
        }
    }

    public void transform(final File workDir, final Map<String, String> settings) {

        String workdirPath = workDir.getPath() + File.separator;

        String inputFolderPath = "corpus/input/";
        String outputFolderPath = "corpus/output/";
        int numThreads = 1;


        BatchExecutor batchExecutor = new FileBatchExecutor(inputFolderPath, outputFolderPath, false, numThreads, "*.txt", false, false);
        Class c = FileProcessor.class;

        try {
            batchExecutor.run(c, context);
        } catch (NejiException ex) {
            throw new RuntimeException("There was a problem processing the files.", ex);
        }

    }

    public void destroy() {
        try {
            context.terminate();
        } catch (NejiException ex) {
            throw new RuntimeException("There was a problem terminating the annotation context.", ex);
        }
    }
}
