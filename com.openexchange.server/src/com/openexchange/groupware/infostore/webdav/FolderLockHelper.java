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
import com.openexchange.groupware.impl.FolderLock;
import com.openexchange.groupware.impl.FolderLockManager;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;

public class FolderLockHelper extends LockHelper {

	private final FolderLockManager lockManager;
	private final SessionHolder sessionHolder;

	public FolderLockHelper(final FolderLockManager lockManager, final SessionHolder sessionHolder, final WebdavPath url) {
		super(lockManager, sessionHolder, url);
		this.lockManager = lockManager;
		this.sessionHolder = sessionHolder;
	}

	@Override
	protected WebdavLock toWebdavLock(final Lock lock) {
		if (lock instanceof FolderLock) {
			final FolderLock folderLock = (FolderLock) lock;
			final WebdavLock l = new WebdavLock();
			l.setDepth(folderLock.getDepth());
			l.setTimeout(folderLock.getTimeout());
			l.setToken("http://www.open-xchange.com/webdav/locks/"+folderLock.getId());
			l.setType(WebdavLock.Type.WRITE_LITERAL);
			l.setScope((folderLock.getScope().equals(LockManager.Scope.EXCLUSIVE)? WebdavLock.Scope.EXCLUSIVE_LITERAL : WebdavLock.Scope.SHARED_LITERAL));
			l.setOwner(lock.getOwnerDescription());
			l.setOwnerID(lock.getOwner());
			return l;
		}
		throw new IllegalArgumentException("Lock must be of type FolderLock");
	}

	@Override
	protected int saveLock(final WebdavLock lock) throws OXException {
        try {
            final ServerSession session = getSession();
            return lockManager.lock(id,
                    (lock.getTimeout() == WebdavLock.NEVER) ? LockManager.INFINITE : lock.getTimeout(), //FIXME
                    lock.getScope().equals(WebdavLock.Scope.EXCLUSIVE_LITERAL) ? LockManager.Scope.EXCLUSIVE : LockManager.Scope.SHARED,
                    LockManager.Type.WRITE,
                    lock.getDepth(),
                    lock.getOwner(),
                    session.getContext(),
                    UserStorage.getInstance().getUser(session.getUserId(), session.getContext()));
        } catch (OXException x) {
            throw x;
        }
	}

	@Override
	protected void relock(final WebdavLock lock) {
		//TODO
	}

	@Override
	protected Lock toLock(final WebdavLock lock) {
		final FolderLock l = new FolderLock();
		l.setId(Integer.valueOf(lock.getToken().substring( 41 )));
		l.setOwner(lock.getOwnerID());
		l.setOwnerDescription(lock.getOwner());
		l.setScope(lock.getScope().equals(WebdavLock.Scope.EXCLUSIVE_LITERAL) ? LockManager.Scope.EXCLUSIVE : LockManager.Scope.SHARED);
		l.setType(LockManager.Type.WRITE);
		l.setDepth(lock.getDepth());
		l.setTimeout(lock.getTimeout());
		return l;
	}

    private ServerSession getSession() throws OXException {
        return ServerSessionAdapter.valueOf(sessionHolder.getSessionObject());
    }



}
