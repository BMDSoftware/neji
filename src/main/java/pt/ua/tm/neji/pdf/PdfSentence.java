package pt.ua.tm.neji.pdf;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.core.corpus.Sentence;

/**
 *
 * @author jeronimo
 */
public class PdfSentence {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static final Logger logger = LoggerFactory.getLogger(PdfSentence.class);
    
    /**
     * Sentence.
     */
    private final Sentence sentence;
    
    /**
     * Page number.
     */
    private final int pageNumber;
    
    /**
     * Type (rhetorical category).
     */
    private final String type;
    
    /**
     * Tokens position.
     */
    private final List<BoundBox> tokensPos; 
    
    /**
     * Position in the PDF page.
     */
    private PdfSentencePosition position;   

    /**
     * Constructor.
     * @param sentence sentence
     * @param pageNumber page number where the sentence belongs
     * @param type the type of the sentence chunk
     * @param tokensPos tokens position
     * @param position position of the sentence in the PDF page
     */
    public PdfSentence(Sentence sentence, int pageNumber, String type, 
            List<BoundBox> tokensPos, PdfSentencePosition position) {
        this.sentence = sentence;
        this.pageNumber = pageNumber;
        this.type = type;
        this.tokensPos = tokensPos;        
        this.position = position;
    }
    
    /**
     * Constructor.
     * @param sentence sentence
     * @param pageNumber page number where the sentence belongs
     * @param type the type of the sentence chunk
     * @param tokensPos tokens position
     * @param startPos sentence start position
     * @param endPos sentence end position
     * @param leftX more left x position
     * @param righX more right x
     */
    public PdfSentence(Sentence sentence, int pageNumber, String type, 
            List<BoundBox> tokensPos, BoundBox startPos, BoundBox endPos, 
            int leftX, int righX) {
        this.sentence = sentence;
        this.pageNumber = pageNumber;
        this.type = type;
        this.tokensPos = tokensPos;        
        this.position = new PdfSentencePosition(startPos, endPos, leftX, righX);
    }

    /**
     * Get sentence.
     * @return sentence
     */
    public Sentence getSentence() {
        return sentence;
    }

    /**
     * Get page number.
     * @return page number
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Get type (rhetorical category).
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Get sentence position in the PDF page.
     * @return position in PDF page
     */
    public PdfSentencePosition getPosition() {
        return position;
    }
    
    /**
     * Get start position.
     * @return start position
     */
    public BoundBox getStartPos() {
        return position.getStartPos();
    }
    
    /**
     * Get end position.
     * @return end position
     */
    public BoundBox getEndPos() {
        return position.getLastPos();
    }
    
    /**
     * Get more left x position.
     * @return more left x position
     */
    public int getLeftXPos() {
        return position.getLeftX();
    }
    
    /**
     * Get more right x position.
     * @return more right x position
     */
    public int getRightXPos() {
        return position.getRightX();
    }
    
    /**
     * Get tokens position.
     * @return list with sentence tokens position
     */
    public List<BoundBox> getTokensPos() {
        return tokensPos;
    }
    
    /**
     * PDF sentence position class.
     */
    public class PdfSentencePosition {
        
        /**
         * Token start position.
         */
        private BoundBox startPos;
        
        /**
         * Token last position.
         */
        private BoundBox lastPos;
        
        /**
         * More left x position.
         */
        private int leftX;
        
        /**
         * More right x position.
         */
        private int rightX;

        /**
         * Constructor.
         * @param startPos token start position
         * @param lastPos token start position
         * @param leftX more left x position
         * @param rightX more left x position
         */
        public PdfSentencePosition(BoundBox startPos, BoundBox lastPos, 
                int leftX, int rightX) {
            this.startPos = startPos;
            this.lastPos = lastPos;
            this.leftX = leftX;
            this.rightX = rightX;
        }

        /**
         * Get token start position.
         * @return token start position
         */
        public BoundBox getStartPos() {
            return startPos;
        }

        /**
         * Get token last position.
         * @return token last position
         */
        public BoundBox getLastPos() {
            return lastPos;
        }

        /**
         * Get more left x position.
         * @return more left x position
         */
        public int getLeftX() {
            return leftX;
        }

        /**
         * Get more right x position.
         * @return more right x position
         */
        public int getRightX() {
            return rightX;
        }        
    }
}
