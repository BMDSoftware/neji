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

package pt.ua.tm.neji.example;

import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.train.config.ModelConfig;
import pt.ua.tm.neji.train.model.CRFModel;
import pt.ua.tm.neji.train.nlp.TrainNLP;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase1;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase2;
import pt.ua.tm.neji.train.reader.BC2Reader;
import pt.ua.tm.neji.train.trainer.DefaultTrainer;

import java.io.*;

/**
 * Created by david on 15/10/2016.
 */
public class Train {
    public static void main(final String... args) throws NejiException, IOException {

        // Set files
        String sentencesFile = "example/train/sentences";
        String annotationsFile = "example/train/annotations";
        String modelConfigurationFile = "example/train/model.config";
        String modelFile = "example/train/model.gz";

        // Create parser
        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.CHUNKING, new LingpipeSentenceSplitter(), false).launch();

        // Set sentences and annotations streams
        InputStream sentencesStream = new FileInputStream(sentencesFile);
        InputStream annotationsStream = new FileInputStream(annotationsFile);

        // Run pipeline to get corpus from sentences and annotations
        Pipeline pipelinePhase1 = new TrainPipelinePhase1()
                .add(new BC2Reader(parser, null, annotationsStream))
                .add(new TrainNLP(parser));
        pipelinePhase1.run(sentencesStream);

        // Close sentences and annotations streams
        sentencesStream.close();
        annotationsStream.close();

        // Get corpus
        Corpus corpus = pipelinePhase1.getCorpus();

        // Get model configuration
        InputStream inputStream = new ByteArrayInputStream(" ".getBytes("UTF-8"));
        ModelConfig modelConfig = new ModelConfig(modelConfigurationFile);

        // Run pipeline to train model on corpus
        Pipeline pipelinePhase2 = new TrainPipelinePhase2()
                .add(new DefaultTrainer(modelConfig));
        pipelinePhase2.setCorpus(corpus);
        pipelinePhase2.run(inputStream);

        // Close input stream
        inputStream.close();

        // Get trained model and write to file
        CRFModel model = (CRFModel) pipelinePhase2.getModuleData("TRAINED_MODEL").get(0);
        model.write(new FileOutputStream(modelFile));
    }
}
