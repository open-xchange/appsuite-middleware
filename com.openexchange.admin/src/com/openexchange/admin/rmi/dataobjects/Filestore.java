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

public class Filestore implements Serializable {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -6970026864761440793L;

    private Integer id;

    private String url;

    private Long size;

    private Long used;

    private Integer maxContexts;

    private Integer currentContexts;

    private String login;

    private String password;

    private String name;

    public Filestore() {
        super();
        init();
    }

    public Filestore(final Integer id) {
        super();
        init();
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(final Integer val) {
        this.id = val;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(final String val) {
        this.url = val;
    }

    public Long getSize() {
        return this.size;
    }

    public void setSize(final Long val) {
        this.size = val;
    }

    public Integer getMaxContexts() {
        return this.maxContexts;
    }

    public void setMaxContexts(final Integer val) {
        this.maxContexts = val;
    }

    public Integer getCurrentContexts() {
        return this.currentContexts;
    }

    public void setCurrentContexts(final Integer val) {
        this.currentContexts = val;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getUsed() {
        return this.used;
    }

    public void setUsed(final Long quota_used) {
        this.used = quota_used;
    }

    private void init() {
        this.maxContexts = null;
        this.id = null;
        this.size = null;
        this.used = null;
        this.currentContexts = null;
        this.url = null;
        this.name = null;
        this.login = null;
        this.password = null;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value
     * format.
     * 
     * @return a <code>String</code> representation of this object.
     */
    @Override
    public String toString() {
        final String TAB = "\n  ";

        final StringBuilder retValue = new StringBuilder();

        retValue.append("Filestore ( ");
        retValue.append(super.toString()).append(TAB);
        if (null != this.id) {
            retValue.append("id = ").append(this.id).append(TAB);
        }        
        if (null != this.url) {
            retValue.append("url = ").append(this.url).append(TAB);
        }        
        if (null != this.size) {
            retValue.append("size = ").append(this.size).append(TAB);
        }        
        if (null != this.used) {
            retValue.append("used = ").append(this.used).append(TAB);
        }        
        if (null != this.maxContexts) {
            retValue.append("maxContexts = ").append(this.maxContexts).append(TAB);
        }        
        if (null != this.currentContexts) {
            retValue.append("currentContexts = ").append(this.currentContexts).append(TAB);
        }        
        if (null != this.login) {
            retValue.append("login = ").append(this.login).append(TAB);
        }        
        if (null != this.name) {
            retValue.append("name = ").append(this.name).append(TAB);
        }        
        retValue.append(" )");

        return retValue.toString();
    }

}
