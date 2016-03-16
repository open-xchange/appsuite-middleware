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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

public abstract class LockHelper {
	private final Map<String, WebdavLock> locks = new HashMap<String, WebdavLock>();
	private final WebdavPath url;
	protected int id;

	private final Set<String> removedLocks = new HashSet<String>();
	private final Set<Integer> removedLockIDs = new HashSet<Integer>();
	private final SessionHolder sessionHolder;
	private final LockManager lockManager;
	private boolean loadedLocks;


	public LockHelper(final LockManager lockManager, final SessionHolder sessionHolder, final WebdavPath url) {
		this.lockManager = lockManager;
		if(null == sessionHolder) {
			throw new IllegalArgumentException("sessionHolder may not be null");
		}
		this.sessionHolder = sessionHolder;
		this.url = url;
	}

	public void setId(final int id){
		this.id = id;
	}

	public WebdavLock getLock(final String token) throws WebdavProtocolException {
		loadLocks();
		return locks.get(token);
	}

	public List<WebdavLock> getAllLocks() throws WebdavProtocolException {
		loadLocks();
		final List<WebdavLock> lockList = new ArrayList<WebdavLock>(locks.values());
		final List<WebdavLock> notExpired = new ArrayList<WebdavLock>();
		final long now = System.currentTimeMillis();
		for(final WebdavLock lock : lockList) {
			if(lock.isActive(now)) {
				notExpired.add(lock);
			} else {
				removeLock(lock.getToken());
			}
		}

		return notExpired;
	}

	public void addLock(final WebdavLock lock) throws WebdavProtocolException {
		try {
			loadLocks();
			if(lock.getToken()!= null && locks.containsKey(lock.getToken())) {
				relock(lock);
                locks.put(lock.getToken(), lock);
                return;
			}

			lock.setOwnerID(getSession().getUserId());
			final int lockId = saveLock(lock);
			lock.setToken("http://www.open-xchange.com/webdav/locks/"+lockId);
			locks.put(lock.getToken(), lock);
		} catch (final OXException e) {
		    throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	protected abstract void relock(WebdavLock lock) throws OXException;

	protected abstract int saveLock(WebdavLock lock) throws OXException;

	public void removeLock(final String token) {
		locks.remove(token);
		markRemovedLock(token);
	}

	public void setLocks(final List<Lock> locks) {
		for(final Lock lock : locks) {
			final WebdavLock l = toWebdavLock(lock);
			this.locks.put(l.getToken(), l);
		}
	}

	protected abstract WebdavLock toWebdavLock(Lock lock);
	protected abstract Lock toLock(WebdavLock lock);

	private synchronized void loadLocks() throws WebdavProtocolException {
		if(loadedLocks) {
			return;
		}
		if(id == 0) {
			return;
		}
		loadedLocks = true;
		try {
            final ServerSession session = getSession();
		    final List<Lock> locks = lockManager.findLocks(id, session);
			final List<Lock> cleanedLocks = new ArrayList<Lock>();
			for(final Lock lock : locks) {
				if (!removedLockIDs.contains(Integer.valueOf(lock.getId()))) {
					cleanedLocks.add(lock);
				}
			}
			setLocks(cleanedLocks);
		} catch (final OXException e) {
		    throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void markRemovedLock(final String token) {
		removedLocks.add(token);
		final int lockId = Integer.parseInt(token.substring(41));
		removedLockIDs.add(Integer.valueOf(lockId));
	}

	public void dumpLocksToDB() throws OXException {
		if(removedLocks.isEmpty()) {
			return;
		}
		final ServerSession session = getSession();
		final Context ctx = session.getContext();
		final User user = UserStorage.getInstance().getUser(session.getUserId(), session.getContext());
		final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext());
		for(final int id : removedLockIDs) {
			lockManager.unlock(id, session);
		}
		removedLocks.clear();
		removedLockIDs.clear();
	}

	public void deleteLocks() throws OXException {
		final ServerSession session = getSession();
		lockManager.removeAll(id, session);
	}

	public void transferLock(final WebdavLock lock) throws OXException {
		final ServerSession session = getSession();
		lockManager.insertLock(id, toLock(lock), session);
	}

    private ServerSession getSession() throws OXException {
        try {
            return ServerSessionAdapter.valueOf(sessionHolder.getSessionObject());
        } catch (final OXException e) {
            throw e;
        }
    }
}
