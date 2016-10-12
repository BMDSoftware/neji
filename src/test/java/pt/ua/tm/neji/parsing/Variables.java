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

package pt.ua.tm.neji.parsing;

import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.dependency.ChunkTag;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.exception.NejiException;

import java.io.IOException;
import java.util.List;

/**
 * Static class which contains variables and information shared between all parsing test classes.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
abstract class Variables {

    private static Parser gdepDependencyParser;
    private static int numUses;
    private static int allowedUsagesOfDepParser = 11;

    static List<Sentence> parseWithDepParser(ParserLevel level, Corpus corpus, String text) throws NejiException {
        try {
            if (gdepDependencyParser == null) {
                gdepDependencyParser = Parser.defaultParserFactory(ParserTool.GDEP, ParserLanguage.ENGLISH, ParserLevel.DEPENDENCY, null);
                gdepDependencyParser.launch();
                numUses = 0;
            }

            List<Sentence> result = gdepDependencyParser.parseWithLevel(level, corpus, text);
            numUses++;

            if (numUses == allowedUsagesOfDepParser) {
                gdepDependencyParser.close();
            }

            return result;

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static final class str1 extends Variables {
        static str1 gdep = new str1().gdep();
        static str1 opennlp = new str1().opennlp();

        private str1(){
            super.text = "Conducting the first genome-wide association study (ICS) for the identification of " +
                    "patients who are at high risk for irinotecan-related severe diarrhea and neutropenia is clinically important. " +
                    "P<0.05 for both.\nIntronic variants in SLCO1B1 related to statin-induced myopathy are associated with " +
                    "the low-density lipoprotein cholesterol response to statins in Chinese patients with hyperlipidaemia.";
            super.sentenceStart = new int[]{ 0, 193, 210 };
            super.sentenceEnd = new int[]{ 192, 209, 395 };
            super.tokenLabel = Constants.LabelTag.O;
        }

        private str1 gdep(){ // elements like (ICS) are separated into "(", "ICS" and ")"
            super.tokenStart = new int[]{
                    0, 11, 15, 21, 27, 28, 33, 45, 51, 52, 55, 57, 61, 65, 80, 83, 92, 96, 100, 103, 108, 113, 117, 127, 128, 136, 143, 152, 156, 168, 171, 182, 191,
                    0, 1, 2, 3, 4, 7, 11, 15,
                    0, 9, 18, 21, 29, 37, 40, 46, 47, 55, 64, 68, 79, 84, 88, 91, 92, 100, 112, 124, 133, 136, 144, 147, 155, 164, 169, 184
            };
            super.tokenEnd = new int[]{
                    9, 13, 19, 26, 27, 31, 43, 49, 51, 54, 55, 59, 63, 78, 81, 90, 94, 98, 101, 106, 111, 115, 126, 127, 134, 141, 150, 154, 166, 169, 180, 190, 191,
                    0, 1, 2, 3, 5, 9, 14, 15,
                    7, 16, 19, 27, 35, 38, 45, 46, 53, 62, 66, 77, 82, 86, 90, 91, 98, 110, 122, 131, 134, 142, 145, 153, 162, 167, 183, 184
            };
            super.tokenText = new String[]{
                    "Conducting", "the", "first", "genome", "-", "wide", "association", "study", "(", "ICS", ")", "for", "the", "identification", "of", "patients", "who", "are", "at", "high", "risk", "for", "irinotecan", "-", "related", "severe", "diarrhea", "and", "neutropenia", "is", "clinically", "important", ".",
                    "P", "<", "0", ".", "05", "for", "both", ".",
                    "Intronic", "variants", "in", "SLCO1B1", "related", "to", "statin", "-", "induced", "myopathy", "are", "associated", "with", "the", "low", "-", "density", "lipoprotein", "cholesterol", "response", "to", "statins", "in", "Chinese", "patients", "with", "hyperlipidaemia", "."
            };
            super.POSValue = new String[]{
                    "VBG", "DT", "JJ", "NN", "HYPH", "JJ", "NN", "NN", "(", "NN", ")", "IN", "DT", "NN", "IN", "NNS", "WP", "VBP", "IN", "JJ", "NN", "IN", "NN", "HYPH", "VBN", "JJ", "NN", "CC", "NN", "VBZ", "RB", "JJ", ".", "NN", "SYM", "CD", ".", "CD", "IN", "DT", ".", "JJ", "NNS", "IN", "NN", "VBN", "TO", "NN", "HYPH", "VBN", "NN", "VBP", "VBN", "IN", "DT", "JJ", "HYPH", "NN", "NN", "NN", "NN", "TO", "NNS", "IN", "JJ", "NNS", "IN", "NN", "."
            };
            super.chunkStart = new int[]{
                    0, 1, 4, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 29, 30, 32,
                    0, 1, 2, 5, 6, 7,
                    0, 2, 3, 4, 5, 6, 7, 10, 12, 13, 20, 21, 22, 23, 25, 26, 27
            };
            super.chunkEnd = new int[]{
                    0, 3, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 20, 21, 22, 23, 28, 29, 31, 32,
                    0, 1, 4, 5, 6, 7,
                    1, 2, 3, 4, 5, 6, 9, 11, 12, 19, 20, 21, 22, 24, 25, 26, 27
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.VP, ChunkTag.NP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.NP, ChunkTag.VP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.VP, ChunkTag.ADJP, ChunkTag.O, ChunkTag.NP, ChunkTag.NP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.VP, ChunkTag.PP, ChunkTag.NP, ChunkTag.NP, ChunkTag.VP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O
            };
            super.chunkBIOTag = new String[]{
                    "B-VP", "B-NP", "I-NP", "I-NP", "B-NP", "I-NP", "I-NP", "I-NP", "O", "B-NP", "O", "B-PP", "B-NP", "I-NP", "B-PP", "B-NP", "B-NP", "B-VP", "B-PP", "B-NP", "I-NP", "B-PP", "B-NP", "O", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "B-VP", "B-ADJP", "I-ADJP", "O", "B-NP", "B-NP", "B-NP", "I-NP", "I-NP", "B-PP", "B-NP", "O", "B-NP", "I-NP", "B-PP", "B-NP", "B-VP", "B-PP", "B-NP", "B-NP", "I-NP", "I-NP", "B-VP", "I-VP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP", "B-PP", "B-NP", "I-NP", "B-PP", "B-NP", "O"
            };
            super.dependencyToken1 = new String[]{
                    "Conducting", "the", "first", "genome", "-", "wide", "association", "study", "(", "ICS", ")", "for", "the", "identification", "of", "patients", "who", "are", "at", "high", "risk", "for", "irinotecan", "-", "related", "severe", "diarrhea", "and", "neutropenia", "clinically", "important", ".",
                    "P", "<", "0", ".", "for", "both", ".",
                    "Intronic", "variants", "in", "SLCO1B1", "related", "to", "statin", "-", "induced", "myopathy", "associated", "with", "the", "low", "-", "density", "lipoprotein", "cholesterol", "response", "to", "statins", "in", "Chinese", "patients", "with", "hyperlipidaemia", "."
            };
            super.dependencyToken2 = new String[]{
                    "is", "genome", "genome", "Conducting", "genome", "study", "study", "-", ")", ")", "study", "study", "identification", "for", "identification", "of", "patients", "who", "are", "risk", "at", "risk", "neutropenia", "neutropenia", "neutropenia", "diarrhea", "neutropenia", "neutropenia", "for", "is", "is", "is",
                    "05", "P", "<", "P", "05", "for", "05",
                    "variants", "are", "variants", "in", "SLCO1B1", "related", "myopathy", "myopathy", "myopathy", "to", "are", "associated", "response", "response", "response", "response", "response", "response", "with", "response", "to", "statins", "patients", "in", "patients", "with", "are"
            };
            super.dependencyTagName = new String[]{
                    "SUB", "NMOD", "NMOD", "OBJ", "NMOD", "NMOD", "NMOD", "OBJ", "DEP", "DEP", "NMOD", "NMOD", "NMOD", "PMOD", "NMOD", "PMOD", "NMOD", "SBAR", "PRD", "NMOD", "PMOD", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "PMOD", "VMOD", "PRD", "P",
                    "SUB", "NMOD", "AMOD", "P", "VMOD", "PMOD", "P",
                    "NMOD", "SUB", "NMOD", "PMOD", "NMOD", "AMOD", "NMOD", "NMOD", "NMOD", "PMOD", "VC", "VMOD", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "PMOD", "NMOD", "PMOD", "NMOD", "NMOD", "PMOD", "NMOD", "PMOD", "P"
            };
            super.dependencyOrdinal = new int[]{
                    5, 1, 1, 6, 1, 1, 1, 6, 7, 7, 1, 1, 1, 3, 1, 3, 1, 9, 11, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 4, 11, 8,
                    5, 1, 2, 8, 4, 3, 8,
                    1, 5, 1, 3, 1, 2, 1, 1, 1, 3, 10, 4, 1, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 3, 1, 3, 8
            };
            return this;
        }

        private str1 opennlp(){ // elements like "(ICS)" are separated into "(" and "ICS)"
            super.tokenStart = new int[]{
                    0, 11, 15, 21, 33, 45, 51, 52, 57, 61, 65, 80, 83, 92, 96, 100, 103, 108, 113, 117, 136, 143, 152, 156, 168, 171, 182, 191,
                    0, 7, 11, 15,
                    0, 9, 18, 21, 29, 37, 40, 55, 64, 68, 79, 84, 88, 100, 112, 124, 133, 136, 144, 147, 155, 164, 169, 184
            };
            super.tokenEnd = new int[]{
                    9, 13, 19, 31, 43, 49, 51, 55, 59, 63, 78, 81, 90, 94, 98, 101, 106, 111, 115, 134, 141, 150, 154, 166, 169, 180, 190, 191,
                    5, 9, 14, 15,
                    7, 16, 19, 27, 35, 38, 53, 62, 66, 77, 82, 86, 98, 110, 122, 131, 134, 142, 145, 153, 162, 167, 183, 184
            };
            super.tokenText = new String[]{
                    "Conducting", "the", "first", "genome-wide", "association", "study", "(", "ICS)", "for", "the", "identification", "of", "patients", "who", "are", "at", "high", "risk", "for", "irinotecan-related", "severe", "diarrhea", "and", "neutropenia", "is", "clinically", "important", ".",
                    "P<0.05", "for", "both", ".",
                    "Intronic", "variants", "in", "SLCO1B1", "related", "to", "statin-induced", "myopathy", "are", "associated", "with", "the", "low-density", "lipoprotein", "cholesterol", "response", "to", "statins", "in", "Chinese", "patients", "with", "hyperlipidaemia", "."
            };
            super.POSValue = new String[]{
                    "VBG", "DT", "JJ", "JJ", "NN", "NN", "-LRB-", "NNP", "IN", "DT", "NN", "IN", "NNS", "WP", "VBP", "IN", "JJ", "NN", "IN", "JJ", "JJ", "NN", "CC", "NN", "VBZ", "RB", "JJ", ".", "CD", "IN", "DT", ".", "NNP", "VBZ", "IN", "NNP", "VBD", "TO", "JJ", "NN", "VBP", "VBN", "IN", "DT", "NN", "NN", "NN", "NN", "TO", "NNS", "IN", "JJ", "NNS", "IN", "NN", "."
            };
            super.chunkStart = new int[]{
                    0, 1, 8, 9, 11, 12, 13, 14, 15, 16, 18, 19, 24, 25, 27,
                    0, 1, 2, 3,
                    0, 1, 2, 3, 4, 5, 6, 8, 10, 11, 16, 17, 18, 19, 21, 22, 23
            };
            super.chunkEnd = new int[]{
                    0, 7, 8, 10, 11, 12, 13, 14, 15, 17, 18, 23, 24, 26, 27,
                    0, 1, 2, 3,
                    0, 1, 2, 3, 4, 5, 7, 9, 10, 15, 16, 17, 18, 20, 21, 22, 23
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.VP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.NP, ChunkTag.VP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.VP, ChunkTag.ADJP, ChunkTag.O, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.VP, ChunkTag.SBAR, ChunkTag.NP, ChunkTag.VP, ChunkTag.PP, ChunkTag.NP, ChunkTag.VP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O
            };
            super.chunkBIOTag = new String[]{
                    "B-VP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP", "B-PP", "B-NP", "B-NP", "B-VP", "B-PP", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "B-VP", "B-ADJP", "I-ADJP", "O", "B-NP", "B-PP", "B-NP", "O", "B-NP", "B-VP", "B-SBAR", "B-NP", "B-VP", "B-PP", "B-NP", "I-NP", "B-VP", "I-VP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP", "B-PP", "B-NP", "I-NP", "B-PP", "B-NP", "O"
            };
            return this;
        }
    }

    static final class str2 extends Variables {
        static str2 gdep = new str2().gdep();
        static str2 opennlp = new str2().opennlp();

        private str2(){
            super.text = "We combined the two SNPs rs37973 and rs1876828 into a predictive test of FEV1 change " +
                    "using a Bayesian model, which identified patients with good or poor steroid response (highest or lowest " +
                    "quartile, respectively) with predictive performance of 65.7% (P=0.039 vs random) area under the " +
                    "receiver-operator characteristic curve in the training population and 65.9% (P=0.025 vs random) in the " +
                    "test population.";
            super.tokenLabel = Constants.LabelTag.O;
        }

        private str2 gdep(){ // elements like "P=0.039" are separated into "P=0", "." and "039"
            super.sentenceStart = new int[]{ 0 };
            super.sentenceEnd = new int[]{ 404 };
            super.tokenStart = new int[]{
                    0, 3, 12, 16, 20, 25, 33, 37, 47, 52, 54, 65, 70, 73, 78, 85, 91, 93, 102, 107, 109, 115, 126, 135, 140, 145, 148, 153, 161, 170, 171, 179, 182, 189, 197, 199, 211, 213, 218, 229, 241, 244, 246, 247, 248, 250, 251, 254, 255, 259, 262, 268, 270, 275, 281, 285, 293, 294, 303, 318, 324, 327, 331, 340, 351, 355, 357, 358, 359, 361, 362, 365, 366, 370, 373, 379, 381, 384, 388, 393, 403
            };
            super.tokenEnd = new int[]{
                    1, 10, 14, 18, 23, 31, 35, 45, 50, 52, 63, 68, 71, 76, 83, 89, 91, 100, 106, 107, 113, 124, 133, 138, 143, 146, 151, 159, 168, 170, 177, 180, 187, 196, 197, 210, 211, 216, 227, 239, 242, 245, 246, 247, 248, 250, 253, 254, 257, 260, 267, 268, 273, 279, 283, 292, 293, 301, 316, 322, 325, 329, 338, 349, 353, 356, 357, 358, 359, 361, 364, 365, 368, 371, 378, 379, 382, 386, 391, 402, 403
            };
            super.tokenText = new String[]{
                    "We", "combined", "the", "two", "SNPs", "rs37973", "and", "rs1876828", "into", "a", "predictive", "test", "of", "FEV1", "change", "using", "a", "Bayesian", "model", ",", "which", "identified", "patients", "with", "good", "or", "poor", "steroid", "response", "(", "highest", "or", "lowest", "quartile", ",", "respectively", ")", "with", "predictive", "performance", "of", "65", ".", "7", "%", "(", "P=0", ".", "039", "vs", "random", ")", "area", "under", "the", "receiver", "-", "operator", "characteristic", "curve", "in", "the", "training", "population", "and", "65", ".", "9", "%", "(", "P=0", ".", "025", "vs", "random", ")", "in", "the", "test", "population", "."
            };
            super.POSValue = new String[]{
                    "PRP", "VBD", "DT", "CD", "NNS", "NN", "CC", "NN", "IN", "DT", "JJ", "NN", "IN", "NN", "NN", "VBG", "DT", "JJ", "NN", ",", "WDT", "VBD", "NNS", "IN", "JJ", "CC", "JJ", "NN", "NN", "(", "JJS", "CC", "JJS", "JJ", ",", "RB", ")", "IN", "JJ", "NN", "IN", "CD", ".", "CD", "NN", "(", "NN", ".", "CD", "CC", "JJ", ")", "NN", "IN", "DT", "NN", "HYPH", "NN", "JJ", "NN", "IN", "DT", "NN", "NN", "CC", "CD", ".", "CD", "NN", "(", "NN", ".", "CD", "CC", "JJ", ")", "IN", "DT", "NN", "NN", "."
            };
            super.chunkStart = new int[]{
                    0, 1, 2, 8, 9, 12, 13, 15, 16, 19, 20, 21, 22, 23, 24, 29, 30, 34, 35, 36, 37, 38, 40, 41, 42, 43, 45, 46, 47, 48, 49, 52, 53, 54, 56, 60, 61, 64, 65, 66, 67, 69, 70, 71, 72, 73, 74, 75, 76, 77, 80
            };
            super.chunkEnd = new int[]{
                    0, 1, 7, 8, 11, 12, 14, 15, 18, 19, 20, 21, 22, 23, 28, 29, 33, 34, 35, 36, 37, 39, 40, 41, 42, 44, 45, 46, 47, 48, 51, 52, 53, 55, 59, 60, 63, 64, 65, 66, 68, 69, 70, 71, 72, 73, 74, 75, 76, 79, 80
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.NP, ChunkTag.VP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.VP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.VP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.ADVP, ChunkTag.O, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.ADJP, ChunkTag.O, ChunkTag.PP, ChunkTag.NP, ChunkTag.O
            };
            super.chunkBIOTag = new String[]{
                    "B-NP", "B-VP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP", "B-VP", "B-NP", "I-NP", "I-NP", "O", "B-NP", "B-VP", "B-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "O", "B-NP", "I-NP", "I-NP", "I-NP", "O", "B-ADVP", "O", "B-PP", "B-NP", "I-NP", "B-PP", "B-NP", "O", "B-NP", "I-NP", "O", "B-NP", "O", "B-NP", "O", "O", "O", "B-NP", "B-PP", "B-NP", "I-NP", "B-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "O", "B-NP", "O", "B-NP", "I-NP", "O", "B-NP", "O", "B-NP", "O", "B-ADJP", "O", "B-PP", "B-NP", "I-NP", "I-NP", "O"
            };
            super.dependencyToken1 = new String[]{
                    "We", "the", "two", "SNPs", "rs37973", "and", "rs1876828", "into", "a", "predictive", "test", "of", "FEV1", "change", "using", "a", "Bayesian", "model", ",", "which", "identified", "patients", "with", "good", "or", "poor", "steroid", "response", "(", "highest", "or", "lowest", "quartile", ",", "respectively", ")", "with", "predictive", "performance", "of", "65", ".", "7", "%", "(", "P=0", ".", "039", "vs", "random", ")", "area", "under", "the", "receiver", "-", "operator", "characteristic", "curve", "in", "the", "training", "population", "and", "65", ".", "9", "%", "(", "P=0", ".", "025", "vs", "random", ")", "in", "the", "test", "population", "."
            };
            super.dependencyToken2 = new String[]{
                    "combined", "SNPs", "SNPs", "rs1876828", "rs1876828", "rs1876828", "combined", "combined", "test", "test", "into", "test", "change", "of", "combined", "model", "model", "using", "model", "model", "which", "identified", "patients", "response", "response", "response", "response", "with", ")", ")", "quartile", "quartile", "highest", "highest", "highest", "response", "patients", "performance", "with", "performance", "of", "using", "%", "area", ")", "random", "P=0", "random", "random", ")", "%", "65", "area", "curve", "curve", "curve", "curve", "curve", "under", "area", "population", "population", "in", "65", ".", "65", "%", ".", ")", "random", "P=0", "random", "random", ")", "%", "%", "population", "population", "in", "combined"
            };
            super.dependencyTagName = new String[]{
                    "SUB", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "OBJ", "VMOD", "NMOD", "NMOD", "PMOD", "NMOD", "NMOD", "PMOD", "VMOD", "NMOD", "NMOD", "OBJ", "P", "NMOD", "SBAR", "OBJ", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "PMOD", "DEP", "DEP", "NMOD", "NMOD", "DEP", "P", "NMOD", "NMOD", "NMOD", "NMOD", "PMOD", "NMOD", "PMOD", "VMOD", "NMOD", "NMOD", "DEP", "NMOD", "P", "NMOD", "NMOD", "DEP", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "PMOD", "NMOD", "NMOD", "NMOD", "PMOD", "NMOD", "PMOD", "NMOD", "NMOD", "OBJ", "DEP", "NMOD", "P", "NMOD", "NMOD", "DEP", "NMOD", "NMOD", "NMOD", "NMOD", "PMOD", "P"
            };
            super.dependencyOrdinal = new int[]{
                    5, 1, 1, 1, 1, 1, 6, 4, 1, 1, 3, 1, 1, 3, 4, 1, 1, 6, 8, 1, 9, 6, 1, 1, 1, 1, 1, 3, 7, 7, 1, 1, 7, 8, 1, 1, 1, 1, 3, 1, 3, 4, 1, 1, 7, 1, 8, 1, 1, 7, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 3, 1, 3, 1, 1, 6, 7, 1, 8, 1, 1, 7, 1, 1, 1, 1, 3, 8
            };
            return this;
        }

        private str2 opennlp(){ // elements like "P=0.039" are maintained, but at elements "vs" the sentences are splitted
            super.sentenceStart = new int[]{ 0, 259, 370 };
            super.sentenceEnd = new int[]{ 258, 369, 404 };
            super.tokenStart = new int[]{
                    0, 3, 12, 16, 20, 25, 33, 37, 47, 52, 54, 65, 70, 73, 78, 85, 91, 93, 102, 107, 109, 115, 126, 135, 140, 145, 148, 153, 161, 170, 171, 179, 182, 189, 197, 199, 211, 213, 218, 229, 241, 244, 248, 250, 251,
                    0, 3, 9, 11, 16, 22, 26, 44, 59, 65, 68, 72, 81, 92, 96, 100, 102, 103,
                    0, 3, 9, 11, 14, 18, 23, 33
            };
            super.tokenEnd = new int[]{
                    1, 10, 14, 18, 23, 31, 35, 45, 50, 52, 63, 68, 71, 76, 83, 89, 91, 100, 106, 107, 113, 124, 133, 138, 143, 146, 151, 159, 168, 170, 177, 180, 187, 196, 197, 210, 211, 216, 227, 239, 242, 247, 248, 250, 257,
                    1, 8, 9, 14, 20, 24, 42, 57, 63, 66, 70, 79, 90, 94, 99, 100, 102, 109,
                    1, 8, 9, 12, 16, 21, 32, 33
            };
            super.tokenText = new String[]{
                    "We", "combined", "the", "two", "SNPs", "rs37973", "and", "rs1876828", "into", "a", "predictive", "test", "of", "FEV1", "change", "using", "a", "Bayesian", "model", ",", "which", "identified", "patients", "with", "good", "or", "poor", "steroid", "response", "(", "highest", "or", "lowest", "quartile", ",", "respectively", ")", "with", "predictive", "performance", "of", "65.7", "%", "(", "P=0.039", "vs", "random", ")", "area", "under", "the", "receiver-operator", "characteristic", "curve", "in", "the", "training", "population", "and", "65.9", "%", "(", "P=0.025", "vs", "random", ")", "in", "the", "test", "population", "."
            };
            super.POSValue = new String[]{
                    "PRP", "VBD", "DT", "CD", "NNS", "VBD", "CC", "VBD", "IN", "DT", "JJ", "NN", "IN", "JJ", "NN", "VBG", "DT", "NNP", "NN", ",", "WDT", "VBD", "NNS", "IN", "JJ", "CC", "JJ", "JJ", "NN", "-LRB-", "JJS", "CC", "JJS", "NN", ",", "RB", "-RRB-", "IN", "JJ", "NN", "IN", "CD", "NN", "-LRB-", "CD", "JJ", "JJ", "-RRB-", "NN", "IN", "DT", "NN", "JJ", "NN", "IN", "DT", "NN", "NN", "CC", "CD", "NN", "-LRB-", "CD", "JJ", "JJ", "-RRB-", "IN", "DT", "NN", "NN", "."
            };
            super.chunkStart = new int[]{
                    0, 1, 2, 5, 6, 7, 8, 9, 12, 13, 15, 16, 19, 20, 21, 22, 23, 24, 34, 35, 36, 37, 38, 40, 41, 43,
                    0, 4, 5, 9, 10, 13, 14, 16,
                    0, 3, 4, 7
            };
            super.chunkEnd = new int[]{
                    0, 1, 4, 5, 6, 7, 8, 11, 12, 14, 15, 18, 19, 20, 21, 22, 23, 33, 34, 35, 36, 37, 39, 40, 42, 44,
                    3, 4, 8, 9, 12, 13, 15, 17,
                    2, 3, 6, 7
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.NP, ChunkTag.VP, ChunkTag.NP, ChunkTag.VP, ChunkTag.O, ChunkTag.VP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.VP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.VP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.ADVP, ChunkTag.VP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O
            };
            super.chunkBIOTag = new String[]{
                    "B-NP", "B-VP", "B-NP", "I-NP", "I-NP", "B-VP", "O", "B-VP", "B-PP", "B-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP", "B-VP", "B-NP", "I-NP", "I-NP", "O", "B-NP", "B-VP", "B-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "O", "B-ADVP", "B-VP", "B-PP", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "O", "O", "B-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "O", "B-NP", "I-NP", "O", "O", "B-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "O"
            };
            return this;
        }
    }

    static final class str3 extends Variables {
        static str3 gdep = new str3().gdep();
        static str3 opennlp = new str3().opennlp();

        private str3(){
            super.text = "29 May 2012; doi:10.1038/tpj.2012.15.";
            super.sentenceStart = new int[]{ 0, 194, 211 };
            super.sentenceEnd = new int[]{ 37, 210, 396 };
            super.tokenLabel = Constants.LabelTag.O;
        }

        private str3 gdep(){ // elements like "doi:10.1038" are separated into "doi", ":", "10", "." and "1038"
            super.tokenStart = new int[]{
                    0, 3, 7, 11, 13, 16, 17, 19, 20, 24, 25, 28, 29, 33, 34, 36
            };
            super.tokenEnd = new int[]{
                    1, 5, 10, 11, 15, 16, 18, 19, 23, 24, 27, 28, 32, 33, 35, 36, 29
            };
            super.tokenText = new String[]{
                    "29", "May", "2012", ";", "doi", ":", "10", ".", "1038", "/", "tpj", ".", "2012", ".", "15", "."
            };
            super.POSValue = new String[]{
                    "CD", "NNP", "CD", ":", "NNS", ":", "CD", ".", "CD", "SYM", "NN", ".", "CD", ".", "CD", "."
            };
            super.chunkStart = new int[]{
                    0, 3, 4, 5, 6, 7, 8, 11, 12, 15
            };
            super.chunkEnd = new int[]{
                    2, 3, 4, 5, 6, 7, 10, 11, 14, 15
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O
            };
            super.chunkBIOTag = new String[]{
                    "B-NP", "I-NP", "I-NP", "O", "B-NP", "O", "B-NP", "O", "B-NP", "I-NP", "I-NP", "O", "B-NP", "I-NP", "I-NP", "O"
            };
            super.dependencyToken1 = new String[]{
                    "29", "May", "2012", ";", "doi", ":", "10", ".", "1038", "/", "tpj", ".", "2012", ".", "."
            };
            super.dependencyToken2 = new String[]{
                    "May", "15", "May", "15", "15", "15", "15", "15", "tpj", "tpj", "15", "tpj", "tpj", "tpj", "15"
            };
            super.dependencyTagName = new String[]{
                    "NMOD", "NMOD", "NMOD", "P", "NMOD", "P", "NMOD", "NMOD", "NMOD", "NMOD", "NMOD", "P", "NMOD", "P", "P"
            };
            super.dependencyOrdinal = new int[]{
                    1, 1, 1, 8, 1, 8, 1, 1, 1, 1, 1, 8, 1, 8, 8
            };
            return this;
        }

        private str3 opennlp(){ // elements like "doi:10.1038" are NOT separated
            super.tokenStart = new int[]{
                    0, 3, 7, 11, 13, 36
            };
            super.tokenEnd = new int[]{
                    1, 5, 10, 11, 35, 36
            };
            super.tokenText = new String[]{
                    "29", "May", "2012", ";", "doi:10.1038/tpj.2012.15", "."
            };
            super.POSValue = new String[]{
                    "CD", "NNP", "CD", ":", "NN", "."
            };
            super.chunkStart = new int[]{
                    0, 3, 4, 5
            };
            super.chunkEnd = new int[]{
                    2, 3, 4, 5
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O
            };
            super.chunkBIOTag = new String[]{
                    "B-NP", "I-NP", "I-NP", "O", "B-NP", "O"
            };
            return this;
        }
    }

    static final class str4 extends Variables {
        static str4 gdep = new str4().gdep();
        static str4 opennlp = new str4().opennlp();

        private str4(){
            super.text = "AA 12 - AA12 AA-12 (12AA) 12.-.";
            super.sentenceStart = new int[]{ 0, 194, 211 };
            super.sentenceEnd = new int[]{ 31, 210, 396 };
            super.tokenLabel = Constants.LabelTag.O;
        }

        private str4 gdep(){ // elements like "12.-." are separated into "12", ".", "-" and "."
            super.tokenStart = new int[]{
                    0, 3, 6, 8, 13, 15, 16, 19, 20, 24, 26, 28, 29, 30
            };
            super.tokenEnd = new int[]{
                    1, 4, 6, 11, 14, 15, 17, 19, 23, 24, 27, 28, 29, 30
            };
            super.tokenText = new String[]{
                    "AA", "12", "-", "AA12", "AA", "-", "12", "(", "12AA", ")", "12", ".", "-", "."
            };
            super.POSValue = new String[]{
                    "NN", "CD", "HYPH", "NN", "NN", "HYPH", "CD", "(", "NN", ")", "CD", ".", "HYPH", "."
            };
            super.chunkStart = new int[]{
                    0, 5, 7, 8, 9, 10, 11
            };
            super.chunkEnd = new int[]{
                    4, 6, 7, 8, 9, 10, 13
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.NP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.O
            };
            super.chunkBIOTag = new String[]{
                    "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "B-NP", "I-NP", "O", "B-NP", "O", "B-NP", "O", "O", "O"
            };
            super.dependencyToken1 = new String[]{
                    "AA", "12", "-", "AA12", "-", "12", "(", "12AA", ")", "12", ".", "-", "."
            };
            super.dependencyToken2 = new String[]{
                    "AA", "AA", "AA", "AA", ")", ")", ")", ")", "AA", "AA", "AA", "AA", "AA"
            };
            super.dependencyTagName = new String[]{
                    "NMOD", "NMOD", "P", "NMOD", "P", "NMOD", "DEP", "DEP", "NMOD", "NMOD", "P", "P", "P"
            };
            super.dependencyOrdinal = new int[]{
                    1, 1, 8, 1, 8, 1, 7, 7, 1, 1, 8, 8, 8
            };
            return this;
        }

        private str4 opennlp(){ // elements like "12.-." are NOT separated
            super.tokenStart = new int[]{
                    0, 3, 6, 8, 13, 19, 20, 24, 26
            };
            super.tokenEnd = new int[]{
                    1, 4, 6, 11, 17, 19, 23, 24, 30
            };
            super.tokenText = new String[]{
                    "AA", "12", "-", "AA12", "AA-12", "(", "12AA", ")", "12.-."
            };
            super.POSValue = new String[]{
                    "NNP", "CD", ":", "NN", "NN", "-LRB-", "CD", "-RRB-", "."
            };
            super.chunkStart = new int[]{
                    0, 2, 3, 4, 8
            };
            super.chunkEnd = new int[]{
                    1, 2, 3, 7, 8
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.NP, ChunkTag.O, ChunkTag.VP, ChunkTag.NP, ChunkTag.O
            };
            super.chunkBIOTag = new String[]{
                    "B-NP", "I-NP", "O", "B-VP", "B-NP", "I-NP", "I-NP", "I-NP", "O"
            };
            return this;
        }
    }



    static final class str5 extends Variables {
        static str5 gdep = new str5().gdep();
        static str5 opennlp = new str5().opennlp();

        private str5(){
            super.text = "❁✐♣❧♥✉❡❂✧❥✈✐♣✧ ②♣❡❂✧❡①\t✴❥✈✐♣t✧❃\n❧❡✭✧✐❡♦✦✧✮❀ ❁✴✐♣❃";
            super.sentenceStart = new int[]{ 0 };
            super.sentenceEnd = new int[]{ 49 };
            super.tokenLabel = Constants.LabelTag.O;
        }

        private str5 gdep(){ // elements after "\n" are not tokenized
            super.tokenStart = new int[]{
                    0, 15, 23
            };
            super.tokenEnd = new int[]{
                    13, 21, 30
            };
            super.tokenText = new String[]{
                    "❁✐♣❧♥✉❡❂✧❥✈✐♣✧", "②♣❡❂✧❡①", "✴❥✈✐♣t✧❃"
            };
            super.POSValue = new String[]{
                    "LS", ":", "NN"
            };
            super.chunkStart = new int[]{
                    0, 1, 2
            };
            super.chunkEnd = new int[]{
                    0, 1, 2
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.LST, ChunkTag.O, ChunkTag.NP
            };
            super.chunkBIOTag = new String[]{
                    "B-LST", "O", "B-NP"
            };
            super.dependencyToken1 = new String[]{
                    "❁✐♣❧♥✉❡❂✧❥✈✐♣✧", "②♣❡❂✧❡①"
            };
            super.dependencyToken2 = new String[]{
                    "✴❥✈✐♣t✧❃", "❁✐♣❧♥✉❡❂✧❥✈✐♣✧"
            };
            super.dependencyTagName = new String[]{
                    "NMOD", "P"
            };
            super.dependencyOrdinal = new int[]{
                    1, 8
            };
            return this;
        }

        private str5 opennlp(){ // nothing is ignored and most symbols are separated from each other
            super.tokenStart = new int[]{
                    0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 17, 18, 19, 20, 21, 23, 25, 26, 30, 32, 34, 35, 36, 37, 38, 39, 40, 41, 42, 44, 46, 47, 48
            };
            super.tokenEnd = new int[]{
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 17, 18, 19, 20, 21, 24, 25, 29, 30, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 45, 46, 47, 48
            };
            super.tokenText = new String[]{
                    "❁✐", "♣", "❧", "♥", "✉", "❡", "❂", "✧", "❥", "✈", "✐", "♣", "✧", "②♣", "❡", "❂", "✧", "❡", "①", "✴❥", "✈", "✐♣t✧", "❃", "❧❡", "✭", "✧", "✐", "❡", "♦", "✦", "✧", "✮", "❀", "❁✴", "✐", "♣", "❃"
            };
            super.POSValue = new String[]{
                    "JJ", "NN", "IN", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", ".", "JJ", "NN", "IN", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "NNP", "."
            };
            super.chunkStart = new int[]{
                    0, 2, 3, 22, 23, 25, 26, 36
            };
            super.chunkEnd = new int[]{
                    1, 2, 21, 22, 24, 25, 35, 36
            };
            super.chunkTag = new ChunkTag[]{
                    ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O, ChunkTag.NP, ChunkTag.PP, ChunkTag.NP, ChunkTag.O
            };
            super.chunkBIOTag = new String[]{
                    "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "O", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "O"
            };
            return this;
        }
    }

//    private static Constants.LabelTag[] defaultLabelTagArray;
//    private static Constants.LabelTag[] defaultLabelTagArray() {
//        if(defaultLabelTagArray ==null){
//            defaultLabelTagArray = new Constants.LabelTag[100];
//            Arrays.fill(defaultLabelTagArray, Constants.LabelTag.O);
//        }
//
//        return defaultLabelTagArray;
//    }

    String text;
    int[] sentenceStart;
    int[] sentenceEnd;
    int[] tokenStart;
    int[] tokenEnd;
    String[] tokenText;
    //    Constants.LabelTag[] tokenLabel;
    Constants.LabelTag tokenLabel;
    String[] POSValue;
    int[] chunkStart;
    int[] chunkEnd;
    ChunkTag[] chunkTag;
    String[] chunkBIOTag;
    String[] dependencyToken1;
    String[] dependencyToken2;
    String[] dependencyTagName;
    int[] dependencyOrdinal;

    private Variables(){}
}
