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

package pt.ua.tm.neji.web.annotate.pubmed;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

/**
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Pubmed {

    public static void main(String... args) {
        File outputFolder = new File(FilenameUtils.separatorsToSystem("/Users/david/Downloads/neji_ppi/in/"));


        //Map<String, Document> map = getCleanTextFromPM(new String[]{"11748933", "11700088", "15282350", "15282345"});
        Map<String, Document> map = getCleanTextFromPM(new String[]{"23142263","23299379","23430246","23477989","23536070","23365075","23400560","23202730","23576130","23374228","23118350","23575824","23172224","23155407","23152192","23446677","23481704","23233058","23138650","23109154","23161037","23082203","23372648","23209315","23211594","23183882","23286786","23173713","23467355","23122958","23092137","23486975","23236499","23486882","23289174","23296102","23173743","23224319","23314196","23474306","23650720","23280791","23100439","23584199","23595759","23085935","23114667","23486963","23109151","23144955","23364356","23076628","23552688","23227576","23109153","23197725","23155049","23178565","23365250","23178660","23276553","23262393","23184605","23789343","23420027","23468550","23642578","23143229","23149447","23226438","23386614","23064434","23368879","23083210","23071606","23295908","23276639","23223301","23152628","23159938","23403102","23249765","23383156","23327526","23656991","23100049","23284610","23123349","23097297","23390179","23142262","23109807","23238159","23284867","23479184","23237352","23370287","23546882","23115165","23278858"});


        for (String pmid : map.keySet()) {
            File file = new File(outputFolder, pmid + ".txt");

            try {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(map.get(pmid).toString());
                fileWriter.close();
            } catch (IOException e) {
                System.err.println("ERROR: ");
                e.printStackTrace();
            }
        }

    }

    /**
     * @param pmids
     * @return
     */
    public static Map<String, Document> getCleanTextFromPM(final String[] pmids) {
        // Join PMCIDs
        String pmidsQuery = StringUtils.join(Arrays.asList(pmids), ',');

        // Build URL
        StringBuilder url = new StringBuilder("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=");
        url.append(pmidsQuery);
        url.append("&retmode=xml");

        // Build URI
        URI uri = UriBuilder.fromUri(url.toString()).build();

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(uri);

        // Get input stream
        InputStream inputStream = service.accept(MediaType.APPLICATION_XML).get(InputStream.class);

        // Parse and return result
        return new PubmedXMLParser().parse(inputStream);
    }
    
    /**
     * @param pmid
     * @return
     */
    public static Document getCleanTextFromPM(final String pmid) {
        
        // Build URL
        StringBuilder url = new StringBuilder("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=");
        url.append(pmid);
        url.append("&retmode=xml");

        // Build URI
        URI uri = UriBuilder.fromUri(url.toString()).build();

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(uri);

        // Get input stream
        InputStream inputStream = service.accept(MediaType.APPLICATION_XML).get(InputStream.class);

        // Parse and return result
        return new PubmedXMLParser().parse(inputStream).get(pmid);
    }
}
