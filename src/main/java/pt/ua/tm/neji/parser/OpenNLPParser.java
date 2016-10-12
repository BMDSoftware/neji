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
package pt.ua.tm.neji.parser;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.exception.NejiException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * OpenNLP Parser wrapper implementation.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 * @since 1.0
 */
public class OpenNLPParser extends Parser implements AutoCloseable {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(OpenNLPParser.class);

    private String languageModelsPath;
    private SentenceDetector sdetector;
    private Tokenizer tokenizer;
    private POSTagger tagger;
    private Chunker chunker;


    public OpenNLPParser(final ParserLanguage language,
                         final ParserLevel level) throws NejiException {
        this(language, level, Constants.OPENNLP_DIR);
    }

    public OpenNLPParser(final ParserLanguage language,
                         final ParserLevel level,
                         final String parserPath) throws NejiException {

        super(ParserTool.OPENNLP, language, level); // validates specified options

        // Check provided directory
        File f1 = new File(parserPath);
        if (!f1.canRead()) {
            throw new RuntimeException("Provided tool directory is not readable: " + f1.getAbsolutePath());
        }
        if (!f1.isDirectory()) {
            throw new RuntimeException("Provided tool directory is not a directory: " + f1.getAbsolutePath());
        }

        // Check "models" sub-directory
        File f2 = new File(f1, "models");
        if (!f2.exists() || !f2.canRead() || !f2.isDirectory()) {
            throw new RuntimeException("Provided tool directory does not contain a 'models' sub-directory: "
                    + f1.getAbsolutePath());
        }

        File result = new File(f2, language.getDirName());
        this.languageModelsPath = result.getAbsolutePath() + File.separator;
    }


    /**
     * Launch the parser.
     *
     * @throws IOException Problem launching the parser.
     */
    @Override
    public Parser launch() throws IOException {
        if(super.launch() == null)
            return this;

        try(InputStream sentIs = new FileInputStream(languageModelsPath +"sent.bin")) {
            sdetector = new SentenceDetectorME(new SentenceModel(sentIs));

            if (level.equals(ParserLevel.TOKENIZATION) ||
                    level.equals(ParserLevel.POS) ||
                    level.equals(ParserLevel.CHUNKING)) {
                try (InputStream is = new FileInputStream(languageModelsPath + "token.bin")) {
                    tokenizer = new TokenizerME(new TokenizerModel(is));
                }
            }

            if (level.equals(ParserLevel.POS) ||
                    level.equals(ParserLevel.CHUNKING)) {
                tagger = new POSTaggerME(new POSModelLoader().load(new File(languageModelsPath + "pos.bin")));
            }

            if (level.equals(ParserLevel.CHUNKING)) {
                try (InputStream is = new FileInputStream(languageModelsPath +"chunk.bin")) {
                    chunker = new ChunkerME(new ChunkerModel(is));
                }
            }
            return this;
        }
    }


    /**
     * Terminates the execution of the parser.
     */
    @Override
    public void close() {
        super.close();
        sdetector = null;
        tokenizer = null;
        tagger = null;
        chunker = null;
        System.gc();
    }


    /**
     * Parse a sentence using OpenNLP
     * return list of parsed sentences, all of them that were included in Corpus
     */
    public List<Sentence> parse(Corpus corpus, String text) throws NejiException {
        return parseWithLevel_(level, corpus, text);
    }

