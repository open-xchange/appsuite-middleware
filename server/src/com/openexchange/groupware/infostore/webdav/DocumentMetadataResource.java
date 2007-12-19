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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.infostore.*;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.GetSwitch;
import com.openexchange.groupware.infostore.database.impl.SetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionHolder;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.*;
import com.openexchange.webdav.protocol.impl.AbstractResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DOCUMENTMETADATARESOURCE,
		component=Component.INFOSTORE
)
public class DocumentMetadataResource extends AbstractResource implements OXWebdavResource {

	private final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(DocumentMetadataResource.class);
	
	private static final Log LOG = LogFactory.getLog(DocumentMetadataResource.class);
	
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
	
	private boolean loadedMetadata;

	private boolean existsInDB;

	private final LockHelper lockHelper;

	private boolean metadataChanged;

	
	public DocumentMetadataResource(final WebdavPath url, final InfostoreWebdavFactory factory) {
		this.factory = factory;
		this.url = url;
		this.sessionHolder = factory.getSessionHolder();
		this.lockHelper = new EntityLockHelper(factory.getInfoLockManager(), sessionHolder, url.toString());
		this.database = factory.getDatabase();
		this.propertyHelper = new PropertyHelper(factory.getInfoProperties(), sessionHolder, url.toString());
	}
	
