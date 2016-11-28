package pt.ua.tm.neji.pdf.json;

import pt.ua.tm.neji.pdf.BoundBox;
import pt.ua.tm.neji.writer.json.JSONTerm;

/**
 *
 * @author jeronimo
 */
public class JSONPdfTerm1 extends JSONTerm {
    
    /**
     * Bounds information.
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
     * @param id term identifier
     * @param start start
     * @param end end
     * @param text term text
     * @param ids normalization identifiers
     * @param score score
     * @param x1 up left x corner position (in PDF)
     * @param y1 up left y corner position (in PDF)
     * @param x2 down right x corner position (in PDF)
     * @param y2 down right y corner position (in PDF)
     */
    public JSONPdfTerm1(int id, int start, int end, String text, String ids, 
            double score, int startX1, int startY1, int startX2, int startY2, 
            int endX1, int endY1, int endX2, int endY2, int leftX, int rightX) {
        super(id, start, end, text, ids, score);
        
        this.startX1 = startX1;
        this.startY1 = startY1;
        this.startX2 = startX2;
        this.startY2 = startY2;
        this.endX1 = endX1;
        this.endY1 = endY1;
        this.endX2 = endX2;
        this.endY2 = endY2;
        this.leftX = leftX;
        this.rightX = rightX;
    }
    
    /**
     * Constructor.
     * @param id term identifier
     * @param start start
     * @param end end
     * @param text term text
     * @param ids normalization identifiers
     * @param score score
     * @param position position (in PDF)
     */
    public JSONPdfTerm1(int id, int start, int end, String text, String ids, 
            double score, BoundBox startPos, BoundBox endPos, int leftX, 
            int rightX) {
        this(id, start, end, text, ids, score, startPos.getX1(), 
                startPos.getY1(), startPos.getX2(), startPos.getY2(), 
                endPos.getX1(), endPos.getY1(), endPos.getX2(), endPos.getY2(), 
                leftX, rightX);
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
