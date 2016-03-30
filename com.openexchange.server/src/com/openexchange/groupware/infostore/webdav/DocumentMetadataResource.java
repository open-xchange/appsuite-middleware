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

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurity;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.SetSwitch;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Streams;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tx.TransactionException;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

public class DocumentMetadataResource extends AbstractResource implements
		OXWebdavResource, OXExceptionConstants {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DocumentMetadataResource.class);

	private final InfostoreWebdavFactory factory;

	private boolean exists;

	private int id;

	private DocumentMetadata metadata = new DocumentMetadataImpl();

	// State
	private final Set<Metadata> setMetadata = new HashSet<Metadata>();

	private final PropertyHelper propertyHelper;

	private WebdavPath url;

	private final SessionHolder sessionHolder;

	private final InfostoreFacade database;

	private final InfostoreSecurity security;

	private boolean loadedMetadata;

	private boolean existsInDB;

	private final LockHelper lockHelper;

	private boolean metadataChanged;

	public DocumentMetadataResource(final WebdavPath url,
			final InfostoreWebdavFactory factory) {
		this.factory = factory;
		this.url = url;
		this.sessionHolder = factory.getSessionHolder();
		this.lockHelper = new EntityLockHelper(factory.getInfoLockManager(),
				sessionHolder, url);
		this.database = factory.getDatabase();
		this.security = factory.getSecurity();
		this.propertyHelper = new PropertyHelper(factory.getInfoProperties(),
				sessionHolder, url);
	}

	public DocumentMetadataResource(final WebdavPath url,
			final DocumentMetadata docMeta, final InfostoreWebdavFactory factory) {
		this.factory = factory;
		this.url = url;
		this.sessionHolder = factory.getSessionHolder();
		this.lockHelper = new EntityLockHelper(factory.getInfoLockManager(),
				sessionHolder, url);
		this.database = factory.getDatabase();
		this.security = factory.getSecurity();
		this.propertyHelper = new PropertyHelper(factory.getInfoProperties(),
				sessionHolder, url);

		this.metadata = docMeta;
		this.loadedMetadata = true;
		this.setId(metadata.getId());
		this.setExists(true);

	}

	@Override
	protected WebdavFactory getFactory() {
		return factory;
	}

	@Override
	public boolean hasBody() throws WebdavProtocolException {
		try {
			loadMetadata();
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					500);
		}
		return metadata.getFileSize() > 0;
	}

	@Override
	protected List<WebdavProperty> internalGetAllProps()
			throws WebdavProtocolException {
		try {
			return propertyHelper.getAllProps();
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					500);
		}
	}

	@Override
	protected WebdavProperty internalGetProperty(final String namespace,
			final String name) throws WebdavProtocolException {
		try {
			return propertyHelper.getProperty(namespace, name);
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					500, e);
		}
	}

	@Override
	protected void internalPutProperty(final WebdavProperty prop) {
		propertyHelper.putProperty(prop);
	}

	@Override
	protected void internalRemoveProperty(final String namespace,
			final String name) {
		propertyHelper.removeProperty(namespace, name);
	}

	@Override
	protected boolean isset(final Property p) {
		/*
		 * if(p.getId() == Protocol.GETCONTENTLANGUAGE) { return false; }
		 */
		return !propertyHelper.isRemoved(new WebdavProperty(p.getNamespace(), p
				.getName()));
	}

	@Override
	public void setCreationDate(final Date date) {
		metadata.setCreationDate(date);
		markChanged();
		markSet(Metadata.CREATION_DATE_LITERAL);
	}

	@Override
	public void create() throws WebdavProtocolException {
		if (exists) {
			throw WebdavProtocolException.Code.DIRECTORY_ALREADY_EXISTS.create(
					getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
		save();
		exists = true;
		try {
			factory.created(this);
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					500, e);
		}
	}

	@Override
	public void delete() throws WebdavProtocolException {
		if (exists) {
			try {
				lockHelper.deleteLocks();
				propertyHelper.deleteProperties();
				deleteMetadata();
				exists = false;
				factory.removed(this);
			} catch (final OXException x) {
				if (com.openexchange.exception.Category.CATEGORY_PERMISSION_DENIED == x
						.getCategory()) {
					throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), HttpServletResponse.SC_UNAUTHORIZED, x);
				}
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(
						getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						x);
			} catch (final Exception x) {
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(
						getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						x);
			}
		}
	}

	@Override
	public boolean exists() {
		return exists;
	}

	@Override
	public InputStream getBody() throws WebdavProtocolException {
		final ServerSession session = getSession();
		try {
			return database.getDocument(id, InfostoreFacade.CURRENT_VERSION, session);
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		} catch (final Exception e) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	public String getContentType() throws WebdavProtocolException {
		loadMetadata();
		return metadata.getFileMIMEType();
	}

	@Override
	public Date getCreationDate() throws WebdavProtocolException {
		loadMetadata();
		return metadata.getCreationDate();
	}

	@Override
	public String getDisplayName() throws WebdavProtocolException {
		loadMetadata();
		return metadata.getFileName();
	}

	@Override
	public String getETag() {
		if (!exists && !existsInDB) {
			/*
			 * try { dumpMetadataToDB(); } catch (Exception e) { throw new
			 * OXException(e.getMessage(), e, getUrl(),
			 * HttpServletResponse.SC_INTERNAL_SERVER_ERROR); }
			 */
			return null;
		}
		return String.format(
				"http://www.open-xchange.com/webdav/etags/%d-%d-%d",
				Integer.valueOf(getSession().getContext().getContextId()),
				Integer.valueOf(metadata.getId()),
				Integer.valueOf(metadata.getVersion()));
	}

	@Override
	public String getLanguage() {
		return null;
	}

	@Override
	public Date getLastModified() throws WebdavProtocolException {
		loadMetadata();
		return metadata.getLastModified();
	}

	@Override
	public Long getLength() throws WebdavProtocolException {
		loadMetadata();
		return Long.valueOf(metadata.getFileSize());
	}

	@Override
	public WebdavLock getLock(final String token)
			throws WebdavProtocolException {
		WebdavLock lock;
		try {
			lock = lockHelper.getLock(token);
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
		if (lock != null) {
			return lock;
		}
		return findParentLock(token);
	}

	@Override
	public List<WebdavLock> getLocks() throws WebdavProtocolException {
		final List<WebdavLock> lockList = getOwnLocks();
		addParentLocks(lockList);
		return lockList;
	}

	@Override
	public WebdavLock getOwnLock(final String token)
			throws WebdavProtocolException {
		try {
			return injectOwner(lockHelper.getLock(token));
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
		try {
			return injectOwner(lockHelper.getAllLocks());
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	private WebdavLock injectOwner(final WebdavLock lock)
			throws WebdavProtocolException {
		if (lock.getOwner() == null || "".equals(lock.getOwner())) {
			loadMetadata();
			final int userId = metadata.getModifiedBy();
			try {
				final User user = UserStorage.getInstance().getUser(userId,
						getSession().getContext());
				String displayName = user.getDisplayName();
				if (displayName == null) {
					displayName = user.getMail();
				}
				if (displayName == null) {
					displayName = Integer.toString(userId);
				}
				lock.setOwner(displayName);

			} catch (final OXException e) {
				// Ignore, if lookup fails set no owner.
			}
		}
		return lock;
	}

	private List<WebdavLock> injectOwner(final List<WebdavLock> allLocks)
			throws WebdavProtocolException {
		for (final WebdavLock webdavLock : allLocks) {
			injectOwner(webdavLock);
		}
		return allLocks;
	}

	@Override
	public String getSource() {
		return null;
	}

	@Override
	public WebdavPath getUrl() {
		return url;
	}

	@Override
	public void lock(final WebdavLock lock) throws WebdavProtocolException {
		try {
			if (!exists) {
				new InfostoreLockNullResource(this, factory).lock(lock);
				factory.invalidate(getUrl(), getId(), Type.RESOURCE);
				return;
			}
			lockHelper.addLock(lock);

			touch();
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	public void unlock(final String token) throws WebdavProtocolException {
		lockHelper.removeLock(token);
		try {
			lockHelper.dumpLocksToDB();
			touch();
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	public void save() throws WebdavProtocolException {
		try {
			dumpMetadataToDB();
			if (propertyHelper.mustWrite()) {
				final EffectiveInfostorePermission perm = security.getInfostorePermission(getSession(), getId());
				if (!perm.canWriteObject()) {
					throw WebdavProtocolException.Code.NO_WRITE_PERMISSION.create(getUrl(), HttpServletResponse.SC_UNAUTHORIZED);
				}
			}
			propertyHelper.dumpPropertiesToDB();
			lockHelper.dumpLocksToDB();
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		} catch (final Exception e) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	public void setContentType(final String type) {
		metadata.setFileMIMEType(type);
		markChanged();
		markSet(Metadata.FILE_MIMETYPE_LITERAL);
	}

	@Override
	public void setDisplayName(final String displayName) {
		metadata.setFileName(displayName);
		markChanged();
		markSet(Metadata.FILENAME_LITERAL);
	}

	@Override
	public void setLength(final Long length) {
		metadata.setFileSize(length.longValue());
		markChanged();
		markSet(Metadata.FILE_SIZE_LITERAL);
	}

	@Override
	public void setSource(final String source) {
		// IGNORE

	}

	@Override
	public void setLanguage(final String language) {
		// IGNORE

	}

	public void setId(final int id) {
		this.id = id;
		propertyHelper.setId(id);
		lockHelper.setId(id);
	}

	public void setExists(final boolean b) {
		exists = b;
	}

	//

	@Override
	public WebdavResource move(final WebdavPath dest, final boolean noroot,
			final boolean overwrite) throws WebdavProtocolException {
		WebdavResource res;
		try {
			res = factory.resolveResource(dest);
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
		if (res.exists()) {
			if (!overwrite) {
				throw WebdavProtocolException.Code.FILE_ALREADY_EXISTS.create(
						getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED,
						dest);
			}
			res.delete();
		}
		final WebdavPath parent = dest.parent();
		final String name = dest.name();

		FolderCollection coll;
		try {
			coll = (FolderCollection) factory.resolveCollection(parent);
			if (!coll.exists()) {
				throw WebdavProtocolException.Code.FOLDER_NOT_FOUND.create(
						getUrl(), HttpServletResponse.SC_CONFLICT, parent);
			}
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}

		loadMetadata();
		metadata.setTitle(name);
		metadata.setFileName(name);
		metadata.setFolderId(coll.getId());

		metadataChanged = true;
		setMetadata.add(Metadata.TITLE_LITERAL);
		setMetadata.add(Metadata.FILENAME_LITERAL);
		setMetadata.add(Metadata.FOLDER_ID_LITERAL);

		factory.invalidate(url, id, Type.RESOURCE);
		factory.invalidate(dest, id, Type.RESOURCE);
		url = dest;
		save();
		try {
			lockHelper.deleteLocks();
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
		return this;
	}

	@Override
	public WebdavResource copy(final WebdavPath dest, final boolean noroot,
			final boolean overwrite) throws WebdavProtocolException {

		try {
			final WebdavPath parent = dest.parent();
			final String name = dest.name();

			final FolderCollection coll = (FolderCollection) factory
					.resolveCollection(parent);
			if (!coll.exists()) {
				throw WebdavProtocolException.Code.FOLDER_NOT_FOUND.create(
						getUrl(), HttpServletResponse.SC_CONFLICT, parent);
			}

			final DocumentMetadataResource copy = (DocumentMetadataResource) factory
					.resolveResource(dest);
			if (copy.exists()) {
				if (!overwrite) {
					throw WebdavProtocolException.Code.FILE_ALREADY_EXISTS
							.create(getUrl(),
									HttpServletResponse.SC_PRECONDITION_FAILED,
									dest);
				}
				copy.delete();
			}
			copyMetadata(copy);
			initDest(copy, name, coll.getId());
			copy.url = dest;
			copyProperties(copy);
			copyBody(copy);

			copy.create();

			factory.invalidate(dest, copy.getId(), Type.RESOURCE);
			try {
				lockHelper.deleteLocks();
			} catch (final OXException e) {
				if (e instanceof WebdavProtocolException) {
					throw (WebdavProtocolException) e;
				}
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(
						getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						e);
			}
			return copy;
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}

	}

	private void initDest(final DocumentMetadataResource copy,
			final String name, final int parentId) {

		copy.metadata.setTitle(name);
		copy.metadata.setFileName(name);
		copy.metadata.setFolderId(parentId);

	}

	private void copyMetadata(final DocumentMetadataResource copy)
			throws WebdavProtocolException {
		loadMetadata();
		copy.metadata = new DocumentMetadataImpl(metadata);
		copy.metadata.setFilestoreLocation(null); // No file attachment in
													// original version
		copy.metadata.setId(InfostoreFacade.NEW);
		copy.metadataChanged = true;
		copy.setMetadata.addAll(Metadata.VALUES);
	}

	private void copyProperties(final DocumentMetadataResource copy)
			throws WebdavProtocolException {
		for (final WebdavProperty prop : internalGetAllProps()) {
			copy.putProperty(prop);
		}
	}

	private void copyBody(final DocumentMetadataResource copy)
			throws WebdavProtocolException {
		final InputStream in = getBody();
		if (in != null) {
			try {
                copy.putBody(in);
            } finally {
                Streams.close(in);
            }
		}
	}

	private void loadMetadata() throws WebdavProtocolException {
		if (!exists) {
			return;
		}
		if (loadedMetadata) {
			return;
		}
		loadedMetadata = true;
		final Set<Metadata> toLoad = new HashSet<Metadata>(Metadata.VALUES);
		toLoad.removeAll(setMetadata);
		final ServerSession session = getSession();

		try {
			final DocumentMetadata metadata = database.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, session);
			final SetSwitch set = new SetSwitch(this.metadata);
			final GetSwitch get = new GetSwitch(metadata);

			for (final Metadata m : toLoad) {
				set.setValue(m.doSwitch(get));
				m.doSwitch(set);
			}
		} catch (final OXException x) {
			if (CATEGORY_PERMISSION_DENIED == x.getCategory()) {
				metadata.setId(getId());
				metadata.setFolderId(((OXWebdavResource) parent()).getId());
				initNameAndTitle();
			} else {
				if (x instanceof WebdavProtocolException) {
					throw (WebdavProtocolException) x;
				}
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(
						getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						x);
			}
		} catch (final Exception x) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, x);
		}
	}

	private void markSet(final Metadata metadata) {
		setMetadata.add(metadata);
	}

	private void markChanged() {
		metadataChanged = true;
	}

	@Override
	public void putBody(final InputStream body, final boolean guessSize)
			throws WebdavProtocolException {
		try {
			if (!exists && !existsInDB) {
				// CREATE WITH FILE
				try {
					dumpMetadataToDB(body, guessSize);
				} catch (WebdavProtocolException x) {
					throw x;
				} catch (final OXException x) {
					if (CATEGORY_PERMISSION_DENIED == x.getCategory()) {
						throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, HttpServletResponse.SC_UNAUTHORIZED);
					}
					throw WebdavProtocolException.Code.GENERAL_ERROR.create(
							url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, x, new Object[0]);
				} catch (final Exception x) {
					throw WebdavProtocolException.Code.GENERAL_ERROR.create(
							url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, x, new Object[0]);
				}
			} else {
				// UPDATE
				final ServerSession session = getSession();
				try {
					database.startTransaction();
					loadMetadata();
					if (guessSize) {
						metadata.setFileSize(0);
					}
					database.saveDocument(metadata, body, Long.MAX_VALUE,
							session);
					database.commit();
				} catch (final Exception x) {
					try {
						database.rollback();
					} catch (final OXException e) {
						LOG.error("Couldn't rollback transaction. Run the recovery tool.");
					}
					if (x instanceof OXException) {
						final OXException iStoreException = (OXException) x;
						if (EnumComponent.INFOSTORE.getAbbreviation().equals(
								iStoreException.getPrefix())) {
							if (415 == iStoreException.getCode()) {
								throw WebdavProtocolException.Code.GENERAL_ERROR
										.create(getUrl(), Protocol.SC_LOCKED);
							}
							if (CATEGORY_PERMISSION_DENIED == iStoreException.getCategory()) {
								throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, HttpServletResponse.SC_UNAUTHORIZED);
							}
						}
					}
					throw WebdavProtocolException.Code.GENERAL_ERROR.create(
							url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							x);
				} finally {
					try {
						database.finish();
					} catch (final OXException e) {
						LOG.error("Couldn't finish transaction: ", e);
					}
				}
			}
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	private void dumpMetadataToDB(final InputStream fileData,
			final boolean guessSize) throws WebdavProtocolException,
			OXException, TransactionException {
		if ((exists || existsInDB) && !metadataChanged) {
			return;
		}
		FolderCollection parent = null;
		try {
			parent = (FolderCollection) parent();
			if (!parent.exists()) {
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(
						getUrl(), HttpServletResponse.SC_CONFLICT);
			} else if (parent.isRoot()) {
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), HttpServletResponse.SC_UNAUTHORIZED);
			}
            EffectivePermission permission = parent.getEffectivePermission();
            if (null != permission && (false == permission.canCreateObjects() || false == permission.canWriteOwnObjects())) {
                // require "write own" permissions, too, when creating objects via WebDAV (#29950 / SCR-1997)
                throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), HttpServletResponse.SC_UNAUTHORIZED);
            }
		} catch (final ClassCastException x) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_CONFLICT);
		}

		initNameAndTitle();
		if (fileData != null && guessSize) {
			metadata.setFileSize(0);
		}

		final ServerSession session = getSession();
		metadata.setFolderId(parent.getId());
		if (!exists && !existsInDB) {
			metadata.setVersion(InfostoreFacade.NEW);
			metadata.setId(InfostoreFacade.NEW);

			database.startTransaction();
			try {
				if (fileData == null) {
					database.saveDocumentMetadata(metadata,
							InfostoreFacade.NEW, session);
				} else {
					database.saveDocument(metadata, fileData,
							InfostoreFacade.NEW, session);
				}
				database.commit();
				setId(metadata.getId());
			} catch (final OXException x) {
				try {
					database.rollback();
				} catch (final OXException x2) {
					LOG.error("Couldn't roll back: ", x2);
				}
				throw x;
			} finally {
				database.finish();
			}
		} else {
			database.startTransaction();
			if (setMetadata.contains(Metadata.FILENAME_LITERAL)) {
				metadata.setTitle(metadata.getFileName());
				setMetadata.add(Metadata.TITLE_LITERAL);
			} // FIXME Detonator Pattern
			try {
				database.saveDocumentMetadata(metadata, Long.MAX_VALUE,
						setMetadata.toArray(new Metadata[setMetadata.size()]),
						session);
				database.commit();
			} catch (final OXException x) {
				try {
					database.rollback();
				} catch (final OXException x2) {
					LOG.error("Can't roll back", x2);
				}
				throw x;
			} finally {
				database.finish();
			}
		}
		existsInDB = true;
		setMetadata.clear();
		metadataChanged = false;
	}

	private void touch() throws WebdavProtocolException {
		try {
			if (!existsInDB && !exists) {
				return;
			}
			database.touch(getId(), getSession());
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	private void initNameAndTitle() {
		if (metadata.getFileName() == null
				|| metadata.getFileName().trim().length() == 0) {
			// if(url.contains("/"))
			metadata.setFileName(url.name());
		}
		metadata.setTitle(metadata.getFileName());
	}

	private void dumpMetadataToDB() throws WebdavProtocolException,
			OXException, TransactionException {
		dumpMetadataToDB(null, false);
	}

	private void deleteMetadata() throws OXException {
		final ServerSession session = getSession();
		database.startTransaction();
		try {
		    DocumentMetadata document = database.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, session);
			List<IDTuple> nd = database.removeDocument(
			    Collections.<IDTuple>singletonList(
			        new IDTuple(
			            Long.toString(document.getFolderId()),
			            Integer.toString(document.getId())
			        )
			    ), Long.MAX_VALUE, session);
			if (nd.size() > 0) {
				database.rollback();
				throw InfostoreExceptionCodes.DELETE_FAILED
						.create(Integer.parseInt(nd.get(0).getId()));
			}
			database.commit();
		} catch (final OXException x) {
			database.rollback();
			throw x;
		} finally {
			database.finish();
		}
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getParentId() throws WebdavProtocolException {
		if (metadata == null) {
			loadMetadata();
		}
		return (int) metadata.getFolderId();
	}

	@Override
	public void removedParent() throws WebdavProtocolException {
		try {
			exists = false;
			factory.removed(this);
		} catch (final OXException e) {
			if (e instanceof WebdavProtocolException) {
				throw (WebdavProtocolException) e;
			}
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	public void transferLock(final WebdavLock lock)
			throws WebdavProtocolException {
		try {
			lockHelper.transferLock(lock);
		} catch (final OXException e) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " :" + id;
	}

	private ServerSession getSession() {
		return ServerSessionAdapter.valueOf(sessionHolder.getSessionObject(), sessionHolder.getContext());
	}

	@Override
	protected SpecialSetSwitch getSetSwitch(String value) {
		return new InfostoreSetSwitch(value);
	}

	private class InfostoreSetSwitch extends AbstractResource.SpecialSetSwitch {

		public InfostoreSetSwitch(String value) {
			super(value);
		}

		@Override
		public Object lastModified() throws WebdavProtocolException {
			try {
				loadMetadata();
				long parsed = Long.parseLong(value);
				metadata.setLastModified(new Date(parsed));
				markChanged();
				markSet(Metadata.LAST_MODIFIED_LITERAL);
			} catch (NumberFormatException x) {
				// IGNORE
			}
			return Boolean.TRUE;
		}

	}
}
