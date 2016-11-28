package pt.ua.tm.neji.pdf.json;

import pt.ua.tm.neji.pdf.PdfSentence;
import pt.ua.tm.neji.writer.json.JSONSentence;

/**
 *
 * @author jeronimo
 */
public class JSONPdfSentence extends JSONSentence {
    
    /**
     * Page number where the sentence occurs.
     */
    private final int page;
    
    /**
     * Sentence type (rhetorical category).
     */
    private final String type;
    
    /**
     * PDF sentence bounds information.
     */
    private final int startX1;
    private final int startY1;
    private final int startX2;
    private final int startY2;
    private final int endX1;
    private final int endY1;   
    private final int endX2;
    private final int endY2;
    private final int leftX;
    private final int rightX;   
    
    /**
     * Constructor.
     * @param id identifier
     * @param start start position on text
     * @param end end position on text
     * @param text text
     * @param sentenceInfo sentence info (bounds, type and 
     */
    public JSONPdfSentence(int id, int start, int end, String text, 
            PdfSentence sentenceInfo) {
        
        super(id, start, end, text);
        
        this.page = sentenceInfo.getPageNumber();
        this.type = sentenceInfo.getType();
        
        this.startX1 = sentenceInfo.getStartPos().getX1();
        this.startY1 = sentenceInfo.getStartPos().getY1();
        this.startX2 = sentenceInfo.getStartPos().getX2();
        this.startY2 = sentenceInfo.getStartPos().getY2();
        
        this.endX1 = sentenceInfo.getEndPos().getX1();
        this.endY1 = sentenceInfo.getEndPos().getY1();
        this.endX2 = sentenceInfo.getEndPos().getX2();
        this.endY2 = sentenceInfo.getEndPos().getY2();
        
        this.leftX = sentenceInfo.getPosition().getLeftX();
        this.rightX = sentenceInfo.getPosition().getRightX();
    }

    /**
     * Get page number.
     * @return page number
     */
    public int getPage() {
        return page;
    }

    /**
     * Get type.
     * @return type
     */
    public String getType() {
        return type;
    }

    public int getStartX1() {
        return startX1;
    }

    public int getStartY1() {
        return startY1;
    }

    public int getStartX2() {
        return startX2;
    }

    public int getStartY2() {
        return startY2;
    }

    public int getEndX1() {
        return endX1;
    }

    public int getEndY1() {
        return endY1;
    }

    public int getEndX2() {
        return endX2;
    }

    public int getEndY2() {
        return endY2;
    }

    public int getLeftX() {
        return leftX;
    }

    public int getRightX() {
        return rightX;
    }   
}
