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

package pt.ua.tm.neji.train.model;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByThreadedLabelLikelihood;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.tsf.FeaturesInWindow;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.pipe.tsf.TokenTextCharNGrams;
import cc.mallet.pipe.tsf.TokenTextCharPrefix;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;
import cc.mallet.pipe.tsf.TrieLexiconMembership;
import cc.mallet.types.InstanceList;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants.Parsing;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.config.ModelConfig;
import pt.ua.tm.neji.train.config.Resources;
import pt.ua.tm.neji.train.features.mallet.Input2TokenSequence;
import pt.ua.tm.neji.train.features.mallet.MixCase;
import pt.ua.tm.neji.train.features.mallet.NumberOfCap;
import pt.ua.tm.neji.train.features.mallet.NumberOfDigit;
import pt.ua.tm.neji.train.features.mallet.RegexContains;
import pt.ua.tm.neji.train.features.mallet.Stemmer;
import pt.ua.tm.neji.train.features.mallet.WordLength;
import pt.ua.tm.neji.train.features.mallet.WordShape;

/**
 *
 * @author jeronimo
 */
public class CRFModel extends CRFBase {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(CRFModel.class);
    /**
     * Regular expression to identify uppercase letters.
     */
    private static String CAPS = "[A-Z]";
    /**
     * Regular expression to identify lowercase letters.
     */
    private static String LOW = "[a-z]";
    /**
     * Regular expression to identify Greek letters.
     */
    private static String GREEK = "(alpha|beta|gamma|delta|epsilon|zeta|eta|theta|iota|kappa|lambda|mu|nu|xi|omicron|pi|rho|sigma|tau|upsilon|phi|chi|psi|omega|α|β|Γ|γ|Δ|δ|ε|ζ|η|Θ|θ|ι|κ|Λ|λ|μ|ν|Ξ|ξ|ο|Π|π|ρ|Σ|σ|τ|υ|Φ|φ|χ|Ψ|ψ|Ω|ω)";
    private static String GREEKSYMBOLS = "(α|β|Γ|γ|Δ|δ|ε|ζ|η|Θ|θ|ι|κ|Λ|λ|μ|ν|Ξ|ξ|ο|Π|π|ρ|Σ|σ|τ|υ|Φ|φ|χ|Ψ|ψ|Ω|ω)";

    /**
     * Constructor.
     *
     * @param config Model configuration.
     * @param parsing Parsing direction.
     */
    public CRFModel(final ModelConfig config, final Parsing parsing) {
        super(config, parsing);
    }

    /**
     * Constructor that loads the model from an input file.
     *
     * @param config Model configuration.
     * @param parsing Parsing direction.
     * @param file File that contains the model.
     * @throws GimliException Problem reading the model from file.
     */
    public CRFModel(final ModelConfig config, final Parsing parsing, final InputStream inputModel) throws NejiException {
        super(config, parsing, inputModel);
    }

