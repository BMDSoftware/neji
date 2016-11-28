/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import pt.ua.tm.neji.core.module.Reader;
import pt.ua.tm.neji.core.module.Writer;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.dictionary.DictionaryTagger;
import pt.ua.tm.neji.dictionary.VariantMatcherLoader;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import pt.ua.tm.neji.reader.PdfReader;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;
import pt.ua.tm.neji.writer.JSONPdfWriter;

/**
 *
 * @author jeronimo
 */
public class TestPDFProgrammatically {
    
    public static void main(String[] args) throws Exception {
        
        File pdfFile = new File("pdf_document.pdf");       
        InputStream pdfInputStream = new FileInputStream(pdfFile);
        
        // Intantiate parser
        Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.CHUNKING, new LingpipeSentenceSplitter(), false);
        
        // Instantiate reader module
        Reader reader = new PdfReader(parser, pdfInputStream);
        
        // Instantiate dictionary tagger module
        DictionaryTagger dicTagger = new DictionaryTagger(new VariantMatcherLoader(true).
                        load("UMLS:C2930957:T047:DISO\thantavirosis|hantavirus fever").
                        load("UMLS:C0004096:T047:DISO\tasthma").
                        getMatcher());
        
        // Intantiate writer
        Writer writer = new JSONPdfWriter();
        
        // Instatiate pipeline for PDF processing        
        Pipeline pipeline = new DefaultPipeline()
            .add(reader)
            .add(dicTagger)
            .add(writer);       
        
        // Run processing pipeline
        OutputStream out = pipeline
                .run(new FileInputStream(pdfFile))
                .get(0);
        
        String result = out.toString();
        System.out.println(result);
    }    
}
