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

package pt.ua.tm.neji.core.batch;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.processor.Processor;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.processor.filewrappers.InputFile;
import pt.ua.tm.neji.processor.filewrappers.OutputFile;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Interface that defines a core batch executor.
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public abstract class BatchExecutor {

    private static Logger logger = LoggerFactory.getLogger(BatchExecutor.class);

    public abstract void run(Class<? extends Processor> processorCls, Context context, Object... args) throws NejiException;

    public abstract Collection<Corpus> getProcessedCorpora();



    protected static <A, B> Processor newProcessor(final Class<? extends Processor> processorCls,
                                                   final Context context,
                                                   final A input,
                                                   final List<B> outputList,
                                                   final Object... args) throws NejiException {
        Validate.notNull(processorCls);
        Validate.notNull(context);
        Validate.notNull(input);
        Validate.notNull(outputList);

        int numberArgs = 3 + (args != null ? args.length : 0);
        List<Object> values = new ArrayList<>(numberArgs);
        values.add(context);
        values.add(input);
        values.add(outputList);

        List<Class> types = new ArrayList<>(numberArgs);
        types.add(context.getClass());
        types.add(input.getClass());
        types.add(outputList.getClass());

        if (args != null) {
            for (Object arg : args) {
                values.add(arg);
                types.add(arg.getClass());
            }
        }

        try {
            return (Processor) ConstructorUtils.invokeConstructor(
                    processorCls, values.toArray(), types.toArray(new Class[types.size()]));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            logger.error("Error creating new pipeline processor.", ex);
            throw new NejiException("Error creating new pipeline processor.", ex);
        }
    }
    protected static <A, B> Processor newProcessor(final Class<? extends Processor> processorCls,
                                                   final Context context,
                                                   final A input,
                                                   final List<B> outputList,
                                                   boolean addAnnotationsWithoutIDs,
                                                   final Object... args) throws NejiException {
        Validate.notNull(processorCls);
        Validate.notNull(context);
        Validate.notNull(input);
        Validate.notNull(outputList);

        int numberArgs = 3 + (args != null ? args.length : 0);
        List<Object> values = new ArrayList<>(numberArgs);
        values.add(context);
        values.add(input);
        values.add(outputList);
        values.add(addAnnotationsWithoutIDs);

        List<Class> types = new ArrayList<>(numberArgs);
        types.add(context.getClass());
        types.add(input.getClass());
        types.add(outputList.getClass());
        types.add(boolean.class);

        if (args != null) {
            for (Object arg : args) {
                values.add(arg);
                types.add(arg.getClass());
            }
        }

        try {
            return (Processor) ConstructorUtils.invokeConstructor(
                    processorCls, values.toArray(), types.toArray(new Class[types.size()]));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            logger.error("Error creating new pipeline processor.", ex);
            throw new NejiException("Error creating new pipeline processor.", ex);
        }
    }
}
