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

package pt.ua.tm.neji.web.test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * Test of web services.
 *
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class TestREST {
    
    static String text = "In Duchenne muscular dystrophy (DMD), the infiltration of skeletal muscle by immune cells aggravates disease, yet the precise mechanisms behind these inflammatory responses remain poorly understood. Chemotactic cytokines, or chemokines, are considered essential recruiters of inflammatory cells to the tissues.\n" +
"We assayed chemokine and chemokine receptor expression in DMD muscle biopsies (n = 9, average age 7 years) using immunohistochemistry, immunofluorescence, and in situ hybridization.\n" +
"CXCL1, CXCL2, CXCL3, CXCL8, and CXCL11, absent from normal muscle fibers, were induced in DMD myofibers. CXCL11, CXCL12, and the ligand-receptor couple CCL2-CCR2 were upregulated on the blood vessel endothelium of DMD patients. CD68(+) macrophages expressed high levels of CXCL8, CCL2, and CCL5.\n" +
"Our data suggest a possible beneficial role for CXCR1/2/4 ligands in managing muscle fiber damage control and tissue regeneration. Upregulation of endothelial chemokine receptors and CXCL8, CCL2, and CCL5 expression by cytotoxic macrophages may regulate myofiber necrosis.";
    
    public static void main(String[] args) throws IOException, KeyStoreException, 
            NoSuchAlgorithmException, CertificateException, KeyManagementException, 
            UnrecoverableKeyException {
        
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        
        MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "utf-8");
        
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", sf, 8010));
        
        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
        
        String url = "https://localhost:8010/annotate/default/export?tool=becas-webapp&email=bioinformatics@ua.pt";
        
        // POST
        CloseableHttpClient client = new DefaultHttpClient(ccm, params);
        
        HttpPost post = new HttpPost(url);
        //post.setHeader("Content-Type", "application/json");
        
        List<NameValuePair> keyValuesPairs = new ArrayList();
        //keyValuesPairs.add(new BasicNameValuePair("format", "neji"));
        keyValuesPairs.add(new BasicNameValuePair("fromFile", "false"));
        //keyValuesPairs.add(new BasicNameValuePair("groups", "{\"DISO\":true,\"ANAT\":true}"));
        //keyValuesPairs.add(new BasicNameValuePair("groups", "{}"));
        //keyValuesPairs.add(new BasicNameValuePair("groups", "{\"DISO\":true}"));
        //keyValuesPairs.add(new BasicNameValuePair("groups", "{\"ANAT\":true}"));
        keyValuesPairs.add(new BasicNameValuePair("text", text));
        keyValuesPairs.add(new BasicNameValuePair("crlf", "false"));
        post.setEntity(new UrlEncodedFormEntity(keyValuesPairs));       
        
        HttpResponse response = client.execute(post);
        
        String result = IOUtils.toString(response.getEntity().getContent());
        
        System.out.println(result);
    }
    
}
