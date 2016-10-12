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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.dictionary;

import java.io.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.config.Resources;
import pt.ua.tm.neji.train.tokenise.Tokenizer;

/**
 *
 * @author david
 */
public class DictionaryAux {

    private static Logger logger = LoggerFactory.getLogger(DictionaryAux.class);
    private static final String END_OF_WORD_TOKEN = "end_of_word";
    private boolean ignoreCase;
    private Hashtable lex;
    private int size;
    private String entity;

    public DictionaryAux(InputStream input, String entity, boolean ignoreCase) throws NejiException {
        this.ignoreCase = ignoreCase;
        this.lex = new Hashtable();
        this.size = 0;
        this.entity = entity;

        // Load lexicon
        load(input);
    }

    private void load(InputStream input) throws NejiException {
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        try {
            while (( line = br.readLine() ) != null) {
                add(line);
            }
        }
        catch (IOException ex) {
            throw new NejiException("There was a problem reading the dictionary.", ex);
        }
    }

    public void add(String word) {
        add(word, false, " ");
    }

    private void add(String word, boolean includeDelims, String delim) {
        boolean newWord = false;
        StringTokenizer st = new StringTokenizer(word, delim, includeDelims);
        Hashtable currentLevel = lex;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (ignoreCase) {
                token = token.toLowerCase();
            }
            if (!currentLevel.containsKey(token)) {
                currentLevel.put(token, new Hashtable());
                newWord = true;
            }
            currentLevel = (Hashtable) currentLevel.get(token);
        }
        currentLevel.put(END_OF_WORD_TOKEN, "");
        if (newWord) {
            size++;
        }
    }

    public void annotate(Corpus c) {
        for (Sentence s : c.getSentences()) {
            annotate(s);
        }
    }

    public void annotate(Sentence sentence) {
        Annotation a;
        int i = 0;
        while (i < sentence.size()) {
            int j = endOfWord(sentence, i);
            if (j == -1) {
                i++;
            } else {
                a = AnnotationImpl.newAnnotationByTokenPositions(sentence, i, j, 1.0);
                sentence.addAnnotationLabels(a);
                i = j + 1;
            }
        }
    }

    private int endOfWord(Sentence ts, int start) {
        if (start < 0 || start >= ts.size()) {
            System.err.println("Lexicon.lastIndexOf: error - out of Sequence boundaries");
            return -1;
        }
        Hashtable currentLevel = lex;
        int end = -1;
        for (int i = start; i < ts.size(); i++) {
            Token t = ts.getToken(i);
            String s = t.getText();
            if (ignoreCase) {
                s = s.toLowerCase();
            }
            currentLevel = (Hashtable) currentLevel.get(s);
            if (currentLevel == null) {
                return end;
            }
            if (currentLevel.containsKey(END_OF_WORD_TOKEN)) {
                end = i;
            }
        }
        return end;
    }

    public int size() {
        return size;
    }

    private static String printEntry(String[] tokens) {
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            sb.append(t);
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static void clean(String fileIn, String fileOut) throws NejiException {
        assert ( fileIn != null );
        assert ( fileOut != null );

        Pattern stopwords;
        try {
            stopwords = Resources.getStopwordsPattern();
        }
        catch (Exception ex) {
            logger.error("There was a problem loading the stopwords pattern.", ex);
            return;
        }

        int counter = 0;

        Tokenizer tokenizer = new Tokenizer(false);
        tokenizer.launch();


        try {
            FileOutputStream out = new FileOutputStream(fileOut);


            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn)));
            String line;
            String[] tokens;
            Matcher m;
            while (( line = br.readLine() ) != null) {
                tokens = tokenizer.tokenize(line);

                if (tokens.length > 1) {
                    out.write(printEntry(tokens).getBytes());
                    out.write("\n".getBytes());
                } else {
                    m = stopwords.matcher(tokens[0].trim());
                    if (!m.matches() && tokens[0].length() > 2) {
                        out.write(printEntry(tokens).getBytes());
                        out.write("\n".getBytes());
                    } else {
                        counter++;
                    }
                }
            }
            br.close();
            out.close();
        }
        catch (IOException ex) {
            throw new NejiException("There was a problem reading the stopwords file.", ex);
        }

        tokenizer.terminate();
    }

    public static Pattern loadStopwords(InputStream input) throws NejiException {
        assert ( input != null );

        StringBuilder sb = new StringBuilder();
        sb.append("(?i)(");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line;
            while (( line = br.readLine() ) != null) {
                sb.append(line);
                sb.append("|");
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append(")");
        }
        catch (IOException ex) {
            throw new NejiException("There was a problem reading the stopwords file.", ex);
        }

        Pattern p = Pattern.compile(sb.toString());
        return p;
    }
}
