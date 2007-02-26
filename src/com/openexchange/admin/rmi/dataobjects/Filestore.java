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

    private int id;

    private String url;

    private long size;
    
    private long quota_used;
    
    private long quota_max;

    private int maxContexts;

    private int currentContexts;
    
    private String login;
    
    private String password;
    
    private String name;  
    
    public Filestore() {
        super();
        init();
    }
    
    public Filestore(int id) {
        super();
        init();
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int val) {
        this.id = val;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String val) {
        this.url = val;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long val) {
        this.size = val;
    }

    public int getMaxContexts() {
        return maxContexts;
    }

    public void setMaxContexts(int val) {
        this.maxContexts = val;
    }

    public int getCurrentContexts() {
        return this.currentContexts;
    }

    public void setCurrentContexts(int val) {
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

    
    public long getQuota_max() {
        return quota_max;
    }

    public void setQuota_max(long quota_max) {
        this.quota_max = quota_max;
    }

    public long getQuota_used() {
        return quota_used;
    }

    public void setQuota_used(long quota_used) {
        this.quota_used = quota_used;
    }

    private void init() {
        this.maxContexts = 100;
        this.id = -1;
        this.size = -1;
        this.quota_used = -1;
        this.quota_max = -1;
        this.currentContexts = -1;
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
