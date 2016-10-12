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
package pt.ua.tm.neji.core.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.corpus.Chunk;
import pt.ua.tm.neji.core.corpus.ChunkList;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.dependency.ChunkTag;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.parser.GDepParser;
import pt.ua.tm.neji.parser.OpenNLPParser;
import pt.ua.tm.neji.sentencesplitter.LingpipeSentenceSplitter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * External parser wrapper.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public abstract class Parser implements AutoCloseable {

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Parser.class);

    private boolean hasInstance;

    private ParserTool tool;
    protected ParserLanguage language;
    protected ParserLevel level;

    protected Parser(ParserTool tool, ParserLanguage language, ParserLevel level) throws NejiException {
        validate(tool, language, level);
        hasInstance = false;
        this.tool = tool;
        this.language = language;
        this.level = level;
    }

    /**
     * Launch the parser.
     * @throws NejiException if there was a problem launching the parser.
     */
    public Parser launch() throws IOException {
        if (hasInstance) {
            return null;
        }
        hasInstance = true;
        return this;
    }

    public boolean isLaunched() {
        return hasInstance;
    }

    /**
     * Terminates the execution of the parser.
     */
    @Override
    public void close(){
        if(!hasInstance)
            return;
        hasInstance = false;
    }

    public ParserTool getTool() {
        return tool;
    }

    public ParserLanguage getLanguage() {
        return language;
    }

    public ParserLevel getLevel() {
        return level;
    }

    private static void validate(ParserTool tool, ParserLanguage language, ParserLevel level) throws NejiException {
        Collection<ParserLevel> obtainedLevels = ParserSupport.levelsSupportedByLanguage(tool, language);

        if(!obtainedLevels.contains(level)) {
            StringBuffer sb = new StringBuffer();
            sb.append("The specified parser level is unsupported with the specified language or parsing tool!\n");
            sb.append("Levels supported by ");
            sb.append(tool);
            sb.append(" parsing tool in the ");
            sb.append(language);
            sb.append(" language: ");

            if(obtainedLevels.isEmpty()) {
                sb.append("<none>");

            } else {
                for (Iterator<ParserLevel> iter = obtainedLevels.iterator(); iter.hasNext(); ) {
                    sb.append(iter.next());
                    if (iter.hasNext())
                        sb.append(", ");
                }
            }
            sb.append(".");
            throw new NejiException(sb.toString());
        }
    }

    /**
     * Parses the specified text and stores the obtained information in the parser.
     *
     * @param corpus the corpus where the resulting sentences from the parsed text will be stored
     * @param text the text to be parsed.
     * @return list of Sentences parsed from this text.
     *         If the Corpus only has sentences that were obtained from this parsing process, using
     *         {@link pt.ua.tm.neji.core.corpus.Corpus#getSentences()} returns exactly the same list.
     * @throws NejiException if there was a problem parsing the sentence.
     */
    public abstract List<Sentence> parse(Corpus corpus, String text) throws NejiException;

    /**
     * Parses the specified text with the specified level, regardless of the level this parser was
     * initialized with, and stores the obtained information in the parser.
     *
     * @param customLevel the parser level to be used, regardless of the initialized level
     * @param corpus the corpus where the resulting sentences from the parsed text will be stored
     * @param text the text to be parsed.
     * @return list of Sentences parsed from this text.
     *         If the Corpus only has sentences that were obtained from this parsing process, using
     *         {@link pt.ua.tm.neji.core.corpus.Corpus#getSentences()} returns exactly the same list.
     * @throws NejiException if there was a problem parsing the sentence or if the specified parser
     *                       level is higher than the initialized level.
     */
    public final List<Sentence> parseWithLevel(ParserLevel customLevel, Corpus corpus, String text) throws NejiException {
        validate(tool, language, customLevel);
        Collection<ParserLevel> supportedLevels = ParserSupport.getEqualOrLowerSupportedLevels(tool, language, level);
        if(!supportedLevels.contains(customLevel)) {
            String s = "Specified parser level " + customLevel + " cannot be " +
                       "performed by a parser initialized with " + level + "\n" +
                       "Supported levels by this parser: " + supportedLevels.toString();
            throw new NejiException(s);
        }

        return parseWithLevel_(customLevel, corpus, text);
    }

    protected abstract List<Sentence> parseWithLevel_(ParserLevel parserLevel, Corpus corpus, String text) throws NejiException;

    protected ChunkList getChunkList(final Sentence s, final List<String> chunkTags) {
        ChunkList chunkList = new ChunkList(s);

        int chunkCounter = 0;

        for (int i = 0; i < chunkTags.size(); i++) {
            String tag = chunkTags.get(i);
            int startTokenIndex = i, endTokenIndex = 0;

            ChunkTag chunkTag;
            if (tag.contains("B-")) {
                for (int j = i + 1; j < chunkTags.size() && chunkTags.get(j).contains("I-"); j++, i++) {
                }
                endTokenIndex = i;
//                chunkTag = ChunkTag.valueOf(tag.substring(2));
                chunkTag = ChunkTag.valueOf(tag.substring(tag.lastIndexOf("-") + 1));
            } else if (tag.equals("O")) { // Tag is "O"
                for (int j = i + 1; j < chunkTags.size() && chunkTags.get(j).equals("O"); j++, i++) {
                }
                endTokenIndex = i;
                chunkTag = ChunkTag.valueOf(tag);
            } else {
                // For a bug of GDep that has I-tag without B before (FUUUUUU)
                endTokenIndex = i;
                chunkTag = ChunkTag.valueOf(tag.substring(tag.lastIndexOf("-") + 1));
            }
            Chunk chunk = new Chunk(s, chunkCounter++, startTokenIndex, endTokenIndex, chunkTag);
            chunkList.add(chunk);
        }

        return chunkList;
    }

    public static Parser defaultParserFactory(ParserTool tool,
                                              ParserLanguage language,
                                              ParserLevel level,
                                              String parserPath) throws NejiException {

        switch (tool){
            case GDEP:
                if(parserPath != null) {
                    return new GDepParser(language, level, new LingpipeSentenceSplitter(), false, parserPath);
                } else {
                    return new GDepParser(language, level, new LingpipeSentenceSplitter(), false);
                }

            case OPENNLP:
                if(parserPath != null) {
                    return new OpenNLPParser(language, level, parserPath);
                } else {
                    return new OpenNLPParser(language, level);
                }
            default:
                return null;
        }
    }
}
