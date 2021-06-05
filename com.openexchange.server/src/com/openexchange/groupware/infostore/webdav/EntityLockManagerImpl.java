/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.infostore.webdav;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

public class EntityLockManagerImpl extends LockManagerImpl<Lock> implements EntityLockManager {

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
    public List<Lock> findLocks(final int entity, final Session session) throws OXException {
		return findLocks(Arrays.asList(Integer.valueOf(entity)), session).get(Integer.valueOf(entity));
	}

    @Override
    public Map<Integer, List<Lock>> findLocks(List<Integer> entities, Session session) throws OXException {
        return findLocks(entities, getContextFrom(session));
    }

    @Override
    public Map<Integer, List<Lock>> findLocks(List<Integer> entities, Context context) throws OXException {
        return findLocksByEntity(entities, context);
    }

	@Override
    public boolean isLocked(final int entity, final Context ctx, final User user) throws OXException {
		return existsLockForEntity(Collections.singletonList(Integer.valueOf(entity)), ctx);
	}

	@Override
    public int lock(final int entity, final long timeout, final Scope scope, final Type type, final String ownerDesc, final Context ctx, final User user) throws OXException {
		return createLock(entity, timeout, scope, type, ownerDesc, ctx, user);
	}

	@Override
    public void unlock(final int id, final Session session) throws OXException {
		removeLock(id, getContextFrom(session));
	}

	@Override
    public void removeAll(final int entity, final Session session) throws OXException {
		removeAllFromEntity(entity, getContextFrom(session));
	}

	@Override
    public void relock(final int lockId, final long timeout, final Scope scope, final Type write, final String owner, final Context context, final User userObject) throws OXException {
		updateLock(lockId, timeout, scope, write, owner, context);
	}

	@Override
    public void insertLock(final int entity, final Lock lock, final Session session) throws OXException{
	    Context ctx = getContextFrom(session);
	    User user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
		createLockForceId(entity, lock.getId(), lock.getTimeout(), lock.getScope(), lock.getType(), lock.getOwnerDescription(),ctx,user);
	}

	@Override
    public void transferLocks(final Context ctx, final int from_user, final int to_user) throws OXException {
		reassign(ctx, from_user, to_user);
	}

	private Context getContextFrom(Session session) throws OXException {
        return session instanceof ServerSession ? ((ServerSession) session).getContext() : ContextStorage.getInstance().getContext(session);
    }

}
