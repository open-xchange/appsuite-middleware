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

import java.lang.reflect.Field;

public class Database extends EnforceableDataObject implements NameAndIdObject {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -3068828009821317094L;

    private Integer id;

    private Integer read_id;

    private String url;

    private String login;

    private String password;

    private String name;

    private String driver;

    private String scheme;

    private Integer clusterWeight;

    private Integer maxUnits;

    private Integer poolHardLimit;

    private Integer poolInitial;

    private Integer poolMax;

    private Integer masterId;

    private Integer currentUnits;

    private Boolean master;

    /**
     * @param id
     */
    public Database(final int id) {
        super();
        init();
        this.id = id;
    }

    /**
     * @param id
     * @param schema
     */
    public Database(final int id, final String schema) {
        super();
        init();
        this.id = id;
        this.scheme = schema;
    }

    /**
     * @param login
     * @param password
     * @param driver
     * @param url
     * @param id
     * @param displayname
     */
    public Database(final String login, final String password, final String driver, final String url, final int id, final String displayname) {
        super();
        this.login = login;
        this.password = password;
        this.driver = driver;
        this.url = url;
        this.id = id;
        this.name = displayname;
    }

    /**
     * 
     */
    public Database() {
        super();
        init();
    }

    // //////////////////////////////////////////////
    // Getter and Setter
    //
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

    public String getLogin() {
        return this.login;
    }

    public void setLogin(final String val) {
        this.login = val;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String val) {
        this.password = val;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String val) {
        this.name = val;
    }

    public String getDriver() {
        return this.driver;
    }

    public void setDriver(final String val) {
        this.driver = val;
    }

    public String getScheme() {
        return this.scheme;
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }

    public Integer getClusterWeight() {
        return this.clusterWeight;
    }

    /**
     * The system weight factor of this database in percent, value is Integer
     * This value defines how contexts will be distributed over multiple
     * databases/db pools.
     */
    public void setClusterWeight(final int clusterWeight) {
        this.clusterWeight = clusterWeight;
    }

    public Integer getMaxUnits() {
        return this.maxUnits;
    }

    public void setMaxUnits(final int maxunits) {
        this.maxUnits = maxunits;
    }

    public Integer getPoolInitial() {
        return this.poolInitial;
    }

    public void setPoolInitial(final int poolInitial) {
        this.poolInitial = poolInitial;
    }

    public Integer getPoolMax() {
        return this.poolMax;
    }

    public void setPoolMax(final int poolMax) {
        this.poolMax = poolMax;
    }

    public Integer getMasterId() {
        return this.masterId;
    }

    public void setMasterId(final int masterId) {
        this.masterId = masterId;
    }

    public Boolean isMaster() {
        return this.master;
    }

    public void setMaster(final boolean master) {
        this.master = master;
    }

    public Integer getRead_id() {
        return this.read_id;
    }

    public void setRead_id(final int read_id) {
        this.read_id = read_id;
    }

    public Integer getCurrentUnits() {
        return this.currentUnits;
    }

    public void setCurrentUnits(final int units) {
        this.currentUnits = units;
    }

    public Integer getPoolHardLimit() {
        return this.poolHardLimit;
    }

    public void setPoolHardLimit(final int poolHardLimit) {
        this.poolHardLimit = poolHardLimit;
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                final Object ob = f.get(this);
                final String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (final IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (final IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append(" ]");
        return ret.toString();
    }

    /**
     * @param login
     * @param password
     * @param driver
     * @param url
     * @param scheme
     */
    public Database(final String login, final String password, final String driver, final String url, final String scheme) {
        super();
        this.login = login;
        this.password = password;
        this.driver = driver;
        this.url = url;
        this.scheme = scheme;
    }

    /**
     * Initializes all members to the default values
     */
    private void init() {
        this.id = null;
        this.read_id = null;
        this.clusterWeight = null;
        this.url = null;
        this.login = null;
        this.password = null;
        this.name = null;
        this.driver = null;
        this.scheme = null;
        this.maxUnits = null;
        this.poolHardLimit = null;
        this.poolInitial = null;
        this.poolMax = null;
        this.masterId = null;
        this.master = null;
        this.currentUnits = null;
    }

    @Override
    public String[] getMandatoryMembersChange() {
        return null;
    }

    @Override
    public String[] getMandatoryMembersCreate() {
        return new String[] { "id", "driver", "url", "scheme" };
    }

    @Override
    public String[] getMandatoryMembersDelete() {
        return new String[] { "driver", "url", "scheme", "password", "login" };
    }

    @Override
    public String[] getMandatoryMembersRegister() {
        return new String[] { "password", "name", "master" };
    }

}
