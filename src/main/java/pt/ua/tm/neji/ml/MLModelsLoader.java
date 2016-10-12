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

package pt.ua.tm.neji.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.exception.NejiException;

import java.io.*;
import java.util.*;

/**
 * Helper to load machine learning models.
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class MLModelsLoader {

    private List<String> priority;
    private Map<String, MLModel> models;

    public MLModelsLoader(final List<String> priority) {
        assert (priority != null);
        this.models = new LinkedHashMap<>();
        this.priority = priority;
    }

    public MLModelsLoader(final InputStream inputPriority) throws NejiException {
        assert (inputPriority != null);
        this.models = new LinkedHashMap<>();
        this.priority = loadPriority(inputPriority);
    }

    public void load(final File folder) {
        for (String properties : priority) {
            properties = folder.getAbsolutePath() + File.separator + properties;

            File input = new File(properties);
            MLModel newModel = new MLModel(input.getParentFile().getName(), input);
            models.put(newModel.getModelName(), newModel);
        }
    }

    public void loadFromPropertiesFile(final File propertiesFile) {
        MLModel newModel = new MLModel(propertiesFile.getParentFile().getName(), propertiesFile);
        models.put(newModel.getModelName(), newModel);
    }

    public static List<String> loadPriority(final InputStream input) throws NejiException {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals("") || line.equals(" ") || line.equals("\n") || line.indexOf(0)=='#') {
                    continue;
                }
                list.add(line);
            }
        } catch (IOException ex) {
            throw new NejiException("There was a problem reading the priority file.", ex);
        }
        return list;
    }

    /**
     * Gets models.
     *
     * @return Value of models.
     */
    public Map<String, MLModel> getModels() {
        return models;
    }
}
