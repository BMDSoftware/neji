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
package pt.ua.tm.neji.train.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.train.external.snowball.EnglishStemmer;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.dictionary.DictionaryAux;

/**
 * Access external resources, such as dictionaries, tokeniser and stemming
 * models.
 *
 * @author David Campos (<a
 * href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Resources {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Resources.class);
    /**
     * Stemmer.
     */
    private static EnglishStemmer stemmer = null;
    /**
     * File with resources configuration.
     */
    private static String fileName = "resources.properties";
    /**
     * Resources properties.
     */
    private static Properties properties = null;
    private static Pattern stopwords = null;

    public static Pattern getStopwordsPattern() throws FileNotFoundException, NejiException {
        if (stopwords == null) {
            stopwords = DictionaryAux.loadStopwords(Resources.getResource("stopwords"));
        }
        return stopwords;
    }

    /**
     * Load Resources Properties.
     */
    private static void loadProperties() {
        if (properties == null) {
            try {
                properties = new Properties();
                properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));

            }
            catch (IOException ex) {
                throw new RuntimeException("Can't load resources properties file");
            }
        }
    }

    /**
     * Load stemmer.
     *
     * @return loaded stemmer.
     */
    public static EnglishStemmer getStemmer() {
        loadProperties();

        if (stemmer == null) {
            stemmer = new EnglishStemmer();
        }
        return stemmer;
    }

    
    public static synchronized InputStream getResource(String name) {
        loadProperties();
        String file = properties.getProperty(name);
        if (file == null) {
            throw new RuntimeException("Resource " + name + " does not exist.");
        }

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        if (is == null) {
            throw new RuntimeException("Resource " + name + " does not exist.");
        }

        return is;
    }

    /**
     * Show used resources.
     */
    public static void print() {
        loadProperties();
        logger.info("RESOURCES:");
        logger.info("PRGE: {}", properties.getProperty("prge"));
        logger.info("Aminoacid: {}", properties.getProperty("aminoacid"));
        logger.info("Nucleicacid: {}", properties.getProperty("nucleicacid"));
        logger.info("Nucleotide: {}", properties.getProperty("nucleotide"));
        logger.info("Nucleoside: {}", properties.getProperty("nucleoside"));
        logger.info("Nucleobase: {}", properties.getProperty("nucleobase"));
        logger.info("Verbs: {}", properties.getProperty("verbs"));
        logger.info("Greek: {}", properties.getProperty("greek"));
        logger.info("Stopwords: {}", properties.getProperty("stopwords"));
    }
}
