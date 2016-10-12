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
package pt.ua.tm.neji.train.dictionary;

import java.util.List;
import monq.jfa.DfaRun;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseHybrid;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.DictionaryMatching;
import pt.ua.tm.neji.exception.NejiException;
import uk.ac.man.entitytagger.Mention;

/**
 *
 * @author jeronimo
 */
@Requires({Resource.Tokens})
@Provides({Resource.Annotations})
public class TrainDictionary extends BaseHybrid {

    private Dictionary dictionary;
    private int startSentence;
    private DictionaryMatching dictionaryMatching;

    public TrainDictionary(Dictionary dictionary, DictionaryMatching dictionaryMatching) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_action, "s");
        super.addActionToXMLTag(end_action, "s");
        assert (dictionary != null);
        this.dictionary = dictionary;
        this.dictionaryMatching = dictionaryMatching;
        this.startSentence = 0;
    }

    public TrainDictionary(Dictionary dictionary) throws NejiException {
        this(dictionary, new DictionaryMatching());
    }

    private Module.Action start_action = new BaseModule.StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            startSentence = yytext.indexOf(">", start) + 1;
        }
    };

    private Module.Action end_action = new BaseModule.SentenceIteratorEndAction() {
        @Override
        public void execute(StringBuffer yytext, int start, Sentence nextSentence) {

            StringBuilder sb = new StringBuilder(yytext.substring(startSentence, start));
            String sentenceText = sb.toString();
            
            List<Mention> mentions = dictionary.getMatcher().match(sentenceText);

            List<Mention> toRemove = dictionaryMatching.removeList(mentions);
            mentions.removeAll(toRemove);
            
            // Add annotations
            for (Mention m : mentions) {
                
                if(dictionaryMatching.discardStopwords(m))
                {
                    continue;
                }

                String id = m.getIdsToString();

                int startEntityChars = m.getStart();
                int endEntityChars = m.getEnd()-1;
                Annotation a = AnnotationImpl.newAnnotationByCharPositions(nextSentence, startEntityChars, endEntityChars, 1.0);
                
                if (a != null) {
                    List<Identifier> ids = Identifier.getIdentifiersFromText(id);
                    a.setIDs(ids);
                    nextSentence.addAnnotationToTreeWithFeatures(a, dictionary.getGroup());
                }

                startSentence = m.getEnd() + 1;
            }
        }
    };
}

