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
package pt.ua.tm.neji.core;

import pt.ua.tm.neji.cli.Main;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Store global constants and type enumerators.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public final class Constants {

    public static final String MODULES_PATH = "pt.ua.tm.neji";

    public static boolean verbose = false;

    public static final String TARGET_PATH;

    static {
        String aux2 = null;
        try {
            String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File decodedPath = new File(URLDecoder.decode(path, "UTF-8"));
            aux2 = decodedPath.getParentFile().getAbsolutePath();
        }catch (UnsupportedEncodingException ex){
            // will not happen, UTF-8 is a valid encoding format
        }

        TARGET_PATH = aux2;
    }

    /**
     * Regular expression to identify uppercase letters.
     */
    public static final String CAPS = "[A-Z]";
    /**
     * Regular expression to identify lowercase letters.
     */
    public static final String LOW = "[a-z]";
    /**
     * Regular expression to identify Greek letters.
     */
    public static final String GREEK = "(alpha|beta|gamma|delta|epsilon|zeta|eta|theta|iota|kappa|lambda|mu|nu|xi|omicron|pi|rho|sigma|tau|upsilon|phi|chi|psi|omega)";
    /**
     * Regular expression to identify Roman numbers.
     */
    public static final String ROMAN = "((?=[MDCLXVI])((M{0,3})((C[DM])|(D?C{0,3}))?((X[LC])|(L?XX{0,2})|L)?((I[VX])|(V?(II{0,2}))|V)?))";

    public static boolean isPunctuation(char ch) {
        return ( "`~!@#$%^&*()-=_+[]\\{}|;':\",./<>?".indexOf(ch) != -1 );
    }

    private static final String RESOURCES_DIR = "resources" + File.separator;
    public static final String MODELS_DIR = new File(RESOURCES_DIR, "models").getAbsolutePath() + File.separator;
    public static final String DICTIONARIES_DIR = new File(RESOURCES_DIR, "lexicons").getAbsolutePath() + File.separator;
    public static final String TOOLS_DIR = new File(RESOURCES_DIR, "tools").getAbsolutePath() + File.separator;
    public static final String GDEP_DIR = TOOLS_DIR + "gdep" + File.separator;
    private static final String GDEP_WIN = "gdep-win32.exe";
    private static final String GDEP_LIN64 = "./gdep-linux64";
    private static final String GDEP_LIN32 = "./gdep-linux32";
    private static final String GDEP_MAC = "./gdep-mac64";
    public static final String OPENNLP_DIR = TOOLS_DIR + "opennlp" + File.separator;

    public static String getGDepTool(){
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("windows")){
            return GDEP_WIN;
        } else if (os.contains("mac")){
            return GDEP_MAC;
        } else {
            if (System.getProperty("os.arch").contains("64"))
                return GDEP_LIN64;
            else
                return GDEP_LIN32;
        }
    }

    /**
     * Parsing direction of the text.
     */
    public enum Parsing {

        /**
         * Parse the text from left to right, forward direction.
         */
        FW,
        /**
         * Parse the text from right to left, backward direction.
         */
        BW
    }

    /**
     * Available annotation encoding formats, which use a combination of
     * {@link LabelTag}.
     * <p>
     * This encoding formats are used to tag the tokens as being part or not
     * of an entity name.
     * <p>
     * Note that <code>IO</code> and <code>BIO</code> were carefully tested.
     * However, only minor tests were performed with <code>BMEWO</code>.
     * Consequently, we do not guarantee the functionality of this format.
     */
    public enum LabelFormat {

        /**
        * The most basic encoding format that only uses Inside and Outside
         * tags.
         */
        IO,
        /**
        * The de facto standard solution, which solves the problem of followed
         * entity names.
         */
        BIO,
        /**
         * Encoding format that also marks the tokens in the middle of the
         * entity name.
         */
        BMEWO;
    }

    /**
     * Symbols used to tag the tokens.
     */
    public enum LabelTag {

        /**
         * The token is in the <b>Beginning</b> of the annotation.
         */
        B,
        /**
         * The token is <b>Inside</b> the annotation.
         */
        I,
        /**
         * The token is <b>Outside</b> the annotation.
         */
        O,
        /**
         * The token is in the <b>Middle</b> of the annotation.
         */
        M,
        /**
         * The token is in the <b>End</b> of the annotation.
         */
        E,
        /**
         * The single token is a complete annotation.
         */
        W;
    }

    /**
     * Biomedical entity names supported by Gimli.
     */
    public enum EntityType {

        /**
         * Used for Gene/Protein entity names.
         */
        protein,
        /**
         * User for DNA entity names.
         */
        DNA,
        /**
         * Used for RNA entity names.
         */
        RNA,
        /**
         * Used for Cell Type entity names.
         */
        cell_type,
        /**
         * Used for Cell Line entity names.
         */
        cell_line;
    }

    /**
     * Dictionary types supported by Gimli.
     */
    public enum DictionaryType {
        /**
         * Gene/protein names.
         */
        PRGE,
        /**
         * Biomedical concepts.
         */
        CONCEPT,
        /**
         * Trigger verbs.
         */
        VERB;
    }

//    /**
//     * Command line to parse texts using Enju, performing tokenisation and using
//     * models trained on biomedical documents (GENIA).
//     */
//    public static final String[] ENJUPARSERCOMMAND = {
//        TOOLS_DIR + "enju-2.4.2/enju",
//        "-genia"
//    };
//
//    /**
//     * Command line to parse texts using Enju, without performing tokenisation
//     * and using models trained on biomedical documents (GENIA).
//     */
//    public static final String[] ENJUPARSERCOMMANDNT = {
//        TOOLS_DIR + "enju-2.4.2/enju",
//        "-genia",
//        "-nt"
//    };
//
//    /**
//     * Command line to parse texts using GeniaTagger, performing tokenisation
//     * and using models trained on biomedical documents (GENIA).
//     */
//    public static final String[] GENIATAGGERCOMMAND = {
//            TOOLS_DIR + "geniatagger/genia"
//    };
//
//    /**
//     * Command line to parse texts using GeniaTagger, without performing
//     * tokenisation and using models trained on biomedical documents (GENIA).
//     */
//    public static final String[] GENIATAGGERCOMMANDNT = {
//            TOOLS_DIR + "geniatagger/genia", "-nt"
//    };

}
