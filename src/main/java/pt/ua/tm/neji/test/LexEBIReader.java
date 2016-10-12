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
package pt.ua.tm.neji.test;

import martin.common.ArgParser;
import martin.common.Loggers;
import monq.ie.Term2Re;
import monq.jfa.DfaRun;
import monq.jfa.Xml;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.pipeline.DefaultPipeline;
import uk.ac.man.entitytagger.EntityTagger;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class LexEBIReader extends BaseLoader {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(LexEBIReader.class);
    private Map<String, List<String>> preferred;
    private Map<String, List<String>> synonyms;
    private Map<String, String> map;
    private List<String> preferredNames, synonymsNames;
    private boolean inEntry;
    private long numEntries, numNames, numVariants, numUP;
    private Matcher speciesMatcher;
    String prgeID;

    private static String[] taxIDsArray = new String[]{"3702", "9913", "6239", "3055", "7955", "44689", "7460", "5476", "7227", "9606",
            "10090", "10116", "4932", "11103", "2104", "5833", "4754", "4896", "4577", "562", "8355"};
    private static List<String> taxIDs = Arrays.asList(taxIDsArray);

    public long getNumEntries() {
        return numEntries;
    }

    public long getNumNames() {
        return numNames;
    }

    public long getNumVariants() {
        return numVariants;
    }

    public long getNumUP() {
        return numUP;
    }

    public Map<String, List<String>> getPreferred() {
        return preferred;
    }

    public Map<String, List<String>> getSynonyms() {
        return synonyms;
    }

    public LexEBIReader() throws NejiException {
        super(DfaRun.UNMATCHED_DROP);
        super.addActionToXMLTag(start_entry, "Entry");
        super.addActionToXMLTag(end_entry, "Entry");
        super.addActionToXMLTag(variant, "Variant");
        super.addActionToXMLTag(source, "SourceDC");
        super.addActionToXMLTag(species, "DC");
        this.preferred = new HashMap<String, List<String>>();
        this.synonyms = new HashMap<String, List<String>>();
        this.preferredNames = new ArrayList<String>();
        this.synonymsNames = new ArrayList<String>();
        this.speciesMatcher = getSpeciesDictionaryMatcher();

        this.inEntry = false;
        this.prgeID = "";
        numEntries = 0;
        numNames = 0;
        numVariants = 0;
        numUP = 0;
        this.map = new HashMap<>();

//        try {
//            Nfa nfa = new Nfa(Nfa.NOTHING);
//            nfa.or(Xml.STag("Entry"), start_entry);
//            nfa.or(Xml.ETag("Entry"), end_entry);
//
//            nfa.or(Xml.EmptyElemTag("Variant"), variant);
//
//
//            nfa.or(Xml.EmptyElemTag("SourceDC"), source);
//
//            nfa.or(Xml.EmptyElemTag("DC"), species);
//
//            setNFA(nfa, DfaRun.UNMATCHED_DROP);
//        } catch (ReSyntaxException ex) {
//            throw new NejiException(ex);
//        }
    }

    private Action start_entry = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inEntry = true;

            // Get preferred term
//            logger.info("PREFERRED: {}", yytext);

            map = Xml.splitElement(yytext, start);
            String preferred = map.get("baseForm");
            preferred = preferred.replaceAll("&amp;gt", "&gt;");
            preferred = StringEscapeUtils.unescapeXml(preferred);
            preferred = preferred.trim();
//            preferred = preferred.replaceAll("\\s+", "");

            if (preferred.equals("")) {
                return;
            }
            if (preferred.length() < 3) {
                return;
            }
            if (isSpecies(speciesMatcher, preferred)) {
                return;
            }
            if (preferred.equalsIgnoreCase("mouse")){
                return;
            }

            preferredNames.add(preferred);

            numEntries++;
            numNames++;

//            logger.info("PREFERRED: {}", preferred);

        }
    };

    private Action end_entry = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inEntry = false;
            preferredNames = new ArrayList<String>();
            synonymsNames = new ArrayList<String>();
        }
    };

    private Action variant = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            // Get Variant
//            logger.info("VARIANT: {}", yytext);

            map = Xml.splitElement(yytext, start);
            String variant = map.get("writtenForm");
            variant = variant.trim();