    @Override
    protected List<Sentence> parseWithLevel_(ParserLevel parserLevel, Corpus corpus, String text) throws NejiException {
        List<Sentence> _LIST = new ArrayList<>();
        if (!isLaunched()) {
            return null;
        }
        try{
            int start, end = 0;
            String[] sentences = sdetector.sentDetect(text);
            int startIndex = 0;
            for(String str : sentences) {
                start = text.indexOf(str, end);
                end = start + str.length();
                
                // Set sentence indexes
                Sentence s = new Sentence(corpus);
                s.setStart(start);
                s.setEnd(end);
                
                int index;
            
            // Set sentence original start index
                if (startIndex < start) {
                    // Verify if it has spaces or tabs before start index
                    for (index = start - 1; index >= startIndex; index--) {
                        if ((text.charAt(index) != ' ') && (text.charAt(index) != '\t')) {
                            break;
                        }
                    }
                    s.setOriginalStart(index + 1);
                } else {
                    s.setOriginalStart(start);
                }

                // Set sentence original end index
                int originalEnd = -1;

                // Verify if it has spaces or tabs, finishing with 1 or more \n, after end index
                for (index = end; index < text.length(); index++) {
                    if ((text.charAt(index) == '\n')) {
                        originalEnd = index;
                    } else if ((text.charAt(index) != ' ') && (text.charAt(index) != '\t')) {
                        break;
                    }
                }

                if (originalEnd != -1) {
                    s.setOriginalEnd(originalEnd + 1);
                    startIndex = originalEnd + 1;
                } else {
                    s.setOriginalEnd(end);
                    startIndex = end;
                }

                switch (parserLevel) {
                    case TOKENIZATION:
                        tkn(s, str);
                        break;
                    case POS:
                        pos(s, str, false);
                        break;
                    case CHUNKING:
                        pos(s, str, true);
                        break;
                }

                corpus.addSentence(s);
                _LIST.add(s);
            }
        } catch (IOException ex) {
            throw new NejiException("An error occurred while parsing the sentence.", ex);
        }

        return _LIST;
    }

    private void tkn(Sentence s, String sentenceText) throws IOException {
        String[] tokens = tokenizer.tokenize(sentenceText);
        addTokens(s, sentenceText, tokens, null, null);
    }



    private void pos(Sentence s, String sentenceText, boolean doChunking) throws IOException {
        List<String> tokensChunk = new ArrayList<>();
        List<String> tagsChunk = new ArrayList<>();
        ObjectStream<String> lineStream = new PlainTextByLineStream(
                new StringReader(s.getText()));

        String line;
        while ((line = lineStream.read()) != null) {
            String[] tokens = tokenizer.tokenize(line);
            String[] tags = tagger.tag(tokens);

            if(doChunking) {
                Collections.addAll(tokensChunk, tokens);
                Collections.addAll(tagsChunk, tags);
            }
            else
                addTokens(s, sentenceText, tokens, tags, null);
        }

        if(doChunking)
            chunking(s, sentenceText, tokensChunk.toArray(new String[0]), tagsChunk.toArray(new String[0]));

    }



    private void chunking(Sentence s, String sentenceText, String[] tokens, String[] tags) throws IOException {

        String[] chunks = chunker.chunk(tokens, tags);
        if (chunks.length!=0) {
            s.setChunks(getChunkList(s, Arrays.asList(chunks)));
        }
        addTokens(s, sentenceText, tokens, tags, chunks);
    }



    private void addTokens(Sentence s, String sentenceText, String[] tokens, String[] posTags, String[] chunks) {
        // Sentence s is only used to allow Token creation
        // calling the method "addSentencesToCorpus" will set a new Sentence according to the specified Corpus
        int start, end, tokenCounter = 0, tokenOffset = 0;
        for (String tokenText : tokens) {

            start = sentenceText.indexOf(tokenText, tokenOffset);
            end = start + tokenText.length() - 1;
            Token token = new Token(s, start, end, tokenCounter);

//            logger.info("tokenText:'{}'  \tstart:{}  \tend:{} \tcounter:{}", new Object[]{tokenText, start, end, tokenCounter+1});
//            logger.info("\t\t\t\t{}", token.getText());


            if (posTags != null) {
                String tag = posTags[tokenCounter];
                token.putFeature("POS", tag);
//                token.addFeature("POS=" + tag);
            }
            if (chunks != null) {
                String chunk = chunks[tokenCounter];
                token.putFeature("CHUNK", chunk);
//                token.addFeature("CHUNK=" + chunk);
            }
            s.addToken(token);
            tokenOffset = end + 1;

            tokenCounter++;
        }
    }
}
