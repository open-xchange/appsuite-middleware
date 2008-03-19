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

package com.openexchange.folder.rdb;

import com.openexchange.folder.FolderModule;

/**
 * {@link RdbFolderModule} - Relational database's folder module
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbFolderModule implements FolderModule {

	/**
	 * Relational database's <b>tasks</b> module
	 */
	public static final FolderModule MODULE_TASK = new RdbFolderModule(1, "tasks");

	/**
	 * Relational database's <b>calendar</b> module
	 */
	public static final FolderModule MODULE_CALENDAR = new RdbFolderModule(2, "calendar");

	/**
	 * Relational database's <b>contacts</b> module
	 */
	public static final FolderModule MODULE_CONTACT = new RdbFolderModule(3, "contacts");

	/**
	 * Relational database's <b>unbound</b> module
	 */
	public static final FolderModule MODULE_UNBOUND = new RdbFolderModule(4, "unbound");

	/**
	 * Relational database's <b>system</b> module
	 */
	public static final FolderModule MODULE_SYSTEM = new RdbFolderModule(5, "system");

	/**
	 * Relational database's <b>projects</b> module
	 */
	public static final FolderModule MODULE_PROJECT = new RdbFolderModule(6, "projects");

	/**
	 * Relational database's <b>infostore</b> module
	 */
	public static final FolderModule MODULE_INFOSTORE = new RdbFolderModule(8, "infostore");

	/**
	 * Gets the module by <code>int</code> value
	 * 
	 * @param value
	 *            The module's <code>int</code> value
	 * @return The module or <code>null</code>
	 */
	public static FolderModule getModuleByValue(final int value) {
		switch (value) {
		case 1:
			return MODULE_TASK;
		case 2:
			return MODULE_CALENDAR;
		case 3:
			return MODULE_CONTACT;
		case 4:
			return MODULE_UNBOUND;
		case 5:
			return MODULE_SYSTEM;
		case 6:
			return MODULE_PROJECT;
		case 8:
			return MODULE_INFOSTORE;
		default:
			return null;
		}
	}

	/*
	 * Members
	 */

	private final String name;

	private final int value;

	/**
	 * Initializes a new {@link RdbFolderModule}
	 */
	private RdbFolderModule(final int value, final String name) {
		super();
		this.value = value;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.folder.FolderModule#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.folder.FolderModule#getValue()
	 */
	public int getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.folder.FolderModule#setName(java.lang.String)
	 */
	public void setName(final String name) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.folder.FolderModule#setValue(int)
	 */
	public void setValue(final int value) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + value;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RdbFolderModule other = (RdbFolderModule) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (value != other.value) {
			return false;
		}
		return true;
	}

}
