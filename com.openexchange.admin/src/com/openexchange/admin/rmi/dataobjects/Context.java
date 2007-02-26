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

public class Context implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -8939189372445990901L;

    private int id;
    private Database readDatabase;
    private Database writeDatabase;
    private Filestore filestore;
    private long average_size;
    private long maxQuota;
    private long usedQuota;
    private MaintenanceReason maintenanceReason;
    private Boolean enabled;

    private String name;

    public Context() {
        super();
        init();
    }

    /**
     * @param id
     */
    public Context(int id) {
        super();
        init();
        this.id = id;
    }

    /**
     * @param id
     * @param name
     */
    public Context(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public int getIdAsInt() {
        return this.id;
    }

    public String getIdAsString() {
        return String.valueOf(this.id);
    }

    public void setID(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void init() {
        this.id = -1;
        this.name = null;
        this.enabled = false;
        this.filestore = null;
        this.average_size = -1;
        this.maintenanceReason = null;
        this.maxQuota = -10;
        this.usedQuota = -10;
        this.readDatabase = null;
        this.writeDatabase = null;
    }

    public Filestore getFilestore() {
        return filestore;
    }

    public void setFilestore(Filestore filestore) {
        this.filestore = filestore;
    }

    public long getMaxQuota() {
        return maxQuota;
    }

    public void setMaxQuota(long maxQuota) {
        this.maxQuota = maxQuota;
    }

    public long getUsedQuota() {
        return usedQuota;
    }

    public void setUsedQuota(long usedQuota) {
        this.usedQuota = usedQuota;
    }

    public MaintenanceReason getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(MaintenanceReason maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Database getReadDatabase() {
        return readDatabase;
    }

    public void setReadDatabase(Database readDatabase) {
        this.readDatabase = readDatabase;
    }

    public Database getWriteDatabase() {
        return writeDatabase;
    }

    public void setWriteDatabase(Database writeDatabase) {
        this.writeDatabase = writeDatabase;
    }

    public long getAverage_size() {
        return average_size;
    }

    public void setAverage_size(long average_size) {
        this.average_size = average_size;
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
