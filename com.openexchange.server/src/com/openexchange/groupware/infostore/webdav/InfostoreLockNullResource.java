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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavMethod;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

public class InfostoreLockNullResource extends AbstractCollection implements OXWebdavResource{

	private static final WebdavMethod[] OPTIONS = {WebdavMethod.PUT, WebdavMethod.MKCOL, WebdavMethod.OPTIONS, WebdavMethod.PROPFIND, WebdavMethod.LOCK, WebdavMethod.UNLOCK, WebdavMethod.TRACE};


	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreLockNullResource.class);


	private final InfostoreWebdavFactory factory;
	private AbstractResource resource;
	private final SessionHolder sessionHolder;
	private final EntityLockHelper lockHelper;


	private final DBProvider provider;


	private boolean exists;

	private int id;

	public InfostoreLockNullResource(final AbstractResource resource, final InfostoreWebdavFactory factory) {
		this.resource = resource;
		this.factory = factory;
		this.sessionHolder = factory.getSessionHolder();
		this.lockHelper = new EntityLockHelper(factory.getLockNullLockManager(), sessionHolder, resource.getUrl());
		this.provider = factory.getProvider();
	}

	public InfostoreLockNullResource(final AbstractResource resource, final InfostoreWebdavFactory factory, final int id ){
		this(resource, factory);
		this.setId(id);
	}

	public static int findInfostoreLockNullResource(final WebdavPath url, final Connection readCon, final Context ctx) throws WebdavProtocolException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = readCon.prepareStatement("SELECT id FROM lock_null WHERE url = ? and cid = ?");
			stmt.setString(1,url.toEscapedString());
			stmt.setInt(2, ctx.getContextId());
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return -1;
		} catch (final SQLException x) {
			throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
	protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
		return Collections.emptyList();
	}

	@Override
	protected WebdavProperty internalGetProperty(final String namespace, final String name)
			throws WebdavProtocolException {
		return null;
	}

	@Override
	protected void internalPutProperty(final WebdavProperty prop)
			throws WebdavProtocolException {
		// IGNORE
	}

	@Override
	protected void internalRemoveProperty(final String namespace, final String name)
			throws WebdavProtocolException {
		// IGNORE
	}

	@Override
	public void putBody(final InputStream body, final boolean guessSize)
			throws WebdavProtocolException {
		resource.putBody(body,guessSize);
	}

	@Override
	public void setCreationDate(final Date date) throws WebdavProtocolException {
		// IGNORE
	}

	@Override
    public void create() throws WebdavProtocolException {
		delete();
		resource.create();
		transferLocks();
	}

	private void transferLocks() throws WebdavProtocolException {
		for(final WebdavLock lock : getOwnLocks()) {
			((OXWebdavResource) resource).transferLock(lock);
		}
	}

	@Override
	public void delete() throws WebdavProtocolException {
		final Context ctx = sessionHolder.getContext();
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
		    rollback(writeCon);
            throw WebdavProtocolException.generalError(getUrl(),HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (final OXException e) {
		    rollback(writeCon);
			throw WebdavProtocolException.generalError(getUrl(),HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					LOG.debug("",e);
				}
			}
			autocommit(writeCon);
			if(writeCon != null) {
				provider.releaseWriteConnection(ctx, writeCon);
			}
		}
	}

	@Override
    public boolean exists() throws WebdavProtocolException {
		return exists;
	}

	@Override
	public InputStream getBody() throws WebdavProtocolException {
		return null;
	}

	@Override
	public String getContentType() throws WebdavProtocolException {
		return null;
	}

	@Override
    public Date getCreationDate() throws WebdavProtocolException {
		return null;
	}

	@Override
    public String getDisplayName() throws WebdavProtocolException {
		return null;
	}

	@Override
	public String getETag() throws WebdavProtocolException {
		return null;
	}

	@Override
	public String getLanguage() throws WebdavProtocolException {
		return null;
	}

	@Override
    public Date getLastModified() throws WebdavProtocolException {
		return null;
	}

	@Override
	public Long getLength() throws WebdavProtocolException {
		return null;
	}

	@Override
    public WebdavLock getLock(final String token) throws WebdavProtocolException {
		final WebdavLock lock = lockHelper.getLock(token);
		if(lock != null) {
			return lock;
		}
		return findParentLock(token);
	}

	@Override
    public List<WebdavLock> getLocks() throws WebdavProtocolException {
		final List<WebdavLock> lockList =  getOwnLocks();
		addParentLocks(lockList);
		return lockList;
	}

	@Override
    public WebdavLock getOwnLock(final String token) throws WebdavProtocolException {
		return lockHelper.getLock(token);
	}

	@Override
    public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
		return lockHelper.getAllLocks();
	}

	@Override
    public String getSource() throws WebdavProtocolException {
		return null;
	}

	@Override
    public WebdavPath getUrl() {
		return resource.getUrl();
	}

	@Override
    public void lock(final WebdavLock lock) throws WebdavProtocolException {
		try {
			dumpToDB();
			lockHelper.addLock(lock);
			lockHelper.dumpLocksToDB();
		} catch (final Exception e) {
			throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
    public void save() throws WebdavProtocolException {
		throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_CONFLICT);
	}

	@Override
	public void setContentType(final String type) throws WebdavProtocolException {
		// IGNORE
	}

	@Override
    public void setDisplayName(final String displayName) throws WebdavProtocolException {
		// IGNORE
	}

	@Override
	public void setLanguage(final String language) throws WebdavProtocolException {
		// IGNORE
	}

	@Override
	public void setLength(final Long length) throws WebdavProtocolException {
		// IGNORE
	}

	@Override
	public void setSource(final String source) throws WebdavProtocolException {
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

	@Override
    public void unlock(final String token) throws WebdavProtocolException {
		lockHelper.removeLock(token);
		if(getOwnLocks().isEmpty()) {
			delete();
		}
	}

	@Override
	public WebdavMethod[] getOptions(){
		return OPTIONS;
	}

	private void dumpToDB() throws SQLException, OXException {
		if(exists) {
			return;
		}
		final Context ctx = sessionHolder.getContext();
		Connection writeCon = null;
		PreparedStatement stmt = null;
		try {
			writeCon = provider.getWriteConnection(ctx);
			writeCon.setAutoCommit(false);
			final int id = IDGenerator.getId(ctx, Types.INFOSTORE, writeCon);
			stmt = writeCon.prepareStatement("INSERT INTO lock_null (cid, id, url) VALUES (?,?,?)");
			stmt.setInt(1,ctx.getContextId());
			stmt.setInt(2, id);
			stmt.setString(3, getUrl().toEscapedString());
			stmt.executeUpdate();
			setId(id);
			writeCon.commit();
		} catch (final SQLException x) {
		    rollback(writeCon);
			throw x;
		} catch (final OXException e) {
		    rollback(writeCon);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			autocommit(writeCon);
			if(writeCon != null) {
				provider.releaseWriteConnection(ctx, writeCon);
			}
		}
	}

	@Override
    public int getId() {
		return id;
	}

	@Override
    public int getParentId() throws WebdavProtocolException {
		return ((OXWebdavResource) parent()).getId();
	}

	@Override
    public void removedParent() throws WebdavProtocolException {
		// IGNORE
	}

	public void setResource(final AbstractResource res) {
		this.resource = res;
	}

	@Override
	protected void internalDelete() throws WebdavProtocolException {
		//IGNORE
	}

	@Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
		return Collections.emptyList();
	}

	@Override
    public void transferLock(final WebdavLock lock) {
		// Nothing to do

	}


}
