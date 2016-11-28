package pt.ua.tm.neji.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeronimo
 */
public class BoundBox {
    
    /**
     * {@link Logger} to be used in the class.
     */
    private static final Logger logger = LoggerFactory.getLogger(BoundBox.class);
    
    /**
     * Up left corner x position.
     */
    private final int x1;
    
    /**
     * Up left corner y position.
     */
    private final int y1;
    
    /**
     * Down right corner x position.
     */
    private final int x2;
    
    /**
     * Down right corner y position.
     */
    private final int y2;
    
    /**
     * Constructor.
     * @param x1 up left corner x position
     * @param y1 up left corner y position
     * @param x2 down right corner x position
     * @param y2 down left corner y position
     */
    public BoundBox(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Get up left corner x position.
     * @return up left corner x position
     */
    public int getX1() {
        return x1;
    }

    /**
     * Get up left corner y position.
     * @return up left corner y position
     */
    public int getY1() {
        return y1;
    }

    /**
     * Get down right corner x position.
     * @return down right corner x position
     */
    public int getX2() {
        return x2;
    }

    /**
     * Get down right corner y position.
     * @return down right corner y position
     */
    public int getY2() {
        return y2;
    }
}
