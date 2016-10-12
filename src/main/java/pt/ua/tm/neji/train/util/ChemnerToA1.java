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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.neji.train.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jeronimo
 */
public class ChemnerToA1 {
    
    public static void main (String[] args) throws IOException, ParserConfigurationException, SAXException {
        
        // Files to read
        //String xmlFilePath = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/train/training.bioc.xml";
        //String outputDirPath = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/train/a1/";
        String xmlFilePath = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/test/evaluation.bioc.xml";
        String outputDirPath = "/home/jeronimo/Desktop/Github/gimliNejiIntegration5/chemner_tests/test/gold_a1/";
        File xmlFile = new File(xmlFilePath);
        File outputDir = new File(outputDirPath);
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        
        NodeList nList = doc.getElementsByTagName("document");
        
        for (int i=0 ; i<nList.getLength() ; i++) {
            
            Node document = nList.item(i);
            Element eElement = (Element) document;
            String id = eElement.getElementsByTagName("id").item(0).getTextContent();
            NodeList passages = eElement.getElementsByTagName("passage");
            Element passage1 = (Element) passages.item(0);
            Element passage2 = (Element) passages.item(1);
                        
            // Text            
            String title = passage1.getElementsByTagName("text").item(0).getTextContent();
            String text = passage2.getElementsByTagName("text").item(0).getTextContent();
            String fullText = title + " " + text;
            int titleLength = title.length() + 1;
            
            // Annotations
            List<A1Annotation> annotations = new ArrayList<>();
            
            NodeList annList1 = passage1.getElementsByTagName("annotation");
            NodeList annList2 = passage2.getElementsByTagName("annotation");
            
            for (int j=0 ; j<annList1.getLength() ; j++) {
                Element a = (Element) annList1.item(j);
                
                String entity = a.getElementsByTagName("infon").item(0).getTextContent().replaceAll("\\s", "");
                String startText = a.getElementsByTagName("location").item(0).getAttributes().getNamedItem("offset").getTextContent();
                int start = Integer.parseInt(startText);
                String lengthText = a.getElementsByTagName("location").item(0).getAttributes().getNamedItem("length").getTextContent();
                int end = start + Integer.parseInt(lengthText);
                String aText = a.getElementsByTagName("text").item(0).getTextContent();
                
                A1Annotation a1 = new A1Annotation(start, end, aText, "PRGE");
                annotations.add(a1);
            }
            
            for (int j=0 ; j<annList2.getLength() ; j++) {
                Element a = (Element) annList2.item(j);
                
                String entity = a.getElementsByTagName("infon").item(0).getTextContent().replaceAll("\\s", "");
                String startText = a.getElementsByTagName("location").item(0).getAttributes().getNamedItem("offset").getTextContent();
                int start = Integer.parseInt(startText) + titleLength;
                String lengthText = a.getElementsByTagName("location").item(0).getAttributes().getNamedItem("length").getTextContent();
                int end = start + Integer.parseInt(lengthText);
                String aText = a.getElementsByTagName("text").item(0).getTextContent();
                
                A1Annotation a1 = new A1Annotation(start, end, aText, "PRGE");
                annotations.add(a1);
            }
            
            // Write
            int part = 0;
            if (i>=0 && i <= 999) part = 1;
            else if (i>=1000 && i <= 1999) part = 2;
            else if (i>=2000 && i <= 2999) part = 3;
            else if (i>=3000 && i <= 3999) part = 4;
            else if (i>=4000 && i <= 4999) part = 5;
            else if (i>=5000 && i <= 5999) part = 6;
            else if (i>=6000 && i <= 6999) part = 7;
            
            //PrintWriter pwt = new PrintWriter(outputDir.getAbsolutePath() + File.separator + "part" + part + File.separator + id + ".txt");
            PrintWriter pwt = new PrintWriter(outputDir.getAbsolutePath() + File.separator + id + ".txt");
            pwt.print(fullText);
            pwt.close();
            
            //pwt = new PrintWriter(outputDir.getAbsolutePath() + File.separator + "part" + part + File.separator + id + ".a1");
            pwt = new PrintWriter(outputDir.getAbsolutePath() + File.separator + id + ".a1");
            
            int tokenIndex = 1;
            for (A1Annotation a : annotations) {
                pwt.println("T" + tokenIndex + "\t" + a.toString());
                tokenIndex++;
            }
            
            pwt.close();
        }
    }
    
    private static class A1Annotation {
        
        // Attributes
        int start;
        int end;
        String text;
        String entity;
        
        // Constructor
        public A1Annotation(int start, int end, String text, String entity) {
            this.start = start;
            this.end = end;
            this.text = text;
            this.entity = entity;
        }
        
        // To String
        @Override
        public String toString() {
            return entity + " " + start + " " + end + "\t" + text;
        }
    }
    
}
