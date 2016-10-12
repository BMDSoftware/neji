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

package pt.ua.tm.neji.postprocessing;

import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module to find abbreviations that correspond to an already annotated concept.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Tokens})
@Provides({Resource.Annotations})
public class Abbreviation extends BaseLoader {
    private static Logger logger = LoggerFactory.getLogger(Abbreviation.class);
    private ExtractAbbreviations extractor;
    private HashMap<String, String> abbreviationPairs;
    private Map<String, List<Identifier>> added;

    private Action start_sentence = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            // Do nothing
        }
    };

    private Action end_sentence = new SentenceIteratorEndAction() {
        @Override
        public void execute(StringBuffer yytext, int start, Sentence nextSentence) {

            // Get start and end of sentenceCounter
            int startSentence = yytext.indexOf("<s id=");
            int endSentence = yytext.lastIndexOf("</s>") + 4;

            int realStart = yytext.indexOf(">", startSentence) + 1;
            int realEnd = endSentence - 4;

            // Get sentenceCounter with XML tags
            String sentenceText = yytext.substring(realStart, realEnd);

            HashMap<String, String> pairs = extractor.extractAbbrPairs(sentenceText);

            for (String st : pairs.keySet()) {
                if (!abbreviationPairs.containsKey(st)) {
                    abbreviationPairs.put(st, pairs.get(st));
                }
            }

            // Add previously added abbreviations
            for (String key : added.keySet()) {
                addAnnotationsFromText(nextSentence, sentenceText, key, added.get(key));
            }


            // Deal with short and long forms
            for (String shortText : abbreviationPairs.keySet()) {
                String longText = abbreviationPairs.get(shortText);
                List<Annotation> shortAnnotations = getAnnotationsFromText(nextSentence, sentenceText, shortText);
                List<Annotation> longAnnotations = getAnnotationsFromText(nextSentence, sentenceText, longText);

                if (shortAnnotations.isEmpty() || longAnnotations.isEmpty()) {
//                    logger.error("Annotations not present!");
                    continue;
                }

                for (Annotation shortAnnotation : shortAnnotations) {
                    for (Annotation longAnnotation : longAnnotations) {
                        if (containsAnnotation(nextSentence, shortAnnotation) && containsAnnotation(nextSentence, longAnnotation)) {
                            continue;
                        } else if (!containsAnnotation(nextSentence, shortAnnotation) && containsAnnotation(nextSentence, longAnnotation)) {
                            // Add short
                            AnnotationImpl tmp = getAnnotation(nextSentence, longAnnotation.getStartIndex(), longAnnotation.getEndIndex());

                            if (!tmp.getIDs().isEmpty()) {
                                shortAnnotation.setIDs(tmp.getIDs());
                                nextSentence.addAnnotationToTree(shortAnnotation);

                                // Add to added annotations
                                if (!added.containsKey(shortText)) {
                                    added.put(shortText, tmp.getIDs());
                                }
                            }
                        } else if (containsAnnotation(nextSentence, shortAnnotation) && !containsAnnotation(nextSentence, longAnnotation)) {
                            // Add long
                            AnnotationImpl tmp = getAnnotation(nextSentence, shortAnnotation.getStartIndex(), shortAnnotation.getEndIndex());
                            if (!tmp.getIDs().isEmpty()) {
                                longAnnotation.setIDs(tmp.getIDs());
                                nextSentence.addAnnotationToTree(longAnnotation);

                                // Add to added annotations
                                if (!added.containsKey(longText)) {
                                    added.put(longText, tmp.getIDs());
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }

        }
    };

    public Abbreviation() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_sentence, "s");
        super.addActionToXMLTag(end_sentence, "s");
        this.extractor = new ExtractAbbreviations();
        this.abbreviationPairs = new HashMap<>();
        this.added = new HashMap<>();
    }

//    public Abbreviation(final Corpus corpus) throws NejiException {
//        this();
//        getPipeline().setCorpus(corpus);
//    }

    private boolean containsAnnotation(Sentence sentence, Annotation a2) {

        for (Annotation a1 : sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, false)) {
            if (a1.getStartIndex() == a2.getStartIndex() && a1.getEndIndex() == a2.getEndIndex()) {
                return true;
            }
        }
        return false;
    }

    private AnnotationImpl getAnnotation(Sentence sentence, int start, int end) {
        for (Annotation a : sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, false)) {
            AnnotationImpl a1 = (AnnotationImpl)a;
            if (a1.getStartIndex() == start && a1.getEndIndex() == end) {
                return a1;
            }
        }
        return null;
    }

    //    /**
//     * Get the annotation from text.
//     *
//     * @param s    The sentence that contains the annotation.
//     * @param text The text of the annotation.
//     * @return The annotation that reflect the input text.
//     */
//    private DefaultAnnotation getAnnotationFromText(final Sentence s, final String text) {
//        String[] tokens = text.split(" ");
//
//        int start;
//        int end;
//        int count;
//        for (int i = 0; i < s.size(); i++) {
//            String tokenText = s.getToken(i).getText();
//
//            for (int j = 0; j < tokenText.length(); j++) {
//
//            }
//
//            if (s.getToken(i).getText().equals(tokens[0])) {
//                end = start = i;
//                count = 1;
//                for (int j = start + 1; j < s.size() && count < tokens.length && s.getToken(j).getText().equals(tokens[count]); j++) {
//                    end++;
//                    count++;
//                }
//                return DefaultAnnotation.newAnnotationIDByTokenPositions(s, start, end, 0.0);
//            }
//        }
//        return null;
//    }
    private List<Annotation> getAnnotationsFromText(final Sentence sentence, final String sentenceText, final String targetText) {

        List<Annotation> annotations = new ArrayList<>();

        Pattern pattern = Pattern.compile(Pattern.quote(targetText));

        Matcher matcher = pattern.matcher(sentenceText);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end() - 1;

            int tokenStart = -1;
            int tokenEnd = -1;

            for (Token token : sentence.getTokens()) {
                if (token.getStart() == start) {
                    tokenStart = token.getIndex();
                }
                if (token.getEnd() == end) {
                    tokenEnd = token.getIndex();
                }
            }

            if (tokenStart == -1 || tokenEnd == -1) {
//                logger.error("Match not found: " + targetText);
                continue;
            }

            Annotation annotation = AnnotationImpl.newAnnotationByTokenPositions(sentence, tokenStart, tokenEnd, 1.0);
            annotations.add(annotation);
        }

        return annotations;
    }

    private List<Annotation> addAnnotationsFromText(final Sentence sentence, final String sentenceText,
                                                    final String targetText, List<Identifier> ids) {

        List<Annotation> annotations = new ArrayList<>();

        Pattern pattern = Pattern.compile(Pattern.quote(targetText));

        Matcher matcher = pattern.matcher(sentenceText);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end() - 1;

            int tokenStart = -1;
            int tokenEnd = -1;

            for (Token token : sentence.getTokens()) {
                if (token.getStart() == start) {
                    tokenStart = token.getIndex();
                }
                if (token.getEnd() == end) {
                    tokenEnd = token.getIndex();
                }
            }

            if (tokenStart == -1 || tokenEnd == -1) {
//                logger.error("Match not found: " + targetText);
                continue;
            }

            Annotation annotation = AnnotationImpl.newAnnotationByTokenPositions(sentence, tokenStart, tokenEnd, 1.0);
            annotation.setIDs(ids);
            if (!ids.isEmpty()) {
                sentence.addAnnotationToTree(annotation);
            }

        }

        return annotations;
    }
}
