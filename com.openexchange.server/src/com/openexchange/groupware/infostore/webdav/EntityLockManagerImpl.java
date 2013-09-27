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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;

public class EntityLockManagerImpl extends LockManagerImpl<Lock> implements
		EntityLockManager {

	public EntityLockManagerImpl(final String tablename) {
		super(tablename);
	}

	public EntityLockManagerImpl(final DBProvider provider, final String tablename) {
		super(provider, tablename);
	}

	@Override
	protected Lock newLock() {
		return new Lock();
	}

	@Override
    public List<Lock> findLocks(final int entity, final Context ctx, final User user) throws OXException {
		return findLocksByEntity(Arrays.asList(Integer.valueOf(entity)), ctx).get(Integer.valueOf(entity));
	}

	@Override
    public boolean isLocked(final int entity, final Context ctx, final User user) throws OXException {
		return existsLockForEntity(Arrays.asList(new Integer[]{Integer.valueOf(entity)}), ctx);
	}


	@Override
    public int lock(final int entity, final long timeout, final Scope scope, final Type type, final String ownerDesc, final Context ctx, final User user) throws OXException {
		return createLock(entity, timeout, scope, type, ownerDesc, ctx, user);
	}

	@Override
    public void unlock(final int id, final Context ctx, final User user) throws OXException {
		removeLock(id, ctx);
	}

	@Override
    public void removeAll(final int entity, final Context context, final User userObject) throws OXException {
		removeAllFromEntity(entity,context);
	}

	@Override
    public void relock(final int lockId, final long timeout, final Scope scope, final Type write, final String owner, final Context context, final User userObject) throws OXException {
		updateLock(lockId, timeout, scope, write, owner, context);
	}

	@Override
    public void insertLock(final int entity, final Lock lock, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException{
		createLockForceId(entity, lock.getId(), lock.getTimeout(), lock.getScope(), lock.getType(), lock.getOwnerDescription(),ctx,user);
	}

	@Override
    public void transferLocks(final Context ctx, final int from_user, final int to_user) throws OXException {
		reassign(ctx, from_user, to_user);
	}


}
