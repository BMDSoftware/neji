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

package pt.ua.tm.neji.sentence;

import com.aliasi.util.Pair;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.module.BaseTagger;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.sentencesplitter.SentenceSplitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Module to perform sentence splitting and tagging.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Passages})
@Provides({Resource.Sentences})
public class SentenceTagger extends BaseTagger {

    /**
     * {@link Logger} to be used in the class.
     */
    private Logger logger = LoggerFactory.getLogger(SentenceTagger.class);
    private int startText;
    private int startTag;
    private boolean inText;
    private int sentenceCounter;
    private SentenceSplitter sentencesplitter;
    private List<Pair> sentencesPositions;
    private int sentencesOffset;

    /**
     * Tag sentences parsing XML content of the specified tags.
     *
     * @throws NejiException Problem loading the sentence tagger.
     */
    public SentenceTagger(final SentenceSplitter sentencesplitter) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToXMLTag(start_text, "roi");
        super.addActionToXMLTag(end_text, "roi");
        this.sentencesplitter = sentencesplitter;
//        this.isToProvidePositions = false;
        sentencesPositions = new ArrayList<>();

        // Initialise XML parser
//        try {
//            Nfa nfa = new Nfa(Nfa.NOTHING);
//            nfa.or(Xml.STag("roi"), start_text);
//            nfa.or(Xml.ETag("roi"), end_text);
//            setNFA(nfa, DfaRun.UNMATCHED_COPY);
//        } catch (ReSyntaxException ex) {
//            throw new NejiException(ex);
//        }
        startText = 0;
        startTag = 0;
//        sentenceCounter = 0;
//        inText = false;
//        sentencesOffset = 0;
    }

    @Override
    public void setPipeline(Pipeline pipeline) {
        super.setPipeline(pipeline);
        pipeline.storeModuleData("sentencesPositionsList", sentencesPositions);
    }

    private Action start_text = new StartAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            inText = true;
            startText = yytext.indexOf(">", start) + 1;
            startTag = start;
        }
    };

    private Action end_text = new EndAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            if (inText) {
                StringBuffer sb = new StringBuffer(yytext.substring(startText, start));

                int sbLength = sb.length();

                int[][] indices = sentencesplitter.split(sb.toString());

                int offset = 0;

                boolean newLine = false;

                for (int i = 0; i < indices.length; i++) {
                    int s = offset + indices[i][0];
                    int e = offset + indices[i][1];

//                    if (isToProvidePositions) {

                    int startSentence = sentencesOffset + indices[i][0];
                    int endSentence = sentencesOffset + indices[i][1] - 1;

                    Pair p = new Pair(startSentence, endSentence);
                    sentencesPositions.add(p);

//                        if (i == indices.length - 1) {
////                            sentencesOffset += indices[i][1];
////                            sentencesOffset += sb.length() - indices[i][1];
////                            sentencesOffset++;
//                            sentencesOffset += sb.length()-1;
//                        }
//                    }
//                    String prefix = "<s id=\"" + sentenceCounter++ + "\">";

                    String prefix = "<s";
                    prefix += " id=\"" + sentenceCounter++ + "\"";
//                    prefix += " start=\"" + (s - sentencesOffset) + "\"";
//                    prefix += " end=\"" + (e - sentencesOffset - 1) + "\"";
                    prefix += ">";

                    String suffix = "</s>";

                    String taggedSentence = prefix + sb.substring(s, e) + suffix;

                    sb.replace(s, e, taggedSentence);

                    offset += prefix.length() + suffix.length();
                }

                sentencesOffset += sbLength;
                sentencesOffset++;

                yytext.replace(startTag, yytext.indexOf(">", start) + 1, sb.toString());

                inText = false;
            }
        }
    };
}
