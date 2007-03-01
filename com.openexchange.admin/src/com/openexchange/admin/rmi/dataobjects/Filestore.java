/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;
import java.lang.reflect.Field;

public class Filestore implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6970026864761440793L;

    private Integer id;

    private String url;

    private Long size;
    
    private Long quota_used;
    
    private Long quota_max;

    private Integer maxContexts;

    private Integer currentContexts;
    
    private String login;
    
    private String password;
    
    private String name;  
    
    public Filestore() {
        super();
        init();
    }
    
    public Filestore(Integer id) {
        super();
        init();
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer val) {
        this.id = val;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String val) {
        this.url = val;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long val) {
        this.size = val;
    }

    public Integer getMaxContexts() {
        return maxContexts;
    }

    public void setMaxContexts(Integer val) {
        this.maxContexts = val;
    }

    public Integer getCurrentContexts() {
        return this.currentContexts;
    }

    public void setCurrentContexts(Integer val) {
        this.currentContexts = val;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public Long getQuota_max() {
        return quota_max;
    }

    public void setQuota_max(Long quota_max) {
        this.quota_max = quota_max;
    }

    public Long getQuota_used() {
        return quota_used;
    }

    public void setQuota_used(Long quota_used) {
        this.quota_used = quota_used;
    }

    private void init() {
        this.maxContexts = null;
        this.id = null;
        this.size = null;
        this.quota_used = null;
        this.quota_max = null;
        this.currentContexts = null;
        this.url = null;
        this.name = null;
        this.login = null;
        this.password = null;
        
    }
    
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                Object ob = f.get(this);
                String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }
}
