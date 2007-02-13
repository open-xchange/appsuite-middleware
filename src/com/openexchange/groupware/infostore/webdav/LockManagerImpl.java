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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.infostore.Classes;

import static com.openexchange.tools.sql.DBUtils.getStatement;


@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_WEBDAV_LOCKMANAGERIMPL, 
		component=Component.INFOSTORE
)
public abstract class LockManagerImpl<T extends Lock> extends DBService implements LockManager{
	
	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(LockManagerImpl.class);
	
	private String INSERT = "INSERT INTO %%tablename%% (entity, timeout, scope, type, ownerDesc, cid, userid, id %%additional_fields%% ) VALUES (?, ?, ?, ?, ?, ?, ?, ? %%additional_question_marks%%)";
	private String DELETE = "DELETE FROM %%tablename%% WHERE cid = ? AND id = ? ";
	private String FIND_BY_ENTITY = "SELECT entity, timeout, scope, type, ownerDesc, cid, userid, id %%additional_fields%% FROM %%tablename%% WHERE entity IN %%entity_ids%% and cid = ? ";
	private String DELETE_BY_ENTITY = "DELETE FROM %%tablename%% WHERE cid = ? AND entity = ?";
	private String UPDATE_BY_ID = "UPDATE %%tablename%% SET timeout = ? , scope = ?, type = ? , ownerDesc = ? %%additional_updates%% WHERE id = ? AND cid = ?";
	
	public LockManagerImpl(String tablename) {
		this(null, tablename);
	}
	
	public LockManagerImpl(DBProvider provider, String tablename) {
		setProvider(provider);
		initTablename(tablename);
	}

	private void initTablename(String tablename) {
		INSERT = INSERT.replaceAll("%%tablename%%", tablename);
		INSERT = initAdditionalINSERT(INSERT);
		DELETE = DELETE.replaceAll("%%tablename%%", tablename);
		
		FIND_BY_ENTITY = FIND_BY_ENTITY.replaceAll("%%tablename%%", tablename);
		FIND_BY_ENTITY = initAdditionalFIND_BY_ENTITY(FIND_BY_ENTITY);
		
		DELETE_BY_ENTITY = DELETE_BY_ENTITY.replaceAll("%%tablename%%", tablename);
		
		UPDATE_BY_ID = UPDATE_BY_ID.replaceAll("%%tablename%%", tablename);
		UPDATE_BY_ID = initAdditionalUPDATE_BY_ID(UPDATE_BY_ID);
	}
	
	
	private String initAdditionalUPDATE_BY_ID(String query) {
		return query.replaceAll("%%additional_updates%%","");
	}

	protected String initAdditionalINSERT(String insert) {
		return insert.replaceAll("%%additional_fields%%","").replaceAll("%%additional_question_marks%%", "");
	}
	
	protected String initAdditionalFIND_BY_ENTITY(String findByEntity) {
		return findByEntity.replaceAll("%%additional_fields%%", "");
	}
	
	protected int getType() {
		return com.openexchange.groupware.Types.WEBDAV; // FIXME
	}
	
	protected abstract T newLock();
	
	protected void fillLock(T lock, ResultSet rs) throws SQLException {
		lock.setId(rs.getInt("id"));
		lock.setOwner(rs.getInt("userid"));
		int scopeNum = rs.getInt("scope");
		for(Scope scope : Scope.values()) {
			if(scopeNum == scope.ordinal())
				lock.setScope(scope);
		}
		lock.setType(Type.WRITE);
		long timeout =  (rs.getLong("timeout") - System.currentTimeMillis());
		lock.setTimeout(timeout);
		lock.setOwnerDescription(rs.getString("ownerDesc"));
	}
	
