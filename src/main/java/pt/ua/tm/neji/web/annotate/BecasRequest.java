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

package pt.ua.tm.neji.web.annotate;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 * @since 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BecasRequest {

    private String text;
    private Map<String, Boolean> groups;
    private String pmid;

    public BecasRequest() {
        this.text = "";
        this.groups = new HashMap<>();
        this.pmid = null;
    }

    public BecasRequest(String text, Map<String, Boolean> groups, String pmid) {
        this.text = text;
        this.groups = groups;
        this.pmid = pmid;
    }

    public Map<String, Boolean> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Boolean> groups) {
        this.groups = groups;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public String getPmid() {
        return pmid;
    }
    
    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BecasRequest other = (BecasRequest) obj;
        if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
            return false;
        }
        if (this.groups != other.groups && (this.groups == null || !this.groups.equals(other.groups))) {
            return false;
        }
        if ((this.pmid == null) ? (other.text != pmid) : !this.pmid.equals(other.pmid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 4;
        hash = 31 * hash + (this.text != null ? this.text.hashCode() : 0);
        hash = 31 * hash + (this.groups != null ? this.groups.hashCode() : 0);
        hash = 31 * hash + (this.pmid != null ? this.pmid.hashCode() : 0);
        return hash;
    }
}
