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

package pt.ua.tm.neji.writer;

import bioc.*;
import bioc.io.BioCDocumentWriter;
import bioc.io.BioCFactory;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Relation;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Writer that represents corpus data in the BioC format.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
@Requires({Resource.Tokens})
public class BioCWriter extends BaseWriter {

    /** {@link org.slf4j.Logger} to be used in the class. */
    private static Logger logger = LoggerFactory.getLogger(NejiWriter.class);
    private int offset;
    private StringBuilder content;
    private BioCDocumentWriter writer;
    private StringWriter output;
    private BioCDocument document;
    private BioCPassage passage;
    private Map<Annotation, String> annotationToRefId;

//    public BioCWriter(final Corpus corpus) throws NejiException {
//        this();
//        getPipeline().setCorpus(corpus);
//    }

    public BioCWriter() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToGoofedElement(text_action, "s");
        super.setEofAction(eof_action);

        this.content = new StringBuilder();
        this.offset = 0;
        this.annotationToRefId = new HashMap<>();

        BioCCollection collection = new BioCCollection();
        collection.setSource("");
        collection.setDate("");

        document = new BioCDocument();
        passage = new BioCPassage();
        passage.setOffset(offset);
        document.addPassage(passage);
        collection.addDocument(document);

        try {
            output = new StringWriter();
            BioCFactory factory = BioCFactory.newFactory(BioCFactory.STANDARD);
            writer = factory.createBioCDocumentWriter(output);
            writer.writeCollectionInfo(collection);
        } catch (XMLStreamException ex) {
            throw new NejiException(ex);
        }
    }

    private EofAction eof_action = new EofAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            try{
                writer.writeDocument(document);
                writer.close();

                content.append(output);

            }catch (IOException | XMLStreamException ex){
                logger.error(ex.toString());
            }

            yytext.replace(0, yytext.length(), content.toString().trim());
        }
    };

    private Action text_action = new SentenceIteratorDefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start, Sentence nextSentence) {

            // Get start and end of sentence
            int startSentence = yytext.indexOf("<s id=");
            int endSentence = yytext.lastIndexOf("</s>") + 4;

            int realStart = yytext.indexOf(">", startSentence) + 1;
            int realEnd = endSentence - 4;


            String sentenceText = yytext.substring(realStart, realEnd);


            yytext.replace(startSentence, endSentence, sentenceText);

            // Get final start and end of sentence
            int startChar = offset + yytext.indexOf(sentenceText);
            int endChar = startChar + sentenceText.length();

            BioCSentence newBioCSentence = new BioCSentence();
            newBioCSentence.setOffset(offset + startSentence);
            newBioCSentence.setText(nextSentence.getText());
            processSentence(newBioCSentence, nextSentence);
            passage.addSentence(newBioCSentence);


            // Remove processed input from input
            yytext.replace(0, endSentence, "");

            offset = endChar;
        }
    };

    private void processSentence(BioCSentence bioCSentence,
                                 Sentence nejiSentence){
        int refId = 1;

        // do annotations
        List<Annotation> annotationList = nejiSentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, false);
        for (Annotation a : annotationList) {
            BioCAnnotation newBioCAnnotation = new BioCAnnotation();

            int offset = bioCSentence.getOffset();
            int start = offset + nejiSentence.getToken(a.getStartIndex()).getStart();
            int end = offset + nejiSentence.getToken(a.getEndIndex()).getEnd() + 1 - start;
            newBioCAnnotation.setLocation(start, end);

            newBioCAnnotation.setText(a.getText());
            newBioCAnnotation.putInfon("type", a.getType().toString());

            String newRefId = "T" + refId;
            newBioCAnnotation.setID(newRefId);
            annotationToRefId.put(a, newRefId);
            refId++;

            bioCSentence.addAnnotation(newBioCAnnotation);
        }

        // do relations
        int relationRefID = 1;
        List<Relation> relationList = nejiSentence.getRelations();
        for (Relation relation : relationList) {
            String concept1ID = annotationToRefId.get(relation.getConcept1());
            String concept2ID = annotationToRefId.get(relation.getConcept2());

            BioCRelation newBioCRelation = new BioCRelation();

            newBioCRelation.setID("R" + relationRefID);
            relationRefID++;

            BioCNode node1 = new BioCNode();
            node1.setRefid(concept1ID);
            newBioCRelation.addNode(node1);

            BioCNode node2 = new BioCNode();
            node2.setRefid(concept2ID);
            newBioCRelation.addNode(node2);

            bioCSentence.addRelation(newBioCRelation);

        }

    }

    @Override
    public OutputFormat getFormat() {
        return OutputFormat.BIOC;
    }
}

