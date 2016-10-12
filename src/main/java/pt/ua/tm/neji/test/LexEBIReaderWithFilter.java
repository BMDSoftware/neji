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

import monq.ie.Term2Re;
import monq.jfa.DfaRun;
import monq.jfa.Xml;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.pipeline.DefaultPipeline;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class LexEBIReaderWithFilter extends BaseLoader {

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(LexEBIReaderWithFilter.class);
    String prgeID;
    private Map<String, List<String>> preferred;
    private Map<String, List<String>> synonyms;
    private Map<String, String> map;
    private List<String> preferredNames, synonymsNames, filter;
    private boolean inEntry;
    private long numEntries, numNames, numVariants, numUP;

    private StartAction start_entry = new StartAction() {
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

            preferredNames.add(preferred);

            numEntries++;
            numNames++;

//            logger.info("PREFERRED: {}", preferred);
        }
    };

    private EndAction end_entry = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inEntry = false;
            preferredNames = new ArrayList<String>();
            synonymsNames = new ArrayList<String>();
        }
    };

    private DefaultAction variant = new DefaultAction() {
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

            synonymsNames.add(variant);

            numNames++;
            numVariants++;
        }
    };

    private DefaultAction species = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {

            map = Xml.splitElement(yytext, start);
            String att = map.get("att");
            String id = map.get("val");

//            boolean isHuman = false;
//            if (att != null && id != null) {
//                if (att.equals("speciesNameNCBI")
//                        && id.equals("9606")) {
//                    isHuman = true;
//                }
//            } else {
//                return;
//            }

//            if (isHuman) {
            if (filter.contains(prgeID)) {
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

    private DefaultAction source = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            map = Xml.splitElement(yytext, start);
            prgeID = map.get("sourceId");
        }
    };

    public LexEBIReaderWithFilter(final List<String> filter) throws NejiException {
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
        this.filter = filter;

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

    public static void main(String[] args) {

        String fileIn = "/Volumes/data/resources/dictionaries/geneProt70.xml.gz";
        String fileFilter = "/Users/david/Downloads/oralome/All-oral.list.txt";
        String outPreferred = "/Users/david/Downloads/oralome/preferred.tsv";
        String outSynonyms = "/Users/david/Downloads/oralome/synonyms.tsv";
        String outPreferredRegex = "/Users/david/Downloads/oralome/preferred_regex.tsv";
        String outSynonymsRegex = "/Users/david/Downloads/oralome/synonyms_regex.tsv";

        String fileIDsWithoutNames = "/Users/david/Downloads/oralome/IDs_without_names.txt";

        try {

            FileOutputStream preferred = new FileOutputStream(outPreferred);
            FileOutputStream synonyms = new FileOutputStream(outSynonyms);
            FileOutputStream preferredRegex = new FileOutputStream(outPreferredRegex);
            FileOutputStream synonymsRegex = new FileOutputStream(outSynonymsRegex);
            List<String> filter = loadFilter(new FileInputStream(fileFilter));

            LexEBIReaderWithFilter reader = new LexEBIReaderWithFilter(filter);

            logger.info("Collecting dictionary data from file...");
            Pipeline p = new DefaultPipeline(reader.getPipeline().getCorpus());
            p.add(reader);
            p.run(new GZIPInputStream(new FileInputStream(fileIn)));

            logger.info("NUM ENTRIES: {}", reader.getNumEntries());
            logger.info("NUM NAMES: {}", reader.getNumNames());
            logger.info("NUM VARIANTS: {}", reader.getNumVariants());
            logger.info("NUM UP: {}", reader.getNumUP());




            // Get IDs without names
            List<String> idsWithoutNames = new ArrayList<>();
            for (String id: filter){
                if (!reader.getPreferred().containsKey(id)){
                    idsWithoutNames.add(id);
                }
            }

            // Write IDs without names to file
            FileWriter fw = new FileWriter(fileIDsWithoutNames);
            for (String id:idsWithoutNames){
                fw.write(id);
                fw.write("\n");
            }
            fw.close();






            writeToFile(reader.getPreferred(), preferred, false);
            writeToFile(reader.getSynonyms(), synonyms, false);
            writeToFile(reader.getPreferred(), preferredRegex, true);
            writeToFile(reader.getSynonyms(), synonymsRegex, true);

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

    private static List<String> loadFilter(final InputStream inputStream) throws IOException {

        List<String> filter = new ArrayList<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = br.readLine()) != null) {
            filter.add(line);
        }

        br.close();
        inputStream.close();

        return filter;
    }

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
}
