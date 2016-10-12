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

package pt.ua.tm.neji.writing;

import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;

import java.io.IOException;

/**
 * Static class which contains variables and information shared between all writing test classes.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
final class Variables {

    private Variables(){}

    static final String expectedA1 =
            "T0\tPRGE 0 23\tInhaled corticosteroids\n" +
            "N0\tReference T0 1:::PRGE\tInhaled corticosteroids\n" +
            "T1\tPRGE 25 28\tICS\n" +
            "N1\tReference T1 1:::PRGE\tICS\n" +
            "T2\tPRGE 68 79\tmedications\n" +
            "N2\tReference T2 2:::PRGE\tmedications\n" +
            "T3\tPRGE 95 101\tasthma\n" +
            "N3\tReference T3 3:::PRGE\tasthma";

    static final String expectedJSON =
            "[{\"id\":0,\"start\":0,\"end\":102,\"text\":\"Inhaled corticosteroids (ICS) are the most " +
            "commonly used controller medications prescribed for asthma.\",\"terms\":[{\"ids\":\"1:::PRGE\",\"score\":1.0," +
            "\"id\":1,\"start\":0,\"end\":23,\"text\":\"Inhaled corticosteroids\",\"terms\":[]}," +
            "{\"ids\":\"1:::PRGE\",\"score\":1.0,\"id\":2,\"start\":25,\"end\":28,\"text\":\"ICS\",\"terms\":[]}," +
            "{\"ids\":\"2:::PRGE\",\"score\":1.0,\"id\":3,\"start\":68,\"end\":79,\"text\":\"medications\",\"terms\":[]}," +
            "{\"ids\":\"3:::PRGE\",\"score\":1.0,\"id\":4,\"start\":95,\"end\":101,\"text\":\"asthma\",\"terms\":[]}]}]";

    static final String expectedBIOC =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE collection SYSTEM \"BioC.dtd\"><collection>" +
            "<source></source><date></date><key></key><document><id></id><passage><offset>0</offset>" +
            "<sentence><offset>0</offset><text>Inhaled corticosteroids (ICS) are the most commonly " +
            "used controller medications prescribed for asthma.</text><annotation id=\"T1\">" +
            "<infon key=\"type\">LEAF</infon><location offset=\"0\" length=\"23\"></location>" +
            "<text>Inhaled corticosteroids</text></annotation><annotation id=\"T2\">" +
            "<infon key=\"type\">LEAF</infon><location offset=\"25\" length=\"3\"></location>" +
            "<text>ICS</text></annotation><annotation id=\"T3\"><infon key=\"type\">LEAF</infon>" +
            "<location offset=\"68\" length=\"11\"></location><text>medications</text></annotation>" +
            "<annotation id=\"T4\"><infon key=\"type\">LEAF</infon>" +
            "<location offset=\"95\" length=\"6\"></location><text>asthma</text></annotation></sentence>" +
            "</passage></document></collection>";

    static final String expectedCoNLL =
            "1\tInhaled\t_\t_\t_\t1:::PRGE\t_\t_\t_\t_\n" +
            "2\tcorticosteroids\t_\t_\t_\t1:::PRGE\t_\t_\t_\t_\n" +
            "3\t(\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "4\tICS\t_\t_\t_\t1:::PRGE\t_\t_\t_\t_\n" +
            "5\t)\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "6\tare\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "7\tthe\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "8\tmost\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "9\tcommonly\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "10\tused\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "11\tcontroller\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "12\tmedications\t_\t_\t_\t2:::PRGE\t_\t_\t_\t_\n" +
            "13\tprescribed\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "14\tfor\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "15\tasthma\t_\t_\t_\t3:::PRGE\t_\t_\t_\t_\n" +
            "16\t.\t_\t_\t_\t0\t_\t_\t_\t_\n" +
            "\n";
    
    static final String expectedPIPE =
            "Document||Disease_Disorder||CUI-less||0||23\n" +
            "Document||Disease_Disorder||CUI-less||25||28\n" +
            "Document||Disease_Disorder||CUI-less||68||79\n" +
            "Document||Disease_Disorder||CUI-less||95||101";
    
    static final String expectedPIPEXT =
            "Document||0||0-1||Inhaled corticosteroids||_ _||_ _||1:::PRGE||0||23\n" +
            "Document||0||3||ICS||_||_||1:::PRGE||25||28\n" +
            "Document||0||11||medications||_||_||2:::PRGE||68||79\n" +
            "Document||0||14||asthma||_||_||3:::PRGE||95||101";
    
    static final String expectedBC2 =
            "S1|0 21|Inhaled corticosteroids\n" +
            "S1|23 25|ICS\n" +
            "S1|59 69|medications\n" +
            "S1|83 88|asthma\n";
    
    static final String expectedIeXML =
            "<sentences>" +
            "<s id=\"test1\">" +
            "<e id=\"1:::PRGE\">Inhaled corticosteroids</e> (<e id=\"1:::PRGE\">ICS</e>) are the most commonly used controller <e id=\"2:::PRGE\">medications</e> prescribed for <e id=\"3:::PRGE\">asthma</e>." +
            "</s>" +
            "</sentences>";
    
    static final String expectedNeji =
            "S1\t   0  102\tInhaled corticosteroids (ICS) are the most commonly used controller medications prescribed for asthma.\n" +
            "\tT1\t   0   23\tInhaled corticosteroids	1:::PRGE\n" +
            "\tT2\t  25   28\tICS	1:::PRGE\n" +
            "\tT3\t  68   79\tmedications	2:::PRGE\n" +
            "\tT4\t  95  101\tasthma	3:::PRGE";
    
    private static Corpus corpus;
    static Corpus corpus() throws NejiException, IOException {

        if(corpus==null){
            corpus = new Corpus();
            String str = "Inhaled corticosteroids (ICS) are the most commonly used controller medications prescribed for asthma.";
            corpus.setText(str);
            
            Parser parser = new GDepParser(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION, new LingpipeSentenceSplitter(), false);
            parser.launch();
            parser.parse(corpus, str);
            parser.close();
            Annotation a;
            Sentence s = corpus.getSentence(0);

            a = AnnotationImpl.newAnnotationByTokenPositions(s, 0, 1, 1.0);   // Inhaled corticosteroids
            a.addID(Identifier.getIdentifierFromText("1:::PRGE"));
            s.addAnnotationToTree(a);
            a = AnnotationImpl.newAnnotationByTokenPositions(s, 3, 3, 1.0);   // ICS
            a.addID(Identifier.getIdentifierFromText("1:::PRGE"));
            s.addAnnotationToTree(a);
            a = AnnotationImpl.newAnnotationByTokenPositions(s, 11, 11, 1.0); // medications
            a.addID(Identifier.getIdentifierFromText("2:::PRGE"));
            s.addAnnotationToTree(a);
            a = AnnotationImpl.newAnnotationByTokenPositions(s, 14, 14, 1.0); // asthma
            a.addID(Identifier.getIdentifierFromText("3:::PRGE"));
            s.addAnnotationToTree(a);
        }

        return corpus;
    }
}
