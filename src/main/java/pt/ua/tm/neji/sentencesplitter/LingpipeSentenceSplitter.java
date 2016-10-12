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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.sentencesplitter;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * Lingpipe implementation of a sentence splitter.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class LingpipeSentenceSplitter implements SentenceSplitter{

    private TokenizerFactory TOKENIZER_FACTORY;
    private SentenceModel SENTENCE_MODEL;
    private SentenceChunker SENTENCE_CHUNKER;
    
    
    
    public LingpipeSentenceSplitter(){
        this.TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
        this.SENTENCE_MODEL = new MedlineSentenceModel();
        this.SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY, SENTENCE_MODEL);
    }
    
    
    
    public int[][] split(String text) {

        Chunking chunking = SENTENCE_CHUNKER.chunk(text.toCharArray(), 0, text.length());
        Set<Chunk> sentences = chunking.chunkSet();

        int size = sentences.size();
        int[][] indices = new int[size][2];

        int i = 0;
        for (Iterator<Chunk> it = sentences.iterator(); it.hasNext();) {

            Chunk sentence = it.next();
            int start = sentence.start();
            int end = sentence.end();

            indices[i][0] = start;
            indices[i][1] = end;

            i++;
        }

        return indices;
    }
}
