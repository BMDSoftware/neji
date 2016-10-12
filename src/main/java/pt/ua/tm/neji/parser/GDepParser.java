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

import com.aliasi.util.Pair;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.corpus.ChunkList;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.corpus.dependency.DependencyTag;
import pt.ua.tm.neji.core.corpus.dependency.LabeledEdge;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.logger.LoggingOutputStream;
import pt.ua.tm.neji.parser.gdep.ProcessConnector;
import pt.ua.tm.neji.sentencesplitter.SentenceSplitter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GDep Parser wrapper implementation.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class GDepParser extends Parser implements AutoCloseable {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(GDepParser.class);

    private String[] parserCommand;
    private File dir;
    private PipedInputStream pis;
    private PipedOutputStream sink;
    private PipedOutputStream pos;
    private PipedInputStream source;
    private BufferedReader br;
    private BufferedWriter bw;
    private ProcessConnector pc;
    private SentenceSplitter splitter;


    public GDepParser(final ParserLanguage language,
                      final ParserLevel level,
                      final SentenceSplitter splitter,
                      final boolean doWhiteSpaceTokenization,
                      final String toolDir) throws NejiException {
        super(ParserTool.GDEP, language, level); // validates specified options
        this.splitter = splitter;

        // Check provided directory
        File f = new File(toolDir);

        if (!f.canRead()) {
            throw new RuntimeException("Provided tool directory is not readable: " + f.getAbsolutePath());
        }
        if (!f.isDirectory()) {
            throw new RuntimeException("Provided tool directory is not a directory: " + f.getAbsolutePath());
        }

        // Set new parser path
        String toolPath = new File(f, Constants.getGDepTool()).getAbsolutePath();

        List<String> command = new ArrayList<String>();
        command.add(toolPath);

        addArguments(level, doWhiteSpaceTokenization, command);

        dir = f;
        parserCommand = command.toArray(new String[command.size()]);
    }


    public GDepParser(final ParserLanguage language,
                      final ParserLevel level,
                      final SentenceSplitter splitter,
                      final boolean doWhiteSpaceTokenization) throws NejiException {
        this(language, level, splitter, doWhiteSpaceTokenization, Constants.GDEP_DIR);
    }


    private void addArguments(final ParserLevel parserLevel,
                              final boolean doWhiteSpaceTokenization, List<String> command) {
        if (doWhiteSpaceTokenization) {
            command.add("-wst");
        }

        if (parserLevel.equals(ParserLevel.TOKENIZATION)) {
            command.add("-tok");
        } else if (parserLevel.equals(ParserLevel.POS)) {
            command.add("-pos");
        } else if (parserLevel.equals(ParserLevel.LEMMATIZATION)) {
            command.add("-lemma");
        } else if (parserLevel.equals(ParserLevel.CHUNKING)) {
            command.add("-chunk");
        } else if (parserLevel.equals(ParserLevel.DEPENDENCY)) {
            command.add("-dep");
        }
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
        pis = new PipedInputStream();
        sink = new PipedOutputStream(pis);
        pos = new PipedOutputStream();
        source = new PipedInputStream(pos);
        br = new BufferedReader(new InputStreamReader(source));
        bw = new BufferedWriter(new OutputStreamWriter(sink));

        if (Constants.verbose) {
            pc = new ProcessConnector(pis, pos, new PrintStream(new LoggingOutputStream(LoggerFactory.getLogger(Parser.class), false), true));
        } else {
            pc = new ProcessConnector(pis, pos, new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                }
            }));
        }


        if (dir == null) {
            pc.create(parserCommand);
        } else {
            pc.create(dir, parserCommand);
        }
        return this;
    }

    /**
     * Terminates the execution of the parser.
     */
    @Override
    public void close() {
        super.close();
        try {
            pis.close();
            pis = null;
            sink.close();
            sink = null;
            pos.close();
            pos = null;
            source.close();
            source = null;
            br.close();
            br = null;
            bw.close();
            bw = null;
            pc.destroy();
            System.gc();
        }catch (IOException ex){
            logger.error("Error terminating GDep parser: "+ex.toString());
        }
    }

    /**
     * Parse the specified text using GDepTranslator, creating and structuring Sentences and
     * adding them to the specified Corpus
     *
     * @param text The text to be parsed.
     * @throws NejiException Problem parsing the sentence.
     */
    @Override
    public List<Sentence> parse(Corpus corpus, String text) throws NejiException {
        return parseWithLevel_(level, corpus, text);
    }


    @Override
    public List<Sentence> parseWithLevel_(ParserLevel parserLevel, Corpus corpus, String text) throws NejiException {
        List<Sentence> _LIST = new ArrayList<>();
        if (!isLaunched()) {
            return null;
        }
        int[][] splitIdx = splitter.split(text);
        List<Pair<Integer, Integer>> splitPairList = new ArrayList<>();

        for(int[] newSplittedSentence : splitIdx) {
            int start = newSplittedSentence[0];
            int end = newSplittedSentence[1];
            splitPairList.add(new Pair<>(start, end));
        }

        List<List<Object>> parserOutputList = new ArrayList<>();
        try {
            for(Pair<Integer, Integer> pair : splitPairList) {
                String s = text.substring(pair.a(), pair.b());

                List<Object> results = new ArrayList<Object>();
                parserOutputList.add(results);

                //String parserLevelArg = levelToArg(parserLevel);
                //bw.write(parserLevelArg + "|" + s.trim() + "\n");
                bw.write(s.trim() + "\n");
                bw.flush();

                while (!br.ready()) {
                    // wait for results
                    Thread.yield();
                }
                String line;

                while (!(line = br.readLine()).equalsIgnoreCase("")) {
                    results.add(line);
                }
            }
        } catch (IOException ex) {
            throw new NejiException("An error occurred while parsing the sentence.", ex);
        }

        int k = 0;
        int startIndex = 0;
        for(Pair<Integer, Integer> pair : splitPairList) {
            String sentenceText = text.substring(pair.a(), pair.b());
            Sentence sentence = new Sentence(corpus);
            
            // Set sentence indexes
            sentence.setStart(pair.a());
            sentence.setEnd(pair.b());

            int index;
            
            // Set sentence original start index
            if(startIndex < pair.a()) {                
                // Verify if it has spaces or tabs before start index
                for (index = pair.a() - 1 ; index >= startIndex ; index--) {
                    if ((text.charAt(index) != ' ') && (text.charAt(index) != '\t')) {
                        break;
                    }
                }                
                sentence.setOriginalStart(index + 1);
            } 
            else {
                sentence.setOriginalStart(pair.a());
            }
            
            // Set sentence original end index
            int originalEnd = -1;
            
            // Verify if it has spaces or tabs, finishing with 1 or more \n, after end index
            for (index = pair.b() ; index < text.length() ; index++) {
                if ((text.charAt(index) == '\n')) {
                    originalEnd = index;
                }
                else if ((text.charAt(index) != ' ') && (text.charAt(index) != '\t')) {
                    break;
                }
            }
            
            if (originalEnd != -1) {
                sentence.setOriginalEnd(originalEnd + 1);
                startIndex = originalEnd + 1;
            }
            else {
                sentence.setOriginalEnd(pair.b());
                startIndex = pair.b();
            }
            
            // Parse tokens
            translate(sentence, sentenceText, parserOutputList.get(k++));
            corpus.addSentence(sentence);
            _LIST.add(sentence);
        }

        return _LIST;
    }

    private String levelToArg(final ParserLevel parserLevel){
        switch (parserLevel){
            case TOKENIZATION: return "tok";
            case POS: return "pos";
            case LEMMATIZATION: return "lem";
            case CHUNKING: return "chu";
            case DEPENDENCY: return "dep";
            default: return null;
        }
    }

    private void translate(Sentence sentence, String sentenceText, final List<Object> parserOutput) {
        int start = 0, end = 0, tokenCounter = 0, offset = 0;

        UndirectedGraph<Token, LabeledEdge> dependencyGraph = new SimpleGraph<>(LabeledEdge.class);

        DependencyList dependencyList = new DependencyList();
        List<String> chunkTags = new ArrayList<String>();
        for (Object result : parserOutput) {
            String[] parts = result.toString().split("\t");

            // Get parsing results
            String tokenText = null, lemma = null, pos = null, chunk = null;
            DependencyTag depTag = null;
            Integer depToken = null;
            if (parts.length >= 3) { // Tokenization parsing
                tokenText = parts[1];
            }
            if (parts.length >= 4) { // Lemmatization parsing
                lemma = parts[2];
            }
            if (parts.length >= 5) { // POS parsing
                pos = parts[3];
            }
            if (parts.length >= 6) { // Chunking parsing
                chunk = parts[3];
                pos = parts[4];
            }
            if (parts.length >= 8) { // Dependency Parsing
                depToken = Integer.valueOf(parts[6]) - 1;
                depTag = DependencyTag.valueOf(parts[7]);
                pos = parts[4];
                chunk = parts[3];
            }

            // Get end without white spaces
            end = start + tokenText.length() - 1;           

            // Create token
            Token token = new Token(sentence, start, end, tokenCounter++);

            if (sentenceText != null) {
                // Get start and end source with white spaces
                start = sentenceText.indexOf(tokenText, offset);
                end = start + tokenText.length() - 1;

                // Set start and end source
                token.setStart(start);
                token.setEnd(end);
            }

            // Add features
            if (lemma != null) {
                token.putFeature("LEMMA", lemma);
//                token.addFeature("LEMMA=" + lemma);
            }
            if (pos != null) {
                token.putFeature("POS", pos);
//                token.addFeature("POS=" + pos);
            }
            if (chunk != null) {
                token.putFeature("CHUNK", chunk);
                chunkTags.add(chunk);
//                token.addFeature("CHUNK=" + chunk);
            }


            // Add dependencies to build graph.
            if (depToken != null && depTag != null) {
                dependencyList.add(new Dependency(depTag, depToken));
                dependencyGraph.addVertex(token);
                
                // Add dependency features to token
                token.putFeature("DEP_TOK", depToken.toString());
                token.putFeature("DEP_TAG", depTag.name());
            }

            // Add token to sentence
            sentence.addToken(token);

            // Offsets
            start += tokenText.length();
            offset = end + 1;
        }
        
        // Add dependency edges
        Token from;
        Token to = null;
        if (!dependencyList.isEmpty()) {
            // Add edges
            for (int i = 0; i < dependencyList.size(); i++) {
                Dependency dependency = dependencyList.get(i);
                if (dependency.tag.equals(DependencyTag.ROOT)) {
                    continue;
                }

                from = sentence.getToken(i);
                to = sentence.getToken(dependency.index);

                LabeledEdge<Token, DependencyTag> edge = new LabeledEdge<>(from, to, dependency.tag);
                dependencyGraph.addEdge(from, to, edge);
                
                // Add dependency features
                from.setDependencyFeatures();
            }

        }
        
        // Set dependency graph
        sentence.setDependencyGraph(dependencyGraph);

        // Add chunks
        ChunkList chunkList = new ChunkList(sentence);
        if (!chunkTags.isEmpty()) {
            chunkList = getChunkList(sentence, chunkTags);
        }
        sentence.setChunks(chunkList);
    }
    
    /**
     * Parse a sentence using GDepTranslator.
     *
     * @param sentence The sentence to be parsed.
     * @return Output of GDepTranslator parser.
     * @throws NejiException Problem parsing the sentence.
     */
    public List<Object> parse(final String sentence) throws NejiException {
        if (!isLaunched()) {
            return null;
        }

        List<Object> results = new ArrayList<Object>();
        try {

            bw.write(sentence.trim() + "\n");
            bw.flush();

            while (!br.ready()) {
                // wait for results
                Thread.yield();
            }
            String line;

            while (!(line = br.readLine()).equalsIgnoreCase("")) {
                results.add(line);
            }
        } catch (IOException ex) {
            throw new NejiException("An error occured while parsing the sentence.", ex);
        }

        return results;
    }   
    
    public List<Sentence> parseWithLevel(ParserLevel parserLevel, Corpus corpus, String text, List<Pair<Integer, Integer>> splitPairList) throws NejiException {
        
        List<Sentence> list = new ArrayList<>();
        if (!isLaunched()) {
            return null;
        }
        
        if (splitPairList == null)
        {
            int[][] splitIdx = splitter.split(text);
            splitPairList = new ArrayList<>();

            for(int[] newSplittedSentence : splitIdx) {
                int start = newSplittedSentence[0];
                int end = newSplittedSentence[1];
                splitPairList.add(new Pair<>(start, end));
            }
        }
 
        List<List<Object>> parserOutputList = new ArrayList<>();
        //int counter = 0;
        //System.out.printf("\r\t%d/%d", counter, splitPairList.size());
        try {
            for(Pair<Integer, Integer> pair : splitPairList) {
                String s = text.substring(pair.a(), pair.b());

                List<Object> results = new ArrayList<Object>();
                parserOutputList.add(results);

                //String parserLevelArg = levelToArg(parserLevel);
                //bw.write(parserLevelArg + "|" + s.trim() + "\n");
                bw.write(s.trim() + "\n");
                bw.flush();

                while (!br.ready()) {
                    // wait for results
                    Thread.yield();
                }
                String line;

                while (!(line = br.readLine()).equalsIgnoreCase("")) {
                    results.add(line);
                }
                
                // Feedback
                //System.out.printf("\r\t%d/%d", ++counter, splitPairList.size());
            }
        } catch (IOException ex) {
            throw new NejiException("An error occurred while parsing the sentence.", ex);
        }

        int k = 0;
        int startIndex = 0;
        for(Pair<Integer, Integer> pair : splitPairList) {
            String sentenceText = text.substring(pair.a(), pair.b());
            Sentence sentence = new Sentence(corpus);
            
            // Set sentence indexes
            sentence.setStart(pair.a());
            sentence.setEnd(pair.b());

            int index;
            
            // Set sentence original start index
            if(startIndex < pair.a()) {                
                // Verify if it has spaces or tabs before start index
                for (index = pair.a() - 1 ; index >= startIndex ; index--) {
                    if ((text.charAt(index) != ' ') && (text.charAt(index) != '\t')) {
                        break;
                    }
                }                
                sentence.setOriginalStart(index + 1);
            } 
            else {
                sentence.setOriginalStart(pair.a());
            }
            
            // Set sentence original end index
            int originalEnd = -1;
            
            // Verify if it has spaces or tabs, finishing with 1 or more \n, after end index
            for (index = pair.b() ; index < text.length() ; index++) {
                if ((text.charAt(index) == '\n')) {
                    originalEnd = index;
                }
                else if ((text.charAt(index) != ' ') && (text.charAt(index) != '\t')) {
                    break;
                }
            }
            
            if (originalEnd != -1) {
                sentence.setOriginalEnd(originalEnd + 1);
                startIndex = originalEnd + 1;
            }
            else {
                sentence.setOriginalEnd(pair.b());
                startIndex = pair.b();
            }
            
            // Parse tokens
            translate(sentence, sentenceText, parserOutputList.get(k++));
            corpus.addSentence(sentence);
            list.add(sentence);
        }

        return list;
    }

    private class Dependency {
        DependencyTag tag;
        int index;

        private Dependency(DependencyTag tag, int index) {
            this.tag = tag;
            this.index = index;
        }
    }

    private class DependencyList extends ArrayList<Dependency> {
    }
}
