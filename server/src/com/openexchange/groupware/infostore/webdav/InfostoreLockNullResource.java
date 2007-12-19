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

import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.sessiond.impl.SessionHolder;
import com.openexchange.webdav.protocol.*;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.Protocol.WEBDAV_METHOD;
import com.openexchange.webdav.protocol.impl.AbstractCollection;
import com.openexchange.webdav.protocol.impl.AbstractResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class InfostoreLockNullResource extends AbstractCollection implements OXWebdavResource{

	private static final WEBDAV_METHOD[] OPTIONS = {WEBDAV_METHOD.PUT, WEBDAV_METHOD.MKCOL, WEBDAV_METHOD.OPTIONS, WEBDAV_METHOD.PROPFIND, WEBDAV_METHOD.LOCK, WEBDAV_METHOD.UNLOCK, WEBDAV_METHOD.TRACE};


	private static final Log LOG = LogFactory.getLog(InfostoreLockNullResource.class);
	
	
	private InfostoreWebdavFactory factory;
	private AbstractResource resource;
	private SessionHolder sessionHolder;
	private EntityLockHelper lockHelper;


	private DBProvider provider;


	private boolean exists;

	private int id;

	public InfostoreLockNullResource(final AbstractResource resource, final InfostoreWebdavFactory factory) {
		this.resource = resource;
		this.factory = factory;
		this.sessionHolder = factory.getSessionHolder();
		this.lockHelper = new EntityLockHelper(factory.getLockNullLockManager(), sessionHolder, resource.getUrl().toString());
		this.provider = factory.getProvider();
	}
	
	public InfostoreLockNullResource(final AbstractResource resource, final InfostoreWebdavFactory factory, final int id ){
		this(resource, factory);
		this.setId(id);
	}
	
	public static int findInfostoreLockNullResource(final WebdavPath url, final Connection readCon, final Context ctx) throws WebdavException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = readCon.prepareStatement("SELECT id FROM lock_null WHERE url = ? and cid = ?");
			stmt.setString(1,url.toString());
			stmt.setInt(2, ctx.getContextId());
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return -1;
		} catch (final SQLException x) {
			throw new WebdavException(url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (final SQLException e1) {
					LOG.debug("",e1);		
				}
			}
			if(rs != null) {
				try {
					rs.close();
				} catch (final SQLException e) {
					LOG.debug("",e);
				}
			}
		}
	}

	private void setId(final int id2) {
		this.id = id2;
		lockHelper.setId(id2);
		exists = true;
	}

	@Override
	protected WebdavFactory getFactory() {
		return factory;
	}
	
	@Override
	protected List<WebdavProperty> internalGetAllProps() throws WebdavException {
		return Collections.emptyList();
	}

	@Override
	protected WebdavProperty internalGetProperty(final String namespace, final String name)
			throws WebdavException {
		return null;
	}

	@Override
	protected void internalPutProperty(final WebdavProperty prop)
			throws WebdavException {
		// IGNORE
	}

	@Override
	protected void internalRemoveProperty(final String namespace, final String name)
			throws WebdavException {
		// IGNORE
	}

	@Override
	public void putBody(final InputStream body, final boolean guessSize)
			throws WebdavException {
		resource.putBody(body,guessSize);
	}

	@Override
	public void setCreationDate(final Date date) throws WebdavException {
		// IGNORE
	}

	public void create() throws WebdavException {
		delete();
		resource.create();
		transferLocks();
	}

	private void transferLocks() throws WebdavException {
		for(final WebdavLock lock : getOwnLocks()) {
			((OXWebdavResource) resource).transferLock(lock);
		}
	}

	@Override
	public void delete() throws WebdavException {
		final Context ctx = sessionHolder.getSessionObject().getContext();
		Connection writeCon = null;
		PreparedStatement stmt = null;
		try {
			writeCon = provider.getWriteConnection(ctx);
			writeCon.setAutoCommit(false);
			stmt = writeCon.prepareStatement("DELETE FROM lock_null WHERE cid = ? and id = ?");
			stmt.setInt(1,ctx.getContextId());
			stmt.setInt(2, id);
			stmt.executeUpdate();
			writeCon.commit();
			exists = false;
			factory.invalidate(getUrl(), getId()	, ((resource.isCollection()) ? Type.COLLECTION : Type.RESOURCE));
		} catch (final SQLException x) {
			try {
				writeCon.rollback();
			} catch (final SQLException e) {
				LOG.debug("",e);
			}
			throw new WebdavException(getUrl(),HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (final TransactionException e) {
			try {
				writeCon.rollback();
			} catch (final SQLException e2) {
				LOG.debug("",e2);
			}
			throw new WebdavException(getUrl(),HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					LOG.debug("",e);
				}
			}
			if(writeCon != null) {
				try {
					writeCon.setAutoCommit(true);
				} catch (final SQLException e) {
					LOG.debug("",e);
				}
				provider.releaseWriteConnection(ctx, writeCon);
			}
		}
	}

	public boolean exists() throws WebdavException {
		return exists;
	}

	@Override
	public InputStream getBody() throws WebdavException {
		return null;
	}

	@Override
	public String getContentType() throws WebdavException {
		return null;
	}

	public Date getCreationDate() throws WebdavException {
		return null;
	}

	public String getDisplayName() throws WebdavException {
		return null;
	}

	@Override
	public String getETag() throws WebdavException {
		return null;
	}

	@Override
	public String getLanguage() throws WebdavException {
		return null;
	}

	public Date getLastModified() throws WebdavException {
		return null;
	}

	@Override
	public Long getLength() throws WebdavException {
		return null;
	}

	public WebdavLock getLock(final String token) throws WebdavException {
		final WebdavLock lock = lockHelper.getLock(token);
		if(lock != null) {
			return lock;
		}
		return findParentLock(token);
	}

	public List<WebdavLock> getLocks() throws WebdavException {
		final List<WebdavLock> lockList =  getOwnLocks();
		addParentLocks(lockList);
		return lockList;
	}

	public WebdavLock getOwnLock(final String token) throws WebdavException {
		return lockHelper.getLock(token);
	}

	public List<WebdavLock> getOwnLocks() throws WebdavException {
		return lockHelper.getAllLocks();
	}

	public String getSource() throws WebdavException {
		return null;
	}

	public WebdavPath getUrl() {
		return resource.getUrl();
	}

	public void lock(final WebdavLock lock) throws WebdavException {
		try {
			dumpToDB();
			lockHelper.addLock(lock);
			lockHelper.dumpLocksToDB();
		} catch (final Exception e) {
			throw new WebdavException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public void save() throws WebdavException {
		throw new WebdavException(getUrl(), HttpServletResponse.SC_CONFLICT);
	}

	@Override
	public void setContentType(final String type) throws WebdavException {
		// IGNORE
	}

	public void setDisplayName(final String displayName) throws WebdavException {
		// IGNORE
	}

	@Override
	public void setLanguage(final String language) throws WebdavException {
		// IGNORE
	}

	@Override
	public void setLength(final Long length) throws WebdavException {
		// IGNORE
	}

	@Override
	public void setSource(final String source) throws WebdavException {
		// IGNORE
	}

	
	@Override
	public boolean isLockNull(){
		return true;
	}
	
	@Override
	protected boolean isset(final Property p) {
		switch(p.getId()) {
		case Protocol.LOCKDISCOVERY : case Protocol.SUPPORTEDLOCK : case Protocol.DISPLAYNAME : 
			return true;
		default: return false;
		}
	}
	
	public void unlock(final String token) throws WebdavException {
		lockHelper.removeLock(token);
		if(getOwnLocks().isEmpty()) {
			delete();
		}
	}
	
	@Override
	public WEBDAV_METHOD[] getOptions(){
		return OPTIONS;
	}
	
	private void dumpToDB() throws SQLException, TransactionException {
		if(exists) {
			return;
		}
		final Context ctx = sessionHolder.getSessionObject().getContext();
		Connection writeCon = null;
		PreparedStatement stmt = null;
		try {
			writeCon = provider.getWriteConnection(ctx);
			writeCon.setAutoCommit(false);
			final int id = IDGenerator.getId(ctx, Types.INFOSTORE, writeCon);
			stmt = writeCon.prepareStatement("INSERT INTO lock_null (cid, id, url) VALUES (?,?,?)");
			stmt.setInt(1,ctx.getContextId());
			stmt.setInt(2, id);
			stmt.setString(3, getUrl().toString());
			stmt.executeUpdate();
			setId(id);
			writeCon.commit();
		} catch (final SQLException x) {
			try {
				writeCon.rollback();
			} catch (SQLException x2) {
				LOG.error("Can't roll back",x2);
			}
			throw x;
		} catch (final TransactionException e) {
			try {
				writeCon.rollback();
			} catch (SQLException x2) {
				LOG.error("Can't roll back",x2);
			}
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if(writeCon != null) {
				writeCon.setAutoCommit(true);
				provider.releaseWriteConnection(ctx, writeCon);
			}
		}
	}

	public int getId() {
		return id;
	}

	public int getParentId() throws WebdavException {
		return ((OXWebdavResource) parent()).getId();
	}

	public void removedParent() throws WebdavException {
		// IGNORE
	}

	public void setResource(final AbstractResource res) {
		this.resource = res;
	}

	@Override
	protected void internalDelete() throws WebdavException {
		//IGNORE
	}

	public List<WebdavResource> getChildren() throws WebdavException {
		return Collections.emptyList();
	}

	public void transferLock(final WebdavLock lock) {
		// TODO Auto-generated method stub
		
	}


}
