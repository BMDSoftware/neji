/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.reader;

import edu.isi.bmkeg.lapdf.controller.LapdfEngine;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import monq.jfa.DfaRun;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.corpus.Token;
import pt.ua.tm.neji.core.module.BaseModule;
import pt.ua.tm.neji.core.module.BaseReader;
import pt.ua.tm.neji.core.module.DynamicNLP;
import pt.ua.tm.neji.core.module.Provides;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.core.parser.Parser;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserSupport;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.pdf.BoundBox;
import pt.ua.tm.neji.pdf.PdfSentence;

/**
 *
 * @author jeronimo
 */
@Provides({Resource.Passages, Resource.Sentences, Resource.DynamicNLP, Resource.Annotations, Resource.Relations})
public class PdfReader extends BaseReader implements DynamicNLP {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(PdfReader.class);    
    
    private final Parser parser;
    private final ParserLevel customLevel;
    
    private LapdfEngine pdfEngine;
    private File rulesFile;
    private InputStream pdfStream;
    
    private List<PdfSentence> sentencesInfo;

    /**
     * Constructor.
     * @param parser parser to use
     * @param pdfStream pdf stream
     * @throws NejiException 
     */
    public PdfReader(Parser parser, InputStream pdfStream) throws NejiException {
        this(parser, parser.getLevel(), pdfStream, null);
    }
    
    /**
     * Constructor.
     * @param parser parser to use
     * @param customLevel parsing level
     * @param pdfStream pdf stream
     * @throws NejiException 
     */
    public PdfReader(Parser parser, ParserLevel customLevel, InputStream pdfStream) throws NejiException {
        this(parser, customLevel, pdfStream, null);
    }
    
    /**
     * Constructor.
     * @param parser parser to use
     * @param pdfStream pdf stream
     * @param rulesFile rules file
     * @throws NejiException 
     */
    public PdfReader(Parser parser, InputStream pdfStream, File rulesFile) throws NejiException {
        this(parser, parser.getLevel(), pdfStream, rulesFile);
    }
    
    /**
     * Constructor
     * @param parser parser to use
     * @param customLevel parsing level
     * @param pdfStream pdf input file
     * @param rulesFile rules file
     * @throws NejiException 
     */
    public PdfReader(Parser parser, ParserLevel customLevel, 
            InputStream pdfStream, File rulesFile) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToRegex(text_action, ".+");
        
        this.parser = parser;
        this.customLevel = customLevel;
        this.pdfStream = pdfStream;        
        this.rulesFile = rulesFile;
        
        try {
            this.pdfEngine = new LapdfEngine();
        }catch (Exception ex) {
            throw new NejiException(ex);
        }
        
