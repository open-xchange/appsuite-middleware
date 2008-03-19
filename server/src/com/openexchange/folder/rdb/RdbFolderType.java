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

import com.openexchange.folder.FolderType;

/**
 * {@link RdbFolderType} - Relational database's folder type
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbFolderType implements FolderType {

	/**
	 * Relational database's <b>private</b> type
	 */
	public static final FolderType TYPE_PRIVATE = new RdbFolderType(1, "private");

	/**
	 * Relational database's <b>public</b> type
	 */
	public static final FolderType TYPE_PUBLIC = new RdbFolderType(2, "public");

	/**
	 * Relational database's <b>shared</b> type
	 */
	public static final FolderType TYPE_SHARED = new RdbFolderType(3, "shared");

	/**
	 * Relational database's <b>system</b> type
	 */
	public static final FolderType TYPE_SYSTEM = new RdbFolderType(RdbFolderModule.MODULE_SYSTEM.getValue(),
			RdbFolderModule.MODULE_SYSTEM.getName());

	/**
	 * Gets the type by specified <code>int</code> value
	 * 
	 * @param value
	 *            The <code>int</code> value
	 * @return The type or <code>null</code>
	 */
	public static FolderType getTypeByValue(final int value) {
		if (value == 1) {
			return TYPE_PRIVATE;
		} else if (value == 2) {
			return TYPE_PUBLIC;
		} else if (value == 3) {
			return TYPE_SHARED;
		} else if (value == RdbFolderModule.MODULE_SYSTEM.getValue()) {
			return TYPE_SYSTEM;
		} else {
			return null;
		}
	}

	/*
	 * Members
	 */

	private final String name;

	private final int value;

	/**
	 * Initializes a new {@link RdbFolderType}
	 */
	private RdbFolderType(final int value, final String name) {
		super();
		this.value = value;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	public void setName(final String name) {
	}

	public void setValue(final int value) {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + value;
		return result;
	}

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
		final RdbFolderType other = (RdbFolderType) obj;
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
