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

package pt.ua.tm.neji.evaluation;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.statistics.StatisticsEntry;
import pt.ua.tm.neji.statistics.StatisticsEntryComparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Evaluator class that performs evaluation based on the provided evaluation type.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class CompleteEvaluator {

    private static final Pattern conceptPattern = Pattern.compile("T[0-9]+");
    private static final Pattern identifiersPattern = Pattern.compile("#[0-9]+");
    private static Logger logger = LoggerFactory.getLogger(CompleteEvaluator.class);
    private final String mappersFolderPath;
    private Map<String, Evaluation> evaluations;
    private Map<String, List<StatisticsEntry>> fns;
    private Map<String, List<StatisticsEntry>> fps;

    public static long numUNIPROTS = 0;
    public static long numUNIPROTSmapped = 0;
    public static long numPRGEnames = 0;
    public static long numPRGEmapped = 0;

    public static long numUMLSCL = 0;
    public static long numUMLSCLmapped = 0;
    public static long numCellNames = 0;
    public static long numCellMapped = 0;

    public static long numPROCFUNC = 0;
    public static long numPROCFUNCwithIDs = 0;

    public static long numUMLSPROCFUNC = 0;
    public static long numUMLSPROCFUNCmapped = 0;
    public static long numPROCFUNCNames = 0;
    public static long numPROCFUNCMapped = 0;



    public CompleteEvaluator(final String mappersFolderPath) {
        this.mappersFolderPath = mappersFolderPath;
        this.evaluations = new HashMap<>();

        this.fns = new HashMap<>();
        this.fps = new HashMap<>();

        // PRGE
        numUNIPROTS = 0;
        numUNIPROTSmapped = 0;
        numPRGEnames = 0;
        numPRGEmapped = 0;

        // Cell
        numUMLSCL = 0;
        numUMLSCLmapped = 0;
        numCellNames = 0;
        numCellMapped = 0;

        // ProcFunc
        numPROCFUNC = 0;
        numPROCFUNCwithIDs = 0;
        numUMLSPROCFUNC = 0;
        numUMLSPROCFUNCmapped = 0;
        numPROCFUNCNames = 0;
        numPROCFUNCMapped = 0;

    }

    public void evaluate(final InputStream goldA1InputStream, final InputStream silverA1Stream, final EvaluationType evaluationType, final IdentifierMatch identifierMatch) {
        ConceptList goldList = getConceptListFromInputStream(goldA1InputStream);
        ConceptList silverList = getConceptListFromInputStream(silverA1Stream);

        evaluate(goldList, silverList, evaluationType, identifierMatch);
    }

    public void evaluate(ConceptList goldList, ConceptList silverList, final EvaluationType evaluationType, final IdentifierMatch identifierMatch) {
        //Silver on Gold
        for (Concept gold : goldList) {

            boolean matched;
            if (evaluationType.equals(EvaluationType.Exact)) {
                matched = silverList.containsExact(gold, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Left)) {
                matched = silverList.containsLeft(gold, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Right)) {
                matched = silverList.containsRight(gold, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Shared)) {
                matched = silverList.containsShared(gold, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Subspan)) {
                matched = silverList.containsSubspan(gold, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Overlap)) {
                matched = silverList.containsOverlap(gold, identifierMatch);
            } else {
                throw new RuntimeException("Evaluation type not supported: " + evaluationType);
            }

            if (!matched) {
                String entity = gold.getEntity();
                Evaluation evaluation = getEvaluation(entity);
                evaluation.addFN(); // fn++;
                evaluations.put(entity, evaluation);

                // FNs
                addEntry(fns, entity, gold.getText());
            }
        }

        // Gold on Silver
        for (Concept silver : silverList) {

            String entity = silver.getEntity();
            Evaluation evaluation = getEvaluation(entity);

            boolean matched;
            if (evaluationType.equals(EvaluationType.Exact)) {
                matched = goldList.containsExact(silver, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Left)) {
                matched = goldList.containsLeft(silver, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Right)) {
                matched = goldList.containsRight(silver, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Shared)) {
                matched = goldList.containsShared(silver, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Subspan)) {
                matched = goldList.containsSubspan(silver, identifierMatch);
            } else if (evaluationType.equals(EvaluationType.Overlap)) {
                matched = goldList.containsOverlap(silver, identifierMatch);
            } else {
                throw new RuntimeException("Evaluation type not supported: " + evaluationType);
            }

            if (!matched) {
                evaluation.addFP(); // fp++;
                // FPs
                addEntry(fps, entity, silver.getText());
            } else {
                evaluation.addTP(); // tp++;
            }
            evaluations.put(entity, evaluation);
        }
    }

    public void printFPs() {
        logger.info("FPs");
        logger.info("============");
        printStatistics(fps);
        logger.info("");
        logger.info("");
        logger.info("");
    }

    public void printFNs() {
        logger.info("FNs");
        logger.info("============");
        printStatistics(fns);
        logger.info("");
        logger.info("");
        logger.info("");
    }

    private void printStatistics(final Map<String, List<StatisticsEntry>> map) {
        for (String group : map.keySet()) {
            logger.info(group.toUpperCase());

            List<StatisticsEntry> lse = map.get(group);
            Collections.sort(lse, new StatisticsEntryComparator());

            for (int i = 0; i < 10 && i < lse.size(); i++) {
                StatisticsEntry se = lse.get(i);
                logger.info("{}:\t{}\t{}", new Object[]{i + 1, se.getName(), se.getOccurrences()});
            }
            logger.info("---");
            logger.info("");
        }
    }

    private void addEntry(Map<String, List<StatisticsEntry>> map, String group, String text) {
        List<StatisticsEntry> lse;
        text = text.toLowerCase();
        StatisticsEntry se = new StatisticsEntry(text, group, 1);


        if ((lse = map.get(group)) == null) {
            lse = new ArrayList<>();
            lse.add(se);
            map.put(group, lse);
        } else {
            if (lse.contains(se)) {
                se = lse.get(lse.indexOf(se));
                se.setOccurrences(se.getOccurrences() + 1);
            } else {
                lse.add(se);
            }
        }
    }

    public void reset() {
        this.evaluations = new HashMap<>();
    }

    public Evaluation getOverall() {
        int overallTP = 0, overallFP = 0, overallFN = 0;

        for (String entity : evaluations.keySet()) {
            Evaluation evaluation = evaluations.get(entity);
            overallTP += evaluation.getTP();
            overallFP += evaluation.getFP();
            overallFN += evaluation.getFN();
        }

        // Overall evaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setTP(overallTP);
        evaluation.setFP(overallFP);
        evaluation.setFN(overallFN);

        return evaluation;
    }

    public Evaluation getGroup(final String[] labels) {
        List<String> labelsList = Arrays.asList(labels);

        int overallTP = 0, overallFP = 0, overallFN = 0;

        for (String entity : evaluations.keySet()) {

            if (!labelsList.contains(entity)) {
                continue;
            }

            Evaluation evaluation = evaluations.get(entity);
            overallTP += evaluation.getTP();
            overallFP += evaluation.getFP();
            overallFN += evaluation.getFN();
        }

        // Overall evaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setTP(overallTP);
        evaluation.setFP(overallFP);
        evaluation.setFN(overallFN);

        return evaluation;
    }

    public void print() {
        for (String entity : evaluations.keySet()) {
            Evaluation evaluation = evaluations.get(entity);
            printEvaluation(entity, evaluation);
        }
        logger.info("");
        // Overall evaluation
        Evaluation evaluation = getOverall();
        printEvaluation("overall", evaluation);
    }

    public void printToExcel() {
        DecimalFormat decimalFormat = new DecimalFormat("0.0000");
        for (String entity : evaluations.keySet()) {
            Evaluation evaluation = evaluations.get(entity);

            System.out.println(String.format("%-10s\t" + decimalFormat.format(evaluation.getPrecision()).replaceAll(",", ".") + "\t" +
                    decimalFormat.format(evaluation.getRecall()).replaceAll(",", ".") + "\t" +
                    decimalFormat.format(evaluation.getF1()).replaceAll(",", ".")
            , entity));

//            System.out.println(entity + "\t" + decimalFormat.format(evaluation.getPrecision()).replaceAll(",", "."));
//            System.out.println("\t" + decimalFormat.format(evaluation.getRecall()).replaceAll(",", "."));
//            System.out.println("\t" + decimalFormat.format(evaluation.getF1()).replaceAll(",", "."));
        }
//        logger.info("");
        // Overall evaluation
        Evaluation evaluation = getOverall();
//        printEvaluation("overall", evaluation);
        System.out.println(String.format("%-10s\t" + decimalFormat.format(evaluation.getPrecision()).replaceAll(",", ".") + "\t" +
                decimalFormat.format(evaluation.getRecall()).replaceAll(",", ".") + "\t" +
                decimalFormat.format(evaluation.getF1()).replaceAll(",", ".")
        , "OVERALL"));
    }

    private void printEvaluation(final String entity, final Evaluation evaluation) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0000");

        logger.info("{}:\tTP:{}\tFP:{}\tFN:{}\t\tP:{}\tR:{}\tF1:{}", new Object[]{
                StringUtils.leftPad(entity.toUpperCase(), 30, " "),
                evaluation.getTP(), evaluation.getFP(), evaluation.getFN(),
                decimalFormat.format(evaluation.getPrecision()), decimalFormat.format(evaluation.getRecall()), decimalFormat.format(evaluation.getF1())
        });

    }

    private enum EntryType {
        ANNOTATION,
        IDENTIFIER
    }

    private ConceptList getConceptListFromInputStream(final InputStream inputStream) {
        Map<Integer, Concept> map = new HashMap<>();

        try (InputStreamReader isr = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                EntryType entryType;

                if (conceptPattern.matcher(parts[0]).matches()) {
                    entryType = EntryType.ANNOTATION;
                } else if (identifiersPattern.matcher(parts[0]).matches()) {
                    entryType = EntryType.IDENTIFIER;
                } else {
                    continue;
                }

                String[] fields;
                Integer identifier;
                Concept concept;


                switch (entryType) {
                    case ANNOTATION:
                        // Get unique identifier
                        identifier = Integer.parseInt(parts[0].substring(1));

                        fields = parts[1].split("\\s+");
                        // Get entity
                        String entity = "";
                        for (int i = 0; i < fields.length - 2; i++) {
                            entity += fields[i] + " ";
                        }
                        entity = entity.trim();

                        // Convert entity
                        entity = evaluateEntity(entity);
                        if (entity == null) {
                            continue;
                        }

                        // Get start and end positions
                        int start = Integer.parseInt(fields[fields.length - 2]);
                        int end = Integer.parseInt(fields[fields.length - 1]);

                        // Get text
                        String text;
                        if (parts.length < 3) {
                            text = "";
                        } else {
                            text = parts[2];
                        }

                        // Set concept
                        concept = new Concept(start, end, entity, text);

                        // Add to map
                        map.put(identifier, concept);
                        break;
                    case IDENTIFIER:
                        boolean UMLS_PROC_FUNC_mapped = false;
                        fields = parts[1].split("\\s+");
                        // Get unique identifier
                        identifier = Integer.parseInt(fields[1].substring(1));

                        // Check if identifier exists on map
                        if (!map.containsKey(identifier)) {
                            continue;
                        }

                        concept = map.get(identifier);
                        String identifiersText = parts[2];
                        String[] identifiers = identifiersText.split("[|]");

                        boolean isPRGE = false;
                        if (concept.getEntity().equals("PRGE") && identifiersText.contains("UNIPROT:")) {
                            numPRGEnames++;
                            isPRGE = true;
                        }

                        boolean isCELL = false;
                        if (concept.getEntity().equals("CELL") && identifiersText.contains("UMLS:")) {
                            numCellNames++;
                            isCELL = true;
                        }

                        if (concept.getEntity().equals("PROC_FUNC")){
                            numPROCFUNC++;
                        }

                        boolean isPROCFUNC = false;
                        if (concept.getEntity().equals("PROC_FUNC") && identifiersText.contains("UMLS:")) {
                            numPROCFUNCNames++;
                            isPROCFUNC = true;
                        }

                        Set<String> uniqueIDs = new HashSet<>();
                        for (String id : identifiers) {
                            if (id.contains(" ")) {
                                id = id.substring(0, id.indexOf(" "));
                            }

                            if (!id.contains(":")) {
                                continue;
                            }

                            String[] idparts = id.split("[:]");

                            String finalSource = idparts[0];
                            String finalID = idparts[1];

                            if (finalSource.equals("NCBITaxon")) {
                                finalSource = "NCBI";
                            }

                            // Trick
                            if (concept.getEntity().equals("SPEC")) {
                                if (finalID.equals("10116")){
                                    finalID = "10114";
                                }
                                if (finalID.equals("10090")){
                                    finalID = "10088";
                                }
                            }
                            String fi = finalSource + ":" + finalID;

                            if (concept.getEntity().equals("PROC_FUNC") && finalSource.equals("UMLS")) {
                                numUMLSPROCFUNC++;
                                IDConverter converter = IDConverter.getInstance(mappersFolderPath);
                                Collection<String> goIDs = converter.getCUI2GO().get(fi);
//                                concept.getIdentifiers().addAll(goIDs);
                                uniqueIDs.addAll(goIDs);
                                if (!goIDs.isEmpty()) {
                                    numUMLSPROCFUNCmapped++;
                                    UMLS_PROC_FUNC_mapped = true;
                                }
                            } else if (concept.getEntity().equals("CELL") && finalSource.equals("UMLS")) {
                                numUMLSCL++;
                                IDConverter converter = IDConverter.getInstance(mappersFolderPath);
                                Collection<String> clIDs = converter.getCUI2CL().get(fi);
//                                concept.getIdentifiers().addAll(goIDs);
                                uniqueIDs.addAll(clIDs);
                                if (!clIDs.isEmpty()) {
                                    numUMLSCLmapped++;
                                }
                            } else if (concept.getEntity().equals("PRGE") && finalSource.equals("UNIPROT")) {
                                numUNIPROTS++;

                                IDConverter converter = IDConverter.getInstance(mappersFolderPath);

                                // Entrez gene
                                Collection<String> egIDs = converter.getUniprot2EG().get(fi);
                                uniqueIDs.addAll(egIDs);
                                if (!egIDs.isEmpty()) {
                                    numUNIPROTSmapped++;
                                }

                                // Protein Ontology
//                                DataList<String> prIDs = converter.getUniprot2PR().get(fi);
//                                uniqueIDs.addAll(prIDs);
//                                if (!prIDs.isEmpty()) {
//                                    numUNIPROTSmapped++;
//                                }

                            } else {
//                                concept.getIdentifiers().add(fi);
                                uniqueIDs.add(fi);
                            }
                        }
                        concept.getIdentifiers().addAll(uniqueIDs);

                        if (isPRGE && !uniqueIDs.isEmpty()) {
                            numPRGEmapped++;
                        }

                        if (isCELL && !uniqueIDs.isEmpty()) {
                            numCellMapped++;
                        } else if (isCELL){
                            boolean s = false;
                        }

                        if (isPROCFUNC && !uniqueIDs.isEmpty() && UMLS_PROC_FUNC_mapped) {
                            numPROCFUNCMapped++;
                        }

                        if (concept.getEntity().equals("PROC_FUNC") && !uniqueIDs.isEmpty()){
                            numPROCFUNCwithIDs++;
                        }

                        break;
                }


//                int start = Integer.parseInt(fields[fields.length - 2]);
//                int end = Integer.parseInt(fields[fields.length - 1]);
//
//                String text;
//                if (parts.length < 3) {
////                    logger.info("{}", line);
////                    continue;
//                    text = "";
//                } else {
//                    text = parts[2];
//                }
//
////                logger.info("{}|\t\t\t{}", line, entity);
//
//                Concept concept = new Concept(start, end, entity, text);
//                if (!conceptList.contains(concept)) {
//                    conceptList.add(concept);
//                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem reading the input stream.", ex);
        }


        ConceptList conceptList = new ConceptList();
        for (Integer id : map.keySet()) {
            Concept concept = map.get(id);
            if (!conceptList.contains(concept)) {
                conceptList.add(concept);
            }
        }
        return conceptList;
    }

    private String evaluateEntity(final String entity) {
        if (entity.equals("sub") || entity.equals("italic") || entity.equals("sup") ||
                entity.equals("bold") || entity.equals("underline") ||
                entity.equals("independent_continuant")) {
            return null;
        }

        // CRAFT
        if (entity.equals("NCBITaxon")) {
            return "SPEC";
        }
        if (entity.equals("taxonomic_rank")) {
            return "SPEC";
        }


        if (entity.equals("EntrezGene")) {
//            return null;
            return "PRGE";
        }
        if (entity.equals("PR")) {
//            return "PRGE";
            return null;

        }
        if (entity.equals("SO")) {
//                    entity = "PRGE";
            return null;
        }

        if (entity.equals("CL")) {
            return "CELL";
        }
        if (entity.equals("CHEBI")) {
            return "CHED";
        }
        if (entity.equals("GO_CC")) {
            return "COMP";
        }

        if (entity.equals("GO_MF")) {
            return "PROC_FUNC";
        }
        if (entity.equals("GO_BP")) {
            return "PROC_FUNC";
        }

        if (entity.equals("DISO")) {
            return null;
        }


        // COCOA
//        if (entity.equals("Organism")) {
//            return "SPEC";
//        }
//        if (entity.equals("Organism1")) {
//            return "SPEC";
//        }
//                if (entity.equals("Organism2")) {
//                    entity = "SPEC";
//                }

//        if (entity.equals("Protein")) {
//            return "PRGE";
//        }
//        if (entity.equals("Molecule")) {
//            return "PRGE";
//        }
//        if (entity.equals("Category")) {
//            return "PRGE";
//        }
//
//
//        if (entity.equals("Bio_Process")) {
//            return "PROC_FUNC";
//        }
//        if (entity.equals("Process")) {
//            return "PROC_FUNC";
//        }
//
//        if (entity.equals("Cell")) {
//            return "CELL";
//        }
//
//        if (entity.equals("Cellular_component")) {
//            return "COMP";
//        }
//        if (entity.equals("Complex")) {
//            return "COMP";
//        }
//        if (entity.equals("Location")) {
//            return "COMP";
//        }
//
//        if (entity.equals("Chemical")) {
//            return "CHED";
//        }


        // ANEM
//                if (entity.equals("Multi-tissue_structure")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Organism_subdivision")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("CELL")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("COMP")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Pathological_formation")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Tissue")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Organism_substance")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Immaterial_anatomical_entity")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Anatomical_system")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Developing_anatomical_structure")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Body_part")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Body part")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Organ")) {
//                    entity = "ANAT";
//                }
//                if (entity.equals("Developing_anatomy")) {
//                    entity = "ANAT";
//                }


//                entity = "ANAT";

        return entity;
    }

    private Evaluation getEvaluation(final String entity) {
        if (evaluations.containsKey(entity)) {
            return evaluations.get(entity);
        } else {
            return new Evaluation();
        }
    }

    public static enum EvaluationType {
        Exact,
        Left,
        Right,
        Shared,
        Subspan,
        Overlap
    }

}