    /**
     * Setup the features to be used by the model.
     *
     * @return The {@link Pipe} that contains the description of the features to
     * be extracted.
     * @throws NejiException Problem specifying the features.
     */
    @Override
    public Pipe getFeaturePipe() throws NejiException {
        ModelConfig config = getConfig();
        ArrayList<Pipe> pipe = new ArrayList<Pipe>();

        try {
            pipe.add(new Input2TokenSequence(getConfig()));
            if (config.isPrge()) {
                pipe.add(new TrieLexiconMembership("PRGE", new InputStreamReader(Resources.getResource("prge")), true));
            }

            if (config.isVerbs()) {
                pipe.add(new TrieLexiconMembership("VERB", new InputStreamReader(Resources.getResource("verbs")), true));
            }

            if (config.isConcepts()) {
                pipe.add(new TrieLexiconMembership("CONCEPT", new InputStreamReader(Resources.getResource("aminoacid")), true));
                pipe.add(new TrieLexiconMembership("CONCEPT", new InputStreamReader(Resources.getResource("nucleicacid")), true));
                pipe.add(new TrieLexiconMembership("CONCEPT", new InputStreamReader(Resources.getResource("nucleobase")), true));
                pipe.add(new TrieLexiconMembership("CONCEPT", new InputStreamReader(Resources.getResource("nucleoside")), true));
                pipe.add(new TrieLexiconMembership("CONCEPT", new InputStreamReader(Resources.getResource("nucleotide")), true));
            }

            if (config.isStem()) {
                pipe.add(new Stemmer("STEM="));
            }

            if (config.isCapitalization()) {
                pipe.add(new RegexMatches("InitCap", Pattern.compile(CAPS + ".*")));
                pipe.add(new RegexMatches("EndCap", Pattern.compile(".*" + CAPS)));
                pipe.add(new RegexMatches("AllCaps", Pattern.compile(CAPS + "+")));
                pipe.add(new RegexMatches("Lowercase", Pattern.compile(LOW + "+")));
                pipe.add(new MixCase());
                pipe.add(new RegexMatches("DigitsLettersAndSymbol", Pattern.compile("[0-9a-zA-z]+[-%/\\[\\]:;()'\"*=+][0-9a-zA-z]+")));
            }

            if (config.isCounting()) {
                pipe.add(new NumberOfCap());
                pipe.add(new NumberOfDigit());
                pipe.add(new WordLength());
            }

            if (config.isSymbols()) {
                pipe.add(new RegexMatches("Hyphen", Pattern.compile(".*[-].*")));
                pipe.add(new RegexMatches("BackSlash", Pattern.compile(".*[/].*")));
                pipe.add(new RegexMatches("OpenSquare", Pattern.compile(".*[\\[].*")));
                pipe.add(new RegexMatches("CloseSquare", Pattern.compile(".*[\\]].*")));
                pipe.add(new RegexMatches("Colon", Pattern.compile(".*[:].*")));
                pipe.add(new RegexMatches("SemiColon", Pattern.compile(".*[;].*")));
                pipe.add(new RegexMatches("Percent", Pattern.compile(".*[%].*")));
                pipe.add(new RegexMatches("OpenParen", Pattern.compile(".*[(].*")));
                pipe.add(new RegexMatches("CloseParen", Pattern.compile(".*[)].*")));
                pipe.add(new RegexMatches("Comma", Pattern.compile(".*[,].*")));
                pipe.add(new RegexMatches("Dot", Pattern.compile(".*[\\.].*")));
                pipe.add(new RegexMatches("Apostrophe", Pattern.compile(".*['].*")));
                pipe.add(new RegexMatches("QuotationMark", Pattern.compile(".*[\"].*")));
                pipe.add(new RegexMatches("Star", Pattern.compile(".*[*].*")));
                pipe.add(new RegexMatches("Equal", Pattern.compile(".*[=].*")));
                pipe.add(new RegexMatches("Plus", Pattern.compile(".*[+].*")));
            }

            if (config.isNgrams()) {
                pipe.add(new TokenTextCharNGrams("CHARNGRAM=", new int[]{2, 3, 4}));
            }

            if (config.isSuffix()) {
                pipe.add(new TokenTextCharSuffix("2SUFFIX=", 2));
                pipe.add(new TokenTextCharSuffix("3SUFFIX=", 3));
                pipe.add(new TokenTextCharSuffix("4SUFFIX=", 4));
            }

            if (config.isPrefix()) {
                pipe.add(new TokenTextCharPrefix("2PREFIX=", 2));
                pipe.add(new TokenTextCharPrefix("3PREFIX=", 3));
                pipe.add(new TokenTextCharPrefix("4PREFIX=", 4));
            }

            if (config.isMorphology()) {
                pipe.add(new WordShape());
            }

            if (config.isGreek()) {
                pipe.add(new RegexMatches("GREEK", Pattern.compile(GREEK, Pattern.CASE_INSENSITIVE)));
                pipe.add(new RegexContains("HASGREEK", Pattern.compile(GREEKSYMBOLS, Pattern.CASE_INSENSITIVE)));
            }

            if (config.isRoman()) {
                pipe.add(new RegexMatches("ROMAN", Pattern.compile("((?=[MDCLXVI])((M{0,3})((C[DM])|(D?C{0,3}))?((X[LC])|(L?XX{0,2})|L)?((I[VX])|(V?(II{0,2}))|V)?))")));
            }

            if (config.isConjunctions()) {
                pipe.add(new OffsetConjunctions(true, Pattern.compile("LEMMA=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
                pipe.add(new OffsetConjunctions(true, Pattern.compile("POS=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
            }

            if (config.isWindow()) {
                //pipe.add(new FeaturesInWindow("WINDOW_LEMMA=", -3, 3, Pattern.compile("LEMMA=.*"), true));
                //pipe.add(new FeaturesInWindow("WINDOW_WORD=", -3, 3, Pattern.compile("WORD=.*"), true));
                //pipe.add(new FeaturesInWindow("WINDOW_LEXICON=", -3, 3, Pattern.compile("LEXICON=.*"), true));
                //pipe.add(new FeaturesInWindow("WINDOW_SPECIAL=", -3, 3, Pattern.compile("SPECIAL=.*"), true));
                //pipe.add(new FeaturesInWindow("WINDOW_FEATURES=", -1, 1));

                //pipe.add(new FeaturesAtWindow(-3, 3));

                // Actual
                pipe.add(new FeaturesInWindow("WINDOW=", -1, 0, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
                pipe.add(new FeaturesInWindow("WINDOW=", -2, -1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
                pipe.add(new FeaturesInWindow("WINDOW=", 0, 1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
                pipe.add(new FeaturesInWindow("WINDOW=", -1, 1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
                pipe.add(new FeaturesInWindow("WINDOW=", -3, -1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));

            }

//            pipe.add(new PrintTokenSequenceFeatures());

            pipe.add(new TokenSequence2FeatureVectorSequence(true, true));

        }
        catch (Exception ex) {
            throw new NejiException("There was a problem initializing the features.", ex);
        }
        return new SerialPipes(pipe);
    }

    /**
     * Train the CRF model.
     *
     * @throws NejiException Problem training the model.
     */
    @Override
    public void train(final Corpus corpus) throws NejiException {

        // Garantee that the corpus order follows the model
        boolean corpusReversed = false;
        if (!getParsing().equals(corpus.getParsing())) {
            corpus.reverse();
            corpusReversed = true;
        }

        ModelConfig config = getConfig();

        // Set pipe
        Pipe p = getFeaturePipe();

        // Load Data
        logger.info("Extracting features and converting data into training format...");
        InstanceList trainingData = corpus.toModelFormatTrain(p);

        // Define CRF
        int order = config.getOrder() + 1;
        int[] orders = new int[order];
        for (int i = 0; i < order; i++) {
            orders[i] = i;
        }

        CRF crf = new CRF(trainingData.getPipe(), (Pipe) null);
        String startStateName = crf.addOrderNStates(
                trainingData,
                orders,
                null, // "defaults" parameter; see mallet javadoc
                "O",
                corpus.getForbiddenPattern(),
                null,
                true); // true for a fully connected CRF

        for (int i = 0; i < crf.numStates(); i++) {
            crf.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
        }
        crf.getState(startStateName).setInitialWeight(0.0);
        crf.setWeightsDimensionAsIn(trainingData, false);

        
        logger.info("Training model...");
        
        int numThreads = 8;
        CRFTrainerByThreadedLabelLikelihood crfTrainer = new CRFTrainerByThreadedLabelLikelihood(crf, numThreads);
        crfTrainer.train(trainingData);
        crfTrainer.shutdown();

        TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
                new InstanceList[]{trainingData},
                new String[]{"train"}, corpus.getAllowedTags(), corpus.getAllowedTags()) {
        };
        evaluator.evaluate(crfTrainer);

        setCRF(crf);

        if (corpusReversed) {
            corpus.reverse();
        }
    }
    
}
