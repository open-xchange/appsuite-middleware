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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.sessiond.SessionHolder;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavLock;

public abstract class LockHelper {
	private final Map<String, WebdavLock> locks = new HashMap<String, WebdavLock>();
	private String url;
	protected int id;
	
	private Set<String> removedLocks = new HashSet<String>();
	private Set<Integer> removedLockIDs = new HashSet<Integer>();
	private SessionHolder sessionHolder;
	private LockManager lockManager;
	private boolean loadedLocks;


	public LockHelper(LockManager lockManager, SessionHolder sessionHolder, String url) {
		this.lockManager = lockManager;
		if(null == sessionHolder)
			throw new IllegalArgumentException("sessionHolder may not be null");
		this.sessionHolder = sessionHolder;
		this.url = url;
	}

	public void setId(int id){
		this.id = id;
	}
	
	public WebdavLock getLock(String token) throws WebdavException {
		loadLocks();
		return locks.get(token);
	}

	public List<WebdavLock> getAllLocks() throws WebdavException {
		loadLocks();
		List<WebdavLock> lockList = new ArrayList<WebdavLock>(locks.values());
		List<WebdavLock> notExpired = new ArrayList<WebdavLock>();
		long now = System.currentTimeMillis();
		for(WebdavLock lock : lockList) {
			if(!lock.isActive(now)) {
				removeLock(lock.getToken());
			} else
				notExpired.add(lock);
		}
		
		return notExpired;
	}

	public void addLock(WebdavLock lock) throws WebdavException {
		try {
			loadLocks();
			if(lock.getToken()!= null && locks.containsKey(lock.getToken())) {
				relock(lock);
				return;
			}
			
			int lockId = saveLock(lock);
			lock.setToken("http://www.open-xchange.com/webdav/locks/"+lockId);
			locks.put(lock.getToken(), lock);
		} catch (OXException e) {
			throw new WebdavException(e.toString(), e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	protected abstract void relock(WebdavLock lock) throws OXException; 

	protected abstract int saveLock(WebdavLock lock) throws OXException ;

	public void removeLock(String token) {
		locks.remove(token);
		markRemovedLock(token);
	}
	
	public void setLocks(List<Lock> locks) {
		for(Lock lock : locks) {
			WebdavLock l = toWebdavLock(lock);
			this.locks.put(l.getToken(), l);
		}
	}
	
	protected abstract WebdavLock toWebdavLock(Lock lock);
	protected abstract Lock toLock(WebdavLock lock);

	private synchronized void loadLocks() throws WebdavException {
		if(loadedLocks)
			return;
		if(id == 0)
			return;
		loadedLocks = true;
		SessionObject session = sessionHolder.getSessionObject();
		try {
			List<Lock> locks = lockManager.findLocks(id, session.getContext(), session.getUserObject(), session.getUserConfiguration());
			List<Lock> cleanedLocks = new ArrayList<Lock>();
			for(Lock lock : locks) {
				if (!removedLockIDs.contains(lock.getId()))
					cleanedLocks.add(lock);
			}
			setLocks(cleanedLocks);
		} catch (OXException e) {
			throw new WebdavException(e.getMessage(), e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private void markRemovedLock(String token) {
		removedLocks.add(token);
		int lockId = Integer.valueOf(token.substring(41));
		removedLockIDs.add(lockId);
	}
	
	public void dumpLocksToDB() throws OXException {
		if(removedLocks.isEmpty())
			return;
		SessionObject session = sessionHolder.getSessionObject();
		Context ctx = session.getContext();
		User user = session.getUserObject();
		UserConfiguration userConfig = session.getUserConfiguration();
		for(int id : removedLockIDs) {
			lockManager.unlock(id, ctx, user, userConfig);
		}
		removedLocks.clear();
		removedLockIDs.clear();
	}
	
	public void deleteLocks() throws OXException {
		SessionObject session = sessionHolder.getSessionObject();
		lockManager.removeAll(id, session.getContext(), session.getUserObject(), session.getUserConfiguration());
	}

	public void transferLock(WebdavLock lock) throws OXException {
		SessionObject session = sessionHolder.getSessionObject();
		lockManager.insertLock(id, toLock(lock), session.getContext(), session.getUserObject(), session.getUserConfiguration());
	}
}
