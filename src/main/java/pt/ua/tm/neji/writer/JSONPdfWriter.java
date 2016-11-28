/*
 * Copyright (c) 2012 David Campos, University of Aveiro.
 *
 * Neji is a framework for modular biomedical concept recognition made easy, fast and accessible.
 *
 * This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.
 *
 * This project is a free software, you are free to copy, distribute, change and transmit it. However, you may not use
 * it for commercial purposes.
 *
 * It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.writer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import monq.jfa.DfaRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.TreeNode;
import pt.ua.tm.neji.writer.json.JSONEntry;
import pt.ua.tm.neji.writer.json.JSONSentence;
import pt.ua.tm.neji.writer.json.JSONTerm;

import java.util.ArrayList;
import java.util.List;
import pt.ua.tm.neji.pdf.BoundBox;
import pt.ua.tm.neji.pdf.PdfSentence;
import pt.ua.tm.neji.pdf.json.JSONPdfSentence;
import pt.ua.tm.neji.pdf.json.JSONPdfTerm1;

/**
 * Writer that provides information in JSON with additional data for PDF files.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Tokens})
public class JSONPdfWriter extends BaseWriter {

    /** {@link Logger} to be used in the class. */
    private static Logger logger = LoggerFactory.getLogger(JSONPdfWriter.class);
    private int offset;
    private int jsonSentenceID;
    private List<JSONSentence> jsonSentences;

    public JSONPdfWriter() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        
        super.addActionToGoofedElement(text_action, "s");
        super.setEofAction(eof_action);
        
        this.offset = 0;
        this.jsonSentenceID = 0;
        this.jsonSentences = new ArrayList<JSONSentence>();
    }

    private EofAction eof_action = new EofAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {

            // Creat json object          
            JsonObject json = new JsonObject();
            json.addProperty("text", getPipeline().getCorpus().getText());
            json.add("sentences", new Gson().toJsonTree(jsonSentences));            
            
            String jsonText = json.toString();

            yytext.replace(0, yytext.length(), jsonText);
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

            // Get sentence with XML tags
            String sentence = yytext.substring(realStart, realEnd);

            //Remove sentence tags and escape XML
            yytext.replace(startSentence, endSentence, sentence);

            // Get final start and end of sentence
            int startChar = offset + yytext.indexOf(sentence);
            int endChar = startChar + sentence.length() + 1;

            // Get sentences information (position, type and page number)
            List sentencesInfo = getPipeline().getModuleData("SENTENCES_INFO");
            
            if (sentencesInfo != null && !sentencesInfo.isEmpty()) {    
                
                PdfSentence sentenceInfo = ((List<PdfSentence>) sentencesInfo
                        .get(0)).remove(0);
                
                // Generate sentence on stand-off format
                JSONPdfSentence js = new JSONPdfSentence(jsonSentenceID,
                    startChar, endChar - 1, sentence, sentenceInfo);
                
                getAnnotationsPdf(nextSentence, sentence, js, startChar, 
                    sentenceInfo.getTokensPos());
                
                jsonSentences.add(js);
                
            } else {
                
                // Generate sentence on stand-off format
                JSONSentence js = new JSONSentence(jsonSentenceID,
                    startChar, endChar - 1, sentence);
                
                getAnnotations(nextSentence, sentence, js, startChar);
                
                jsonSentences.add(js);
            }

            // Remove processed input from input
            yytext.replace(0, endSentence, "");

            jsonSentenceID++;

            offset = endChar;
        }
    };

    private void getAnnotations(Sentence s, String source, JSONSentence js, int offset) {
        getAnnotations(s.getTree().getRoot(), source, js, 0, 0, 1, offset);
    }

    private void getAnnotations(TreeNode<Annotation> node, String source, JSONEntry j, int level, int counter,
                                int subcounter, int offset) {
        Annotation data = node.getData();

        JSONEntry je;

        if (level != 0 || !data.getIDs().isEmpty()) {
            // Add result to StringBuilder

            int id;

            if (level <= 1) {
                id = counter;
            } else {
                id = subcounter;
            }

            Sentence s = data.getSentence();

            int startAnnotationInSentence = s.getToken(data.getStartIndex()).getStart();
            int endAnnotationInSentence = s.getToken(data.getEndIndex()).getEnd() + 1;

            int startChar = offset + startAnnotationInSentence;
            int endChar = offset + endAnnotationInSentence;


            JSONTerm jt = new JSONTerm(id, startChar,
                    endChar, source.substring(startAnnotationInSentence, endAnnotationInSentence).trim(),
                    data.getStringIDs(), data.getScore());
            j.addTerm(jt);

            je = jt;
        } else {
            je = j;
        }


        int i = 0;
        for (TreeNode<Annotation> child : node.getChildren()) {
            getAnnotations(child, source, je, level + 1, ++counter, ++i, offset);
        }
    }
    
    private void getAnnotationsPdf(Sentence s, String source, JSONPdfSentence js, 
            int offset, List<BoundBox> tokensPos) {
        getAnnotationsPdf(s.getTree().getRoot(), source, js, 0, 0, 1, offset, 
                tokensPos);
    }

    private void getAnnotationsPdf(TreeNode<Annotation> node, String source, 
            JSONEntry j, int level, int counter, int subcounter, int offset, 
            List<BoundBox> tokensPos) {
        
        Annotation data = node.getData();

        JSONEntry je;

        if (level != 0 || !data.getIDs().isEmpty()) {
            
            // Add result to StringBuilder
            int id;

            if (level <= 1) {
                id = counter;
            } else {
                id = subcounter;
            }

            Sentence s = data.getSentence();

            int startAnnotationInSentence = s.getToken(data.getStartIndex()).getStart();
            int endAnnotationInSentence = s.getToken(data.getEndIndex()).getEnd() + 1;

            int startChar = offset + startAnnotationInSentence;
            int endChar = offset + endAnnotationInSentence;
            
            // Get pdf terms positions
            BoundBox startPos = tokensPos.get(data.getStartIndex());
            BoundBox endPos = tokensPos.get(data.getEndIndex());

            int leftX = Integer.MAX_VALUE;
            int rightX = Integer.MIN_VALUE;
            
            // Verify left and right x positions
            for (int i=data.getStartIndex() ; i <= data.getEndIndex() ; i++) {
                BoundBox word = tokensPos.get(i);
                if (word.getX1() < leftX) leftX = word.getX1();
                if (word.getX2() > rightX) rightX = word.getX2(); 
            }

            JSONTerm jt = new JSONPdfTerm1(id, startChar,
                    endChar, source.substring(startAnnotationInSentence, endAnnotationInSentence).trim(),
                    data.getStringIDs(), data.getScore(), startPos, endPos, leftX, rightX);
            j.addTerm(jt);

            je = jt;
        } else {
            je = j;
        }


        int i = 0;
        for (TreeNode<Annotation> child : node.getChildren()) {
            getAnnotationsPdf(child, source, je, level + 1, ++counter, ++i, offset, 
                    tokensPos);
        }
    }

    @Override
    public OutputFormat getFormat() {
        return OutputFormat.JSONPDF;
    }
}
