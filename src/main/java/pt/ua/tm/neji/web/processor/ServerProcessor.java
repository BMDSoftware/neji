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

package pt.ua.tm.neji.web.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextProcessors;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.core.processor.BaseProcessor;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.web.services.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Processor for a pre-defined pipeline, used by the deployable server.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>)
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 2.0
 * @since 1.0
 */
public class ServerProcessor extends BaseProcessor {

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(ServerProcessor.class);

    private final Service service;
    private final InputStream inputStream;
    private final List<OutputStream> outputStreamList;
    private final Pipeline pipeline;
    private Map<String, Boolean> groups;
    private boolean filterGroups;
    private String[] xmlTags;

    public ServerProcessor(Context context, InputStream inputStream, List<OutputStream> outputStreamList,
                           Service service, Pipeline pipeline, Map<String, Boolean> groups, 
                           boolean filterGroups) {
        super(context);
        this.service = service;
        this.inputStream = inputStream;
        this.outputStreamList = outputStreamList;
        this.pipeline = pipeline;
        this.groups = groups;
        this.filterGroups = filterGroups;
        this.xmlTags = null;
    }

    public ServerProcessor(Context context, InputStream inputStream, List<OutputStream> outputStreamList,
                           Service service, Pipeline pipeline, Map<String, Boolean> groups,
                           boolean filterGroups, String[] xmlTags) {
        this(context, inputStream, outputStreamList, service, pipeline, groups, filterGroups);
        this.xmlTags = xmlTags;
    }

    @Override
    public void run() {

        // Get context and processors
        Context context = getContext();
        ContextProcessors cp;        
        try {
            cp = context.take();
        } catch (InterruptedException ex) {
            throw new RuntimeException("There was a problem getting the context processors.", ex);
        }

        try {

            // Get dictionaries and models
            List<Dictionary> dictionaries = service.takeDictionaries(context);
            List<MLModel> models = service.takeModels(context);

            // Create Pipeline
            if(filterGroups) {
                instantiateModulesFromGroups(dictionaries, models, 
                        service.getParserLevel(), cp, context, pipeline, 
                        groups, xmlTags, service.isNoIds());
            } else {
                instantiateModules(dictionaries, models, service.getParserLevel(), 
                        cp, context, pipeline, xmlTags, service.isNoIds());
            }

            // Execute the pipeline
            pipeline.run(inputStream, outputStreamList);

            // Return processors
            context.put(cp);
            pipeline.clear();

            synchronized (this) {
                notify();
            }

        } catch (InterruptedException | NejiException ex) {

            // Return context processors to context
            try {
                context.put(cp);
            } catch (InterruptedException e) {
                throw new RuntimeException("There was a problem returning the context processors.", ex);
            }

            throw new RuntimeException("There was a problem annotating the stream.", ex);
        }
    }
}
