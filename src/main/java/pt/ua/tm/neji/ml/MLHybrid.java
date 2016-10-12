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
package pt.ua.tm.neji.ml;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.*;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.postprocessing.Abbreviation;
import pt.ua.tm.neji.ml.postprocessing.Parentheses;
import pt.ua.tm.neji.train.config.ModelConfig;
import pt.ua.tm.neji.train.model.CRFBase;
import uk.ac.man.entitytagger.Mention;

/**
 * Module to perform concept recognition using Machine Learning.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.DynamicNLP})
@Provides({Resource.Annotations})
public class MLHybrid extends BaseHybrid implements DynamicNLP {

    private static Logger logger = LoggerFactory.getLogger(MLHybrid.class);

    private CRFBase crf;
    private Collection<Dictionary> dictionaries;
    private boolean doNormalization;
    private int startSentence;
    private String group;
    private boolean addAnnotationsWithoutIDs;

    public MLHybrid(CRFBase crf, String group) throws NejiException {
        this(crf, group, null, false);
    }

    public MLHybrid(MLModel model, CRFBase crf, boolean addAnnotationsWithoutIDs) throws NejiException {
        this(crf, model.getSemanticGroup(), model.getNormalizationDictionaries(), addAnnotationsWithoutIDs);
    }

    public MLHybrid(CRFBase crf, String group, Collection<Dictionary> dictionaries, boolean addAnnotationsWithoutIDs) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_action, "s");
        super.addActionToXMLTag(end_action, "s");
        assert (crf != null);
        this.crf = crf;
        this.dictionaries = dictionaries;
        this.doNormalization = true;
        this.startSentence = 0;
        this.group = group.toUpperCase();
        this.addAnnotationsWithoutIDs = addAnnotationsWithoutIDs;
    }

    public MLHybrid(CRFBase crf, String group, boolean addAnnotationsWithoutIDs) throws NejiException {
        this(crf, group, null, addAnnotationsWithoutIDs);
    }

    private Action start_action = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            startSentence = yytext.indexOf(">", start) + 1;
        }
    };


    private Action end_action = new SentenceIteratorEndAction() {
        @Override
        public void execute(StringBuffer yytext, int start, Sentence nextSentence) {
            try {
                // Get start and end of sentence
                int startSentence = yytext.indexOf("<s id=");
                int endSentence = yytext.lastIndexOf("</s>") + 4;

                int realStart = yytext.indexOf(">", startSentence) + 1;
                int realEnd = endSentence - 4;

                // Get sentence with XML tags
                String sentenceText = yytext.substring(realStart, realEnd);

                // Annotate sentence
                List<Annotation> annotationsML = MLAnnotator.annotate(nextSentence, crf);

                // Post-processing
                annotationsML = Parentheses.processRemoving(annotationsML);
                Abbreviation.process(nextSentence, annotationsML);

                if (doNormalization) {
                    // normalization (which will also move normalized annotations to tree)
                    normalize(nextSentence, sentenceText, annotationsML);
                }
                else {
                    // simply move all annotations to tree
                    moveAnnotationsToTree(nextSentence, annotationsML, group);
                }

            } catch (NejiException ex) {
                throw new RuntimeException("There was a problem annotating the sentence.", ex);
            }
        }
    };

    private void normalize(Sentence s, String sourceText, List<Annotation> annotationsML) {
        Annotation newAnnotation;

        for (Annotation a : annotationsML) {
            boolean matched = false;
            newAnnotation = AnnotationImpl.newAnnotationByTokenPositions(
                    a.getSentence(), a.getStartIndex(), a.getEndIndex(),
                    a.getScore());
            
            if (dictionaries != null) {
                for (Dictionary d : dictionaries) {
                    if (match(d, newAnnotation, group, sourceText)) {
                        matched = true;
                        break;
                    }
                }
            }

            if (matched) {
                s.addAnnotationToTree(newAnnotation);
            } else {
                if (addAnnotationsWithoutIDs) {
                    newAnnotation.addID(new Identifier("", "", "", group));
                    s.addAnnotationToTree(newAnnotation);
                }
            }
        }

        // Clean ML annotations since they are already on the tree
        annotationsML.clear();
    }

    /**
     * To be used when no normalization solution is used.
     */
    private void moveAnnotationsToTree(Sentence s, List<Annotation> annotationsML, String group) {
        for (Annotation a : annotationsML) {
            Annotation newAnnotation = AnnotationImpl.newAnnotationByTokenPositions(s, a.getStartIndex(), a.getEndIndex(), a.getScore());

            Identifier newAnnotationID = new Identifier("", "", "", group);
            newAnnotation.addID(newAnnotationID);

            s.addAnnotationToTree(newAnnotation);
        }

        // Clean ML annotations since they are already on the tree
        annotationsML.clear();
    }

    private boolean match(Dictionary dictionary, Annotation a, String group, String sourceText) {
        String[] ids;

        Sentence s = a.getSentence();


        int startChar = s.getToken(a.getStartIndex()).getStart();
        int endChar = s.getToken(a.getEndIndex()).getEnd() + 1;

        String text = sourceText.substring(startChar, endChar);

        List<Mention> mentions = dictionary.getMatcher().match(text);

        // Alternate
        if (mentions.size() >= 1) {
            ids = mentions.get(0).getIds();
            for (String textId : ids) {
                Identifier id = Identifier.getIdentifierFromText(textId);
                id.setGroup(group);
                a.addID(id);
            }
            return true;
        }
        return false;
    }

    @Override
    public Collection<ParserLevel> getLevels() {
        Collection<ParserLevel> levels = new HashSet<>();
        ModelConfig config = this.crf.getConfig();

        if(config.isToken()) {
            levels.add(ParserLevel.TOKENIZATION);
        }
        if(config.isLemma()) {
            levels.add(ParserLevel.TOKENIZATION);
            levels.add(ParserLevel.LEMMATIZATION);
        }
        if(config.isPos()) {
            levels.add(ParserLevel.TOKENIZATION);
            levels.add(ParserLevel.LEMMATIZATION);
            levels.add(ParserLevel.POS);
        }
        if(config.isChunk()) {
            levels.add(ParserLevel.TOKENIZATION);
            levels.add(ParserLevel.LEMMATIZATION);
            levels.add(ParserLevel.POS);
            levels.add(ParserLevel.CHUNKING);
        }
        if(config.isNLP()) {
            levels.add(ParserLevel.TOKENIZATION);
            levels.add(ParserLevel.LEMMATIZATION);
            levels.add(ParserLevel.POS);
            levels.add(ParserLevel.CHUNKING);
            levels.add(ParserLevel.DEPENDENCY);
        }

        return levels;
    }
}
