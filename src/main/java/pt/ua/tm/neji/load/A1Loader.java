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

package pt.ua.tm.neji.load;

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
import pt.ua.tm.neji.evaluation.Concept;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.processor.filewrappers.InputFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class A1Loader extends BaseLoader {

    private static final Pattern triggerPattern = Pattern.compile("T[0-9]+");
    private static Logger logger = LoggerFactory.getLogger(A1Loader.class);
    private InputFile a1FileFormat;
    private List<Concept> annotations;
    private Pattern discard;
    private Map<String, Pattern> aggregate;

//    private AbstractFaAction eof = new AbstractFaAction() {
//        @Override
//        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
//            // Add annotations from A1 file
////            parseCorpus(a1FileFormat);
//            parse();
//        }
//    };


    private EndAction end_action = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            parse();
        }
    };

    public A1Loader(final InputFile a1FileFormat,
                    final Pattern discard, final Map<String, Pattern> aggregate) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(end_action, "s");
        this.a1FileFormat = a1FileFormat;
        this.discard = discard;
        this.aggregate = aggregate;
        this.annotations = loadAnnotations(a1FileFormat);

//        try {
//            Nfa nfa = new Nfa(Nfa.NOTHING);
//            nfa.or(Xml.ETag("s"), eof);
//            setNFA(nfa, DfaRun.UNMATCHED_COPY);
//        } catch (ReSyntaxException ex) {
//            throw new NejiException(ex);
//        }
    }

//    public A1Loader(final Corpus corpus, final InputFile a1FileFormat,
//                    final Pattern discard, final Map<String, Pattern> aggregate) throws NejiException {
//        this(a1FileFormat, discard, aggregate);
//        getPipeline().setCorpus(corpus);
//    }

    private List<Concept> loadAnnotations(final InputFile inputPile) {
        List<Concept> annotations = new ArrayList<>();
        String line;
        try (
                InputStreamReader reader = new InputStreamReader(inputPile.getInStream());
                BufferedReader br = new BufferedReader(reader)
        ) {
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (!triggerPattern.matcher(parts[0]).matches()) {
                    continue;
                }
                String[] fields = parts[1].split("\\s+");

                String group = fields[0];

                if (discard.matcher(group).matches()) {
                    continue;
                }

                // Aggregate groups
                for (String newGroup : aggregate.keySet()) {
                    if (aggregate.get(newGroup).matcher(group).matches()) {
                        group = newGroup;
                    }
                }

                int startCharPos = Integer.parseInt(fields[1]);
                int endCharPos = Integer.parseInt(fields[2]) - 1;
                String text = parts[2];

//                addAnnotation(group, startCharPos, endCharPos, text);
                annotations.add(new Concept(startCharPos, endCharPos, group, text));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return annotations;
    }

//    private void parseCorpus(final InputFileFormat inputCorpus) {
//        String line;
//        try (
//                InputStreamReader reader = new InputStreamReader(inputCorpus.getInStream());
//                BufferedReader br = new BufferedReader(reader)
//        ) {
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split("\t");
//                String[] fields = parts[1].split("\\s+");
//
//                // Discard events
//                if (parts.length < 3) {
//                    continue;
//                }
//
//                String group = fields[0];
//                int startCharPos = Integer.parseInt(fields[1]);
//                int endCharPos = Integer.parseInt(fields[2]) - 1;
//                String text = parts[2];
//
//                addAnnotation(group, startCharPos, endCharPos, text);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void parse() {
        for (Concept annotation : annotations) {
            addAnnotation(annotation.getEntity(), annotation.getStart(), annotation.getEnd(), "");
        }
    }

    private void addAnnotation(final String group, int start, int end, String text) {

        for (Sentence sentence : getPipeline().getCorpus()) {
            if (start >= sentence.getStart() && end <= sentence.getEnd()) {

                int startToken = 0, endToken = 0;
                for (int i = 0; i < sentence.size(); i++) {
                    Token t = sentence.getToken(i);

                    if (start >= (sentence.getStart() + t.getStart())) {
                        startToken = i;
                    }
                    if (end <= (sentence.getStart() + t.getEnd())) {
                        endToken = i;
                        break;
                    }
                }

                if (endToken < startToken) {
                    // Annotation that does not fit our tokenisation
                    logger.debug("Annotation is not compatible with tokenization: {}", text);
                } else {

                    Annotation annotation = AnnotationImpl.newAnnotationByTokenPositions(sentence, startToken, endToken, 1.0);
                    Identifier identifier = new Identifier("", "", "", group);
                    annotation.addID(identifier);
                    sentence.addAnnotationToTree(annotation);
                }
            }
        }
    }


}
