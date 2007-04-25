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



package com.openexchange.push.udp;

import com.openexchange.tools.StringCollection;

/**
 * PushObject
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class PushObject {
	
	private int folderId;
	
	private int module;
	
	private int contextId;
	
	private int users[];
	
	private boolean isSync;
	
	public PushObject(int folderId, int module, int contextId, int[] users, boolean isSync) throws Exception {
		this.folderId = folderId;
		this.module = module;
		this.contextId = contextId;
		this.users = users;
		this.isSync = isSync;
	}

	public int getFolderId() {
		return folderId;
	}

	public int getModule() {
		return module;
	}

	public int getContextId() {
		return contextId;
	}
	
	public int[] getUsers() {
		return users;
	}

	public boolean isSync() {
		return isSync;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o.hashCode() == hashCode()) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new StringBuilder().append('F').append(folderId).append('M').append(module).append('C')
				.append(contextId).toString().hashCode();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append("FOLDER_ID=")
		.append(folderId)
		.append(",MODULE=")
		.append(module)
		.append(",CONTEXT_ID=")
		.append(contextId)
		.append(",USERS=")
		.append(StringCollection.convertArray2String(users))
		.append(",IS_SYNC=")
		.append(isSync).toString();
	}
}
