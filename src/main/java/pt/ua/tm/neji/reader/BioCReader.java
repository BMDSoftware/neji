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

package pt.ua.tm.neji.reader;


import bioc.*;
import bioc.io.BioCDocumentReader;
import bioc.io.BioCFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import javax.xml.stream.XMLStreamException;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.AnnotationType;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Relation;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseReader;
import pt.ua.tm.neji.core.module.DynamicNLP;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserSupport;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Module to tag sentences and read annotations and relations from BioC data.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
@Provides({Resource.Passages, Resource.Sentences, Resource.DynamicNLP, Resource.Annotations, Resource.Relations})
public class BioCReader extends BaseReader implements DynamicNLP {

    private static Logger logger = LoggerFactory.getLogger(BioCReader.class);

    private List<Sentence> processedSentences;
    private final Parser parser;
    private final ParserLevel customLevel;

    public BioCReader(Parser parser) throws NejiException {
        this(parser, parser.getLevel());
    }

    public BioCReader(Parser parser, ParserLevel customLevel) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToRegex(text_action, ".+");
        this.processedSentences = new ArrayList<>();
        this.parser = parser;
        this.customLevel = customLevel;
    }

    private DefaultAction text_action = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            StringBuffer sb = new StringBuffer();
            List<String> sentencesText = new ArrayList<>();

            // create factory
            BioCFactory factory = BioCFactory.newFactory(BioCFactory.STANDARD);
            Corpus corpus = getPipeline().getCorpus();
            
            // Set original corpus text
            corpus.setText(solveEscaping(yytext.toString()));
            
            try{
                if (!parser.isLaunched())
                    parser.launch();

                BioCDocumentReader reader =
                        factory.createBioCDocumentReader(new StringReader(yytext.toString()));

                BioCDocument document;
                while ((document = reader.readDocument()) != null) {
                    for(BioCPassage passage : document.getPassages()){

                        // if passage has text, ignores BIOC Sentences
                        if(!passage.getText().isEmpty() && passage.getSentences().isEmpty()) {
                            String str = passage.getText();
                            sb.append(str.trim());
                            
                            List<Sentence> parsedSentences = parser.parseWithLevel(customLevel, corpus, str);
                            processSentences(parsedSentences, passage.getAnnotations(), passage.getRelations());

                            // Get sentences text
                            for (Sentence s : parsedSentences) {
                                sentencesText.add(str.substring(s.getStart(), s.getEnd()));
                            }
                            
                        } else {
                            // if document has BIOC Sentences
                            for (Iterator<BioCSentence> iter = passage.getSentences().listIterator(); iter.hasNext();) {
                                BioCSentence bioCSentence = iter.next();
                                String str = bioCSentence.getText();
                                
                                sb.append(str);
                                if (iter.hasNext()) {
                                    sb.append(" ");
                                }                               
                                
                                List<Sentence> temp = parser.parseWithLevel(customLevel, corpus, str);
                                
                                // Get sentences text
                                for (Sentence s : temp) {
                                    sentencesText.add(str.substring(s.getStart(), s.getEnd()));
                                }
                                
                                for(Sentence s : temp){
                                    s.setStart(s.getStart() + bioCSentence.getOffset());
                                    s.setEnd(s.getEnd() + bioCSentence.getOffset());
                                }
                                
                                processSentences(temp, bioCSentence.getAnnotations(), bioCSentence.getRelations());
                            }
                        }
                    }
                }

                reader.close();
            }catch (XMLStreamException| NejiException | IOException ex) {
                throw new RuntimeException("There was a problem parsing the sentence. Document: " +
                        corpus.getIdentifier(), ex);
            }
            
            // after the Sentences were constructed, tag these in the text buffer
            int sentenceCounter = 0, offset = 0, s = 0, e = 0;
            int sentenceStart, sentenceEnd = 0, deviation;
            String text = corpus.getText();
            for(int i = 0 ; i < processedSentences.size() ; i++) {
                Sentence sentence = processedSentences.get(i);
                String sentenceText = sentencesText.get(i);
                
                s = sb.indexOf(sentenceText, e + offset);
                e = s + sentence.getText().length();

                String prefix = "<s";
                prefix += " id=\"" + sentenceCounter++ + "\"";
                prefix += ">";
                String suffix = "</s>";
                String taggedSentence = prefix + sb.substring(s, e) + suffix;
                sb.replace(s, e, taggedSentence);

                offset =+ prefix.length() + suffix.length();
                
                // Set sentence indexes
                sentenceStart = text.indexOf(sentenceText, sentenceEnd);
                sentenceEnd = sentenceStart + sentence.getText().length();
                
                deviation = sentenceStart - sentence.getStart();
                
                sentence.setStart(sentenceStart);
                sentence.setEnd(sentenceEnd);
                sentence.setOriginalStart(sentence.getOriginalStart() + deviation);
                sentence.setOriginalEnd(sentence.getOriginalEnd() + deviation);
            }

            processedSentences.clear();
            yytext.replace(start, yytext.length(), sb.toString());
        }
    };

    private void processSentences(List<Sentence> parsedSentences,
                                  List<BioCAnnotation> bioCAnnotationList,
                                  List<BioCRelation> bioCRelationList){
        // the maps below link reference IDs to annotations, and are used to find the
        // corresponding concepts for the detected Relations
        Multimap<Sentence, Annotation> processedAnnotations = HashMultimap.create();
        Map<String, Annotation> refIdsToAnnotations = new HashMap<>();

        // the set below is used to avoid unnecessary checks
        Set<BioCAnnotation> toRemove = new LinkedHashSet<>();

        for(Sentence newSentence : parsedSentences){

            // do annotations
            for (BioCAnnotation bioCAnnotation : bioCAnnotationList) {
                int startA = 0, endA = 0;
                for (BioCLocation location : bioCAnnotation.getLocations()) {
                    startA = location.getOffset();
                    endA = location.getOffset() + location.getLength();

                    // check if this annotation can be contained in this Sentence
                    if(startA<newSentence.getStart() || startA>=newSentence.getEnd() ||
                            endA<=newSentence.getStart() || endA>newSentence.getEnd()) {
                        continue;
                    }

                    int realStart = startA - newSentence.getStart();
                    int realEnd = endA - newSentence.getStart();

                    Annotation newAnnotation =
                            AnnotationImpl.newAnnotationByCharPositions(
                                    newSentence,
                                    realStart,
                                    realEnd - 1,
                                    1.0);

                    // the static constructor above will check for existing corresponding Tokens
                    // if there are no Tokens for this annotation at the specified start and end indexes,
                    // it returns null, meaning this Annotation does not belong to this Sentence.
                    if(newAnnotation==null)
                        continue;



//                        logger.info("StartA:\t\t{}, EndA:\t\t{}", startA, endA);
//                        logger.info("RealStart:\t{}, RealEnd:\t{}", realStart, realEnd);
//                        logger.info(": SUBSTRING TEXT      \t:\t"+newSentence.getText().substring(realStart, realEnd));
//                        logger.info(": BIOC ANNOTATION      \t:\t"+bioCAnnotation.getText());
//                        logger.info(": NEW  ANNOTATION ADDED\t:\t"+newAnnotation.getText());
//                        logger.info("+------------------------+--------------------------------");



                    try{
                        newAnnotation.setType(AnnotationType.valueOf(bioCAnnotation.getInfon("type")));
                    }catch (IllegalArgumentException | EnumConstantNotPresentException ex){
                        // AnnotationType info was not found at Infon. This simply means the annotation
                        // won't have a type, but it should still be added to the Sentence.
                    }

                    newSentence.addAnnotationToTree(newAnnotation); // adds annotation to Sentence
                    processedAnnotations.put(newSentence, newAnnotation); // puts on
                    refIdsToAnnotations.put(bioCAnnotation.getID(), newAnnotation);
                    toRemove.add(bioCAnnotation);
                }
            }

            bioCAnnotationList.removeAll(toRemove);
            processedSentences.add(newSentence);
        }


        // do relations
        for (BioCRelation relation : bioCRelationList) {
            Annotation concept1=null, concept2=null;
            int c = 0;
            for (BioCNode node : relation.getNodes()) {
                String ID = node.getRefid();
                if (c == 0) {
                    concept1 = refIdsToAnnotations.get(ID);
                }
                if (c == 1) {
                    concept2 = refIdsToAnnotations.get(ID);
                }
                c++;
            }
            if (c == 2) {
                for(Sentence s : parsedSentences){
                    Collection<Annotation> ann = processedAnnotations.get(s);
                    if(ann.contains(concept1) && ann.contains(concept2))
                        s.addRelation(new Relation(s, concept1, concept2));

                }
            }
        }
    }

    @Override
    public Collection<ParserLevel> getLevels() {
        Collection<ParserLevel> levels = ParserSupport.levelsSupportedByLanguage(parser.getTool(), parser.getLanguage());
        Collection<ParserLevel> levelsFilter = new ArrayList<>();
        switch (customLevel) {
            case TOKENIZATION:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                break;
            case POS:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                levelsFilter.add(ParserLevel.POS);
                break;
            case CHUNKING:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                levelsFilter.add(ParserLevel.POS);
                levelsFilter.add(ParserLevel.CHUNKING);
            case LEMMATIZATION:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                levelsFilter.add(ParserLevel.POS);
                levelsFilter.add(ParserLevel.LEMMATIZATION);
                break;
            case DEPENDENCY:
                levelsFilter.add(ParserLevel.TOKENIZATION);
                levelsFilter.add(ParserLevel.DEPENDENCY);
                break;
        }

        levels.retainAll(levelsFilter);
        return levels;
    }

    @Override
    public InputFormat getFormat() {
        return InputFormat.BIOC;
    }
}
