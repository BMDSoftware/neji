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

package pt.ua.tm.neji.ml;

import cc.mallet.fst.CRF;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.SumLatticeDefault;
import cc.mallet.fst.Transducer;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Sequence;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.Constants.LabelTag;
import pt.ua.tm.neji.core.Constants.Parsing;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.train.model.CRFBase;
import pt.ua.tm.neji.train.model.CRFModel;

/**
 * Class used to annotate any {@link Corpus} using one or several
 * {@link CRFModel} trained by Gimli.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class MLAnnotator {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(MLAnnotator.class);

    /**
     * The {@link Sentence} to be annotated by this {@link MLAnnotator}.
     */
    public static List<Annotation> annotate(Sentence s, CRFBase modelCRF) throws NejiException {
        CRF crf = modelCRF.getCRF();

        //The ML annotationsML associated with the Sentence s.
        List<Annotation> annotationsML = new ArrayList<>();

        // Check parsing direction
        boolean sentenceReversed = false;
        if (!modelCRF.getParsing().equals(s.getCorpus().getParsing())) {
            reverseAnnotationsML(annotationsML, s);
            sentenceReversed = true;
        }

        // Get pipe
        crf.getInputPipe().getDataAlphabet().stopGrowth();
        Pipe pipe = crf.getInputPipe();

        // Get instance
        Instance i = new Instance(s.toExportFormat(), null, 0, null);
        i = pipe.instanceFrom(i);

        // Get predictions
        NoopTransducerTrainer crfTrainer = modelCRF.getTransducer();

        Sequence input = (Sequence) i.getData();
        Transducer tran = crfTrainer.getTransducer();
        Sequence pred = tran.transduce(input);

        // Get score
        double logScore = new SumLatticeDefault(crf, input, pred).getTotalWeight();
        double logZ = new SumLatticeDefault(crf, input).getTotalWeight();
        double prob = Math.exp(logScore - logZ);

        // Add tags
        LabelTag p;
        for (int j = 0; j < pred.size(); j++) {
            p = LabelTag.valueOf(pred.get(j).toString());
            s.getToken(j).setLabel(p);
        }

        // Add annotationsML from tags
        if (modelCRF.getParsing().equals(Parsing.FW)) {
            addAnnotationsMLFromTagsForward(annotationsML, s, prob);
        } else {
            addAnnotationsMLFromTagsBackward(annotationsML, s, prob);
        }

        // Get sentence back to its original parsing direction
        if (sentenceReversed) {
            reverseAnnotationsML(annotationsML, s);
        }

        return annotationsML;
    }

    /**
     * Change the parsing order of the sentence.
     */
    private static void reverseAnnotationsML(List<Annotation> annotationsML, Sentence s) {
        Annotation newAnnotation, a;
        s.reverseTokens();

        int sentenceSize = s.getTokens().size() - 1;
        int annotationSize;
        int newStart;
        int newEnd;

        for (int i = 0; i < annotationsML.size(); i++) {
            a = annotationsML.get(i);
            annotationSize = a.getEndIndex() - a.getStartIndex();
            newStart = sentenceSize - a.getEndIndex();
            newEnd = sentenceSize - a.getEndIndex() + annotationSize;

            newAnnotation = AnnotationImpl.newAnnotationByTokenPositions(s, newStart, newEnd, a.getScore());
            annotationsML.set(i, newAnnotation);
        }
    }

    /**
     * Add annotationsML to the sentence considering that the tokens are already
     * tagged.
     *
     * @param score The confidence value to generate the annotationsML.
     */
    private static void addAnnotationsMLFromTags(List<Annotation> annotationsML, Sentence s, double score) {
        if (s.getCorpus().getParsing().equals(Parsing.BW)) {
            addAnnotationsMLFromTagsBackward(annotationsML, s, score);
        } else {
            addAnnotationsMLFromTagsForward(annotationsML, s, score);
        }
    }

    /**
     * Add annotationsML to the sentence considering that the tokens are already
     * tagged, and the corpus is in Forward direction.
     *
     * @param score The confidence value to generate the annotationsML.
     */
    private static void addAnnotationsMLFromTagsForward(List<Annotation> annotationsML, Sentence s, final double score) {
        LabelTag label;
        boolean isAnnotation;
        int start = 0, end = 0;
        for (int i = 0; i < s.getTokens().size(); i++) {
            label = s.getToken(i).getLabel();
            isAnnotation = false;

            if (s.getCorpus().getFormat().equals(Constants.LabelFormat.IO)) {
                if (label.equals(LabelTag.I)) {
                    isAnnotation = true;
                }
            } else {
                if (label.equals(LabelTag.B)) {
                    isAnnotation = true;
                }
            }

            if (isAnnotation) {
                start = end = i;

                for (int k = start + 1; k < s.getTokens().size(); k++) {
                    label = s.getToken(k).getLabel();
                    if (label.equals(LabelTag.B) || label.equals(LabelTag.O)) {
                        break;
                    }
                    end++;
                }

                annotationsML.add(AnnotationImpl.newAnnotationByTokenPositions(s, start, end, score));
                i = end;
            }
        }
    }

    /**
     * Add annotationsML to the sentence considering that the tokens are already
     * tagged, and the corpus is in Backward direction.
     *
     * @param score The confidence value to generate the annotationsML.
     */
    private static void addAnnotationsMLFromTagsBackward(List<Annotation> annotationsML, Sentence s, final double score) {
        LabelTag label;
        boolean isAnnotation;
        int start = 0, end = 0;
        for (int i = s.getTokens().size() - 1; i >= 0; i--) {
            label = s.getToken(i).getLabel();
            isAnnotation = false;

            if (s.getCorpus().getFormat().equals(Constants.LabelFormat.IO)) {
                if (label.equals(LabelTag.I)) {
                    isAnnotation = true;
                }
            } else {
                if (label.equals(LabelTag.B)) {
                    isAnnotation = true;
                }
            }

            if (isAnnotation) {
                start = end = i;

                for (int k = end - 1; k >= 0; k--) {
                    label = s.getToken(k).getLabel();
                    if (label.equals(LabelTag.B) || label.equals(LabelTag.O)) {
                        break;
                    }
                    start--;
                }

                annotationsML.add(AnnotationImpl.newAnnotationByTokenPositions(s, start, end, score));
                i = start;
            }
        }
    }

    /**
     * The {@link Corpus} whose {@link Sentence} is/are to be annotated by this {@link MLAnnotator}.
     */
    public static void annotate(Corpus c, CRFBase crf) throws NejiException {
        for (Sentence s : c.getSentences()) {
            annotate(s, crf);
        }
    }
}
