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

package com.openexchange.groupware.infostore.facade.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.event.impl.EventClient;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreException;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.database.impl.*;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.validation.InvalidCharactersValidator;
import com.openexchange.groupware.infostore.validation.ValidationChain;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.Lock;
import com.openexchange.groupware.infostore.webdav.LockManager;
import com.openexchange.groupware.infostore.webdav.LockManager.Scope;
import com.openexchange.groupware.infostore.webdav.LockManager.Type;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.results.TimedResultImpl;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBProviderUser;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.groupware.tx.ReuseReadConProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.SaveFileWithQuotaAction;
import com.openexchange.tools.iterator.CombinedSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * DatabaseImpl
 * 
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin
 *         Otterbach</a>
 */

@OXExceptionSource(classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_FACADE_IMPL_INFOSTOREFACADEIMPL, component = Component.INFOSTORE)
public class InfostoreFacadeImpl extends DBService implements InfostoreFacade,
		DBProviderUser {

	private static final ValidationChain VALIDATION = new ValidationChain();
	static {
		VALIDATION.add(new InvalidCharactersValidator());
		// Add more infostore validators here, as needed
	}

	private static final Log LOG = LogFactory.getLog(InfostoreFacadeImpl.class);
	private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(InfostoreFacadeImpl.class);

	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(
			InfostoreFacadeImpl.class);

	public static final InfostoreQueryCatalog QUERIES = new InfostoreQueryCatalog();

	private final DatabaseImpl db = new DatabaseImpl();

	private InfostoreSecurity security = new InfostoreSecurityImpl();

	private final EntityLockManager lockManager = new EntityLockManagerImpl(
			"infostore_lock");

	private final ThreadLocal<List<String>> fileIdRemoveList = new ThreadLocal<List<String>>();

	private final ThreadLocal<Context> ctxHolder = new ThreadLocal<Context>();

	public InfostoreFacadeImpl() {
		super();
	}

	public InfostoreFacadeImpl(final DBProvider provider) {
		setProvider(provider);
	}

    public void setSecurity(InfostoreSecurity security) {
        this.security = security;
        if(null != getProvider()) {
            setProvider(getProvider());
        }
    }

    public boolean exists(final int id, final int version, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		try {
			return security.getInfostorePermission(id, ctx, user, userConfig).canReadObject();
		} catch (InfostoreException x) {
			if(x.getDetailNumber() == Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DATABASE_IMPL_INFOSTORESECURITYIMPL*100) {
				return false;
			}
			throw x;
		}
	}

	@OXThrowsMultiple(
			category = {Category.USER_INPUT, Category.USER_INPUT},
			desc = {"The User does not have read permissions on the requested Infoitem. ", "The document could not be loaded because it doesn't exist."},
			exceptionId = {0,38},
			msg = {"You do not have sufficient read permissions.", "The document you requested doesn't exist."}
	)
	public DocumentMetadata getDocumentMetadata(final int id, final int version,
			final Context ctx, final User user, final UserConfiguration userConfig)
			throws OXException {
		final EffectiveInfostorePermission infoPerm = security
				.getInfostorePermission(id, ctx, user, userConfig);

		if (!infoPerm.canReadObject()) {
			throw EXCEPTIONS.create(0);
		}
		
		return addLocked(load(id,version,ctx), ctx, user, userConfig);
	}

	private DocumentMetadata load(final int id, final int version, final Context ctx) throws OXException {
		final InfostoreIterator iter = InfostoreIterator.loadDocumentIterator(id, version, getProvider(), ctx);
		if(!iter.hasNext()) {
			throw EXCEPTIONS.create(38);
		}
		DocumentMetadata dm;
		try {
			dm = iter.next();
			iter.close();
		} catch (final SearchIteratorException e) {
			throw new InfostoreException(e);
		}
		return dm;
	}

	public void saveDocumentMetadata(final DocumentMetadata document,
			final long sequenceNumber, final ServerSession sessionObj) throws OXException {
		saveDocument(document, null, sequenceNumber, sessionObj);
	}

	public void saveDocumentMetadata(final DocumentMetadata document,
			final long sequenceNumber, final Metadata[] modifiedColumns,
			final ServerSession sessionObj) throws OXException {
		saveDocument(document, null, sequenceNumber, modifiedColumns,
				sessionObj);
	}

	@OXThrowsMultiple(
			category = {Category.USER_INPUT, Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.SUBSYSTEM_OR_SERVICE_DOWN},
			desc = {"The User does not have read permissions on the requested Infoitem. ", "The file store couldn't be reached and is probably down.", "The file could not be found in the file store. This means either that the file store was not available or that database and file store are inconsistent. Run the recovery tool."},
			exceptionId = {1,39,40},
			msg = {"You do not have sufficient read permissions.","The file store could not be reched", "The file could not be retrieved."})
	public InputStream getDocument(final int id, final int version, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		final EffectiveInfostorePermission infoPerm = security
				.getInfostorePermission(id, ctx, user, userConfig);
		if (!infoPerm.canReadObject()) {
			throw EXCEPTIONS.create(1);
		}
		final DocumentMetadata dm = load(id, version, ctx);
		FileStorage fs = null;
		try {
			fs = getFileStorage(ctx);
		} catch (final FilestoreException e) {
			throw new InfostoreException(e);
		} catch (final FileStorageException e) {
			throw new InfostoreException(e);
		}
		try {
			return fs.getFile(dm.getFilestoreLocation());
		} catch (final FileStorageException e) {
			throw new InfostoreException(e);
		}
	}

	@OXThrows(category = Category.USER_INPUT, desc = "The user does not have sufficient write permissions to lock this infoitem.", exceptionId = 18, msg = "You need write permissions to lock a document.")
	public void lock(final int id, final long diff, final ServerSession sessionObj)
			throws OXException {
		final EffectiveInfostorePermission infoPerm = security
				.getInfostorePermission(id, sessionObj.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj));
		if (!infoPerm.canWriteObject()) {
			throw EXCEPTIONS.create(18);
		}
		checkWriteLock(id, sessionObj);
		long timeout = 0;
		if (timeout == -1) {
			timeout = LockManager.INFINITE;
		} else {
			timeout = System.currentTimeMillis() + diff;
		}
		lockManager.lock(id, timeout, Scope.EXCLUSIVE, Type.WRITE, sessionObj
				.getUserlogin(), sessionObj.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj));
		touch(id, sessionObj);
	}

	@OXThrows(category = Category.USER_INPUT, desc = "The user does not have sufficient write permissions to unlock this infoitem.", exceptionId = 17, msg = "You need write permissions to unlock a document.")
	public void unlock(final int id, final ServerSession sessionObj) throws OXException {
		final EffectiveInfostorePermission infoPerm = security
				.getInfostorePermission(id, sessionObj.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj));
		if (!infoPerm.canWriteObject()) {
			throw EXCEPTIONS.create(17);
		}
		checkMayUnlock(id, sessionObj);
		lockManager.removeAll(id, sessionObj.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj));
		touch(id, sessionObj);
	}

	private void touch(final int id, final ServerSession sessionObj) throws OXException {
		try {
			final DocumentMetadata oldDocument = load(id, CURRENT_VERSION, sessionObj
							.getContext());
			final DocumentMetadata document = new DocumentMetadataImpl(oldDocument);

			document.setLastModified(new Date());
			document.setModifiedBy(sessionObj.getUserId());

			final UpdateDocumentAction updateDocument = new UpdateDocumentAction();
			updateDocument.setContext(sessionObj.getContext());
			updateDocument.setDocuments(Arrays.asList(document));
			updateDocument.setModified(Metadata.LAST_MODIFIED_LITERAL,
					Metadata.MODIFIED_BY_LITERAL);
			updateDocument.setOldDocuments(Arrays.asList(oldDocument));
			updateDocument.setProvider(this);
			updateDocument.setQueryCatalog(QUERIES);
			updateDocument.setTimestamp(oldDocument.getSequenceNumber());

			perform(updateDocument, true);

			final UpdateVersionAction updateVersion = new UpdateVersionAction();
			updateVersion.setContext(sessionObj.getContext());
			updateVersion.setDocuments(Arrays.asList(document));
			updateVersion.setModified(Metadata.LAST_MODIFIED_LITERAL,
					Metadata.MODIFIED_BY_LITERAL);
			updateVersion.setOldDocuments(Arrays.asList(oldDocument));
			updateVersion.setProvider(this);
			updateVersion.setQueryCatalog(QUERIES);
			updateVersion.setTimestamp(oldDocument.getSequenceNumber());

			perform(updateVersion, true);

			final EventClient ec = new EventClient(sessionObj);
			ec.modify(document);
		} catch (final OXException x) {
			throw x;
		} catch (final Exception e) {
			// FIXME Client
			LOG.error("", e);
		}
	}

	@OXThrows(category = Category.INTERNAL_ERROR, desc = "The system couldn't iterate the result dataset. This can have numerous exciting causes.", exceptionId = 13, msg = "Could not iterate result")
	private Delta addLocked(final Delta delta, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		try {
			return new LockDelta(delta, ctx, user, userConfig);
		} catch (final SearchIteratorException e) {
			throw EXCEPTIONS.create(13,e);
		}
	}

	@OXThrows(category = Category.INTERNAL_ERROR, desc = "The system couldn't iterate the result dataset. This can have numerous exciting causes.", exceptionId = 14, msg = "Could not iterate result")
	private TimedResult addLocked(final TimedResult tr, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		try {
			return new LockTimedResult(tr, ctx, user, userConfig);
		} catch (final SearchIteratorException e) {
			throw EXCEPTIONS.create(14);
		}
	}

	private DocumentMetadata addLocked(final DocumentMetadata document, final Context ctx,
			final User user, final UserConfiguration userConfig) throws OXException {
		final List<Lock> locks = lockManager.findLocks(document.getId(), ctx, user,
				userConfig);
		long max = 0;
		for (final Lock l : locks) {
			if (l.getTimeout() > max) {
				max = l.getTimeout();
			}
		}
		document.setLockedUntil(new Date(System.currentTimeMillis() + max));
		return document;
	}

	private SearchIterator<?> lockedUntilIterator(final SearchIterator<?> iter,
			final Context ctx, final User user, final UserConfiguration userConfig)
			throws SearchIteratorException, OXException {
		final List<DocumentMetadata> list = new ArrayList<DocumentMetadata>();
		while (iter.hasNext()) {
			final DocumentMetadata m = (DocumentMetadata) iter.next();
			addLocked(m, ctx, user, userConfig);
			list.add(m);
		}
		return new SearchIteratorAdapter(list.iterator());
	}

	private DocumentMetadata checkWriteLock(final int id, final ServerSession sessionObj)
			throws OXException {
		final DocumentMetadata document = load(id, CURRENT_VERSION,
				sessionObj.getContext());
		checkWriteLock(document, sessionObj);
		return document;
	}

	@OXThrows(category = Category.CONCURRENT_MODIFICATION, desc = "The infoitem was locked by some other user. Only the user that locked the item (the one that modified the entry) can modify a locked infoitem.", exceptionId = 15, msg = "This document is locked.")
	private void checkWriteLock(final DocumentMetadata document,
			final ServerSession sessionObj) throws OXException {
		if (document.getModifiedBy() == sessionObj.getUserId()) {
			return;
		}
		
		if(lockManager.isLocked(document.getId(), sessionObj.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj))) {
			throw EXCEPTIONS.create(15);			
		}
		
	}

	@OXThrows(category = Category.CONCURRENT_MODIFICATION, desc = "The infoitem was locked by some other user. Only the user that locked the item and the creator of the item can unlock a locked infoitem.", exceptionId = 16, msg = "You cannot unlock this document.")
	private void checkMayUnlock(final int id, final ServerSession sessionObj)
			throws OXException {
		final DocumentMetadata document = load(id, CURRENT_VERSION,
				sessionObj.getContext());
		if (document.getCreatedBy() == sessionObj.getUserId()
				|| document.getModifiedBy() == sessionObj.getUserId()) {
			return;
		}
		final List<Lock> locks = lockManager.findLocks(id, sessionObj.getContext(),
				getUser(sessionObj), getUserConfiguration(sessionObj));
		if (locks.size() > 0) {
			throw EXCEPTIONS.create(16);
		}
	}

	@OXThrowsMultiple(category = { Category.USER_INPUT,
			Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.INTERNAL_ERROR }, desc = {
			"The user may not create objects in the given folder. ",
			"The file store couldn't be reached.",
			"The IDGenerator threw an SQL Exception look at that one to find out what's wrong." }, exceptionId = {
			2, 19, 20 }, msg = {
			"You do not have sufficient permissions to create objects in this folder.",
			"The file store could not be reached.", "Could not generate new ID." })
	public void saveDocument(final DocumentMetadata document, final InputStream data,
			final long sequenceNumber, final ServerSession sessionObj) throws OXException {
		security.checkFolderId(document.getFolderId(), sessionObj.getContext());
		if (document.getId() == InfostoreFacade.NEW) {
			final EffectivePermission isperm = security.getFolderPermission(document
					.getFolderId(), sessionObj.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj));
			if (!isperm.canCreateObjects()) {
				throw EXCEPTIONS.create(2);
			}
			setDefaults(document);
			checkUniqueFilename(document.getFileName(), document.getFolderId(), document.getId(), sessionObj.getContext());
			
			VALIDATION.validate(document);
			
			Connection writeCon = null;
			try {
				startDBTransaction();
				writeCon = getWriteConnection(sessionObj.getContext());
				document.setId(getId(sessionObj.getContext(), writeCon));
				commitDBTransaction();
			} catch (final SQLException e) {
				throw EXCEPTIONS.create(20, e);
			} finally {
				releaseWriteConnection(sessionObj.getContext(), writeCon);
				finishDBTransaction();
			}
			document.setCreationDate(new Date(System.currentTimeMillis()));
			document.setLastModified(document.getCreationDate());
			document.setCreatedBy(sessionObj.getUserId());
			document.setModifiedBy(sessionObj.getUserId());

			// db.createDocument(document, data, sessionObj.getContext(),
			// sessionObj.getUserObject(), getUserConfiguration(sessionObj));

			if (null != data) {
				document.setVersion(1);
			} else {
				document.setVersion(0);
			}

			final CreateDocumentAction createAction = new CreateDocumentAction();
			createAction.setContext(sessionObj.getContext());
			createAction.setDocuments(Arrays.asList(document));
			createAction.setProvider(this);
			createAction.setQueryCatalog(QUERIES);

			try {
				perform(createAction, true);
			} catch (final OXException x) {
				throw x;
			} catch (final AbstractOXException e1) {
				throw new InfostoreException(e1);
			}

			final DocumentMetadata version0 = new DocumentMetadataImpl(document);
			version0.setFileName(null);
			version0.setFileSize(0);
			version0.setFileMD5Sum(null);
			version0.setFileMIMEType(null);
			version0.setVersion(0);

			CreateVersionAction createVersionAction = new CreateVersionAction();
			createVersionAction.setContext(sessionObj.getContext());
			createVersionAction.setDocuments(Arrays.asList(version0));
			createVersionAction.setProvider(this);
			createVersionAction.setQueryCatalog(QUERIES);

			try {
				perform(createVersionAction, true);
			} catch (final OXException x) {
				throw x;
			} catch (final AbstractOXException e1) {
				throw new InfostoreException(e1);
			}

			if (data != null) {
				final SaveFileWithQuotaAction saveFile = new SaveFileWithQuotaAction();
				try {
					final QuotaFileStorage qfs = (QuotaFileStorage) getFileStorage(sessionObj
							.getContext());
					saveFile.setStorage(qfs);
					saveFile.setSizeHint(document.getFileSize());
					saveFile.setIn(data);

					perform(saveFile, false);

					document.setVersion(1);
					document.setFilestoreLocation(saveFile.getId());
					if (document.getFileSize() == 0) {
						document.setFileSize(qfs.getFileSize(saveFile.getId()));
					}

					createVersionAction = new CreateVersionAction();
					createVersionAction.setContext(sessionObj.getContext());
					createVersionAction.setDocuments(Arrays.asList(document));
					createVersionAction.setProvider(this);
					createVersionAction.setQueryCatalog(QUERIES);

					perform(createVersionAction, true);

				} catch (final FileStorageException e) {
					throw new InfostoreException(e);
				} catch (final ContextException e) {
					throw new InfostoreException(e);
				} catch (final OXException x) {
					throw x;
				} catch (final AbstractOXException e) {
					throw new InfostoreException(e);
				}

			}

			final EventClient ec = new EventClient(sessionObj);
			try {
				ec.create(document);
			} catch (final Exception e) {
				LOG.error("", e);
			}

		} else {
			saveDocument(document, data, sequenceNumber, nonNull(document),
					sessionObj);
		}
	}

	private void setDefaults(final DocumentMetadata document) {
		if(document.getTitle() == null || "".equals(document.getTitle())) {
			document.setTitle(document.getFileName());
		}
	}

	// FIXME Move 2 query builder
	private int getNextVersionNumberForInfostoreObject(final int cid,
			final int infostore_id, final Connection con) throws SQLException {
		int retval = 0;

		PreparedStatement stmt = con
				.prepareStatement("SELECT MAX(version_number) FROM infostore_document WHERE cid=? AND infostore_id=?");
		stmt.setInt(1, cid);
		stmt.setInt(2, infostore_id);
		ResultSet result = stmt.executeQuery();
		if (result.next()) {
			retval = result.getInt(1);
		}
		result.close();
		stmt.close();

		stmt = con
				.prepareStatement("SELECT MAX(version_number) FROM del_infostore_document WHERE cid=? AND infostore_id=?");
		stmt.setInt(1, cid);
		stmt.setInt(2, infostore_id);
		result = stmt.executeQuery();
		if (result.next()) {
			final int delVersion = result.getInt(1);
			if (delVersion > retval) {
				retval = delVersion;
			}
		}
		result.close();
		stmt.close();

		return retval + 1;
	}
	
	@OXThrows(
			category=Category.USER_INPUT,
			desc="To remain consistent in WebDAV no two current versions in a given folder may contain a file with the same filename. The user must either choose a different filename, or switch the other file to a version with a different filename.", 
			exceptionId=41,
			msg="Files attached to InfoStore items must have unique names. Filename: %s. The other document with this file name is %s."
	)
	private void checkUniqueFilename(final String filename, final long folderId, final int id, final Context ctx) throws OXException  {
		if(null == filename) {
			return;
		}
		if("".equals(filename.trim())) {
			return;
		}
		InfostoreIterator iter = null;
		try {
			iter = InfostoreIterator.documentsByFilename(folderId, filename, new Metadata[]{Metadata.ID_LITERAL, Metadata.TITLE_LITERAL}, getProvider(), ctx);
			while(iter.hasNext()) {
				final DocumentMetadata dm = iter.next();
				if(dm.getId() != id) {
					throw EXCEPTIONS.create(41,filename, dm.getTitle());
				}
			}
		} catch (final SearchIteratorException e) {
			throw new InfostoreException(e);
		} finally {
			if (iter != null) {
				try {
					iter.close();
				} catch (final SearchIteratorException e) {
					throw new InfostoreException(e);
				}
			}
		}
		
	}

	protected FileStorage getFileStorage(final Context ctx) throws
			FilestoreException, FileStorageException {
		return FileStorage.getInstance(FilestoreStorage.createURI(ctx),ctx, this.getProvider());
	}

	private Metadata[] nonNull(final DocumentMetadata document) {
		final List<Metadata> nonNull = new ArrayList<Metadata>();
		final GetSwitch get = new GetSwitch(document);
		for (final Metadata metadata : Metadata.HTTPAPI_VALUES) {
			if (null != metadata.doSwitch(get)) {
				nonNull.add(metadata);
			}
		}
		return nonNull.toArray(new Metadata[nonNull.size()]);
	}

	@OXThrowsMultiple(category = { Category.USER_INPUT, Category.USER_INPUT, Category.USER_INPUT }, desc = {
			"The user doesn't have the required write permissions to update the infoitem.",
			"The user isn't allowed to create objects in the target folder when moving an infoitem.",
			"Need delete permissions in original folder to move an item"}, exceptionId = {
			3, 4, 21 }, msg = { "You are not allowed to update this item.",
			"You are not allowed to create objects in the target folder.",
			"You are not allowed to delete objects in the source folder, so this document cannot be mo Tved."})
	public void saveDocument(final DocumentMetadata document, final InputStream data,
			final long sequenceNumber, Metadata[] modifiedColumns,
			final ServerSession sessionObj) throws OXException {
		try {
			final EffectiveInfostorePermission infoPerm = security
					.getInfostorePermission(document.getId(), sessionObj
							.getContext(), getUser(sessionObj),
							getUserConfiguration(sessionObj));
			if (!infoPerm.canWriteObject()) {
				throw EXCEPTIONS.create(3);
			}
			if ((Arrays.asList(modifiedColumns)
					.contains(Metadata.FOLDER_ID_LITERAL))
					&& (document.getFolderId() != -1) && infoPerm.getObject().getFolderId() != document.getFolderId()) {
				security.checkFolderId(document.getFolderId(), sessionObj
						.getContext());
				final EffectivePermission isperm = security.getFolderPermission(
						document.getFolderId(), sessionObj.getContext(),
						getUser(sessionObj), getUserConfiguration(sessionObj));
				if (!(isperm.canCreateObjects())) {
					throw EXCEPTIONS.create(4);
				}
				
				if(!infoPerm.canDeleteObject()) {
					throw EXCEPTIONS.create(21);
				}
			}
			
			final DocumentMetadata oldDocument = checkWriteLock(document.getId(), sessionObj);
			
			
			
			document.setLastModified(new Date());
			document.setModifiedBy(sessionObj.getUserId());
			
			
			VALIDATION.validate(document);
			
			
			// db.updateDocument(document, data, sequenceNumber,
			// modifiedColumns, sessionObj.getContext(),
			// sessionObj.getUserObject(), getUserConfiguration(sessionObj));

			// db.createDocument(document, data, sessionObj.getContext(),
			// sessionObj.getUserObject(), getUserConfiguration(sessionObj));
			
			final Set<Metadata> updatedCols = new HashSet<Metadata>(Arrays
					.asList(modifiedColumns));
			updatedCols.add(Metadata.LAST_MODIFIED_LITERAL);
			updatedCols.add(Metadata.MODIFIED_BY_LITERAL);
			
			if(updatedCols.contains(Metadata.VERSION_LITERAL)) {
				final String fname = load(document.getId(), document.getVersion(), sessionObj.getContext()).getFileName();
				if(fname != null && !fname.equals(oldDocument.getFileName())) {
					checkUniqueFilename(fname, oldDocument.getFolderId(), oldDocument.getId(), sessionObj.getContext());
				}
			}
			
			modifiedColumns = updatedCols.toArray(new Metadata[updatedCols
					.size()]);

			if(document.getFileName() != null && !document.getFileName().equals(oldDocument.getFileName())) {
				checkUniqueFilename(document.getFileName(), oldDocument.getFolderId(), oldDocument.getId(), sessionObj.getContext());
			}
			
			if (data != null) {
				
				final SaveFileWithQuotaAction saveFile = new SaveFileWithQuotaAction();
				try {
					final QuotaFileStorage qfs = (QuotaFileStorage) getFileStorage(sessionObj
							.getContext());
					saveFile.setStorage(qfs);
					saveFile.setSizeHint(document.getFileSize());
					saveFile.setIn(data);
					perform(saveFile, false);
					document.setFilestoreLocation(saveFile.getId());

					if (document.getFileSize() == 0) {
						document.setFileSize(qfs.getFileSize(saveFile.getId()));
					}

					final GetSwitch get = new GetSwitch(oldDocument);
					final SetSwitch set = new SetSwitch(document);
					final Set<Metadata> alreadySet = new HashSet<Metadata>(Arrays
							.asList(modifiedColumns));
					for (final Metadata m : Arrays.asList(
							Metadata.DESCRIPTION_LITERAL,
							Metadata.TITLE_LITERAL, Metadata.URL_LITERAL)) {
						if (alreadySet.contains(m)) {
							continue;
						}
						set.setValue(m.doSwitch(get));
						m.doSwitch(set);
					}

					document.setCreatedBy(sessionObj.getUserId());
					document.setCreationDate(new Date());
					Connection con = null;
					try {
						con = getReadConnection(sessionObj.getContext());
						document
								.setVersion(getNextVersionNumberForInfostoreObject(
										sessionObj.getContext().getContextId(),
										document.getId(), con));
						updatedCols.add(Metadata.VERSION_LITERAL);
					} catch (final SQLException e) {
						LOG.error("SQLException: ", e);
					} finally {
						releaseReadConnection(sessionObj.getContext(), con);
					}
					
					final CreateVersionAction createVersionAction = new CreateVersionAction();
					createVersionAction.setContext(sessionObj.getContext());
					createVersionAction.setDocuments(Arrays.asList(document));
					createVersionAction.setProvider(this);
					createVersionAction.setQueryCatalog(QUERIES);
					
					perform(createVersionAction, true);

				} catch (final FileStorageException e) {
					throw new InfostoreException(e);
				} catch (final ContextException e) {
					throw new InfostoreException(e);
				} catch (final OXException x) {
					throw x;
				} catch (final AbstractOXException e) {
					throw new InfostoreException(e);
				}

			} else if (QUERIES.updateVersion(modifiedColumns)) {
				if (!updatedCols.contains(Metadata.VERSION_LITERAL)) {
					document.setVersion(oldDocument.getVersion());
				}
				final UpdateVersionAction updateVersionAction = new UpdateVersionAction();
				updateVersionAction.setContext(sessionObj.getContext());
				updateVersionAction.setDocuments(Arrays.asList(document));
				updateVersionAction.setOldDocuments(Arrays.asList(oldDocument));
				updateVersionAction.setProvider(this);
				updateVersionAction.setQueryCatalog(QUERIES);
				updateVersionAction.setModified(modifiedColumns);
				updateVersionAction.setTimestamp(sequenceNumber);
				try {
					perform(updateVersionAction, true);
				} catch (final OXException x) {
					throw x;
				} catch (final AbstractOXException e1) {
					throw new InfostoreException(e1);
				}
			}

			modifiedColumns = updatedCols.toArray(new Metadata[updatedCols
					.size()]);
			if (QUERIES.updateDocument(modifiedColumns)) {
				final UpdateDocumentAction updateAction = new UpdateDocumentAction();
				updateAction.setContext(sessionObj.getContext());
				updateAction.setDocuments(Arrays.asList(document));
				updateAction.setOldDocuments(Arrays.asList(oldDocument));
				updateAction.setProvider(this);
				updateAction.setQueryCatalog(QUERIES);
				updateAction.setModified(modifiedColumns);
				updateAction.setTimestamp(sequenceNumber);
				try {
					perform(updateAction, true);
				} catch (final OXException x) {
					throw x;
				} catch (final AbstractOXException e1) {
					throw new InfostoreException(e1);
				}
			}

			final EventClient ec = new EventClient(sessionObj);
			final DocumentMetadataImpl docForEvent = new DocumentMetadataImpl(oldDocument);
			final SetSwitch set = new SetSwitch(docForEvent);
			final GetSwitch get = new GetSwitch(document);
			for(final Metadata metadata : modifiedColumns) {
				set.setValue(metadata.doSwitch(get));
				metadata.doSwitch(set);
			}
			ec.modify(docForEvent);
		} catch (final OXException x) {
			throw x;
		} catch (final Exception e) {
			// FIXME Client
			LOG.error("", e);
		}
	}

	public void removeDocument(final long folderId, final long date,
			final ServerSession sessionObj) throws OXException {
		final DBProvider reuseProvider = new ReuseReadConProvider(getProvider());
		try {
			final List<DocumentMetadata> allVersions = InfostoreIterator
					.allVersionsWhere("infostore.folder_id = " + folderId,
							Metadata.VALUES_ARRAY, reuseProvider,
							sessionObj.getContext()).asList();
			final List<DocumentMetadata> allDocuments = InfostoreIterator
					.allDocumentsWhere("infostore.folder_id = " + folderId,
							Metadata.VALUES_ARRAY, reuseProvider,
							sessionObj.getContext()).asList();
			removeDocuments(allDocuments, allVersions, date, sessionObj, null);
		} catch (final SearchIteratorException x) {
			throw new InfostoreException(x);
		}
	}

	@OXThrows(category = Category.CONCURRENT_MODIFICATION, desc = "Not all infoitems in the given folder could be deleted. This may be due to the infoitems being modified since the last request, or the objects might not even exist anymore or the user doesn't have enough delete permissions on certain objects.", exceptionId = 5, msg = "Could not delete all objects.")
	private void removeDocuments(final List<DocumentMetadata> allDocuments,
			final List<DocumentMetadata> allVersions, final long date,
			final ServerSession sessionObj, final List<DocumentMetadata> rejected)
			throws OXException {
		final List<DocumentMetadata> delDocs = new ArrayList<DocumentMetadata>();
		final List<DocumentMetadata> delVers = new ArrayList<DocumentMetadata>();
		final Set<Integer> rejectedIds = new HashSet<Integer>();

		final Date now = new Date(); // FIXME: Recovery will change lastModified;

		for (final DocumentMetadata m : allDocuments) {
			if (m.getSequenceNumber() > date) {
				if (rejected == null) {
					throw EXCEPTIONS.create(5);
				}
				rejected.add(m);
				rejectedIds.add(Integer.valueOf(m.getId()));
			} else {
				try {
					checkWriteLock(m, sessionObj);
					m.setLastModified(now);
					delDocs.add(m);
				} catch (final InfostoreException x) {
					if (rejected != null) {
						rejected.add(m);
						rejectedIds.add(Integer.valueOf(m.getId()));
					} else {
						throw x;
					}
				}
			}
		}

		for (final DocumentMetadata m : allVersions) {
			if (!rejectedIds.contains(Integer.valueOf(m.getId()))) {
				delVers.add(m);
				m.setLastModified(now);
				removeFile(sessionObj.getContext(), m.getFilestoreLocation());
			}
		}

		// Set<Integer> notDeleted = db.removeDocuments(deleteMe,
		// timed.sequenceNumber(), sessionObj.getContext(),
		// sessionObj.getUserObject(), getUserConfiguration(sessionObj));

		final DeleteVersionAction deleteVersion = new DeleteVersionAction();
		deleteVersion.setContext(sessionObj.getContext());
		deleteVersion.setDocuments(delVers);
		deleteVersion.setProvider(this);
		deleteVersion.setQueryCatalog(QUERIES);

		try {
			perform(deleteVersion, true);
		} catch (final OXException x) {
			throw x;
		} catch (final AbstractOXException e1) {
			throw new InfostoreException(e1);
		}

		final DeleteDocumentAction deleteDocument = new DeleteDocumentAction();
		deleteDocument.setContext(sessionObj.getContext());
		deleteDocument.setDocuments(delDocs);
		deleteDocument.setProvider(this);
		deleteDocument.setQueryCatalog(QUERIES);

		try {
			perform(deleteDocument, true);
		} catch (final OXException x) {
			throw x;
		} catch (final AbstractOXException e1) {
			throw new InfostoreException(e1);
		}

		final EventClient ec = new EventClient(sessionObj);

		for (final DocumentMetadata m : allDocuments) {
			try {
				ec.delete(m);
			} catch (final Exception e) {
				LOG.error("", e);
			}
		}
	}

	@OXThrows(category = Category.SUBSYSTEM_OR_SERVICE_DOWN, desc = "Could not remove file from file store.", exceptionId = 37, msg = "Could not remove file from file store.")
	private void removeFile(final Context context, final String filestoreLocation)
			throws OXException {
		if (filestoreLocation == null) {
			return;
		}
		if (fileIdRemoveList.get() != null) {
			fileIdRemoveList.get().add(filestoreLocation);
			ctxHolder.set(context);
		} else {
			try {
				final QuotaFileStorage qfs = (QuotaFileStorage) getFileStorage(context);
				qfs.deleteFile(filestoreLocation);
			} catch (final FileStorageException x) {
				throw new InfostoreException(x);
			} catch (final FilestoreException e) {
				throw new InfostoreException(e);
			}
		}
	}

	public int[] removeDocument(final int[] id, final long date,
			final ServerSession sessionObj) throws OXException {
		final StringBuilder ids = new StringBuilder().append('(');
		for (final int i : id) {
			ids.append(i).append(',');
		}
		ids.setLength(ids.length() - 1);
		ids.append(')');

		List<DocumentMetadata> allVersions = null;
		List<DocumentMetadata> allDocuments = null;

		final DBProvider reuseProvider = new ReuseReadConProvider(getProvider());
		try {
			allVersions = InfostoreIterator.allVersionsWhere(
					"infostore.id IN " + ids.toString(), Metadata.VALUES_ARRAY,
					reuseProvider, sessionObj.getContext()).asList();
			allDocuments = InfostoreIterator.allDocumentsWhere(
					"infostore.id IN " + ids.toString(), Metadata.VALUES_ARRAY,
					reuseProvider, sessionObj.getContext()).asList();
		} catch (final SearchIteratorException x) {
			throw new InfostoreException(x);
		} catch (final Throwable t) {
			LOG.error("Unexpected Error:", t);
		}

		// Check Permissions

		final List<DocumentMetadata> rejected = new ArrayList<DocumentMetadata>();
		final Set<Integer> rejectedIds = new HashSet<Integer>();

		final Set<Integer> idSet = new HashSet<Integer>();
		for (final int i : id) {
			idSet.add(i);
		}

		final Map<Long, EffectivePermission> perms = new HashMap<Long, EffectivePermission>();

		final List<DocumentMetadata> toDeleteDocs = new ArrayList<DocumentMetadata>();
		final List<DocumentMetadata> toDeleteVersions = new ArrayList<DocumentMetadata>();

		if (allDocuments != null) {
			for (final DocumentMetadata m : allDocuments) {
				idSet.remove(Integer.valueOf(m.getId()));
				EffectivePermission p = perms.get(Long.valueOf(m.getFolderId()));
				if (p == null) {
					p = security.getFolderPermission(m.getFolderId(), sessionObj
							.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj));
					perms.put(Long.valueOf(m.getFolderId()), p);
				}
				final EffectiveInfostorePermission infoPerm = new EffectiveInfostorePermission(
						p, m, getUser(sessionObj));
				if (!infoPerm.canDeleteObject()) {
					rejected.add(m);
					rejectedIds.add(Integer.valueOf(m.getId()));
				} else {
					toDeleteDocs.add(m);
				}
			}
		}

		if (allVersions != null) {
			for (final DocumentMetadata m : allVersions) {
				if (!rejectedIds.contains(Integer.valueOf(m.getId()))) {
					toDeleteVersions.add(m);
				}
			}
		}

		removeDocuments(toDeleteDocs, toDeleteVersions, date, sessionObj,
				rejected);

		final int[] nd = new int[rejected.size() + idSet.size()];
		int i = 0;
		for (final DocumentMetadata rej : rejected) {
			nd[i++] = rej.getId();
		}
		for (final int notFound : idSet) {
			nd[i++] = notFound;
		}

		return nd;
	}

	@OXThrows(category = Category.USER_INPUT, desc = "The user must be allowed to delete the object in order to delete a version of it.", exceptionId = 6, msg = "You do not have sufficient permission to delete this version.")
	public int[] removeVersion(final int id, final int[] versionId, final ServerSession sessionObj)
			throws OXException { 
		if (versionId.length <= 0) {
			return versionId;
		}

		DocumentMetadata metadata = load(id,
				InfostoreFacade.CURRENT_VERSION, sessionObj.getContext());
		try {
			checkWriteLock(metadata, sessionObj);
		} catch (final OXException x) {
			return versionId;
		}
		final EffectiveInfostorePermission infoPerm = security
				.getInfostorePermission(id, sessionObj.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj));
		if (!infoPerm.canDeleteObject()) {
			throw EXCEPTIONS.create(6);
		}
		final StringBuilder versions = new StringBuilder().append('(');
		final Set<Integer> versionSet = new HashSet<Integer>();

		for (final int v : versionId) {
			versions.append(v).append(',');
			versionSet.add(Integer.valueOf(v));
		}
		versions.setLength(versions.length() - 1);
		versions.append(')');

		List<DocumentMetadata> allVersions = null;
		try {
			allVersions = InfostoreIterator.allVersionsWhere(
					"infostore_document.infostore_id = " + id
							+ " AND infostore_document.version_number IN "
							+ versions.toString()
							+ " and infostore_document.version_number != 0 ",
					Metadata.VALUES_ARRAY, this, sessionObj.getContext())
					.asList();
		} catch (final SearchIteratorException x) {
			throw new InfostoreException(x);
		}

		final Date now = new Date();

		boolean removeCurrent = false;
		for (final DocumentMetadata v : allVersions) {
			if (v.getVersion() == metadata.getVersion()) {
				removeCurrent = true;
			}
			versionSet.remove(Integer.valueOf(v.getVersion()));
			v.setLastModified(now);
			removeFile(sessionObj.getContext(), v.getFilestoreLocation());
		}

		final DeleteVersionAction deleteVersion = new DeleteVersionAction();
		deleteVersion.setContext(sessionObj.getContext());
		deleteVersion.setDocuments(allVersions);
		deleteVersion.setProvider(this);
		deleteVersion.setQueryCatalog(QUERIES);

		try {
			perform(deleteVersion, true);
		} catch (final OXException x) {
			throw x;
		} catch (final AbstractOXException e1) {
			throw new InfostoreException(e1);
		}

		final DocumentMetadata update = new DocumentMetadataImpl(metadata);

		update.setLastModified(now);
		update.setModifiedBy(sessionObj.getUserId());

		final Set<Metadata> updatedFields = new HashSet<Metadata>();
		updatedFields.add(Metadata.LAST_MODIFIED_LITERAL);
		updatedFields.add(Metadata.MODIFIED_BY_LITERAL);

		if (removeCurrent) {
			
			// Update Version 0
			final DocumentMetadata oldVersion0 = load(id, 0,
					sessionObj.getContext());
			
			final DocumentMetadata version0 = new DocumentMetadataImpl(metadata);
			version0.setVersion(0);

			final UpdateVersionAction updateVersion = new UpdateVersionAction();
			updateVersion.setContext(sessionObj.getContext());
			updateVersion.setDocuments(Arrays.asList(version0));
			updateVersion.setModified(Metadata.DESCRIPTION_LITERAL,
					Metadata.TITLE_LITERAL, Metadata.URL_LITERAL,
					Metadata.LAST_MODIFIED_LITERAL,
					Metadata.MODIFIED_BY_LITERAL);
			updateVersion.setOldDocuments(Arrays.asList(oldVersion0));
			updateVersion.setProvider(this);
			updateVersion.setQueryCatalog(QUERIES);
			updateVersion.setTimestamp(Long.MAX_VALUE);
			try {
				perform(updateVersion, true);
			} catch (final OXException x) {
				throw x;
			} catch (final AbstractOXException e1) {
				throw new InfostoreException(e1);
			}

			// Set new Version Number
			update.setVersion(db.getMaxActiveVersion(metadata.getId(),
					sessionObj.getContext()));
			updatedFields.add(Metadata.VERSION_LITERAL);
		}

		final UpdateDocumentAction updateDocument = new UpdateDocumentAction();
		updateDocument.setContext(sessionObj.getContext());
		updateDocument.setDocuments(Arrays.asList(update));
		updateDocument.setModified(updatedFields
				.toArray(new Metadata[updatedFields.size()]));
		updateDocument.setOldDocuments(Arrays.asList(metadata));
		updateDocument.setProvider(this);
		updateDocument.setQueryCatalog(QUERIES);
		updateDocument.setTimestamp(Long.MAX_VALUE);
		
		if(removeCurrent) {
			metadata = load(metadata.getId(), update.getVersion(), sessionObj.getContext());
			checkUniqueFilename(metadata.getFileName(), metadata.getFolderId(), metadata.getId(), sessionObj.getContext());
		}
		
		try {
			perform(updateDocument, true);
		} catch (final OXException x) {
			throw x;
		} catch (final AbstractOXException e1) {
			throw new InfostoreException(e1);
		}

		final EventClient ec = new EventClient(sessionObj);
		try {
			ec.modify(metadata);
		} catch (final Exception e) {
			LOG.error("", e); // FIXME
		}

		final int[] retval = new int[versionSet.size()];
		int i = 0;
		for (final Integer integer : versionSet) {
			retval[i++] = integer.intValue();
		}

		return retval;
	}

	public TimedResult getDocuments(final long folderId, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		return getDocuments(folderId,
				Metadata.HTTPAPI_VALUES_ARRAY, null, 0, ctx, user,
				userConfig);
	}

	public TimedResult getDocuments(final long folderId, final Metadata[] columns,
			final Context ctx, final User user, final UserConfiguration userConfig)
			throws OXException {
		return getDocuments(folderId, columns, null, 0, ctx, user, userConfig);
	}

	@OXThrows(category = Category.USER_INPUT, desc = "The user may not read objects in the given folder. ", exceptionId = 7, msg = "You do not have sufficient permissions to read objects in this folder.")
	public TimedResult getDocuments(final long folderId, final Metadata[] columns,
			final Metadata sort, final int order, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		boolean onlyOwn = false;
		final EffectivePermission isperm = security.getFolderPermission(folderId,
				ctx, user, userConfig);
		if (isperm.getReadPermission() == EffectivePermission.NO_PERMISSIONS) {
			throw EXCEPTIONS.create(7);
		} else if (isperm.getReadPermission() == EffectivePermission.READ_OWN_OBJECTS) {
			onlyOwn = true;
		}
		boolean addLocked = false;
		for (final Metadata m : columns) {
			if (m == Metadata.LOCKED_UNTIL_LITERAL) {
				addLocked = true;
				break;
			}
		}
		
		InfostoreIterator iter = null;
		if(onlyOwn) {
			iter = InfostoreIterator.documentsByCreator(folderId, user.getId(), columns, sort, order, getProvider(), ctx);
		} else {
			iter = InfostoreIterator.documents(folderId, columns, sort, order, getProvider(), ctx);	
		}
		final TimedResult tr = new TimedResultImpl(iter, System.currentTimeMillis());
		if (addLocked) {
			return addLocked(tr, ctx, user, userConfig);
		}
		return tr;
	}

	public TimedResult getVersions(final int id, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		return getVersions(id, Metadata.HTTPAPI_VALUES_ARRAY,
				null, 0, ctx, user, userConfig);
	}

	public TimedResult getVersions(final int id, final Metadata[] columns, final Context ctx,
			final User user, final UserConfiguration userConfig) throws OXException {
		return getVersions(id, columns, null, 0, ctx, user, userConfig);
	}

	@OXThrows(category = Category.USER_INPUT, desc = "The user may not read objects in the given folder. ", exceptionId = 8, msg = "You do not have sufficient permissions to read objects in this folder.")
	public TimedResult getVersions(final int id, final Metadata[] columns, final Metadata sort,
			final int order, final Context ctx, final User user, final UserConfiguration userConfig)
			throws OXException {
		final EffectiveInfostorePermission infoPerm = security
				.getInfostorePermission(id, ctx, user, userConfig);
		if (!infoPerm.canReadObject()) {
			throw EXCEPTIONS.create(8);
		}
		boolean addLocked = false;
		for (final Metadata m : columns) {
			if (m == Metadata.LOCKED_UNTIL_LITERAL) {
				addLocked = true;
				break;
			}
		}
		
		final InfostoreIterator iter = InfostoreIterator.versions(id, columns, sort, order, getProvider(), ctx);	
		final TimedResult tr = new TimedResultImpl(iter, System.currentTimeMillis());
		
		
		if (addLocked) {
			return addLocked(tr, ctx, user, userConfig);
		}
		return tr;

	}

	@OXThrows(category = Category.USER_INPUT, desc = "The user may not read objects in the given folder. ", exceptionId = 9, msg = "You do not have sufficient permissions to read objects in this folder.")
	public TimedResult getDocuments(final int[] ids, final Metadata[] columns, final Context ctx,
			final User user, final UserConfiguration userConfig) throws OXException {

		try {
			security.injectInfostorePermissions(ids, ctx, user, userConfig,
					null, new Injector<Object, EffectiveInfostorePermission>() {

						public Object inject(final Object list,
								final EffectiveInfostorePermission element) {
							if (!element.canReadObject()) {
								throw new NotAllowed(element.getObjectID());
							}
							return list;
						}

					});
		} catch (final NotAllowed na) {
			throw EXCEPTIONS.create(9);
		}
		final InfostoreIterator iter = InfostoreIterator.list(ids, columns, getProvider(), ctx);	
		final TimedResult tr = new TimedResultImpl(iter, System.currentTimeMillis());
		
		for(final Metadata m : columns) {
			if(m == Metadata.LOCKED_UNTIL_LITERAL) {
				return addLocked(tr, ctx, user, userConfig);
			}
		}
		return tr;
		
	}

	public Delta getDelta(final long folderId, final long updateSince, final Metadata[] columns,
			final boolean ignoreDeleted, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		return getDelta(folderId, updateSince, columns, null, 0, ignoreDeleted,
				ctx, user, userConfig);
	}

	@OXThrows(category = Category.USER_INPUT, desc = "The user may not read objects in the given folder. ", exceptionId = 10, msg = "You do not have sufficient permissions to read objects in this folder.")
	public Delta getDelta(final long folderId, final long updateSince, final Metadata[] columns,
			final Metadata sort, final int order, final boolean ignoreDeleted, final Context ctx,
			final User user, final UserConfiguration userConfig) throws OXException {
		boolean onlyOwn = false;

		final EffectivePermission isperm = security.getFolderPermission(folderId,
				ctx, user, userConfig);
		if (isperm.getReadPermission() == EffectivePermission.NO_PERMISSIONS) {
			throw EXCEPTIONS.create(10);
		} else if (isperm.getReadPermission() == EffectivePermission.READ_OWN_OBJECTS) {
			onlyOwn = true;
		}
		boolean addLocked = true;
		for (final Metadata m : columns) {
			if (m == Metadata.LOCKED_UNTIL_LITERAL) {
				addLocked = true;
				break;
			}
		}
		
		final DBProvider reuse = new ReuseReadConProvider(getProvider());
		
		InfostoreIterator newIter = null;
		InfostoreIterator modIter = null;
		InfostoreIterator delIter = null;
		
		if(onlyOwn) {
			newIter = InfostoreIterator.newDocumentsByCreator(folderId, user.getId(), columns, sort, order, updateSince, reuse, ctx);
			modIter = InfostoreIterator.modifiedDocumentsByCreator(folderId, user.getId(), columns, sort, order, updateSince, reuse, ctx);
			if(!ignoreDeleted) {
				delIter = InfostoreIterator.deletedDocumentsByCreator(folderId, user.getId(), sort, order, updateSince, reuse, ctx);
			}
		} else {
			newIter = InfostoreIterator.newDocuments(folderId, columns, sort, order, updateSince, reuse, ctx);
			modIter = InfostoreIterator.modifiedDocuments(folderId, columns, sort, order, updateSince, reuse, ctx);
			if(!ignoreDeleted) {
				delIter = InfostoreIterator.deletedDocuments(folderId, sort, order, updateSince, reuse, ctx);
			}
		}
		
		final Delta delta = new DeltaImpl(newIter, modIter, (ignoreDeleted ? SearchIteratorAdapter.createEmptyIterator() : delIter), System.currentTimeMillis());
		
		if (addLocked) {
			return addLocked(delta, ctx,
					user, userConfig);
		}
		return delta;
	}

	@OXThrows(category = Category.USER_INPUT, desc = "The user may not read objects in the given folder. ", exceptionId = 11, msg = "You do not have sufficient permissions to read objects in this folder.")
	public int countDocuments(final long folderId, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		boolean onlyOwn = false;
		final EffectivePermission isperm = security.getFolderPermission(folderId,
				ctx, user, userConfig);
		if (!(isperm.canReadAllObjects()) && !(isperm.canReadOwnObjects())) {
			throw EXCEPTIONS.create(11);
		} else if (isperm.canReadOwnObjects()) {
			onlyOwn = true;
		}
		return db.countDocuments(folderId, onlyOwn, ctx, user, userConfig);
	}

	public boolean hasFolderForeignObjects(final long folderId, final Context ctx,
			final User user, final UserConfiguration userConfig) throws OXException {
		return db.hasFolderForeignObjects(folderId, ctx, user, userConfig);
	}

	public boolean isFolderEmpty(final long folderId, final Context ctx) throws OXException {
		return db.isFolderEmpty(folderId, ctx);
	}

	public void removeUser(final int id, final Context ctx, final ServerSession session) throws OXException {
		db.removeUser(id, ctx, session, lockManager);
	}

	private int getId(final Context context, final Connection writeCon) throws SQLException {
		final boolean autoCommit = writeCon.getAutoCommit();
		if (autoCommit) {
			writeCon.setAutoCommit(false);
		}
		try {
			return IDGenerator.getId(context, Types.INFOSTORE, writeCon);
		} finally {
			if (autoCommit) {
				writeCon.commit();
				writeCon.setAutoCommit(true);
			}
		}
	}

    public InfostoreSecurity getSecurity() {
        return security;
    }

    private static final class NotAllowed extends RuntimeException {
		private static final long serialVersionUID = 4872889537922290831L;

		public int id;

		public NotAllowed(final int id) {
			this.id = id;
		}
	}

    private static enum ServiceMethod {
        COMMIT, FINISH, ROLLBACK, SET_REQUEST_TRANSACTIONAL, START_TRANSACTION, SET_PROVIDER;

        public void call(Object o, Object...args) {
            if(!(o instanceof DBService)) {
                return;
            }
            DBService service = (DBService) o;
            switch(this) {
                default : return;
                case SET_REQUEST_TRANSACTIONAL: service.setRequestTransactional((Boolean) args[0]); break;
                case SET_PROVIDER: service.setProvider((DBProvider)args[0]); break;
            }
        }

        public void callUnsafe(Object o, Object...args) throws TransactionException {
            if(!(o instanceof DBService)) {
                return;
            }
            DBService service = (DBService) o;
            switch(this) {
                default : call(o, args); break;
                case COMMIT: service.commit(); break;
                case FINISH: service.finish(); break;
                case ROLLBACK: service.rollback(); break;
                case START_TRANSACTION: service.startTransaction(); break;
            }
        }

    }


    @Override
	@OXThrowsMultiple(category = { Category.SUBSYSTEM_OR_SERVICE_DOWN,
			Category.SUBSYSTEM_OR_SERVICE_DOWN }, desc = {
			"Cannot reach the file store so some documents were not deleted.",
			"Cannot reach the file store so some documents were not deleted. This propably means that file store and db are inconsistent. Run the recovery tool." }, exceptionId = {
			35, 36 }, msg = {
			"Cannot reach the file store so I cannot remove the documents.",
			"Cannot remove file. Database and file store are probably inconsistent. Please contact an administrator to run the recovery tool." }

	)
    public void commit() throws TransactionException {
		db.commit();
		ServiceMethod.COMMIT.callUnsafe(security);
		lockManager.commit();
		if (null != fileIdRemoveList.get() && fileIdRemoveList.get().size() > 0) {
			try {
				final QuotaFileStorage qfs = (QuotaFileStorage) getFileStorage(ctxHolder
						.get());
				for (final String id : fileIdRemoveList.get()) {
					try {
						//System.out.println("REMOVE " + id);
						qfs.deleteFile(id);
					} catch (final FileStorageException x) {
						throw new TransactionException(x);
					}
				}
			} catch (final FilestoreException e) {
				throw new TransactionException(e);
			} catch (final FileStorageException e) {
				rollback();
				throw new TransactionException(e);
			}
		}
		super.commit();
	}

	@Override
	public void finish() throws TransactionException {
		fileIdRemoveList.set(null);
		ctxHolder.set(null);
		db.finish();
        ServiceMethod.FINISH.callUnsafe(security);
		super.finish();
	}

	@Override
	public void rollback() throws TransactionException {
		db.rollback();
        ServiceMethod.ROLLBACK.callUnsafe(security);
		lockManager.rollback();
		super.rollback();
	}

	@Override
	public void setRequestTransactional(final boolean transactional) {
		db.setRequestTransactional(transactional);
        ServiceMethod.SET_REQUEST_TRANSACTIONAL.call(security, transactional);
		lockManager.setRequestTransactional(transactional);
		super.setRequestTransactional(transactional);
	}

	@Override
	public void setTransactional(final boolean transactional) {
		lockManager.setTransactional(transactional);
	}

	@Override
	public void startTransaction() throws TransactionException {
		fileIdRemoveList.set(new ArrayList<String>());
		ctxHolder.set(null);
		db.startTransaction();
        ServiceMethod.START_TRANSACTION.callUnsafe(security);
		lockManager.startTransaction();
		super.startTransaction();
	}

	@Override
	public void setProvider(final DBProvider provider) {
		super.setProvider(provider);
		db.setProvider(provider);
        ServiceMethod.SET_PROVIDER.call(security, provider);
        ServiceMethod.SET_PROVIDER.call(lockManager, provider);
	}

	private static final UserConfiguration getUserConfiguration(final ServerSession sessionObj) {
		return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(),
				sessionObj.getContext());
	}

	private static final User getUser(final ServerSession sessionObj) {
		return UserStorage.getStorageUser(sessionObj.getUserId(), sessionObj.getContext());
	}

	private final class LockTimedResult implements TimedResult {

		private long sequenceNumber;

		private SearchIterator results;

		public LockTimedResult(final TimedResult delegate, final Context ctx, final User user,
				final UserConfiguration userConfig) throws SearchIteratorException,
				OXException {
			sequenceNumber = delegate.sequenceNumber();

			this.results = lockedUntilIterator(delegate.results(), ctx, user,
					userConfig);
		}

		public SearchIterator results() {
			return results;
		}

		public long sequenceNumber() {
			return sequenceNumber;
		}

	}

	private final class LockDelta implements Delta {

		private long sequenceNumber;

		private SearchIterator newIter;

		private SearchIterator modified;

		private SearchIterator deleted;

		public LockDelta(final Delta delegate, final Context ctx, final User user,
				final UserConfiguration userConfig) throws SearchIteratorException,
				OXException {
			final SearchIterator deleted = delegate.getDeleted();
			if(null != deleted) {
				this.deleted = lockedUntilIterator(deleted, ctx,
						user, userConfig);
			}
			this.modified = lockedUntilIterator(delegate.getModified(), ctx,
					user, userConfig);
			this.newIter = lockedUntilIterator(delegate.getNew(), ctx, user,
					userConfig);
			this.sequenceNumber = delegate.sequenceNumber();
		}

		public SearchIterator getDeleted() {
			return deleted;
		}

		public SearchIterator getModified() {
			return modified;
		}

		public SearchIterator getNew() {
			return newIter;
		}

		public SearchIterator results() {
			return new CombinedSearchIterator(newIter, modified);
		}

		public long sequenceNumber() {
			return sequenceNumber;
		}

		public void close() throws SearchIteratorException {
			newIter.close();
			modified.close();
			deleted.close();
		}

	}
}
