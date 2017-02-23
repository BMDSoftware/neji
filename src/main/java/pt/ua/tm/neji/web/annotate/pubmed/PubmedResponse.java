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

import java.util.List;
import java.util.Map;

/**
 * Pubmed response.
 * 
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class PubmedResponse {
    
    // Attributes
    private String pmid;
    private String title;
    private String abstract_;
    private List<String> entities_title;
    private List<String> entities_abstract;
    private List<String> ids;

    public PubmedResponse() {        
    }
    
    public PubmedResponse(String pmid, String title, String abstract_, 
            List<String> entities_title, List<String> entities_abstract, List<String> ids) {
        this.pmid = pmid;
        this.title = title;
        this.abstract_ = abstract_;
        this.entities_title = entities_title;
        this.entities_abstract = entities_abstract;
        this.ids = ids;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstract_() {
        return abstract_;
    }

    public void setAbstract_(String abstract_) {
        this.abstract_ = abstract_;
    }

    public List<String> getEntities_title() {
        return entities_title;
    }

    public void setEntities_title(List<String> entities_title) {
        this.entities_title = entities_title;
    }

    public List<String> getEntities_abstract() {
        return entities_abstract;
    }

    public void setEntities_abstract(List<String> entities_abstract) {
        this.entities_abstract = entities_abstract;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    
}
