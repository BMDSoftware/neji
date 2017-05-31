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

package pt.ua.tm.neji.dictionary;

import martin.common.ArgParser;
import martin.common.Loggers;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.exception.NejiException;
import uk.ac.man.entitytagger.EntityTagger;
import uk.ac.man.entitytagger.matching.Matcher;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Helper to load dictionaries.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class DictionariesLoader {

    private List<String> priority;
    private Map<String, Dictionary> dictionaries;

    public DictionariesLoader(List<String> priority) {
        assert (priority != null);
        this.dictionaries = new LinkedHashMap<>();
        this.priority = priority;
    }

    public DictionariesLoader(InputStream input) throws NejiException {
        assert (input != null);
        this.dictionaries = new LinkedHashMap<>();
        this.priority = loadPriority(input);
    }

    public static List<String> loadPriority(InputStream input) throws NejiException {
        List<String> result = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line;

        try {
            while ((line = br.readLine()) != null) {
                if (line.equals("") || line.equals(" ") || line.equals("\n") || line.indexOf(0) == '#') {
                    continue;
                }
                line = line.replace(".txt", ".tsv");
                result.add(line);
            }
            br.close();
        } catch (IOException ex) {
            throw new NejiException("There was a problem reading the priority file.", ex);
        }
        return result;
    }

    public void load(File folder, boolean ignoreCase) {
        assert (folder != null);
        Pattern groupPattern = Pattern.compile("([A-Za-z0-9]+?)\\.");

        for (String name : priority) {
//            String group = null;
//            java.util.regex.Matcher m = groupPattern.matcher(name);
//            while (m.find()) {
//                group = m.group(1);
//            }
//            if (group == null) {
//                throw new RuntimeException(
//                        "The file name of the lexicon does not follow the required format: *GROUP.*");
//            }
            String dictionaryFileName = folder.getAbsolutePath() + File.separator + name;
            // Get group from first ID
            String group;
            try (FileInputStream fis = new FileInputStream(dictionaryFileName);
                 InputStreamReader isr = new InputStreamReader(fis);
                 BufferedReader br = new BufferedReader(isr);) {

                String line = br.readLine();
                String[] parts = line.split("\t");
                String[] fields = parts[0].split(":");
                    group = fields[3];
            } catch (IOException ex) {
                throw new RuntimeException("There was a problem obtaining the group from the dictionary: " + dictionaryFileName, ex);
            }

            Matcher matcher = getExactMatcher(dictionaryFileName, ignoreCase);

            Dictionary d = new Dictionary(matcher, group);
            dictionaries.put(name, d);
        }
    }

    private Matcher getExactMatcher(String fileName, boolean ignoreCase) {
        Boolean b = ignoreCase;
        ArgParser ap = new ArgParser(new String[]{"--variantMatcher", fileName, "--ignoreCase", b.toString()});

        java.util.logging.Logger l = Loggers.getDefaultLogger(ap);
        if (Constants.verbose){
            l.setLevel(Level.INFO);
        } else {
            l.setLevel(Level.OFF);
        }


        return EntityTagger.getMatcher(ap, l);
    }

    public Map<String, Dictionary> getDictionaries() {
        return dictionaries;
    }
}