        this.sentencesInfo = new ArrayList<>();
    }

    private BaseModule.DefaultAction text_action = new BaseModule.DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            
            // Get corpus
            Corpus corpus = getPipeline().getCorpus();
            StringBuilder corpusText = new StringBuilder();
            StringBuilder sb = new StringBuilder();
            int sentenceCounter = 0;
            int startIndex = 0;
            
            // Read PDF file (from yytext)
            // To change: Change LA-PDFText to read an inputstream instead of a 
            // File, this will save a lot of processing time!!!           
            
            try {
                
                // ----
                //LapdfDocument doc = readPDFFile(new File(yytext.toString()));

                // Write data to temp file
                File tempFile = new File("newFile.pdf");
                tempFile.deleteOnExit();
                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    IOUtils.copy(pdfStream, out);
                }
                    
                LapdfDocument doc = readPDFFile(tempFile);                
                
                // Initialize parser
                if (!parser.isLaunched()) {
                    parser.launch();
                }
            
                // Iterate over the PDF file pages
                for (int i=1 ; i<=doc.getTotalNumberOfPages() ; i++) {                
                    
                    PageBlock page = doc.getPage(i);

                    // Iterate over the page chunks
                    for (ChunkBlock chunk : page.getAllChunkBlocks(
                            SpatialOrdering.PAGE_COLUMN_AWARE_MIXED_MODE)) {

                        // Get chunk text and iterator
                        String chunkText = chunk.readChunkText().replace("\n", " ");
                        String text = chunkText + "\n";
                        Iterator<SpatialEntity> chunkWordsIt = page
                                .containsByType(chunk, SpatialOrdering.MIXED_MODE, WordBlock.class)
                                .iterator();
                                
                        // Build corpus text
                        corpusText.append(text);

                        // Parse the text
                        List<Sentence> parsedSentences = 
                                parser.parseWithLevel(customLevel, corpus, text);
                        
                        // Process sentences (add <s> tags for next modules)
                        for(Sentence sentence : parsedSentences) {
                            
                            int s = sentence.getStart();
                            int e = sentence.getEnd();
                            
                            String prefix = "<s";
                            prefix += " id=\"" + sentenceCounter++ + "\"";
                            prefix += ">";
                            
                            String suffix = "</s>";
                            
                            String sentenceText = text.substring(s, e);
                            String taggedSentence = prefix + sentenceText + suffix;

                            sb.append(taggedSentence);

                            // Set sentence indexes
                            sentence.setStart(sentence.getStart() + startIndex);
                            sentence.setEnd(sentence.getEnd() + startIndex);
                            sentence.setOriginalStart(sentence.getOriginalStart() + startIndex);
                            sentence.setOriginalEnd(sentence.getOriginalEnd() + startIndex);
                            
                            // Map PDF data
                            mapPdfData(sentence, sentenceText, page, chunk, 
                                    chunkWordsIt);
                        }
                        
                        startIndex += text.length();
                    }
                }
                
            } catch (NejiException | IOException ex) {
                throw new RuntimeException("There was a problem parsing the sentence. Document: " +
                        corpus.getIdentifier(), ex);
            }
            
            // Set corpus text
            corpus.setText(corpusText.toString());
            
            yytext.replace(0, yytext.length(), sb.toString());
            
            // Add sentences info to pipeline
            getPipeline().storeModuleData("SENTENCES_INFO", sentencesInfo);
        }       
    };
    
    /**
     * Read PDF file (blockify and classify it).
     * @param pdfFile pdf file
     * @return a pdf document object structured with pages, chunks and word blocks
     */
    private LapdfDocument readPDFFile(File pdfFile) {
        
        LapdfDocument doc;       
        
        try {
            // Create document
            doc = pdfEngine.blockifyPdfFile(pdfFile);
            
            // Classify document   
            if (rulesFile != null) {
                pdfEngine.classifyDocument(doc, rulesFile);
            } else {
                pdfEngine.classifyDocumentWithBaselineRules(doc);
            }
    
        } catch (Exception ex) {
            throw new RuntimeException("There was a problem parsing the PDF "
                    + "file. Document: " 
                    + getPipeline().getCorpus().getIdentifier(), ex);
        }
        
        return doc;
    }
        
    private void mapPdfData(Sentence sentence, String text, PageBlock page,
            ChunkBlock chunck, Iterator<SpatialEntity> chunkWordsIt) {
        
        BoundBox startPos = null;
        BoundBox endPos;
        int leftX = Integer.MAX_VALUE;
        int rightX = Integer.MIN_VALUE;
        
        WordBlock word = null;
        boolean first = true;
        
        List<BoundBox> tokensPos = new ArrayList<>();
                
        // Get tokens iterator
        Iterator<Token> tokensIt = sentence.getTokens().iterator();       

        // Iterate over chunk word blocks and tokens
        while (tokensIt.hasNext() && chunkWordsIt.hasNext()) {

            word = (WordBlock) chunkWordsIt.next();
            
            if (first) {
                // Set start position
                startPos = new BoundBox(word.getX1(), word.getY1(), 
                        word.getX2(), word.getY2());
                first = false;
            }
                        
            int wordLength = word.getWord().length();
            int tokensLength = 0;
            
            do {                
                Token token = tokensIt.next();
                tokensLength += token.getEnd() - token.getStart() + 1;

                // Save token position
                BoundBox pos = new BoundBox(word.getX1(), word.getY1(), 
                        word.getX2(), word.getY2());
                tokensPos.add(pos); // NOTE: ignoring the fact that a word block
                                         // can be composed by more than 1 token
            } while (tokensLength < wordLength);

            // Verify left and right x positions
            if (word.getX1() < leftX) leftX = word.getX1();
            if (word.getX2() > rightX) rightX = word.getX2();           
        }

        endPos = new BoundBox(word.getX1(), word.getY1(), 
                word.getX2(), word.getY2());
        
        // Instantiate PDF sentence
        PdfSentence pdfSentence = new PdfSentence(sentence, page.getPageNumber(), 
                chunck.getType(), tokensPos, startPos, endPos, leftX, rightX);
        
        // Add sentences info to list
        sentencesInfo.add(pdfSentence);
    }

    @Override
    public InputFormat getFormat() {
        return InputFormat.PDF;
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
    
}
