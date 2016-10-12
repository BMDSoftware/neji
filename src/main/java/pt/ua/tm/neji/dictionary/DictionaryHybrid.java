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

import monq.jfa.DfaRun;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseHybrid;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import uk.ac.man.entitytagger.Mention;

import java.util.List;

/**
 * Hybrid module to perform dictionary matching and load the resulting concepts into the internal {@link Corpus}
 * representation.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Tokens})
@Provides({Resource.Annotations})
public class DictionaryHybrid extends BaseHybrid {

    private Dictionary dictionary;
    private int startSentence;
    private DictionaryMatching dictionaryMatching;

    public DictionaryHybrid(Dictionary dictionary, DictionaryMatching dictionaryMatching) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_action, "s");
        super.addActionToXMLTag(end_action, "s");
        assert (dictionary != null);
        this.dictionary = dictionary;
        this.dictionaryMatching = dictionaryMatching;
        this.startSentence = 0;
    }

    public DictionaryHybrid(Dictionary dictionary) throws NejiException {
        this(dictionary, new DictionaryMatching());
    }

//    public DictionaryHybrid(final Corpus corpus, Dictionary dictionary) throws NejiException {
//        this(dictionary);
//        getPipeline().setCorpus(corpus);
//    }

//    public DictionaryHybrid(final Corpus corpus, Dictionary dictionary, DictionaryMatching dictionaryMatching) throws NejiException {
//        this(dictionary, dictionaryMatching);
//        getPipeline().setCorpus(corpus);
//    }

    private Action start_action = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            startSentence = yytext.indexOf(">", start) + 1;
        }
    };

    private Action end_action = new SentenceIteratorEndAction() {
        @Override
        public void execute(StringBuffer yytext, int start, Sentence nextSentence) {

            StringBuilder sb = new StringBuilder(yytext.substring(startSentence, start));
            String sentenceText = sb.toString();

            List<Mention> mentions = dictionary.getMatcher().match(sentenceText);

            List<Mention> toRemove = dictionaryMatching.removeList(mentions);
            mentions.removeAll(toRemove);

//            dictionaryMatching.getStopwordsPattern();

//            int endLastEntity = 0;
//            int previousNumChars = 0;
            // Add annotations
            for (Mention m : mentions) {
                
                if(dictionaryMatching.discardStopwords(m))
                    continue;

//                String sentenceBeforeEntity = sentenceText.substring(endLastEntity, m.getStart());

//                int numChars = Char.getNumNonWhiteSpaceChars(sentenceBeforeEntity);

//                int startEntityChars = previousNumChars + numChars;
//                int endEntityChars = previousNumChars + numChars + Char.getNumNonWhiteSpaceChars(m.getText()) - 1;
//

                String id = m.getIdsToString();

                int startEntityChars = m.getStart();
                int endEntityChars = m.getEnd()-1;
                Annotation a = AnnotationImpl.newAnnotationByCharPositions(nextSentence, startEntityChars, endEntityChars, 1.0);

                if (a != null) {
                    List<Identifier> ids = Identifier.getIdentifiersFromText(id);
                    a.setIDs(ids);
                    nextSentence.addAnnotationToTree(a);
                }

                startSentence = m.getEnd() + 1;
//                previousNumChars = endEntityChars + 1;
//                endLastEntity = m.getEnd();
            }
        }
    };
}

