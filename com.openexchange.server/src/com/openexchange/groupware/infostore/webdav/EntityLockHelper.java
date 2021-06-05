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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;

public class EntityLockHelper extends LockHelper {

	private final EntityLockManager entityLockManager;
	protected SessionHolder sessionHolder;


	private static final String TOKEN_PREFIX = "http://www.open-xchange.com/webdav/locks/";
	private static final int TOKEN_PREFIX_LENGTH = TOKEN_PREFIX.length();

	public EntityLockHelper(final EntityLockManager entityLockManager, final SessionHolder sessionHolder, final WebdavPath url) {
		super(entityLockManager, sessionHolder, url);
		this.sessionHolder = sessionHolder;
		this.entityLockManager = entityLockManager;
	}

	@Override
	protected WebdavLock toWebdavLock(final Lock lock) {
		final WebdavLock l = new WebdavLock();
		l.setDepth(0);
		l.setTimeout(lock.getTimeout());
		l.setToken(TOKEN_PREFIX+lock.getId());
		l.setType(WebdavLock.Type.WRITE_LITERAL);
		l.setScope((lock.getScope().equals(LockManager.Scope.EXCLUSIVE)? WebdavLock.Scope.EXCLUSIVE_LITERAL : WebdavLock.Scope.SHARED_LITERAL));
		l.setOwner(lock.getOwnerDescription());
		l.setOwnerID(lock.getOwner());
		return l;
	}

	@Override
	protected Lock toLock(final WebdavLock lock) {
		final Lock l = new Lock();
		l.setId(Integer.parseInt(lock.getToken().substring( 41 )));
		l.setOwner(lock.getOwnerID());
		l.setOwnerDescription(lock.getOwner());
		l.setScope(lock.getScope().equals(WebdavLock.Scope.EXCLUSIVE_LITERAL) ? LockManager.Scope.EXCLUSIVE : LockManager.Scope.SHARED);
		l.setType(LockManager.Type.WRITE);
		l.setTimeout(lock.getTimeout());
		return l;
	}

	@Override
	protected int saveLock(final WebdavLock lock) throws OXException {
		final ServerSession session = getSession();
		return entityLockManager.lock(id,
				(lock.getTimeout() == WebdavLock.NEVER) ? LockManager.INFINITE : lock.getTimeout(),
						lock.getScope().equals(WebdavLock.Scope.EXCLUSIVE_LITERAL) ? LockManager.Scope.EXCLUSIVE : LockManager.Scope.SHARED,
						LockManager.Type.WRITE,
						lock.getOwner(),
						session.getContext(),
						UserStorage.getInstance().getUser(session.getUserId(), session.getContext()));
	}

	@Override
	protected void relock(final WebdavLock lock) throws OXException {
		final ServerSession session = getSession();
		final int lockId = getLockId(lock);
		entityLockManager.relock(
				lockId,
				(lock.getTimeout() == WebdavLock.NEVER) ? LockManager.INFINITE : lock.getTimeout(),
						lock.getScope().equals(WebdavLock.Scope.EXCLUSIVE_LITERAL) ? LockManager.Scope.EXCLUSIVE : LockManager.Scope.SHARED,
						LockManager.Type.WRITE,
						lock.getOwner(),
						session.getContext(),
						UserStorage.getInstance().getUser(session.getUserId(), session.getContext())
		);
	}

	private int getLockId(final WebdavLock lock) {
		return Integer.parseInt(lock.getToken().substring(TOKEN_PREFIX_LENGTH));
	}

    private ServerSession getSession() throws OXException {
        try {
            return ServerSessionAdapter.valueOf(sessionHolder.getSessionObject());
        } catch (OXException e) {
            throw e;
        }
    }


}
