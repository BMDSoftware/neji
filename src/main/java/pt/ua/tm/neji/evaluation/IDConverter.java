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

package pt.ua.tm.neji.evaluation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Converter of identifiers read during evaluation.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class IDConverter {

    private static IDConverter instance;

    private String mappersFolderPath;

    private Multimap<String, String> CUI2GO;
    private Multimap<String, String> CUI2CL;
    private Multimap<String, String> Uniprot2EG;
    private Multimap<String, String> Uniprot2PR;

    private IDConverter(String mappersFolderPath) {
        this.mappersFolderPath = mappersFolderPath;
        this.CUI2GO = loadCUI2GO();
        this.CUI2CL = loadCUI2CL();
        this.Uniprot2EG = loadUniprot2EG();
        this.Uniprot2PR = loadUniprot2PR();
    }

    public Multimap<String, String> getCUI2CL() {
        return CUI2CL;
    }

    public Multimap<String, String> getCUI2GO() {
        return CUI2GO;
    }

    public Multimap<String, String> getUniprot2EG() {
        return Uniprot2EG;
    }

    public Multimap<String, String> getUniprot2PR() {
        return Uniprot2PR;
    }

    public static IDConverter getInstance(String mappersFolderPath) {
        if (instance == null) {
            instance = new IDConverter(mappersFolderPath);
        }
        return instance;
    }

    private Multimap<String, String> loadCUI2GO() {
        String filePath = mappersFolderPath + "GO_CUI_mapping_db.txt";

        Multimap<String, String> map = HashMultimap.create();

        try (
                FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
        ) {
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                String parts[] = line.split("\t");
                String go = parts[0];
                String cui = "UMLS:" + parts[1];

                map.put(cui, go);
            }

        } catch (IOException ex) {
            throw new RuntimeException("There was a problem loading the ID mapper file: " + filePath, ex);
        }

        return map;
    }

    private Multimap<String, String> loadUniprot2EG() {
//        String filePath = "/Users/david/Downloads/Uniprot2Entrez_hsa";
        String filePath = mappersFolderPath + "Uniprot2Entrez_selected";
//        String filePath = "resources/mappers/Uniprot2Entrez";

        Multimap<String, String> map = HashMultimap.create();

        try (
                FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
        ) {
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                String parts[] = line.split("\t");
                String uniprot = "UNIPROT:" + parts[0];
                String eg = "EG:" + parts[1];

                String[] egs = eg.split(";");
                for (String e : egs) {
                    map.put(uniprot, e.trim());
                }

//                map.put(uniprot, eg);
            }

        } catch (IOException ex) {
            throw new RuntimeException("There was a problem loading the ID mapper file: " + filePath, ex);
        }

        System.out.println("Done loading UNIPROT to Entrez mapper.");
        return map;
    }

    private Multimap<String, String> loadUniprot2PR() {
        String filePath = mappersFolderPath + "uniprotmapping.txt";

        Multimap<String, String> map = HashMultimap.create();

        try (
                FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
        ) {
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                String parts[] = line.split("\t");
                String uniprot = parts[1];
                String pr = parts[0];

                map.put(uniprot, pr);
            }

        } catch (IOException ex) {
            throw new RuntimeException("There was a problem loading the ID mapper file: " + filePath, ex);
        }

        return map;
    }

    private Multimap<String, String> loadCUI2CL() {
        String filePath = mappersFolderPath + "CL_CUI_mapping.txt";

        Multimap<String, String> map = HashMultimap.create();

        try (
                FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
        ) {
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                String parts[] = line.split("\t");
                String cl = parts[0];
                String cui = "UMLS:" + parts[1];

                map.put(cui, cl);
            }

        } catch (IOException ex) {
            throw new RuntimeException("There was a problem loading the ID mapper file: " + filePath, ex);
        }

        return map;
    }
}
