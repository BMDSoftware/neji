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
package pt.ua.tm.neji.disambiguator;

import monq.jfa.DfaRun;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Module that performs annotations disambiguation in the pipeline's sentences, removing
 * ambiguity between concepts in the concept tree.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Annotations})
@Provides({Resource.Annotations})
public class Disambiguate extends BaseLoader {

    private boolean discardByDepth;
    private int depth;
    private boolean discardNestedSameGroup, discardSameGroupByPriority;


    private Action sentence_action = new SentenceIteratorDefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start, Sentence nextSentence) {

            if (discardByDepth) {
                Disambiguator.discardByDepth(nextSentence, depth);
            }

            if (discardSameGroupByPriority) {
                Disambiguator.discardSameGroupByPriority(nextSentence);
            }

            if (discardNestedSameGroup) {
                Disambiguator.discardNestedSameGroup(nextSentence);
            }
        }
    };

    public Disambiguate(final boolean discardNestedSameGroup, final boolean discardSameGroupByPriority)
            throws NejiException {
        this(discardNestedSameGroup, discardSameGroupByPriority, -1);
        this.discardByDepth = false;
    }

//    public Disambiguate(final Corpus corpus, final boolean discardNestedSameGroup, final boolean discardSameGroupByPriority)
//            throws NejiException {
//        this(discardNestedSameGroup, discardSameGroupByPriority);
//        getPipeline().setCorpus(corpus);
//    }

    public Disambiguate(final boolean discardNestedSameGroup,
                        final boolean discardSameGroupByPriority, final int depth) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToGoofedElement(sentence_action, "s");
        this.discardNestedSameGroup = discardNestedSameGroup;
        this.discardSameGroupByPriority = discardSameGroupByPriority;
        this.discardByDepth = true;
        this.depth = depth;
    }

//    public Disambiguate(final Corpus corpus, final boolean discardNestedSameGroup,
//                        final boolean discardSameGroupByPriority, final int depth) throws NejiException {
//        this(discardNestedSameGroup, discardSameGroupByPriority, depth);
//        getPipeline().setCorpus(corpus);
//    }
}
