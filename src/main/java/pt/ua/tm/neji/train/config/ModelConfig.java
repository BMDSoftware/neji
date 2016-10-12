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

package pt.ua.tm.neji.train.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants.Parsing;
import pt.ua.tm.neji.exception.NejiException;

/**
 *
 * @author jeronimo
 */
public class ModelConfig implements Serializable {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(ModelConfig.class);
    private boolean token;
    private boolean stem;
    private boolean lemma;
    private boolean pos;
    private boolean chunk;
    private boolean nlp;
    private boolean capitalization;
    private boolean counting;
    private boolean symbols;
    private boolean ngrams;
    private boolean suffix;
    private boolean prefix;
    private boolean greek;
    private boolean roman;
    private boolean morphology;
    private boolean prge;
    private boolean concepts;
    private boolean verbs;
    private boolean window;
    private boolean conjunctions;
    private int order;
    
    private Parsing parsing;
    private List<String> entity;

    /**
     * Constructor that will load the features from a properties file.
     *
     * @param file The file that contains the model properties.
     */
    public ModelConfig(final String file) {
        loadFromFile(file);
    }

    public ModelConfig(boolean token, boolean stem, boolean lemma, boolean pos,
                       boolean chunk, boolean nlp, boolean capitalization, boolean counting,
                       boolean symbols, boolean ngrams, boolean suffix, boolean prefix,
                       boolean greek, boolean roman, boolean morphology, boolean prge,
                       boolean concepts, boolean verbs, boolean window, boolean conjunctions,
                       int order, Parsing parsingDirection, List<String> entity) {
        this.token = token;
        this.stem = stem;
        this.lemma = lemma;
        this.pos = pos;
        this.chunk = chunk;
        this.nlp = nlp;
        this.capitalization = capitalization;
        this.counting = counting;
        this.symbols = symbols;
        this.ngrams = ngrams;
        this.suffix = suffix;
        this.prefix = prefix;
        this.greek = greek;
        this.roman = roman;
        this.morphology = morphology;
        this.prge = prge;
        this.concepts = concepts;
        this.verbs = verbs;
        this.window = window;
        this.conjunctions = conjunctions;
        this.order = order;
        
        this.parsing = parsingDirection;
        this.entity = entity;
    }

