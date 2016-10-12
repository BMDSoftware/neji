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

package pt.ua.tm.neji.core;

import org.reflections.Reflections;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.module.Hybrid;
import pt.ua.tm.neji.core.module.Module;
import pt.ua.tm.neji.core.module.Reader;
import pt.ua.tm.neji.core.module.Writer;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 * @since 1.0
 */
public class ModuleLookup {
    private static final Reflections r = new Reflections(Constants.MODULES_PATH);
    private static final List<Class<? extends Module>> existingModuleClasses = new ArrayList<>();
    private static final List<Class<? extends Module>> existingProcessingClasses = new ArrayList<>();
    private static final List<Class<? extends Reader>> existingReaderClasses = new ArrayList<>();
    private static final List<Class<? extends Writer>> existingWriterClasses = new ArrayList<>();

    public static List<Class<? extends Module>> getAllModuleClasses(){
        if(existingModuleClasses.isEmpty()) {
            Set<Class<? extends Module>> existingModuleClassesAux = r.getSubTypesOf(Module.class);
            for (Class<? extends Module> m : existingModuleClassesAux) {
                if (!Modifier.isAbstract(m.getModifiers()) && !Modifier.isInterface(m.getModifiers())) {
                    existingModuleClasses.add(m);
                }
            }
        }
        return existingModuleClasses;
    }

    public static List<Class<? extends Module>> getExistingProcessingClasses(){
        if(existingProcessingClasses.isEmpty()) {
            existingProcessingClasses.addAll(existingModuleClasses);
            existingProcessingClasses.removeAll(getAllReaderClasses()); // do not include reader modules
            existingProcessingClasses.removeAll(getAllWriterClasses()); // do not include writer modules
            existingProcessingClasses.removeAll(r.getSubTypesOf(Hybrid.class)); // exclude hybrids too!
        }
        return existingProcessingClasses;
    }


    public static List<Class<? extends Reader>> getAllReaderClasses() {
        if(existingReaderClasses.isEmpty()) {
            Set<Class<? extends Reader>> existingReaderClassesAux = r.getSubTypesOf(Reader.class);
            for (Class<? extends Reader> m : existingReaderClassesAux) {
                if (!Modifier.isAbstract(m.getModifiers()) && !Modifier.isInterface(m.getModifiers())) {
                    existingReaderClasses.add(m);
                }
            }
        }
        return existingReaderClasses;
    }


    public static List<Class<? extends Writer>> getAllWriterClasses() {
        if(existingWriterClasses.isEmpty()) {
            Set<Class<? extends Writer>> existingWriterClassesAux = r.getSubTypesOf(Writer.class);
            for (Class<? extends Writer> m : existingWriterClassesAux) {
                if (!Modifier.isAbstract(m.getModifiers()) && !Modifier.isInterface(m.getModifiers())) {
                    existingWriterClasses.add(m);
                }
            }
        }
        return existingWriterClasses;
    }
}
