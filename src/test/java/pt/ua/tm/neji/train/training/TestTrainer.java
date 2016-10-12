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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.training;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.VariantMatcherLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.MLHybrid;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.reader.RawReader;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.train.config.ModelConfig;
import pt.ua.tm.neji.train.model.CRFModel;
import pt.ua.tm.neji.train.nlp.TrainNLP;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase1;
import pt.ua.tm.neji.train.pipeline.TrainPipelinePhase2;
import pt.ua.tm.neji.train.reader.A1Reader;
import pt.ua.tm.neji.train.trainer.DefaultTrainer;
import pt.ua.tm.neji.writer.A1Writer;

/**
 *
 * @author jeronimo
 */
public class TestTrainer extends TestCase {
    
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestTrainer.class);
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestTrainer(String testName){
        super(testName);
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestTrainer.class);
    }
    
    public void test() throws IOException, NejiException {
        
        Constants.verbose = true;
        
        Pipeline p1 = new TrainPipelinePhase1();
        
        String str1Txt = "Expression of the Bcl-3 proto-oncogene suppresses p53 activation. \n" +
                         "While Bcl-3 expression in cancer was originally thought to be limited to B-cell lymphomas with a 14;19 chromosomal translocation, more recent evidence indicates that expression of this presumptive oncoprotein is significantly more widespread in cancer. However, an oncogenic role for Bcl-3 has not been clearly identified. Experiments presented here indicate that Bcl-3 is inducible by DNA damage and is required for the induction of Hdm2 gene expression and the suppression of persistent p53 activity. Furthermore, constitutive expression of Bcl-3 suppresses DNA damage-induced p53 activation and inhibits p53-induced apoptosis through a mechanism that is at least partly dependent on the up-regulation of Hdm2. The results provide insight into a mechanism whereby altered expression of Bcl-3 leads to tumorigenic potential. Since Bcl-3 is required for germinal center formation, these results suggest functional similarities with the unrelated Bcl-6 oncoprotein in suppressing potential p53-dependent cell cycle arrest and apoptosis in response to somatic hypermutation and class switch recombination. ";
        String str1A1 = "T1	Gene 14 38	the Bcl-3 proto-oncogene\n" +
                        "T2	SPAN 73 78	Bcl-3\n" +
                        "T3	Gene 431 436	Bcl-3\n" +
                        "T4	SPAN 501 510	Hdm2 gene\n" +
                        "T5	Gene 610 615	Bcl-3\n" +
                        "T6	Gene 774 778	Hdm2\n" +
                        "T7	Gene_Expression 833 840	altered\n" +
                        "T8	Gene 855 860	Bcl-3";
        
        // Generate corpus using first pipeline (TrainPipelinePhase1)
        InputStream inTxt = new ByteArrayInputStream(str1Txt.getBytes("UTF-8"));
        InputStream inA1 = new ByteArrayInputStream(str1A1.getBytes("UTF-8"));
        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.CHUNKING, new LingpipeSentenceSplitter(), false).launch();
        
        p1.add(new A1Reader(parser, null, inA1)).
                add(new TrainNLP(parser)).
                run(inTxt);
                
        inTxt.close();
        inA1.close();
            
        Corpus c = p1.getCorpus();
        
        // Train a model using the second pipeline (TrainPipelinePhase2)
        Pipeline p2 = new TrainPipelinePhase2();     
        
        p2.setCorpus(c);
        
        InputStream input = new ByteArrayInputStream(" ".getBytes("UTF-8"));
        ModelConfig config = new ModelConfig(true, false, true, true, true, false, true, true, true, true,
                true, true, true, true, false, true, true, true, true, false, 2, Constants.Parsing.BW, new ArrayList<String>());
        
        p2.add(new DefaultTrainer(config)).
                run(input);
        
        input.close();
        
        // Get trained model
        CRFModel model = (CRFModel) p2.getModuleData("TRAINED_MODEL").get(0);
        
        // Use Neji defauts pipeline to annotate a document and verify it
        inTxt = new ByteArrayInputStream(str1Txt.getBytes("UTF-8"));
        Parser parser2 = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.CHUNKING, new LingpipeSentenceSplitter(), false).launch();
                        
        final List<Dictionary> dict = new ArrayList<>();
        dict.add(new Dictionary(new VariantMatcherLoader(true)
            .load("1:::PRGE\tthe Bcl-3 proto-oncogene")
            .load("2:::PRGE\tBcl-3")
            .load("3:::PRGE\tHdm2 gene")
            .load("4:::PRGE\tHdm2")
            .load("5:::PRGE\taltered")
            .getMatcher(), "PRGE"));
        
        Pipeline p3 = new DefaultPipeline();        
        
        OutputStream out = p3.add(new RawReader())
                .add(new NLP(parser2))
                .add(new MLHybrid(model, "PRGE", dict, true))
                .add(new A1Writer()).run(inTxt).get(0);
        
        String result = out.toString();
        
        inTxt.close();
        out.close();
        
        // Verify annotations        
        String expectedResult = "T0\tPRGE 14 38\tthe Bcl-3 proto-oncogene\n" +
                                "N0\tReference T0 1:::PRGE\tthe Bcl-3 proto-oncogene\n" +
                                "T1\tPRGE 73 78\tBcl-3\n" +
                                "N1\tReference T1 2:::PRGE\tBcl-3\n" +
                                "T2\tPRGE 431 436\tBcl-3\n" +
                                "N2\tReference T2 2:::PRGE\tBcl-3\n" +
                                "T3\tPRGE 501 510\tHdm2 gene\n" +
                                "N3\tReference T3 4:::PRGE\tHdm2 gene\n" +
                                "T4\tPRGE 610 615\tBcl-3\n" +
                                "N4\tReference T4 2:::PRGE\tBcl-3\n" +
                                "T5\tPRGE 774 778\tHdm2\n" +
                                "N5\tReference T5 4:::PRGE\tHdm2\n" +
                                "T6\tPRGE 833 840\taltered\n" +
                                "N6\tReference T6 5:::PRGE\taltered\n" +
                                "T7\tPRGE 855 860\tBcl-3\n" +
                                "N7\tReference T7 2:::PRGE\tBcl-3";
        
        assertEquals(expectedResult, result);
    }
}
