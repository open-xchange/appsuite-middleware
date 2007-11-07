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

package com.openexchange.groupware.update;


/**
 * This class is a data container for the update information of a database
 * schema.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SchemaImpl implements Schema {

    /**
     * First schema version.
     */
    public static final SchemaImpl FIRST = new SchemaImpl(false, 0, true, true);

    /**
     * Actual schema after all updates are applied.
     */
    public static final SchemaImpl ACTUAL = new SchemaImpl(false, UpdateTaskCollection.getHighestVersion(), true, true); 
	    
    /**
     * Currently locked?
     */
    private boolean locked;

    /**
     * Version of the schema.
     */
    private int dbVersion;

    /**
     * Is the update compatible for the groupware?
     */
    private boolean groupwareCompatible;

    /**
     * Is the update compatible for the admin?
     */
    private boolean adminCompatible;

    /**
     * Hostname of the server that updates the database.
     */
    private String server;
    
    /**
     * Schema name
     */
    private String schema;

    /**
     * Default constructor.
     */
    SchemaImpl() {
        super();
    }
    
    /**
     * @param locked
     * @param dbVersion
     * @param groupwareCompatible
     * @param adminCompatible
     */
    public SchemaImpl(final boolean locked, final int dbVersion,
        final boolean groupwareCompatible, final boolean adminCompatible) {
        super();
        this.locked = locked;
        this.dbVersion = dbVersion;
        this.groupwareCompatible = groupwareCompatible;
        this.adminCompatible = adminCompatible;
    }

    /**
     * {@inheritDoc}
     */
    public int getDBVersion() {
        return dbVersion;
    }

    /**
     * @param dbVersion the dbVersion to set
     */
    public void setDBVersion(final int dbVersion) {
        this.dbVersion = dbVersion;
    }

    /**
     * @return the adminCompatible
     */
    public boolean isAdminCompatible() {
        return adminCompatible;
    }

    /**
     * @param adminCompatible the adminCompatible to set
     */
    public void setAdminCompatible(final boolean adminCompatible) {
        this.adminCompatible = adminCompatible;
    }

    /**
     * @return the groupwareCompatible
     */
    public boolean isGroupwareCompatible() {
        return groupwareCompatible;
    }

    /**
     * @param groupwareCompatible the groupwareCompatible to set
     */
    public void setGroupwareCompatible(final boolean groupwareCompatible) {
        this.groupwareCompatible = groupwareCompatible;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked the locked to set
     */
    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(final String server) {
        this.server = server;
    }

	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.update.Schema#getSchema()
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * @param schema - the schema name
	 */
	public void setSchema(final String schema) {
		this.schema = schema;
	}
    
}
