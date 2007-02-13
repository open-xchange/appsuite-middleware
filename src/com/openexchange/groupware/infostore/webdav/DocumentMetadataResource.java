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

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.infostore.ConflictException;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.GetSwitch;
import com.openexchange.groupware.infostore.database.impl.SetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.sessiond.SessionHolder;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.impl.AbstractResource;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.infostore.Classes;


@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DOCUMENTMETADATARESOURCE,
		component=Component.INFOSTORE
)
public class DocumentMetadataResource extends AbstractResource implements OXWebdavResource {

	private InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(DocumentMetadataResource.class);
	
	private Log LOG = LogFactory.getLog(DocumentMetadataResource.class);
	
	private InfostoreWebdavFactory factory = null;

	private boolean exists;

	private int id;
	
	private DocumentMetadata metadata = new DocumentMetadataImpl();
	
	// State
	private final Set<Metadata> setMetadata = new HashSet<Metadata>();
	
	private PropertyHelper propertyHelper;

	private String url;
	
	private SessionHolder sessionHolder;
	
	private InfostoreFacade database;
	
	private boolean loadedMetadata;

	private boolean existsInDB;

	private LockHelper lockHelper;

	private boolean metadataChanged;

	
	public DocumentMetadataResource(String url, InfostoreWebdavFactory factory) {
		this.factory = factory;
		this.url = url;
		this.sessionHolder = factory.getSessionHolder();
		this.lockHelper = new EntityLockHelper(factory.getInfoLockManager(), sessionHolder, url);
		this.database = factory.getDatabase();
		this.propertyHelper = new PropertyHelper(factory.getInfoProperties(), sessionHolder, url);
	}
	
