package pt.ua.tm.neji.pdf.json;

import pt.ua.tm.neji.pdf.BoundBox;
import pt.ua.tm.neji.writer.json.JSONTerm;

/**
 *
 * @author jeronimo
 */
public class JSONPdfTerm extends JSONTerm {
    
    /**
     * Bounds information.
     */
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;

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
    public JSONPdfTerm(int id, int start, int end, String text, String ids, 
            double score, int x1, int y1, int x2, int y2) {
        super(id, start, end, text, ids, score);
        
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
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
    public JSONPdfTerm(int id, int start, int end, String text, String ids, 
            double score, BoundBox position) {
        this(id, start, end, text, ids, score, position.getX1(), 
                position.getY1(), position.getX2(), position.getY2());
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }    
}
