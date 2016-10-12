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

package pt.ua.tm.neji.writer;

import monq.jfa.DfaRun;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.core.module.Requires;
import pt.ua.tm.neji.core.module.Resource;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Writer that serializes the {@link Corpus} representation and encodes the serialization data into Base64.
 *
 * @author Tiago Nunes
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
@Requires({Resource.Tokens})
public class Base64Writer extends BaseWriter {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Base64Writer.class);
    private CorpusDumper dumper;

    public Base64Writer() throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.setEofAction(eof_action);
        setDumper(getPipeline().getCorpus());
    }

//    public Base64Writer(final Corpus corpus) throws NejiException {
//        super(DfaRun.UNMATCHED_COPY);
//        super.setEofAction(eof_action);
//        getPipeline().setCorpus(corpus);
//        setDumper(corpus);
//    }

    @Override
    public void setPipeline(final Pipeline pipeline) {
        super.setPipeline(pipeline);
        try{
            setDumper(getPipeline().getCorpus());
        } catch (NejiException e) {
            logger.error(e.toString());
        }
    }

    private void setDumper(final Corpus corpus) throws NejiException {
        this.dumper = new CorpusDumper(corpus);
    }

    private EofAction eof_action = new EofAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try(ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(dumper.corpus);
            } catch (IOException ex) {
                logger.error("Error serializing corpus object", ex);
                throw new RuntimeException("Error serializing corpus object", ex);
            }

            yytext.replace(0, yytext.length(), Base64.encodeBase64String(baos.toByteArray()));
        }
    };

    private static final class CorpusDumper {

        private final Corpus corpus;

        private CorpusDumper(Corpus corpus) {
            this.corpus = corpus;
        }
    }

    @Override
    public OutputFormat getFormat() {
        return OutputFormat.B64;
    }
}
