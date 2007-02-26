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

public class Database implements Serializable{
    /**
     * For serialization
     */
    private static final long serialVersionUID = -3068828009821317094L;

    private Integer id;
    
    private Integer read_id;

    private String url;

    private String login;

    private String password;

    private String displayname;

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
    public Database(int id) {
        super();
        init();
        this.id = id;
    }

    /**
     * @param id
     * @param schema
     */
    public Database(int id, String schema) {
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
    public Database(String login, String password, String driver, String url, int id, String displayname) {
        super();
        this.login = login;
        this.password = password;
        this.driver = driver;
        this.url = url;
        this.id = id;
        this.displayname = displayname;
    }

    /**
     * 
     */
    public Database () {
        super();
        init();
    }

    ////////////////////////////////////////////////
	// Getter and Setter
	//
    public Integer getId () {
        return id;
    }

    public void setId (int val) {
        this.id = val;
    }

    public String getUrl () {
        return url;
    }

    public void setUrl (String val) {
        this.url = val;
    }

    public String getLogin () {
        return login;
    }

    public void setLogin (String val) {
        this.login = val;
    }

    public String getPassword () {
        return password;
    }

    public void setPassword (String val) {
        this.password = val;
    }

    public String getDisplayname () {
        return displayname;
    }

    public void setDisplayname (String val) {
        this.displayname = val;
    }

    public String getDriver () {
        return driver;
    }

    public void setDriver (String val) {
        this.driver = val;
    }

    public String getScheme () {
        return scheme;
    }

    public void setScheme (String scheme) {
        this.scheme = scheme;
    }
    
    public Integer getClusterWeight () {
        return clusterWeight;
    }

    /**
     * The system weight factor of this database in percent, value is Integer
     * This value defines how contexts will be distributed over multiple databases/db pools.
     */
    public void setClusterWeight (int clusterWeight) {
        this.clusterWeight = clusterWeight;
    }

    public Integer getMaxUnits () {
        return this.maxUnits;
    }

    public void setMaxUnits (int maxunits) {
        this.maxUnits = maxunits;
    }

    public Integer getPoolInitial () {
        return poolInitial;
    }

    public void setPoolInitial (int poolInitial) {
        this.poolInitial = poolInitial;
    }

    public Integer getPoolMax () {
        return poolMax;
    }

    public void setPoolMax (int poolMax) {
        this.poolMax = poolMax;
    }

    public Integer getMasterId() {
        return masterId;
    }

    public void setMasterId(int masterId) {
        this.masterId = masterId;
    }

    public Boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public Integer getRead_id() {
        return read_id;
    }

    public void setRead_id(int read_id) {
        this.read_id = read_id;
    }

    public Integer getCurrentUnits() {
        return this.currentUnits;
    }

    public void setCurrentUnits(int units) {
        this.currentUnits = units;
    }

    public Integer getPoolHardLimit () {
        return poolHardLimit;
    }

    public void setPoolHardLimit (int poolHardLimit) {
        this.poolHardLimit = poolHardLimit;
    }

    
        ////////////////////////////////////////////////
        // Other methods
        //
    /**
     * This methods checks if the required attributes are set, so that a database
     * can be created with this object
     */
    public boolean attributesforcreateset() {
    	if (-1 != this.id || null != this.driver || null != this.url || null != this.scheme) {
    		return true;
    	} else {
    		return false;
    	}
    		
    }
    
    public boolean attributesfordeleteset() {
    	if (null != this.driver || null != this.url || null != this.scheme) {
    		return true;
    	} else {
    		return false;
    	}
    		
    }
    public boolean attributesforregisterset() {
    	if (null != this.driver || null != this.url || null != this.scheme || null != this.login || null!=this.password || -1!=this.clusterWeight || null!=this.displayname) {
    		return true;
    	} else {
    		return false;
    	}
    		
    }
    
    public String toString() {
    	StringBuilder ret = new StringBuilder();
    	ret.append("[ \n");
    	for( final Field f : this.getClass().getDeclaredFields() ) {
    		try {
				Object ob = f.get(this);
				String tname = f.getName();
				if( ob != null && ! tname.equals("serialVersionUID") ) {
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
    public Database(String login, String password, String driver, String url, String scheme) {
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
        this.displayname = null;
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

}