	@OXThrows(
			category=Category.PROGRAMMING_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=0,
			msg="Invalid SQL: '%s'"
	)
	protected int createLockForceId(int entity, int id, long timeout, Scope scope, Type type, String ownerDesc,
			Context ctx, User user, UserConfiguration userConfig, Object...additional) throws OXException {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getWriteConnection(ctx);
			stmt = con.prepareStatement(INSERT);
			long tm = 0;
			if(timeout != INFINITE) {
				tm = System.currentTimeMillis()+timeout; 
				// Previously Infinite Locks exceed long range if ms counter increased by 1 since loading
				if(tm<0)
					tm = Long.MAX_VALUE;
			} else
				tm = Long.MAX_VALUE;
			set(1, stmt, additional, entity, tm, scope.ordinal(), type.ordinal(), ownerDesc, ctx.getContextId(), user.getId(), id);
			stmt.executeUpdate();
			return id;
		} catch (SQLException x) {
			throw EXCEPTIONS.create(0, x, getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, con);
		}
	}

	@OXThrows(
			category=Category.PROGRAMMING_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=1,
			msg="Error in SQL Update"
	)
	protected int createLock(int entity, long timeout, Scope scope, Type type, String ownerDesc,
			Context ctx, User user, UserConfiguration userConfig, Object...additional) throws OXException {
		try {
			return createLockForceId(entity, IDGenerator.getId(ctx, getType()), timeout, scope, type, ownerDesc, ctx, user, userConfig, additional);
		} catch (SQLException e) {
			throw EXCEPTIONS.create(1,e);
		}
	}
	
	@OXThrows(
			category=Category.PROGRAMMING_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=2,
			msg="Invalid SQL: '%s'"
	)
	protected void updateLock(int lockId, long timeout, Scope scope, Type type, String ownerDesc, Context ctx, User user, UserConfiguration userConfig, Object...additional) throws OXException {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getWriteConnection(ctx);
			stmt = con.prepareStatement(UPDATE_BY_ID);
			long tm = 0;
			if(timeout != INFINITE)
				tm = System.currentTimeMillis()+timeout;
			else
				tm = Long.MAX_VALUE;
			int index = set(1, stmt, additional, tm, scope.ordinal(), type.ordinal(), ownerDesc);
			set(index, stmt, null, lockId, ctx.getContextId());
			stmt.executeUpdate();
		} catch (SQLException x) {
			throw EXCEPTIONS.create(2,x,getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, con);
		}
	}

	@OXThrows(
			category=Category.PROGRAMMING_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=3,
			msg="Invalid SQL: '%s'"
	)
	protected void removeLock(int id, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getWriteConnection(ctx);
			stmt = con.prepareStatement(DELETE);
			set(1, stmt, null, ctx.getContextId(), id);
			stmt.executeUpdate();
		} catch (SQLException x) {
			throw EXCEPTIONS.create(3,x,getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, con);
		}
	}
	
	@OXThrows(
			category=Category.PROGRAMMING_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=4,
			msg="Invalid SQL: '%s'"
	)
	public Map<Integer,List<T>> findLocksByEntity(List<Integer> entities, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuilder entityIds = new StringBuilder("(");
			entityIds.append(join(entities));
			entityIds.append(")");
			
			con = getReadConnection(ctx);
			stmt = con.prepareStatement(FIND_BY_ENTITY.replaceAll("%%entity_ids%%", entityIds.toString()));
			set(1, stmt, null, ctx.getContextId());
			rs = stmt.executeQuery();
			Map<Integer, List<T>> locks = new HashMap<Integer, List<T>>();
			Set<Integer> entitySet = new HashSet<Integer>(entities);
			
			while(rs.next()) {
				int entity = rs.getInt("entity");
				entitySet.remove(entity);
				List<T> lockList = locks.get(entity);
				if(null == lockList) {
					lockList = new ArrayList<T>();
					locks.put(entity, lockList);
				}
				
				T lock = newLock();
				fillLock(lock, rs);
				if(lock.getTimeout()<1){
					removeLock(lock.getId(), ctx, user, userConfig);
				} else {
					lockList.add(lock);
				}
			}
			for(Integer entity : entitySet) {
				locks.put(entity, new ArrayList<T>());
			}
			return locks;
		} catch (SQLException x) {
			throw EXCEPTIONS.create(4,x,getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseReadConnection(ctx, con);
		}
	}
	
	@OXThrows(
			category=Category.PROGRAMMING_ERROR, 
			desc="Indicates a faulty SQL query or a problem with the database. Ususally only R&D can do anything about this.", 
			exceptionId=5,
			msg="Invalid SQL: '%s'"
	)
	protected void removeAllFromEntity(int entity, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		Connection writeCon = null;
		PreparedStatement stmt = null;
		try {
			writeCon = getWriteConnection(ctx);
			stmt = writeCon.prepareStatement(DELETE_BY_ENTITY);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, entity);
			stmt.executeUpdate();
		} catch (SQLException x) {
			throw EXCEPTIONS.create(5,x,getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, writeCon);
		}
	}
	
	protected CharSequence join(List<Integer> entities) {
		StringBuilder b = new StringBuilder();
		for(int entity : entities) { b.append(entity); b.append(", "); }
		b.setLength(b.length()-2);
		return b;
	}

	protected final int set(int index, PreparedStatement stmt, Object[] additional, Object...values) throws SQLException {
		for(Object o : values) {
			stmt.setObject(index++,o);
		}
		if(null == additional)
			return index;
		for(Object o : additional) {
			stmt.setObject(index++,o);
		}
		return index;
	}

}
