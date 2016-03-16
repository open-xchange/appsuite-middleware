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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.session.SessionHolder;
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
        } catch (final OXException e) {
            throw e;
        }
    }


}
