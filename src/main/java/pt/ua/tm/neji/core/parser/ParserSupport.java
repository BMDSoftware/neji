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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;

import java.util.*;


/**
 * Class with map structures that represent the supported relationship between
 * parsing languages and levels for each implemented tool.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class ParserSupport {

    private static final Map<ParserTool, ImmutableMultimap<ParserLanguage, ParserLevel>> supportMaps = new HashMap<>();

    private ParserSupport() {}

    private static void generateGDepMap() {
        ImmutableMultimap.Builder<ParserLanguage, ParserLevel> builder = ImmutableMultimap.builder();

        builder.put(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION);
        builder.put(ParserLanguage.ENGLISH, ParserLevel.POS);
        builder.put(ParserLanguage.ENGLISH, ParserLevel.CHUNKING);
        builder.put(ParserLanguage.ENGLISH, ParserLevel.LEMMATIZATION);
        builder.put(ParserLanguage.ENGLISH, ParserLevel.DEPENDENCY);

        supportMaps.put(ParserTool.GDEP, builder.build());
    }

    private static void generateOpenNLPMap() {
        ImmutableMultimap.Builder<ParserLanguage, ParserLevel> builder = ImmutableMultimap.builder();

        builder.put(ParserLanguage.DANISH, ParserLevel.TOKENIZATION);
        builder.put(ParserLanguage.DANISH, ParserLevel.POS);
        builder.put(ParserLanguage.DUTCH, ParserLevel.TOKENIZATION);
        builder.put(ParserLanguage.DUTCH, ParserLevel.POS);
        builder.put(ParserLanguage.ENGLISH, ParserLevel.TOKENIZATION);
        builder.put(ParserLanguage.ENGLISH, ParserLevel.POS);
        builder.put(ParserLanguage.ENGLISH, ParserLevel.CHUNKING);
        builder.put(ParserLanguage.FRENCH, ParserLevel.TOKENIZATION);
        builder.put(ParserLanguage.FRENCH, ParserLevel.POS);
        builder.put(ParserLanguage.FRENCH, ParserLevel.CHUNKING);
        builder.put(ParserLanguage.GERMAN, ParserLevel.TOKENIZATION);
        builder.put(ParserLanguage.GERMAN, ParserLevel.POS);
        builder.put(ParserLanguage.PORTUGUESE, ParserLevel.TOKENIZATION);
        builder.put(ParserLanguage.PORTUGUESE, ParserLevel.POS);
        builder.put(ParserLanguage.SWEDISH, ParserLevel.TOKENIZATION);
        builder.put(ParserLanguage.SWEDISH, ParserLevel.POS);

        supportMaps.put(ParserTool.OPENNLP, builder.build());
    }

    private static ImmutableMultimap<ParserLanguage, ParserLevel> getMapforTool(ParserTool tool) {
        ImmutableMultimap<ParserLanguage, ParserLevel> supportMap = supportMaps.get(tool);
        if(supportMap == null){
            switch(tool) {
                case GDEP:    generateGDepMap(); break;
                case OPENNLP: generateOpenNLPMap(); break;
            }
            return getMapforTool(tool);
        } else {
            return supportMap;
        }
    }

    public static List<ParserLevel> levelsSupportedByLanguage(ParserTool tool, ParserLanguage language){
        ImmutableMultimap<ParserLanguage, ParserLevel> mapToUse = getMapforTool(tool);
        return Lists.newArrayList(mapToUse.get(language));
    }

    public static List<ParserLanguage> languagesSupportedByLevel(ParserTool tool, ParserLevel level){
        ImmutableMultimap<ParserLanguage, ParserLevel> mapToUse = getMapforTool(tool);
        return Lists.newArrayList(mapToUse.inverse().get(level));
    }

    public static List<ParserLevel> getEqualOrLowerSupportedLevels(ParserTool tool,
                                                                   ParserLanguage language,
                                                                   ParserLevel level) {

        Collection<ParserLevel> levels = ParserSupport.levelsSupportedByLanguage(tool, language);
        Collection<ParserLevel> levelsFilter = new ArrayList<>();
        switch (level) {
            case TOKENIZATION:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                break;
            case LEMMATIZATION:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                levelsFilter.add(ParserLevel.LEMMATIZATION);
                break;
            case POS:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                levelsFilter.add(ParserLevel.LEMMATIZATION);
                levelsFilter.add(ParserLevel.POS);
                break;
            case CHUNKING:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                levelsFilter.add(ParserLevel.LEMMATIZATION);
                levelsFilter.add(ParserLevel.POS);
                levelsFilter.add(ParserLevel.CHUNKING);
                break;
            case DEPENDENCY:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                levelsFilter.add(ParserLevel.LEMMATIZATION);
                levelsFilter.add(ParserLevel.POS);
                levelsFilter.add(ParserLevel.CHUNKING);
                levelsFilter.add(ParserLevel.DEPENDENCY);
                break;
        }

        levels.retainAll(levelsFilter);
        return Lists.newArrayList(levels);
    }
}