//            variant = variant.replaceAll("\\s+", "");

            if (variant.equals("")) {
                return;
            }

            variant = variant.replaceAll("&amp;gt", "&gt;");
            variant = StringEscapeUtils.unescapeXml(variant);

            if (variant.contains("<up>")) {
                numUP++;
                return;
            }

            if (variant.length() < 3) {
                return;
            }

            if (isSpecies(speciesMatcher, variant)) {
                return;
            }

            if (variant.equalsIgnoreCase("mouse")){
                return;
            }

            synonymsNames.add(variant);

            numNames++;
            numVariants++;
        }
    };

    private Action species = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {

            map = Xml.splitElement(yytext, start);
            String att = map.get("att");
            String id = map.get("val");

            boolean isAllowedSpecies = false;
            if (att != null && id != null) {

                if (att.equals("speciesNameNCBI") && taxIDs.contains(id)) {
                    isAllowedSpecies = true;
                }

//                if (att.equals("speciesNameNCBI")
//                        && id.equals("9606")) {
//                    isAllowedSpecies = true;
//                }
            } else {
                return;
            }


            if (isAllowedSpecies) {
                if (preferred.containsKey(prgeID)) {
                    preferred.get(prgeID).addAll(preferredNames);
                } else {
                    preferred.put(prgeID, preferredNames);
                }

                if (synonyms.containsKey(prgeID)) {
                    synonyms.get(prgeID).addAll(synonymsNames);
                } else {
                    synonyms.put(prgeID, synonymsNames);
                }
            }

        }
    };

    private Action source = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            map = Xml.splitElement(yytext, start);
            prgeID = map.get("sourceId");
        }
    };


    private boolean isSpecies(Matcher speciesMatcher, String name) {
        name = "mouse";
        List<Mention> mentions = speciesMatcher.match(name);
        if (mentions.size() == 1) {
            Mention mention = mentions.get(0);
            if (mention.getText().equalsIgnoreCase(name)) {
                return true;
            } else {
                return false;
            }
        } else if (mentions.size()>1){
            return false;
        } else {
            return false;
        }
    }

    private Matcher getSpeciesDictionaryMatcher() {
        String linnaeusPath = "resources/lexicons/species/";

        logger.info("Loading LINNAEUS...");
        // Load species lexicon
        String variantMatcher = linnaeusPath + "dict-species-proxy.tsv";
        String ignoreCase = "true";
        String ppStopTerms = linnaeusPath + "stoplist.tsv";
        String ppAcrProbs = linnaeusPath + "synonyms-acronyms.tsv";
        String ppSpeciesFreqs = linnaeusPath + "species-frequency.tsv";

        ArgParser ap = new ArgParser(new String[]{"--variantMatcher", variantMatcher,
                "--ignoreCase", ignoreCase,
                "--ppStopTerms", ppStopTerms,
                "--ppAcrProbs", ppAcrProbs,
                "--ppSpeciesFreqs", ppSpeciesFreqs,
                "--postProcessing"});

        java.util.logging.Logger l = Loggers.getDefaultLogger(ap);
        if (Constants.verbose) {
            l.setLevel(Level.INFO);
        } else {
            l.setLevel(Level.OFF);
        }

        logger.info("Done!");
        return EntityTagger.getMatcher(ap, l);
    }

    public static void main(String[] args) {

        String fileIn = "/Volumes/data/resources/dictionaries/geneProt70.xml.gz";
        String outPreferred = "/Volumes/data/Dropbox/PhD/work/platform/Code/neji/resources/lexicons/prge_selected/lexebi_prge_selected_preferred.tsv";
        String outSynonyms = "/Volumes/data/Dropbox/PhD/work/platform/Code/neji/resources/lexicons/prge_selected/lexebi_prge_selected_synonyms.tsv";
//        String outPreferredRegex = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/lexicons/prge/lexebi_prge_human_preferred_regex.tsv";
//        String outSynonymsRegex = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/lexicons/prge/lexebi_prge_human_synonyms_regex.tsv";

        try {

            FileOutputStream preferred = new FileOutputStream(outPreferred);
            FileOutputStream synonyms = new FileOutputStream(outSynonyms);
//            FileOutputStream preferredRegex = new FileOutputStream(outPreferredRegex);
//            FileOutputStream synonymsRegex = new FileOutputStream(outSynonymsRegex);
            LexEBIReader reader = new LexEBIReader();

            logger.info("Collecting dictionary data from file...");
            Pipeline p = new DefaultPipeline(reader.getPipeline().getCorpus());
            p.add(reader);
            p.run(new GZIPInputStream(new FileInputStream(fileIn)));

            logger.info("NUM ENTRIES: {}", reader.getNumEntries());
            logger.info("NUM NAMES: {}", reader.getNumNames());
            logger.info("NUM VARIANTS: {}", reader.getNumVariants());
            logger.info("NUM UP: {}", reader.getNumUP());


            writeToFile(reader.getPreferred(), preferred, false);
            writeToFile(reader.getSynonyms(), synonyms, false);
//            writeToFile(reader.getPreferred(), preferredRegex, true);
//            writeToFile(reader.getSynonyms(), synonymsRegex, true);

        } catch (IOException | NejiException ex) {
            ex.printStackTrace();
        }

    }

    private static void writeToFile(Map<String, List<String>> dict, OutputStream out, boolean useRegex)
            throws IOException {
        Iterator<String> it = dict.keySet().iterator();
        while (it.hasNext()) {
            String id = it.next();

            List<String> names = dict.get(id);

            if (useRegex) {
                names = getRegexNames(names);
            }

            String toWrite = entryToTSV(id, names);

            if (toWrite != null) {
                out.write(toWrite.getBytes());
            }
        }
        out.close();
    }

    public static List<String> getRegexNames(List<String> names) {
        List<String> regexNames = new ArrayList<String>();

        for (String name : names) {

            String regex = Term2Re.convert(name);
            regex = "(" + regex + ")";

            regex = regex.replaceAll("\\{", "\\\\{");
            regex = regex.replaceAll("\\}", "\\\\}");

            if (!regexNames.contains(regex)) {
                regexNames.add(regex);
            }
        }

        return regexNames;
    }

    private static String entryToTSV(String id, List<String> names) {
        StringBuilder sb = new StringBuilder();

        if (names.isEmpty()) {
            return null;
        }

        sb.append("UNIPROT:");
        sb.append(id);
        sb.append(":T116:PRGE");
        sb.append("\t");
        for (String name : names) {
            sb.append(name);
            sb.append("|");
        }
        sb.setLength(sb.length() - 1);


        // Temporary
//        sb.append("\t");
//        sb.append("UNIPROT:");
//        sb.append(id);

        sb.append("\n");
        return sb.toString();
    }
}
