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

package com.openexchange.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.webdav.Lock;
import com.openexchange.groupware.infostore.webdav.LockManagerImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.DBProvider;

public class FolderLockManagerImpl extends LockManagerImpl<FolderLock> implements
		FolderLockManager {
	
	private String findLocks = "SELECT * FROM oxfolder_lock WHERE cid = ? AND ((entity = ?) OR (entity = ? AND depth = 1) OR (entity IN (%%path%%) AND depth = "+INFINITE+" ) )";
	
	public FolderLockManagerImpl(){
		super("oxfolder_lock");
	}
	
	public FolderLockManagerImpl(DBProvider provider) {
		super(provider, "oxfolder_lock");
	}
	
	public List<Lock> findLocks(int entity, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		return new ArrayList<Lock>(loadOwnLocks(Arrays.asList(entity), ctx, user, userConfig).get(entity));
	}
	
	public List<FolderLock> findAllLocks(int entity, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		return findFolderLocks(entity, ctx, user, userConfig);
	}

	public List<FolderLock> findFolderLocks(int entity, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		FolderTreeUtil treeUtil = new FolderTreeUtilImpl(getProvider());
		List<Integer> path = treeUtil.getPath(entity, ctx, user, userConfig);
		int parent = path.get(path.size()-2);
		path = path.subList(0, path.size()-2);
		String query = findLocks.replaceAll("%%path%%", join(path).toString());
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(query);
			set(1, stmt, null, ctx.getContextId(), entity, parent);
			rs = stmt.executeQuery();
			List<FolderLock> locks = new ArrayList<FolderLock>();
			while(rs.next()) {
				FolderLock lock = newLock();
				fillLock(lock, rs);
				if(lock.getTimeout()<1) {
					removeLock(lock.getId(), ctx, user, userConfig);
				} else {
					locks.add(lock);
				}
			}
			return locks;
		} catch (SQLException x) {
			throw new OXException();
		} finally {
			close(stmt, rs);
			releaseReadConnection(ctx, readCon);
		}
	}

	public Map<Integer, List<FolderLock>> loadOwnLocks(List<Integer> entities, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		return findLocksByEntity(entities, ctx, user, userConfig);
	}

	public void insertLock(int entity, Lock lock, Context ctx, User user, UserConfiguration userConfig) throws OXException{
		createLockForceId(entity, lock.getId(), lock.getTimeout(), lock.getScope(), lock.getType(), lock.getOwnerDescription(),ctx,user,userConfig, ((FolderLock)lock).getDepth());
	}
	
	public int lock(int entity, long timeout, Scope scope, Type type, int depth, String ownerDesc, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		return createLock(entity, timeout, scope, type, ownerDesc, ctx, user, userConfig, depth);
	}

	public void unlock(int id, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		removeLock(id, ctx, user, userConfig);
	}

	@Override
	protected FolderLock newLock() {
		return new FolderLock();
	}

	@Override
	protected void fillLock(FolderLock lock, ResultSet rs) throws SQLException {
		super.fillLock(lock, rs);
		lock.setDepth(rs.getInt("depth"));
	}

	@Override
	protected String initAdditionalFIND_BY_ENTITY(String findByEntity) {
		return findByEntity.replaceAll("%%additional_fields%%", ", depth");
	}

	@Override
	protected String initAdditionalINSERT(String insert) {
		insert = initAdditionalFIND_BY_ENTITY(insert);
		return insert.replaceAll("%%additional_question_marks%%", ", ?");
	}
	
	@Override
	protected int getType(){
		return Types.INFOSTORE;
	}

	public void removeAll(int entity, Context context, User userObject, UserConfiguration userConfiguration) throws OXException {
		removeAllFromEntity(entity, context, userObject, userConfiguration);
	}
}
