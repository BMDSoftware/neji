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

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public enum ParserLanguage {
    // LANGUAGE_NAME("directory_name")

    DANISH("da"),
    DUTCH("nl"),
    ENGLISH("en"),
    FRENCH("fr"),
    GERMAN("de"),
    PORTUGUESE("pt"),
    SWEDISH("se");

    private final String dirName;

    private ParserLanguage(String dirName) {
        this.dirName = dirName;
    }

    public String getDirName() {
        return dirName;
    }
}
