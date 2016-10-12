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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.module.BaseTagger;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.util.List;

/**
 * Tagger module to perform dictionary matching and provide the concepts to the stream.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Sentences})
@Provides({}) // only concept tags are provided
public class DictionaryTagger extends BaseTagger {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(DictionaryTagger.class);
    private Matcher matcher;
    private int startSentence;
    private DictionaryMatching dictionaryMatching;
    private String start_e = "<e ";
    private String end_start_e = ">";
    private String end_e = "</e>";
    private String start_id = "id=\"";
    private String end_id = "\"";
    private int offset = start_e.length() + end_start_e.length()
            + end_e.length() + start_id.length() + end_id.length();

    public DictionaryTagger(Matcher matcher, DictionaryMatching dictionaryMatching) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_sentence, "s");
        super.addActionToXMLTag(end_sentence, "s");
        assert (matcher != null);
        this.matcher = matcher;
        this.dictionaryMatching = dictionaryMatching;
        this.startSentence = 0;
    }

    public DictionaryTagger(Matcher matcher) throws NejiException {
        this(matcher, new DictionaryMatching());
    }

    private Action start_sentence = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            startSentence = yytext.indexOf(">", start) + 1;
        }
    };

    private Action end_sentence = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            StringBuffer sb = new StringBuffer(yytext.substring(startSentence, start));

            List<Mention> mentions = matcher.match(sb.toString());
            int sum_offset = 0;

            List<Mention> toRemove = dictionaryMatching.removeList(mentions);

            if (!toRemove.isEmpty() && Constants.verbose) {
                for (Mention m : toRemove) {
                    logger.info("INTERSECTION REMOVED: {}-{} {}", new String[]{
                            new Integer(m.getStart()).toString(),
                            new Integer(m.getEnd()).toString(),
                            m.getText()
                    });
                }
            }
            mentions.removeAll(toRemove);

//            dictionaryMatching.getStopwordsPattern();

            // Add annotations
            for (Mention m : mentions) {

                if(dictionaryMatching.discardStopwords(m))
                    continue;

                StringBuilder s = new StringBuilder();
                s.append(start_e);
                s.append(start_id);

                // Solve problem with IDs that contain scores
                String ids = m.getIdsToString();
                ids = ids.replaceAll("[\\\\?][\\d]+[\\\\.,][\\d]+", "");

                s.append(ids);
                //s.append(m.getIdsToString());
                s.append(end_id);
                s.append(end_start_e);
                s.append(m.getText());
                s.append(end_e);

                sb = sb.replace(sum_offset + m.getStart(), sum_offset + m.getEnd(), s.toString());

                //sum_offset += offset + m.getIdsToString().length();
                sum_offset += offset + ids.length();
            }

            // Replace sentence with species annotations
            yytext.replace(startSentence, start, sb.toString());
        }
    };
}