	public DocumentMetadataResource(final WebdavPath url, final DocumentMetadata docMeta, final InfostoreWebdavFactory factory) {
		this.factory = factory;
		this.url = url;
		this.sessionHolder = factory.getSessionHolder();
		this.lockHelper = new EntityLockHelper(factory.getInfoLockManager(), sessionHolder, url.toString());
		this.database = factory.getDatabase();
		this.propertyHelper = new PropertyHelper(factory.getInfoProperties(), sessionHolder, url.toString());
		
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
	public boolean hasBody() throws WebdavException {
		loadMetadata();
		return metadata.getFileSize()>0;
	}

	@Override
	protected List<WebdavProperty> internalGetAllProps() throws WebdavException {
		return propertyHelper.getAllProps();
	}

	@Override
	protected WebdavProperty internalGetProperty(final String namespace, final String name) throws WebdavException {
		return propertyHelper.getProperty(namespace, name);
	}

	@Override
	protected void internalPutProperty(final WebdavProperty prop) throws WebdavException {
		propertyHelper.putProperty(prop);
	}

	@Override
	protected void internalRemoveProperty(final String namespace, final String name) throws WebdavException {
		propertyHelper.removeProperty(namespace, name);
	}

	@Override
	protected boolean isset(final Property p) {
		/*if(p.getId() == Protocol.GETCONTENTLANGUAGE) {
			return false;
		}*/
		return !propertyHelper.isRemoved(new WebdavProperty(p.getNamespace(), p.getName()));
	}

	@Override
	public void setCreationDate(final Date date) throws WebdavException {
		metadata.setCreationDate(date);
		markChanged();
		markSet(Metadata.CREATION_DATE_LITERAL);
	}

	public void create() throws WebdavException {
		if(exists) {
			throw new WebdavException("The directory exists already", getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
		save();
		exists=true;
		factory.created(this);
	}

	public void delete() throws WebdavException {
		if(exists) {
			try {
				lockHelper.deleteLocks();
				propertyHelper.deleteProperties();
				deleteMetadata();
				exists = false;
				factory.removed(this);
			} catch (final InfostoreException x) {
				if(InfostoreExceptionFactory.isPermissionException(x)) {
					throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_FORBIDDEN);				
				}
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (final Exception x) {
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	public boolean exists() throws WebdavException {
		return exists;
	}

	public InputStream getBody() throws WebdavException {
		final Session session = sessionHolder.getSessionObject();
		try {
			return database.getDocument(id, InfostoreFacade.CURRENT_VERSION, session.getContext(), UserStorage.getStorageUser(
					session.getUserId(), session.getContext()), UserConfigurationStorage.getInstance()
					.getUserConfigurationSafe(session.getUserId(), session.getContext()));
		} catch (final Exception e) {
			throw new WebdavException(e.getMessage(), e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public String getContentType() throws WebdavException {
		loadMetadata();
		return metadata.getFileMIMEType();
	}

	public Date getCreationDate() throws WebdavException {
		loadMetadata();
		return metadata.getCreationDate();
	}

	public String getDisplayName() throws WebdavException {
		loadMetadata();
		return metadata.getFileName();
	}

	public String getETag() throws WebdavException {
		if(!exists && !existsInDB) {
			/*try {
				dumpMetadataToDB();
			} catch (Exception e) {
				throw new WebdavException(e.getMessage(), e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} */
			return null;
		}
		return String.format("http://www.open-xchange.com/webdav/etags/%d-%d-%d", Integer.valueOf(sessionHolder.getSessionObject().getContext().getContextId()), Integer.valueOf(metadata.getId()), Integer.valueOf(metadata.getVersion()));
	}

	public String getLanguage() throws WebdavException {
		return null;
	}

	public Date getLastModified() throws WebdavException {
		loadMetadata();
		return metadata.getLastModified();
	}

	public Long getLength() throws WebdavException {
		loadMetadata();
		return Long.valueOf(metadata.getFileSize());
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
		return url;
	}

	public void lock(final WebdavLock lock) throws WebdavException {
		if(!exists) {
			new InfostoreLockNullResource(this, factory).lock(lock);
			factory.invalidate(getUrl(), getId(), Type.RESOURCE);
			return;
		}
		lockHelper.addLock(lock);
	}
	
	public void unlock(final String token) throws WebdavException {
		lockHelper.removeLock(token);
		try {
			lockHelper.dumpLocksToDB();
		} catch (final OXException e) {
			throw new WebdavException("",e,getUrl(),HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public void save() throws WebdavException {
		try {
			dumpMetadataToDB();
			propertyHelper.dumpPropertiesToDB();
			lockHelper.dumpLocksToDB();
		} catch (final WebdavException x) {
			throw x;
		} catch (final InfostoreException x) {
			if(InfostoreExceptionFactory.isPermissionException(x)){
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_FORBIDDEN);
			}
			throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (final Exception x) {
			throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}


	public void setContentType(final String type) throws WebdavException {
		metadata.setFileMIMEType(type);
		markChanged();
		markSet(Metadata.FILE_MIMETYPE_LITERAL);
	}

	public void setDisplayName(final String displayName) throws WebdavException {
		metadata.setFileName(displayName);
		markChanged();
		markSet(Metadata.FILENAME_LITERAL);
	}

	public void setLength(final Long length) throws WebdavException {
		metadata.setFileSize(length.longValue());
		markChanged();
		markSet(Metadata.FILE_SIZE_LITERAL);
	}

	public void setSource(final String source) throws WebdavException {
		// IGNORE
		
	}

	public void setLanguage(final String language) throws WebdavException {
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
	public WebdavResource move(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavException {
		final WebdavResource res = factory.resolveResource(dest);
		if(res.exists()) {
			if(!overwrite) {
				throw new WebdavException(String.format("%s exists", dest),getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED);
			}
			res.delete();
		}
		final WebdavPath parent = dest.parent();
		final String name = dest.name();
		
		final FolderCollection coll = (FolderCollection) factory.resolveCollection(parent);
		if(!coll.exists()) {
			throw new WebdavException(String.format("The folder %s doesn't exist",parent),getUrl(), HttpServletResponse.SC_CONFLICT);
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
			throw new WebdavException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this;
	}
	
	@Override
	public WebdavResource copy(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavException {

		final WebdavPath parent = dest.parent();
		final String name = dest.name();
		
		final FolderCollection coll = (FolderCollection) factory.resolveCollection(parent);
		if(!coll.exists()) {
			throw new WebdavException(String.format("The folder %s doesn't exist", parent),getUrl(), HttpServletResponse.SC_CONFLICT);
		}	
		
		final DocumentMetadataResource copy = (DocumentMetadataResource) factory.resolveResource(dest);
		if(copy.exists()) {
			if(!overwrite) {
				throw new WebdavException(String.format("%s exists", dest),getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED);
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
			throw new WebdavException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return copy;
			
	}
	
	private void initDest(final DocumentMetadataResource copy, final String name, final int parentId) {
		
		copy.metadata.setTitle(name);
		copy.metadata.setFileName(name);
		copy.metadata.setFolderId(parentId);
		
	}

	private void copyMetadata(final DocumentMetadataResource copy) throws WebdavException {
		loadMetadata();
		copy.metadata = new DocumentMetadataImpl(metadata);
		copy.metadata.setId(InfostoreFacade.NEW);
		copy.metadataChanged = true;
		copy.setMetadata.addAll(Metadata.VALUES);
	}
	
	private void copyProperties(final DocumentMetadataResource copy) throws WebdavException {
		for(final WebdavProperty prop : internalGetAllProps()) {
			copy.putProperty(prop);
		}
	}
	
	private void copyBody(final DocumentMetadataResource copy) throws WebdavException {
		final InputStream in = getBody();
		if(in != null) {
			copy.putBody(in);
		}
	}

	private void loadMetadata() throws WebdavException {
		if(!exists) {
			return;
		}
		if(loadedMetadata) {
			return;
		}
		loadedMetadata = true;
		final Set<Metadata> toLoad = new HashSet<Metadata>(Metadata.VALUES);
		toLoad.removeAll(setMetadata);
		final Session session = sessionHolder.getSessionObject();
		
		try {
			final DocumentMetadata metadata = database.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, session
					.getContext(), UserStorage.getStorageUser(session.getUserId(), session.getContext()),
					UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
							session.getContext()));
			final SetSwitch set = new SetSwitch(this.metadata);
			final GetSwitch get = new GetSwitch(metadata);

			for (final Metadata m : toLoad) {
				set.setValue(m.doSwitch(get));
				m.doSwitch(set);
			}
		} catch (final Exception x) {
			throw new WebdavException(x.getMessage(), x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void markSet(final Metadata metadata) {
		setMetadata.add(metadata);
	}

	private void markChanged() {
		metadataChanged = true;
	}

	@Override
	public void putBody(final InputStream body, final boolean guessSize) throws WebdavException{
		if(!exists && !existsInDB) {
			// CREATE WITH FILE
			try {
				dumpMetadataToDB(body, guessSize);
			} catch (final WebdavException x) {
				throw x;
			} catch (final InfostoreException x) {
				if(InfostoreExceptionFactory.isPermissionException(x)){
					throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_FORBIDDEN);
				}
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);		
			} catch (final Exception x) {
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			// UPDATE
			final Session session = sessionHolder.getSessionObject();
			try {
				loadMetadata();
				if(guessSize) {
					metadata.setFileSize(0);
				}
				database.saveDocument(metadata, body, Long.MAX_VALUE, session);
				database.commit();
			} catch (final Exception x) {
				try {
					database.rollback();
				} catch (final TransactionException e) {
					LOG.error("Couldn't rollback transaction. Run the recovery tool.");
				}
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		
	}
	
	private void dumpMetadataToDB(InputStream fileData, boolean guessSize) throws OXException, IllegalAccessException, ConflictException, WebdavException{
		if((exists || existsInDB) && !metadataChanged) {
			return;
		}
		FolderCollection parent = null;
		try{
			parent = (FolderCollection) parent();
			if(!parent.exists()) {
				throw new WebdavException(getUrl(), HttpServletResponse.SC_CONFLICT);
			}
		} catch (final ClassCastException x) {
			throw new WebdavException(getUrl(), HttpServletResponse.SC_CONFLICT);
		}
		final Session session = sessionHolder.getSessionObject();
		metadata.setFolderId(parent.getId());
		if(!exists && !existsInDB) {
			metadata.setVersion(InfostoreFacade.NEW);
			metadata.setId(InfostoreFacade.NEW);
			if(metadata.getFileName() == null || metadata.getFileName().trim().length()==0){
				//if(url.contains("/"))
					metadata.setFileName(url.name());
			}
			metadata.setTitle(metadata.getFileName());
			if(fileData != null && guessSize) {
				metadata.setFileSize(0);
				
			}
			try {
				if(fileData == null) {
					database.saveDocumentMetadata(metadata, InfostoreFacade.NEW, session);
				} else {
					database.saveDocument(metadata, fileData, InfostoreFacade.NEW, session);
				}
				database.commit();
				setId(metadata.getId());
			} catch (final OXException x) {
				try {
					database.rollback();
				} catch (TransactionException x2) {
					LOG.error("Couldn't roll back: ",x2);
				}
				throw x;
			}
		} else {
			if(setMetadata.contains(Metadata.FILENAME_LITERAL)) {
				metadata.setTitle(metadata.getFileName());
				setMetadata.add(Metadata.TITLE_LITERAL);
			} //FIXME Detonator Pattern
			try {
				database.saveDocumentMetadata(metadata, Long.MAX_VALUE, setMetadata.toArray(new Metadata[setMetadata.size()]),session);
				database.commit();
			} catch (final OXException x) {
				try {
					database.rollback();
				} catch (TransactionException x2) {
					LOG.error("Can't roll back", x2);
				}
				throw x;
			}
		}
		existsInDB = true;
		setMetadata.clear();
		metadataChanged=false;
	}

	private void dumpMetadataToDB() throws OXException, IllegalAccessException, ConflictException, WebdavException {
		dumpMetadataToDB(null,false);
	}

	@OXThrows(
			category=Category.CONCURRENT_MODIFICATION,
			desc="The DocumentMetadata entry in the DB for the given resource could not be created. This is mostly due to someone else modifying the entry. This can also mean, that the entry has been deleted already.",
			exceptionId=0,
			msg="Could not delete DocumentMetadata %d. Please try again."
	)
	private void deleteMetadata() throws OXException, IllegalAccessException {
		final Session session = sessionHolder.getSessionObject();
		final int[] nd = database.removeDocument(new int[]{ id }, Long.MAX_VALUE,session); 
		if(nd.length>0) {
			database.rollback();
			throw EXCEPTIONS.create(0,Integer.valueOf(nd[0]));
		}
		database.commit();
	}

	public int getId() {
		return id;
	}

	public int getParentId() throws WebdavException {
		if(metadata == null)
			loadMetadata();
		return (int) metadata.getFolderId();
	}

	public void removedParent() throws WebdavException {
		exists = false;
		factory.removed(this);
	}

	public void transferLock(final WebdavLock lock) throws WebdavException {
		try {
			lockHelper.transferLock(lock);
		} catch (final OXException e) {
			throw new WebdavException(e.getMessage(),e,getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	@Override
	public String toString(){
		return super.toString()+" :"+id;
	}
}