	public DocumentMetadataResource(String url, DocumentMetadata docMeta, InfostoreWebdavFactory factory) {
		this.factory = factory;
		this.url = url;
		this.sessionHolder = factory.getSessionHolder();
		this.lockHelper = new EntityLockHelper(factory.getInfoLockManager(), sessionHolder, url);
		this.database = factory.getDatabase();
		this.propertyHelper = new PropertyHelper(factory.getInfoProperties(), sessionHolder, url);
		
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
	protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavException {
		return propertyHelper.getProperty(namespace, name);
	}

	@Override
	protected void internalPutProperty(WebdavProperty prop) throws WebdavException {
		propertyHelper.putProperty(prop);
	}

	@Override
	protected void internalRemoveProperty(String namespace, String name) throws WebdavException {
		propertyHelper.removeProperty(namespace, name);
	}

	@Override
	protected boolean isset(Property p) {
		/*if(p.getId() == Protocol.GETCONTENTLANGUAGE) {
			return false;
		}*/
		return !propertyHelper.isRemoved(new WebdavProperty(p.getNamespace(), p.getName()));
	}

	@Override
	public void setCreationDate(Date date) throws WebdavException {
		metadata.setCreationDate(date);
		markChanged();
		markSet(Metadata.CREATION_DATE_LITERAL);
	}

	public void create() throws WebdavException {
		if(exists)
			throw new WebdavException("The directory exists already", getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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
			} catch (Exception x) {
				LOG.debug(x.getMessage(),x);
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	public boolean exists() throws WebdavException {
		return exists;
	}

	public InputStream getBody() throws WebdavException {
		SessionObject session = sessionHolder.getSessionObject();
		try {
			return database.getDocument(id, InfostoreFacade.CURRENT_VERSION, session.getContext(), session.getUserObject(), session.getUserConfiguration());
		} catch (Exception e) {
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
		return String.format("http://www.open-xchange.com/webdav/etags/%d-%d-%d", sessionHolder.getSessionObject().getContext().getContextId(), metadata.getId(), metadata.getVersion());
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
		return metadata.getFileSize();
	}

	public WebdavLock getLock(String token) throws WebdavException {
		WebdavLock lock = lockHelper.getLock(token);
		if(lock != null)
			return lock;
		return findParentLock(token);
	}

	public List<WebdavLock> getLocks() throws WebdavException {
		List<WebdavLock> lockList =  getOwnLocks();
		addParentLocks(lockList);
		return lockList;
	}

	public WebdavLock getOwnLock(String token) throws WebdavException {
		return lockHelper.getLock(token);
	}

	public List<WebdavLock> getOwnLocks() throws WebdavException {
		return lockHelper.getAllLocks();
	}

	public String getSource() throws WebdavException {
		return null;
	}

	public String getUrl() {
		return url;
	}

	public void lock(WebdavLock lock) throws WebdavException {
		if(!exists) {
			new InfostoreLockNullResource(this, factory).lock(lock);
			factory.invalidate(getUrl(), getId(), Type.RESOURCE);
			return;
		}
		lockHelper.addLock(lock);
	}
	
	public void unlock(String token) throws WebdavException {
		lockHelper.removeLock(token);
		try {
			lockHelper.dumpLocksToDB();
		} catch (OXException e) {
			throw new WebdavException("",e,getUrl(),HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public void save() throws WebdavException {
		try {
			dumpMetadataToDB();
			propertyHelper.dumpPropertiesToDB();
			lockHelper.dumpLocksToDB();
		} catch (WebdavException x) {
			throw x;
		} catch (Exception x) {
			throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}


	public void setContentType(String type) throws WebdavException {
		metadata.setFileMIMEType(type);
		markChanged();
		markSet(Metadata.FILE_MIMETYPE_LITERAL);
	}

	public void setDisplayName(String displayName) throws WebdavException {
		metadata.setFileName(displayName);
		markChanged();
		markSet(Metadata.FILENAME_LITERAL);
	}

	public void setLength(Long length) throws WebdavException {
		metadata.setFileSize(length);
		markChanged();
		markSet(Metadata.FILE_SIZE_LITERAL);
	}

	public void setSource(String source) throws WebdavException {
		// IGNORE
		
	}

	public void setLanguage(String language) throws WebdavException {
		// IGNORE
		
	}

	public void setId(int id) {
		this.id = id;
		propertyHelper.setId(id);
		lockHelper.setId(id);
	}

	public void setExists(boolean b) {
		exists = b;
	}
	
	
	
	// 

	@Override
	public WebdavResource move(String dest, boolean noroot, boolean overwrite) throws WebdavException {
		WebdavResource res = factory.resolveResource(dest);
		if(res.exists())
			if(!overwrite)
				throw new WebdavException(String.format("%s exists", dest), HttpServletResponse.SC_PRECONDITION_FAILED);
			else
				res.delete();
		int index = dest.lastIndexOf('/');
		String parent = dest.substring(0, index);
		String name = dest.substring(index+1);
		
		FolderCollection coll = (FolderCollection) factory.resolveCollection(parent);
		if(!coll.exists())
			throw new WebdavException(String.format("The folder %s doesn't exist",parent), HttpServletResponse.SC_CONFLICT);
		
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
		} catch (OXException e) {
			LOG.debug("",e);
			throw new WebdavException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this;
	}
	
	public WebdavResource copy(String dest, boolean noroot, boolean overwrite) throws WebdavException {
		int index = dest.lastIndexOf('/');
		String parent = dest.substring(0, index);
		String name = dest.substring(index+1);
		
		FolderCollection coll = (FolderCollection) factory.resolveCollection(parent);
		if(!coll.exists()) {
			throw new WebdavException(String.format("The folder %s doesn't exist", parent), HttpServletResponse.SC_CONFLICT);
		}	
		
		DocumentMetadataResource copy = (DocumentMetadataResource) factory.resolveResource(dest);
		if(copy.exists()) {
			if(!overwrite)
				throw new WebdavException(String.format("%s exists", dest), HttpServletResponse.SC_PRECONDITION_FAILED);
			else
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
		} catch (OXException e) {
			LOG.debug("",e);
			throw new WebdavException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return copy;
			
	}
	
	private void initDest(DocumentMetadataResource copy, String name, int parentId) {
		
		copy.metadata.setTitle(name);
		copy.metadata.setFileName(name);
		copy.metadata.setFolderId(parentId);
		
	}

	private void copyMetadata(DocumentMetadataResource copy) throws WebdavException {
		loadMetadata();
		copy.metadata = new DocumentMetadataImpl(metadata);
		copy.metadata.setId(InfostoreFacade.NEW);
		copy.metadataChanged = true;
		copy.setMetadata.addAll(Metadata.VALUES);
	}
	
	private void copyProperties(DocumentMetadataResource copy) throws WebdavException {
		for(WebdavProperty prop : internalGetAllProps()) {
			copy.putProperty(prop);
		}
	}
	
	private void copyBody(DocumentMetadataResource copy) throws WebdavException {
		InputStream in = getBody();
		if(in != null)
			copy.putBody(in);
	}

	private void loadMetadata() throws WebdavException {
		if(!exists)
			return;
		if(loadedMetadata)
			return;
		loadedMetadata = true;
		Set<Metadata> toLoad = new HashSet<Metadata>(Metadata.VALUES);
		toLoad.removeAll(setMetadata);
		SessionObject session = sessionHolder.getSessionObject();
		
		try {
			DocumentMetadata metadata = database.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, session.getContext(), session.getUserObject(), session.getUserConfiguration());
			SetSwitch set = new SetSwitch(this.metadata);
			GetSwitch get = new GetSwitch(metadata);
			
			for(Metadata m : toLoad) {
				set.setValue(m.doSwitch(get));
				m.doSwitch(set);
			}
		} catch (Exception x) {
			throw new WebdavException(x.getMessage(), x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void markSet(Metadata metadata) {
		setMetadata.add(metadata);
	}

	private void markChanged() {
		metadataChanged = true;
	}

	@Override
	public void putBody(InputStream body, boolean guessSize) throws WebdavException{
		if(!exists && !existsInDB) {
			try {
				dumpMetadataToDB();
			} catch (WebdavException x) {
				throw x;
			} catch (Exception x) {
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		SessionObject session = sessionHolder.getSessionObject();
		try {
			loadMetadata();
			if(guessSize)
				metadata.setFileSize(0);
			//FIXME Detonator Pattern
			database.saveDocument(metadata, body, Long.MAX_VALUE, session);
		} catch (Exception x) {
			throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void dumpMetadataToDB() throws OXException, IllegalAccessException, ConflictException, WebdavException {
		if((exists || existsInDB) && !metadataChanged)
			return;
		FolderCollection parent = null;
		try{
			parent = (FolderCollection) parent();
			if(!parent.exists()) {
				throw new WebdavException(getUrl(), HttpServletResponse.SC_CONFLICT);
			}
		} catch (ClassCastException x) {
			throw new WebdavException(getUrl(), HttpServletResponse.SC_CONFLICT);
		}
		SessionObject session = sessionHolder.getSessionObject();
		metadata.setFolderId(parent.getId());
		if(!exists && !existsInDB) {
			metadata.setVersion(InfostoreFacade.NEW);
			metadata.setId(InfostoreFacade.NEW);
			if(metadata.getFileName() == null || metadata.getFileName().trim().length()==0) {
				if(url.contains("/"))
					metadata.setFileName(url.substring(url.lastIndexOf("/")+1));
			}
			metadata.setTitle(metadata.getFileName());
			
			database.saveDocumentMetadata(metadata, InfostoreFacade.NEW, session);
			setId(metadata.getId());
		} else {
			if(setMetadata.contains(Metadata.FILENAME_LITERAL)) {
				metadata.setTitle(metadata.getFileName());
				setMetadata.add(Metadata.TITLE_LITERAL);
			} //FIXME Detonator Pattern
			database.saveDocumentMetadata(metadata, Long.MAX_VALUE, setMetadata.toArray(new Metadata[setMetadata.size()]),session);
		}
		existsInDB = true;
		setMetadata.clear();
		metadataChanged=false;
	}

	@OXThrows(
			category=Category.CONCURRENT_MODIFICATION,
			desc="The DocumentMetadata entry in the DB for the given resource could not be created. This is mostly due to someone else modifying the entry. This can also mean, that the entry has been deleted already.",
			exceptionId=0,
			msg="Could not delete DocumentMetadata %d. Please try again."
	)
	private void deleteMetadata() throws OXException, IllegalAccessException {
		SessionObject session = sessionHolder.getSessionObject();
		int[] nd = database.removeDocument(new int[]{ id }, Long.MAX_VALUE,session); //FIXME
		if(nd.length>0)
			throw EXCEPTIONS.create(0,nd[0]);
	}

	public int getId() {
		return id;
	}

	public int getParentId() throws WebdavException {
		loadMetadata();
		return (int) metadata.getFolderId();
	}

	public void removedParent() throws WebdavException {
		exists = false;
		factory.removed(this);
	}

	public void transferLock(WebdavLock lock) throws WebdavException {
		try {
			lockHelper.transferLock(lock);
		} catch (OXException e) {
			e.printStackTrace();
			throw new WebdavException(e.getMessage(),e,getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	public String toString(){
		return super.toString()+" :"+id;
	}
}
