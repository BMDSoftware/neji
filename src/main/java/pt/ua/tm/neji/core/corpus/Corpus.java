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

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.MatrixOps;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.train.model.CRFBase;
import pt.ua.tm.neji.core.Constants.LabelFormat;
import pt.ua.tm.neji.core.Constants.LabelTag;
import pt.ua.tm.neji.core.Constants.Parsing;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Class that represents a Corpus.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class Corpus implements Iterable<Sentence>, Cloneable, Serializable {
    private static final long serialVersionUID = 3L;

    /**
     * {@link Logger} to be used by this class.
     */
    private static Logger logger = LoggerFactory.getLogger(Corpus.class);

    /**
     * The set of {@link Sentence} objects of the corpus.
     */
    private List<Sentence> sentences;

    /**
     * The encoding format used in this corpus.
     */
    private LabelFormat format;

    /**
     * The target entity name of this corpus. If empty doesn't filter (accepts all entities).
     */
    private List<String> entity;

    /**
     * The parsing direction of the corpus.
     */
    private Parsing parsing;

    /**
     * The unique identifier of this corpus.
     */
    private String identifier;
    
    /**
     * The text of the document.
     */
    private String text;

    /**
     * Constructor.
     *
     * @param format The encoding format.
     * @param entity The target entity type.
     */
    public Corpus(final LabelFormat format, final List<String> entity) {
        this.format = format;
        this.entity = entity;
        sentences = new ArrayList<>();
        this.parsing = Parsing.FW;
        this.identifier = "<none>";
        this.text = null;
    }

    /**
     * Constructor.
     */
    public Corpus() {
        this(LabelFormat.BIO, null);
    }
    
    /**
     * Add a new sentence to this corpus.
     * @param s sentence to add
     */
    public void addSentence(final Sentence s) {
        sentences.add(s);
    }

    /**
     * Change the sentence in the specified index to a new Sentence.
     * @param i the index where to place the specified Sentence in
     * @param s the new sentence to be placed.
     */
    public void setSentence(final int i, final Sentence s) {
        sentences.set(i, s);
    }

    /**
     * Get the number of sentences in this corpus.
     * @return the number of sentences
     */
    public int size() {
        return sentences.size();
    }

    /**
     * Get sentence from corpus.
     * @param i Get the sentence from the specific index.
     * @return The sentence in the position <code>i</code>.
     */
    public Sentence getSentence(final int i) {
        try{
            return sentences.get(i);
        }catch (IndexOutOfBoundsException ex){
            return null;
        }
    }

    /**
     * Get the {@link List} of {@link Sentence} in this corpus.
     * @return a list of sentences
     */
    public List<Sentence> getSentences() {
        return sentences;
    }

    /**
     * Get the {@link Iterator} of {@link Sentence} in this corpus. 
     * @return a iterator of sentences
     */
    @Override
    public Iterator<Sentence> iterator() {
        return sentences.iterator();
    }

    /**
     * Get the encoding format used in the corpus.
     * @return the encoding format
     */
    public LabelFormat getFormat() {
        return format;
    }

    /**
     * Get the target entity list of the corpus.
     * @return the target entity list
     */
    public List<String> getEntity() {
        return entity;
    }

    /**
     * Get the current parsing direction of the corpus.
     * @return The {@link Parsing} direction.
     */
    public Parsing getParsing() {
        return parsing;
    }

    /**
     * Get unique identifier of the corpus.
     * @return the unique identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set unique identifier of the corpus.
     * @param identifier the new identifier to be used
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    /**
     * Get text of the corpus.
     * @return the text of the corpus
     */
    public String getText() {
        return text;
    }
    
    /**
     * Get text of the corpus between two indexes. Ignores spaces between sentences.
     * @param start start index
     * @param end end index
     * @return 
     */
    public String getText(int start, int end) {
        return text.substring(start, end);
    }
    
    /**
     * Set the text of the corpus.
     * @param text the text of the corpus
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Remove all the annotations of the corpus.
     */
    public void cleanAnnotations() {
        for (Sentence s : sentences) {
            s.cleanAnnotationsTree();
        }
    }

    /**
     * Get the total number of annotations of the corpus.
     *
     * @return The total number of annotations.
     */
    public int getNumberAnnotations() {
        int count = 0;
        for (Sentence s : sentences) {
            count += s.getNumberAnnotations();
        }
        return count;
    }

    /**
     * Change the order of the corpus.
     * If the current is forward, changes to backward, and vice-versa.
     */
    public void reverse() {
        for (int i = 0; i < size(); i++) {
            getSentence(i).reverseTokens();
        }

        // Set parsing
        if (parsing.equals(Parsing.FW)) {
            parsing = Parsing.BW;
        } else {
            parsing = Parsing.FW;
        }
    }

    /**
     * Randomly split a corpus into various parts considering an array of ratios.
     *
     * @param ratios Array of ratios, considering that the sum of the ratios must be 1.0.
     * @return The various corpora with a number of sentences that associated with the respective ratio.
     */
    public Corpus[] split(double[] ratios) {
        assert (ratios != null);
        assert (ratios.length > 0);
        assert (MatrixOps.sum(ratios) == 1.0);

        // Get new corpora
        Corpus[] corpora = new Corpus[ratios.length];
        for (int i = 0; i < corpora.length; i++) {
            corpora[i] = new Corpus(this.getFormat(), this.getEntity());
        }

        // Get the number of sentences for each part
        int numSentences = this.size();
        int[] max = new int[ratios.length];
        int sum = 0;
        for (int i = 0; i < ratios.length; i++) {
            max[i] = (int) (ratios[i] * numSentences);
            sum += max[i];
        }

        if (sum != numSentences) {
            max[0] += numSentences - sum;
        }

        // Initialize counters
        int[] counters = new int[ratios.length];
        for (int i = 0; i < counters.length; i++) {
            counters[i] = 0;
        }

        // Get Sentences
        List<Sentence> shuffledSentences = this.getSentences();

        // Random sort
        Collections.shuffle(shuffledSentences, new Random(System.currentTimeMillis()));

        int currentCorpus = 0;
        for (Sentence s : shuffledSentences) {
            corpora[currentCorpus].addSentence(s.clone(corpora[currentCorpus]));
            counters[currentCorpus]++;

            if (counters[currentCorpus] >= max[currentCorpus]) {
                currentCorpus++;
            }
        }

        return corpora;
    }

    /**
     * Merge various corpora into a single corpus. Note that all corpora must have the same Encoding format, Parsing
     * direction and target entity type.
     *
     * @param corpora The corpora to be merged.
     * @return The corpus that contains all the provided corpora.
     * @throws NejiException Provided corpora are not compatible to be merged.
     */
    public static Corpus merge(Corpus[] corpora) throws NejiException {

        if (corpora.length < 2) {
            throw new NejiException("You have to provide at least two corpora to merge.");
        }

        for (int i = 0; i < corpora.length - 1; i++) {
            if ((!corpora[i].getParsing().equals(corpora[i + 1].getParsing()))
                    || (!corpora[i].getFormat().equals(corpora[i + 1].getFormat()))
                    || (!(corpora[i].getEntity() == null && corpora[i+1].getEntity() == null) && (!corpora[i].getEntity().equals(corpora[i + 1].getEntity())))) {
                throw new NejiException(
                        "All the corpora must have the same Encoding format, Parsing direction and target entity type.");
            }
        }

        Corpus c = new Corpus(corpora[0].getFormat(), corpora[0].getEntity());
        for (int i = 0; i < corpora.length; i++) {
            for (int j = 0; j < corpora[i].size(); j++) {
                c.addSentence(corpora[i].getSentence(j).clone(c));
            }
        }
        return c;
    }
    
    /**
     * Merge other corpus into this corpus. Note that both corpus must have the same Encoding format, Parsing
     * direction and target entity type.
     * @param otherCorpus Corpus to merge to this one
     * @throws NejiException 
     */
    public void merge(Corpus otherCorpus) throws NejiException {
        
        // Verify if other corpus is null
        if (otherCorpus == null) {
            return;
        }
        
        // Verify if both corpus have the same Encoding format, Parsing direction and target entity type
        if ((!this.getParsing().equals(otherCorpus.getParsing()))
             || (!this.getFormat().equals(otherCorpus.getFormat()))
             || (!(this.getEntity() == null && otherCorpus.getEntity() == null) && (!this.getEntity().equals(otherCorpus.getEntity())))) {
                throw new NejiException(
                        "All the corpora must have the same Encoding format, Parsing direction and target entity type.");
        }
        
        // -- Merge corpus --
        
        // Merge text
        int additionalLength = this.getText().length() + 1;
        StringBuilder newTextSb = new StringBuilder(this.getText());
        newTextSb.append('\n');
        newTextSb.append(otherCorpus.getText());
        this.setText(newTextSb.toString());
        
        // Merge sentences
        for (Sentence s : otherCorpus) {
            s.setCorpus(this);
            s.setStart(s.getStart() + additionalLength);
            s.setEnd(s.getEnd() + additionalLength);
            s.setOriginalStart(s.getOriginalStart() + additionalLength);
            s.setOriginalEnd(s.getOriginalEnd() + additionalLength);
            this.addSentence(s);
        }
    }

    /**
     * Convert sentences from CoNNL file to Mallet instances.
     *
     * @param p Pipe that specifies the features to be extracted
     * @return List of instances to train/test CRF
     */
     public synchronized InstanceList toModelFormat(Pipe p) {
         InstanceList instances = new InstanceList(p);
         String text;
         Sentence s;

         for (Integer i = 0; i < size(); i++) {
             s = getSentence(i); text = s.toExportFormat();
             //instances.addThruPipe(new Instance(text, null, i, null));
             instances.add(p.instanceFrom(new Instance(text, null, i, null)));
         }

         return instances;
     }

     /**
     * Convert sentences from CoNNL file to Mallet instances.
     *
     * @param model CRF that contains the pipe that specifies the features to be extracted
     * @return List of instances to test CRF
     */
    public InstanceList toModelFormatTest(CRFBase model) {

        model.getCRF().getInputPipe().getDataAlphabet().stopGrowth();
        Pipe p2 = model.getCRF().getInputPipe();

        //model.getCRF().getInputPipe().getDataAlphabet().stopGrowth();
        //Pipe p2 = model.getCRF().getInputPipe();

        InstanceList instances = new InstanceList(p2);
        String text;
        Sentence s;

        for (int i = 0; i < size(); i++) {
            s = getSentence(i);
            text = s.toExportFormat();
            instances.addThruPipe(new Instance(text, null, i, null));
            //instances.add(p1.instanceFrom(new Instance(text, null, i, null)));
        }
        return instances;
    }

     /**
     * Convert sentences from CoNNL file to Mallet instances.
     *
     * @param p2 Pipe that specifies the features to be extracted
     * @return List of instances to train CRF
     */
    public InstanceList toModelFormatTrain(Pipe p2) {
        InstanceList instances = new InstanceList(p2);
        String text;
        Sentence s;

        for (int i = 0; i < size(); i++) {
            s = getSentence(i);
            text = s.toExportFormat();
            instances.addThruPipe(new Instance(text, null, i, null));
            //instances.add(p1.instanceFrom(new Instance(text, null, i, null)));
        }
        return instances;
    }

    /**
     * Considering the {@link LabelFormat} used by the corpus, provides the pattern that should not be allowed during
     * the training. For instance, considering the BIO format, the appearance of one token with the label I will be
     * always preceeded with one with the label B. Thus, the pattern O>I is not allowed.
     *
     * @return The forbidden patterns.
     */
    public Pattern getForbiddenPattern() {
        String forbiddenPattern = null;
        if (format.equals(LabelFormat.BIO)) {
            if (parsing.equals(Parsing.FW)) {
                forbiddenPattern = LabelTag.O + "," + LabelTag.I;
            } else {
                forbiddenPattern = LabelTag.I + "," + LabelTag.O;
            }
            return Pattern.compile(forbiddenPattern);
        } else if (format.equals(LabelFormat.IO)) {
            return null;
        } else if (format.equals(LabelFormat.BMEWO)) {
            if (parsing.equals(Parsing.FW)) {
                forbiddenPattern = LabelTag.O + "," + LabelTag.M;
            } else {
                forbiddenPattern = LabelTag.M + "," + LabelTag.O;
            }
            return Pattern.compile(forbiddenPattern);
        }
        return null;
    }

    /**
     * Provides the tags that are used for entity names annotations. Considering the BIO format, only the tags B and I
     * are used to identify the entity names.
     *
     * @return The allowed tags.
     */
    public String[] getAllowedTags() {
        String[] allowedTags = null;
        if (format.equals(LabelFormat.BIO)) {
            allowedTags = new String[]{LabelTag.B.toString(), LabelTag.I.toString()};
        } else if (format.equals(LabelFormat.IO)) {
            allowedTags = new String[]{LabelTag.I.toString()};
        } else if (format.equals(LabelFormat.BMEWO)) {
            allowedTags = new String[]{LabelTag.B.toString(), LabelTag.M.toString(), LabelTag.E.toString(), LabelTag.W.toString()};
        }
        return allowedTags;
    }

    /**
     * Save corpus in a file.
     *
     * @param out File to save the corpus with the tokens, features and labels.
     * @throws NejiException Problem writing the data in the output file.
     */
    public void write(final OutputStream out) throws NejiException {
        logger.info("Writing corpus in CoNNL format...");

        Sentence s;
        try {
            for (int i = 0; i < size(); i++) {
                s = getSentence(i);
                out.write(s.getId().getBytes());
                out.write("\n".getBytes());
                out.write(s.toExportFormat().getBytes());
            }
            out.close();
        } catch (IOException ex) {
            throw new NejiException("There was an error writing data to the compressed file.", ex);
        }
    }

    /**
     * Creates and returns a copy of this corpus.
     * @return the cloned corpus
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        Corpus c = new Corpus(format, entity);
        c.parsing = parsing;
        c.text = text;

        for (Sentence s : sentences) {
            c.addSentence(s.clone(c));
        }
        return c;
    }
}
