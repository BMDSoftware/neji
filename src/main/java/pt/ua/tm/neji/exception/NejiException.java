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
package pt.ua.tm.neji.exception;

/**
 * Exception handling.
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class NejiException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with message.
     * @param m Associated message.
     */
    public NejiException(final String m) {
        super(m);
    }

    /**
     * Constructor with exception.
     * @param e Associated exception.
     */
    public NejiException(final Exception e) {
        super(e);
    }

    /**
     * Constructor with message and throwable exception.
     * @param m Associated message.
     * @param t Associated throwable exception.
     */
    public NejiException(final String m, final Throwable t) {
        super(m, t);
    }
}