    public void write(OutputStream output) {

        try {
            output.write("token=".getBytes());
            output.write((token ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("stem=".getBytes());
            output.write((stem ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("lemma=".getBytes());
            output.write((lemma ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("pos=".getBytes());
            output.write((pos ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("chunk=".getBytes());
            output.write((chunk ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("nlp=".getBytes());
            output.write((nlp ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("capitalization=".getBytes());
            output.write((capitalization ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("counting=".getBytes());
            output.write((counting ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("symbols=".getBytes());
            output.write((symbols ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("ngrams=".getBytes());
            output.write((ngrams ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("suffix=".getBytes());
            output.write((suffix ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("prefix=".getBytes());
            output.write((prefix ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("morphology=".getBytes());
            output.write((morphology ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("greek=".getBytes());
            output.write((greek ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("roman=".getBytes());
            output.write((roman ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("prge=".getBytes());
            output.write((prge ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("concepts=".getBytes());
            output.write((concepts ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("verbs=".getBytes());
            output.write((verbs ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("window=".getBytes());
            output.write((window ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("conjunctions=".getBytes());
            output.write((conjunctions ? "1" : "0").getBytes());
            output.write("\n".getBytes());

            output.write("order=".getBytes());
            output.write(("" + order).getBytes());
            output.write("\n".getBytes());
            
            output.write("parsing=".getBytes());
            output.write(("" + parsing.name()).getBytes());
            output.write("\n".getBytes());
            
            output.write("entity=".getBytes());
            output.write(("" + entityToString()).getBytes());
            output.write("\n".getBytes());

            output.close();
        } catch (IOException e) {
            throw new RuntimeException("There was a problem writing the configuration file.");
        }
    }

    /**
     * Load configurations from file.
     *
     * @param file File to load the model configurations.
     */
    private void loadFromFile(final String file) {
        try {
            Properties properties = new Properties();
            FileInputStream fis = new FileInputStream(file);
            properties.load(fis);

            try {
                token = String2Boolean(properties.getProperty("token"));
                stem = String2Boolean(properties.getProperty("stem"));
                lemma = String2Boolean(properties.getProperty("lemma"));
                pos = String2Boolean(properties.getProperty("pos"));
                chunk = String2Boolean(properties.getProperty("chunk"));
                nlp = String2Boolean(properties.getProperty("nlp"));
                capitalization = String2Boolean(properties.getProperty("capitalization"));
                counting = String2Boolean(properties.getProperty("counting"));
                symbols = String2Boolean(properties.getProperty("symbols"));
                ngrams = String2Boolean(properties.getProperty("ngrams"));
                suffix = String2Boolean(properties.getProperty("suffix"));
                prefix = String2Boolean(properties.getProperty("prefix"));
                greek = String2Boolean(properties.getProperty("greek"));
                roman = String2Boolean(properties.getProperty("roman"));
                morphology = String2Boolean(properties.getProperty("morphology"));
                prge = String2Boolean(properties.getProperty("prge"));
                concepts = String2Boolean(properties.getProperty("concepts"));
                verbs = String2Boolean(properties.getProperty("verbs"));
                window = String2Boolean(properties.getProperty("window"));
                conjunctions = String2Boolean(properties.getProperty("conjunctions"));
                order = Integer.parseInt(properties.getProperty("order"));
                
                parsing = Parsing.valueOf(properties.getProperty("parsing"));
                
                String entityText = properties.getProperty("entity");
                if (entityText.trim().length() == 0) entity = null; // accepts all annotations (doesn't filter)
                else entity = Arrays.asList(entityText.toLowerCase().split("\\s*,\\s*"));
                
            } catch (NejiException ex) {
                throw new RuntimeException("There was a problem loading the features.", ex);
            }
            fis.close();
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem loading the features from the properties file.", ex);
        }
    }

    public void print() {
        logger.info("Token: {}", token);
        logger.info("Stem: {}", stem);
        logger.info("Lemma: {}", lemma);
        logger.info("POS: {}", pos);
        logger.info("Chunk: {}", chunk);
        logger.info("NLP: {}", nlp);
        logger.info("Capitalization: {}", capitalization);
        logger.info("Counting: {}", counting);
        logger.info("Symbols: {}", symbols);
        logger.info("NGrams: {}", ngrams);
        logger.info("Suffix: {}", suffix);
        logger.info("Prefix: {}", prefix);
        logger.info("Greek: {}", greek);
        logger.info("Roman: {}", roman);
        logger.info("Word Shape: {}", morphology);
        logger.info("PRGE: {}", prge);
        logger.info("Biomedical Concepts: {}", concepts);
        logger.info("Verbs: {}", verbs);
        logger.info("Window: {}", window);
        logger.info("Conjunctions: {}", conjunctions);
        logger.info("Order: {}", order);
        
        logger.info("Parsing: {}", parsing.name());
        logger.info("Entity: {}", entityToString());
    }

    private boolean String2Boolean(String s) throws NejiException {
        if (s.equals("1")) {
            return true;
        } else if (s.equals("0")) {
            return false;
        }
        throw new NejiException("String value must be 0 or 1 to be converted to boolean.");
    }

    public boolean isCapitalization() {
        return capitalization;
    }

    public void setCapitalization(boolean capitalization) {
        this.capitalization = capitalization;
    }

    public boolean isChunk() {
        return chunk;
    }

    public void setChunk(boolean chunk) {
        this.chunk = chunk;
    }

    public boolean isConcepts() {
        return concepts;
    }

    public void setConcepts(boolean concepts) {
        this.concepts = concepts;
    }

    public boolean isConjunctions() {
        return conjunctions;
    }

    public void setConjunctions(boolean conjunctions) {
        this.conjunctions = conjunctions;
    }

    public boolean isCounting() {
        return counting;
    }

    public void setCounting(boolean counting) {
        this.counting = counting;
    }

    public boolean isGreek() {
        return greek;
    }

    public void setGreek(boolean greek) {
        this.greek = greek;
    }

    public boolean isLemma() {
        return lemma;
    }

    public void setLemma(boolean lemma) {
        this.lemma = lemma;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        ModelConfig.logger = logger;
    }

    public boolean isMorphology() {
        return morphology;
    }

    public void setMorphology(boolean morphology) {
        this.morphology = morphology;
    }

    public boolean isNgrams() {
        return ngrams;
    }

    public void setNgrams(boolean ngrams) {
        this.ngrams = ngrams;
    }

    public boolean isNLP() {
        return nlp;
    }

    public void setNLP(boolean nlp) {
        this.nlp = nlp;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isPos() {
        return pos;
    }

    public void setPos(boolean pos) {
        this.pos = pos;
    }

    public boolean isPrefix() {
        return prefix;
    }

    public void setPrefix(boolean prefix) {
        this.prefix = prefix;
    }

    public boolean isPrge() {
        return prge;
    }

    public void setPrge(boolean prge) {
        this.prge = prge;
    }

    public boolean isRoman() {
        return roman;
    }

    public void setRoman(boolean roman) {
        this.roman = roman;
    }

    public boolean isStem() {
        return stem;
    }

    public void setStem(boolean stem) {
        this.stem = stem;
    }

    public boolean isSuffix() {
        return suffix;
    }

    public void setSuffix(boolean suffix) {
        this.suffix = suffix;
    }

    public boolean isSymbols() {
        return symbols;
    }

    public void setSymbols(boolean symbols) {
        this.symbols = symbols;
    }

    public boolean isToken() {
        return token;
    }

    public void setToken(boolean token) {
        this.token = token;
    }

    public boolean isVerbs() {
        return verbs;
    }

    public void setVerbs(boolean verbs) {
        this.verbs = verbs;
    }

    public boolean isWindow() {
        return window;
    }

    public void setWindow(boolean window) {
        this.window = window;
    }
    
    public Parsing getParsing() {
        return this.parsing;
    }
    
    public void setParsing(Parsing parsing) {
        this.parsing = parsing;
    }
    
    public List<String> getEntity() {
        return this.entity;
    }
    
    public void setEntity(List<String> entity) {
        this.entity = entity;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        
        sb.append("token=");
        sb.append(token);
        sb.append("\n");
        
        sb.append("stem=");
        sb.append(stem);
        sb.append("\n");
        
        sb.append("lemma=");
        sb.append(lemma);
        sb.append("\n");
        
        sb.append("pos=");
        sb.append(pos);
        sb.append("\n");
        
        sb.append("chunk=");
        sb.append(chunk);
        sb.append("\n");
        
        sb.append("nlp=");
        sb.append(nlp);
        sb.append("\n");
        
        sb.append("capitalization=");
        sb.append(capitalization);
        sb.append("\n");
        
        sb.append("counting=");
        sb.append(counting);
        sb.append("\n");
        
        sb.append("symbols=");
        sb.append(symbols);
        sb.append("\n");
        
        sb.append("ngrams=");
        sb.append(ngrams);
        sb.append("\n");
        
        sb.append("suffix=");
        sb.append(suffix);
        sb.append("\n");
        
        sb.append("prefix=");
        sb.append(prefix);
        sb.append("\n");
        
        sb.append("morphology=");
        sb.append(morphology);
        sb.append("\n");
        
        sb.append("greek=");
        sb.append(greek);
        sb.append("\n");
        
        sb.append("roman=");
        sb.append(roman);
        sb.append("\n");
        
        sb.append("prge=");
        sb.append(prge);
        sb.append("\n");
        
        sb.append("concepts=");
        sb.append(concepts);
        sb.append("\n");

        sb.append("verbs=");
        sb.append(verbs);
        sb.append("\n");
        
        sb.append("window=");
        sb.append(window);
        sb.append("\n");
        
        sb.append("conjunctions=");
        sb.append(conjunctions);
        sb.append("\n");
        
        sb.append("order=");
        sb.append(order);
        sb.append("\n");
        
        sb.append("parsing=");
        sb.append(parsing.name());
        sb.append("\n");
        
        sb.append("entity=");
        sb.append(entityToString());
        sb.append("\n");
        
        return sb.toString().trim();
    }
    
    /**
     * Convert entities list to string.
     * @return a string with all entities separated by a ','
     */
    private String entityToString() {
        
        // If null return an empty string
        if (entity == null)
        {
            return "";
        }
        
        // Concatenate entities
        String entityText = "";
        entityText += entity.get(0);
        for (int i = 1 ; i < entity.size() ; i++) {
            entityText += "," + entity.get(i);
        }
        
        return entityText;
    }
}
