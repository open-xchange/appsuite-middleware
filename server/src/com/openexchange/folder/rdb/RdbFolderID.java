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

import java.io.Serializable;

import com.openexchange.groupware.contexts.Context;

/**
 * {@link RdbFolderID} - The ID for a folder kept in relational database.
 * <p>
 * Consists of folder's context ID and its folder ID.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbFolderID implements Cloneable, Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 3582205794948302557L;

	/*
	 * ++++++++++++ Constants ++++++++++++
	 */

	public static final int SYSTEM_ROOT_FOLDER_ID = 0;

	public static final int SYSTEM_PRIVATE_FOLDER_ID = 1;

	public static final int SYSTEM_PUBLIC_FOLDER_ID = 2;

	public static final int SYSTEM_SHARED_FOLDER_ID = 3;

	public static final int SYSTEM_FOLDER_ID = 4;

	public static final int SYSTEM_GLOBAL_FOLDER_ID = 5;

	public static final int SYSTEM_LDAP_FOLDER_ID = 6;

	public static final int SYSTEM_OX_FOLDER_ID = 7;

	public static final int SYSTEM_OX_PROJECT_FOLDER_ID = 8;

	public static final int SYSTEM_INFOSTORE_FOLDER_ID = 9;

	/*
	 * ++++++++++++ Members ++++++++++++
	 */

	public final Context ctx;

	public final int fuid;

	private int hash = -1;

	/**
	 * Initializes a new {@link RdbFolderID}
	 */
	public RdbFolderID(final int fuid, final Context ctx) {
		super();
		this.ctx = ctx;
		this.fuid = fuid;
		hash = _hashCode();
	}

	@Override
	public String toString() {
		return new StringBuilder(16).append(fuid).append(" in context ").append(ctx.getContextId()).toString();
	}

	private int _hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ctx.getContextId();
		result = prime * result + fuid;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash;
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
		final RdbFolderID other = (RdbFolderID) obj;
		if (ctx.getContextId() != other.ctx.getContextId()) {
			return false;
		}
		if (fuid != other.fuid) {
			return false;
		}
		return true;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new InternalError(
					"RdbFolderID.clone(): CloneNotSupportedException even though Cloneable is implemented");
		}
	}
}
