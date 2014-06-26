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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.soap.dataobjects;

import java.util.HashSet;
import java.util.Map;


public class Context {

    private Integer id;

    private Database readDatabase;

    private Database writeDatabase;

    private Integer filestore_id;

    private String filestore_name;

    private Long average_size;

    private Long maxQuota;

    private Long usedQuota;

    private Boolean enabled;

    private String name;

    private String[] login_mappings;

    private SOAPStringMapMap userAttributes;


    /**
     * @return the userAttributes
     */
    public final SOAPStringMapMap getUserAttributes() {
        return userAttributes;
    }


    /**
     * @param userAttributes the userAttributes to set
     */
    public final void setUserAttributes(SOAPStringMapMap userAttributes) {
        this.userAttributes = userAttributes;
    }

    public Context() {
        super();
    }

    public Context(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Context(Integer id) {
        this.id = id;
    }

    public Context(com.openexchange.admin.rmi.dataobjects.Context c) {
        this.setId(c.getId());
        this.setAverage_size(c.getAverage_size());
        this.setEnabled(c.getEnabled());
        this.setFilestore_name(c.getFilestore_name());
        this.setFilestoreId(c.getFilestoreId());
        HashSet<String> lmappings = c.getLoginMappings();
        if( null != lmappings && lmappings.size() > 0 ) {
            this.setLoginMappings(lmappings.toArray(new String[lmappings.size()]));
        }
        this.setMaxQuota(c.getMaxQuota());
        this.setName(c.getName());
        this.setReadDatabase(new Database(c.getReadDatabase()));
        this.setUsedQuota(c.getUsedQuota());
        this.setWriteDatabase(new Database(c.getWriteDatabase()));
        Map<String, Map<String, String>> userattrs = c.getUserAttributes();
        if( null != userattrs ) {
            this.setUserAttributes(SOAPStringMapMap.convertFromMapMap(userattrs));
        }
    }

    public final Integer getId() {
        return this.id;
    }

    public final void setId(final Integer id) {
        this.id = id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final void setLoginMappings(final String[] mappings) {
        this.login_mappings = mappings;
    }


    public final String[] getLoginMappings() {
        return this.login_mappings;
    }

    public final Integer getFilestoreId() {
        return filestore_id;
    }

    public final void setFilestoreId(final Integer filestore_id) {
        this.filestore_id = filestore_id;
    }

    /**
     * @return max Quota (in MB)
     */
    public final Long getMaxQuota() {
        return maxQuota;
    }

    /**
     *
     * @param maxQuota (in MB)
     */
    public final void setMaxQuota(final Long maxQuota) {
        this.maxQuota = maxQuota;
    }

    /**
     * @return used Quota (in MB)
     */
    public final Long getUsedQuota() {
        return usedQuota;
    }

    public final void setUsedQuota(final Long usedQuota) {
        this.usedQuota = usedQuota;
    }

    public final void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public final Database getReadDatabase() {
        return readDatabase;
    }

    public final void setReadDatabase(final Database readDatabase) {
        this.readDatabase = readDatabase;
    }

    public final Database getWriteDatabase() {
        return writeDatabase;
    }

    public final void setWriteDatabase(final Database writeDatabase) {
        this.writeDatabase = writeDatabase;
    }

    /**
     * @return configured average size (in MB)
     */
    public final Long getAverage_size() {
        return average_size;
    }

    /**
     * The context average size can only be configured in AdminDaemon.properties
     */
    public final void setAverage_size(final Long average_size) {
        this.average_size = average_size;
    }

    public final String getFilestore_name() {
        return filestore_name;
    }

    public final void setFilestore_name(final String filestore_name) {
        this.filestore_name = filestore_name;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
