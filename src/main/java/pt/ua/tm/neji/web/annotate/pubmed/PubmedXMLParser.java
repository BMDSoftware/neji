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

import monq.jfa.*;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Pubmed XML parser.
 * 
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class PubmedXMLParser {
    private Dfa dfa;
    private StringBuilder body, title;
    private Map<String, Document> articles;
    private Map<String, String> map;
    private boolean tagOfInterest;
    private String tag, pmid;
    private boolean pmidAssigned;

    private static final String activatorsOfInterest = "(Article)";
    private static final String tagsOfInterest = "(ArticleTitle|AbstractText)";

    public PubmedXMLParser() {
        try {
            Nfa nfa = new Nfa(Nfa.NOTHING);
            nfa.or(Xml.STag("PubmedArticle"), startArticle);
            nfa.or(Xml.ETag("PubmedArticle"), endArticle);

            nfa.or(Xml.GoofedElement("PMID"), getPMID);

            nfa.or(Xml.STag(activatorsOfInterest), startOfInterest);
            nfa.or(Xml.ETag(activatorsOfInterest), endOfInterest);
            nfa.or(Xml.GoofedElement(tagsOfInterest), getText);
            this.dfa = nfa.compile(DfaRun.UNMATCHED_DROP);
        } catch (ReSyntaxException ex) {
            throw new RuntimeException("There is problem with the XML parser syntax.", ex);
        } catch (CompileDfaException ex) {
            throw new RuntimeException("There is problem compiling the XML parser.", ex);
        }
        this.tagOfInterest = false;
        this.map = new HashMap<>();
    }

    private AbstractFaAction startArticle = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
            body = new StringBuilder();
            title = new StringBuilder();
            pmid = null;
            pmidAssigned = false;
        }
    };
    private AbstractFaAction endArticle = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
            articles.put(pmid, new Document(title.toString(), body.toString()));
        }
    };

    private AbstractFaAction getPMID = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
            if (!pmidAssigned){
                pmid = yytext.toString().replaceAll("\\<.*?>", "");
                pmidAssigned = true;
            }
        }
    };

    private AbstractFaAction startOfInterest = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
            tagOfInterest = true;
        }
    };
    private AbstractFaAction endOfInterest = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
            tagOfInterest = false;
        }
    };

    private AbstractFaAction getText = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
            if (!tagOfInterest) {
                return;
            }

            if (yytext.toString().contains(" ")) {
                tag = yytext.substring(1, yytext.indexOf(">"));
                if (tag.contains(" ")){
                    tag = tag.substring(0, tag.indexOf(" "));
                }
            } else {
                tag = yytext.substring(1, yytext.length() - 1);
            }

            if (tag.equals("AbstractText")) {
                map = Xml.splitElement(yytext, start);
                String type = map.get("Label");
                if (type != null){
                    body.append(type);
                    body.append("\n\n");
                }
            }


            // Get clean text
            String text = yytext.toString();
            text = text.replaceAll("\\<.*?>", "");
            text = StringEscapeUtils.unescapeXml(text);
            text = text.replaceAll("\\P{Print}", " ");

            // Put on title or body
            if (tag.equals("ArticleTitle")){
                title.append(text);
            } else {
                body.append(text);
                body.append("\n\n");
            }

            //
//            if (yytext.indexOf("<title>") != -1 || yytext.indexOf("<p>") != -1 || yytext.indexOf("<label>") != -1) {
//                body.append("\n");
//            }
        }
    };

    public Map<String, Document> parse(final InputStream inputStream) {
        body = new StringBuilder();
        articles = new HashMap<String, Document>();
        try {
            DfaRun run = new DfaRun(dfa);
            run.setIn(new ReaderCharSource(inputStream, "UTF-8"));
            run.filter();
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem parsing the input stream.", ex);
        }
        
        return articles;
    }
}
