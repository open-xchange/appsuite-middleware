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

package com.openexchange.groupware.infostore.webdav;

import java.util.Arrays;
import java.util.List;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.DBProvider;

public class EntityLockManagerImpl extends LockManagerImpl<Lock> implements
		EntityLockManager {

	public EntityLockManagerImpl(String tablename) {
		super(tablename);
	}
	
	public EntityLockManagerImpl(DBProvider provider, String tablename) {
		super(provider, tablename);
	}

	@Override
	protected Lock newLock() {
		return new Lock();
	}

	public List<Lock> findLocks(int entity, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		return findLocksByEntity(Arrays.asList(entity), ctx, user, userConfig).get(entity);
	}

	public int lock(int entity, long timeout, Scope scope, Type type, String ownerDesc, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		return createLock(entity, timeout, scope, type, ownerDesc, ctx, user, userConfig);
	}

	public void unlock(int id, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		removeLock(id, ctx, user, userConfig);
	}

	public void removeAll(int entity, Context context, User userObject, UserConfiguration userConfiguration) throws OXException {
		removeAllFromEntity(entity,context,userObject,userConfiguration);
	}

	public void relock(int lockId, long timeout, Scope scope, Type write, String owner, Context context, User userObject, UserConfiguration userConfiguration) throws OXException {
		updateLock(lockId, timeout, scope, write, owner, context, userObject, userConfiguration);
	}

	public void insertLock(int entity, Lock lock, Context ctx, User user, UserConfiguration userConfig) throws OXException{
		createLockForceId(entity, lock.getId(), lock.getTimeout(), lock.getScope(), lock.getType(), lock.getOwnerDescription(),ctx,user,userConfig);
	}

}
