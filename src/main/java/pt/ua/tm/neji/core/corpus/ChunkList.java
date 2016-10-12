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

package pt.ua.tm.neji.core.corpus;

import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Sub-class of ArrayList that represents a list of {@link Chunk} and with the
 * possibility to obtain a chunk from its {@link Token}.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class ChunkList extends ArrayList<Chunk> implements Serializable, Cloneable {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ChunkList.class);
    private Sentence sentence;

    public ChunkList(final Sentence sentence) {
        super();
        this.sentence = sentence;
    }

    public Sentence getSentence() {
        return sentence;
    }

    public void setSentence(Sentence sentence) {
        this.sentence = sentence;
        for(Chunk c : this) {
            c.setSentence(sentence);
        }
    }

    public Chunk getTokenChunk(final Token token) {
        for (Chunk chunk : this) {
            if (token.getIndex() >= chunk.getStart() && token.getIndex() <= chunk.getEnd()) {
                return chunk;
            }
        }
        throw new NoSuchElementException("There is no chunk with the provided token: " + token.getText());
    }

    public void print() {
        logger.info("Chunk List:");
        for (Chunk chunk : this) {
            Sentence sentence = chunk.getSentence();

            // Get chunk tokens' text
            StringBuilder sb = new StringBuilder();
            for (int i = chunk.getStart(); i <= chunk.getEnd(); i++) {
                sb.append(sentence.getToken(i).getText());
                sb.append(" ");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }

            logger.info("{}: {}", chunk.getTag(), sb.toString());
        }
    }
}
