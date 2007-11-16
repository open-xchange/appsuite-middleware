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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.FolderLockManager;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.PathResolver;
import com.openexchange.groupware.infostore.Resolved;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBProviderUser;
import com.openexchange.groupware.tx.Service;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.sessiond.Session;
import com.openexchange.sessiond.impl.SessionHolder;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.webdav.loader.BulkLoader;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.impl.AbstractResource;

public class InfostoreWebdavFactory implements WebdavFactory, BulkLoader {
	
	private static final Protocol PROTOCOL = new Protocol();
	
	private static final class State {
		public final Map<String, DocumentMetadataResource> resources = new HashMap<String, DocumentMetadataResource>();
		public final Map<String, FolderCollection> folders = new HashMap<String, FolderCollection>();
		
		public final Map<String, DocumentMetadataResource> newResources = new HashMap<String, DocumentMetadataResource>();
		public final Map<String, FolderCollection> newFolders = new HashMap<String, FolderCollection>();
	
		public final Map<String, OXWebdavResource> lockNull = new HashMap<String, OXWebdavResource>();
		
		
		public final Map<Integer, FolderCollection> collectionsById = new HashMap<Integer, FolderCollection>();
		public final Map<Integer, DocumentMetadataResource> resourcesById = new HashMap<Integer, DocumentMetadataResource>();
		
		public void addResource(final OXWebdavResource res) {
			if(res.isCollection()) {
				addCollection((FolderCollection)res);
			} else {
				addResource((DocumentMetadataResource)res);
			}
		}

		public void addResource(final DocumentMetadataResource resource) {
			resources.put(resource.getUrl(), resource);
			resourcesById.put(Integer.valueOf(resource.getId()), resource);
		}

		public void addCollection(final FolderCollection collection) {
			folders.put(collection.getUrl(), collection);
			collectionsById.put(Integer.valueOf(collection.getId()), collection);
		}
		
		public void invalidate(final String url, final int id, final Type type) {
			lockNull.remove(url);
			switch(type) {
			case COLLECTION:
				folders.remove(url);
				newFolders.remove(url);
				collectionsById.remove(Integer.valueOf(id));
				return;
			case RESOURCE:
				resources.remove(url);
				newResources.remove(url);
				resourcesById.remove(url);
				return;
			default :
				throw new IllegalArgumentException("Unkown Type "+type);
			}
		}
		
		public void remove(final OXWebdavResource resource) throws WebdavException {
			final int id = resource.getParentId();
			final FolderCollection coll = getFolder(id);
			if(coll == null) {
				return;
			}
			coll.unregisterChild(resource);
		}
		
		public void registerNew(final OXWebdavResource resource) throws WebdavException {
			if(resource.isCollection()) {
				collectionsById.put(Integer.valueOf(resource.getId()), (FolderCollection) resource);
			} else {
				resourcesById.put(Integer.valueOf(resource.getId()), (DocumentMetadataResource) resource);
			}
			final int id = resource.getParentId();
			final FolderCollection coll = getFolder(id);
			if(coll == null) {
				return;
			}
			coll.registerChild(resource);
			
			
		}

		private FolderCollection getFolder(final int id) {
			return collectionsById.get(Integer.valueOf(id));
		}

		public void addNewResource(final OXWebdavResource res) {
			if(res.isCollection()) {
				newFolders.put(res.getUrl(), (FolderCollection) res);
			} else {
				newResources.put(res.getUrl(), (DocumentMetadataResource) res);
			}
		}

		public void addLockNull(final OXWebdavResource res) {
			lockNull.put(res.getUrl(), res);
		}
	}
	
	private final Set<Service> services = new HashSet<Service>();

	private final ThreadLocal<State> state = new ThreadLocal<State>();
	private PathResolver resolver;
	private SessionHolder sessionHolder;
	private EntityLockManager lockNullLockManager;
	private EntityLockManager infoLockManager;
	private FolderLockManager folderLockManager;
	private PropertyStore infoProperties;
	private PropertyStore folderProperties;
	private InfostoreFacade database;
	private DBProvider provider;
	
	private final Log LOG = LogFactory.getLog(InfostoreWebdavFactory.class);

	
	public Protocol getProtocol() {
		return PROTOCOL;
	}
	
	public WebdavCollection resolveCollection(String url)
			throws WebdavException {
		url = normalize(url);
		final State s = state.get();
		if(s.folders.containsKey(url)) {
			return s.folders.get(url);
		}
		if(s.newFolders.containsKey(url)) {
			return s.newFolders.get(url);
		}
		if(s.lockNull.containsKey(url)) {
			final InfostoreLockNullResource res = (InfostoreLockNullResource) s.lockNull.get(url);
			res.setResource(new FolderCollection(url, this));
			return res;
		}
		OXWebdavResource res;
		try {
			res =  tryLoad(url, new FolderCollection(url, this));
			if (res.isLockNull()) {
				s.addLockNull(res);
			} else if(res.exists()) {
				s.addResource(res);
			} else {
				s.addNewResource(res);
			}
		} catch (final OXException e) {
			throw new WebdavException(e.getMessage(),e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		if(!res.isCollection()) {
			throw new WebdavException(url, HttpServletResponse.SC_PRECONDITION_FAILED);
		}
		return (WebdavCollection) res;
	}

	public WebdavResource resolveResource(String url) throws WebdavException {
		url = normalize(url);
		final State s = state.get();
		if(s.resources.containsKey(url)) {
			return s.resources.get(url);
		}
		if(s.folders.containsKey(url)) {
			return s.folders.get(url);
		}
		if(s.newResources.containsKey(url)) {
			return s.newResources.get(url);
		}
		if(s.newFolders.containsKey(url)) {
			return s.newFolders.get(url);
		}
		if (s.lockNull.containsKey(url)) {
			final InfostoreLockNullResource res = (InfostoreLockNullResource) s.lockNull.get(url);
			res.setResource(new DocumentMetadataResource(url,this));
		}
		try {
			final OXWebdavResource res = tryLoad(url, new DocumentMetadataResource(url,this));	
			if (res.isLockNull()) {
				s.addLockNull(res);
			} else if(res.exists()) {
				s.addResource(res);
			} else {
				s.addNewResource(res);
			}
			return res;
		} catch (final OXException e) {
			throw new WebdavException(e.getMessage(),e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private Set<Service> services(){
		return this.services;
	}
	
	private OXWebdavResource tryLoad(final String url, final OXWebdavResource def) throws OXException, WebdavException {
		final State s = state.get();
		final Context ctx = sessionHolder.getSessionObject().getContext();
		final Session session = sessionHolder.getSessionObject();
		try {
			final Resolved resolved = resolver.resolve(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, url, session
					.getContext(), UserStorage.getStorageUser(session.getUserId(), session.getContext()), UserConfigurationStorage.getInstance()
					.getUserConfigurationSafe(session.getUserId(), session.getContext()));
			if(resolved.isFolder()) {
				
				return loadCollection(url, resolved.getId(), s);
			}
			final DocumentMetadataResource resource = new DocumentMetadataResource(url, this);
			resource.setId(resolved.getId());
			resource.setExists(true);
			s.addResource(resource);
			return resource;
		} catch (final OXObjectNotFoundException x) {
			Connection readCon = null;
			try {
				readCon = provider.getReadConnection(ctx);
				final int lockNullId = InfostoreLockNullResource.findInfostoreLockNullResource(url, readCon, ctx);
				if(lockNullId>0) {
					return new InfostoreLockNullResource((AbstractResource) def, this,lockNullId);
				}
			} finally {
				if(readCon != null) {
					provider.releaseReadConnection(ctx, readCon);
				}
			}
			return def;
		}
	}
	
	private FolderCollection loadCollection(final String url, final int id, final State s) throws WebdavException {
		final FolderCollection collection = new FolderCollection(url, this);
		collection.setId(id);
		collection.setExists(true);
		s.addCollection(collection);
		if(url == null) {
			collection.initUrl();
		}
		return collection;
	}

	public void load(final LoadingHints loading) {
		load(Arrays.asList(loading));
	}

	public void load(final List<LoadingHints> hints) {
		// TODO Auto-generated method stub
		
	}

	public PathResolver getResolver() {
		return resolver;
	}

	public void setResolver(final PathResolver resolver) {
		removeService(this.resolver);
		this.resolver = resolver;
		addService(this.resolver);
	}

	public SessionHolder getSessionHolder() {
		return sessionHolder;
	}

	public void setSessionHolder(final SessionHolder sessionHolder) {
		this.sessionHolder = sessionHolder;
	}

	public FolderLockManager getFolderLockManager() {
		return folderLockManager;
	}

	public void setFolderLockManager(final FolderLockManager folderLockManager) {
		removeService(this.folderLockManager);
		this.folderLockManager = folderLockManager;
		addService(this.folderLockManager);
	}

	public PropertyStore getFolderProperties() {
		return folderProperties;
	}

	public void setFolderProperties(final PropertyStore folderProperties) {
		removeService(this.folderProperties);
		this.folderProperties = folderProperties;
		addService(this.folderProperties);
	}

	public EntityLockManager getInfoLockManager() {
		return infoLockManager;
	}

	public void setInfoLockManager(final EntityLockManager infoLockManager) {
		removeService(this.infoLockManager);
		this.infoLockManager = infoLockManager;
		addService(this.infoLockManager);
	}

	public PropertyStore getInfoProperties() {
		return infoProperties;
	}

	public void setInfoProperties(final PropertyStore infoProperties) {
		removeService(this.infoProperties);
		this.infoProperties = infoProperties;
		addService(this.infoProperties);
	}
	
	public EntityLockManager getLockNullLockManager() {
		return lockNullLockManager;
	}
	
	public void setLockNullLockManager(final EntityLockManager infoLockManager) {
		removeService(this.lockNullLockManager);
		this.lockNullLockManager = infoLockManager;
		addService(this.lockNullLockManager);
	}

	public InfostoreFacade getDatabase() {
		return database;
	}
	
	public void setDatabase(final InfostoreFacade database){
		removeService(this.database);
		this.database=database;
		addService(this.database);
	}

	public Collection<? extends OXWebdavResource> getCollections(final List<Integer> subfolderIds) throws WebdavException {
		final State s = state.get();
		final Set<Integer> toLoad = new HashSet<Integer>(subfolderIds);
		final List<OXWebdavResource> retVal = new ArrayList<OXWebdavResource>(subfolderIds.size());
		for(final int id : subfolderIds) {
			if(toLoad.contains(Integer.valueOf(id)) && s.collectionsById.containsKey(Integer.valueOf(id))) {
				retVal.add(s.collectionsById.get(Integer.valueOf(id)));
				toLoad.remove(Integer.valueOf(id));
			}
		}
		if(subfolderIds.isEmpty()) {
			return retVal;
		}
		
		for(final int id : toLoad) {
			try {
				retVal.add(loadCollection(null, id, s)); // FIXME 101 SELECT PROBLEM
			} catch (final WebdavException x) {
				//System.out.println(x.getStatus());
				if(x.getStatus() != HttpServletResponse.SC_FORBIDDEN) {
					throw x;
				}
			}
		}
		
		return retVal;
	}

	public Collection<? extends OXWebdavResource> getResourcesInFolder(final FolderCollection collection, final int folderId) throws OXException, IllegalAccessException, SearchIteratorException {
		if(folderId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
			return new ArrayList<OXWebdavResource>();
		}
		final State s = state.get();
		final Session session = sessionHolder.getSessionObject();
		final SearchIterator<?> iter = database.getDocuments(
				folderId,
				session.getContext(),
				UserStorage.getStorageUser(session.getUserId(), session.getContext()),
				UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext())).results();
		final List<OXWebdavResource> retVal = new ArrayList<OXWebdavResource>();
		
		while(iter.hasNext()) {
			final DocumentMetadata docMeta = (DocumentMetadata) iter.next();
			if(null == docMeta.getFileName() || docMeta.getFileName().equals("")) {
				continue;
			}
			DocumentMetadataResource res = s.resourcesById.get(Integer.valueOf(docMeta.getId()));
			if(res == null) {
				res = new DocumentMetadataResource(collection.getUrl()+"/"+docMeta.getFileName(), docMeta, this);
				s.addResource(res);
			}
			retVal.add(res);
		}
		return retVal;
	}
	
	private void addService(final Service service) {
		services.add(service);
		if (service instanceof DBProviderUser) {
			final DBProviderUser defService = (DBProviderUser) service;
			defService.setProvider(getProvider());
		}
	}

	private void removeService(final Service service) {
		if(null == service) {
			return;
		}
		services.remove(service);
	}

	public void beginRequest() {
		state.set(new State());
		for(final Service service : services()) {
			try {
				service.startTransaction();
			} catch (final TransactionException e) {
				LOG.error("",e);
			}
		}
	}

	public void endRequest(final int status) {
		state.set(null);
		for (final Service service : services()) {
			try {
				service.finish();
			} catch (final TransactionException e) {
				LOG.error("",e);
			}
		}
	}

	public DBProvider getProvider() {
		return provider;
	}

	public void setProvider(final DBProvider provider) {
		this.provider = provider;
		for(final Service service : services()) {
			if (service instanceof DBProviderUser) {
				final DBProviderUser defService = (DBProviderUser) service;
				defService.setProvider(getProvider());
			}
		}
	}

	public void invalidate(final String url, final int id, final Type type) {
		final State s = state.get();
		s.invalidate(url,id,type);
		
		for(final Service service : services) {
			if (service instanceof URLCache) {
				final URLCache urlCache = (URLCache) service;
				urlCache.invalidate(url,id,type);
			}
		}
	}

	public void created(final DocumentMetadataResource resource) throws WebdavException {
		final State s = state.get();
		s.registerNew(resource);
	}

	public void created(final FolderCollection collection) throws WebdavException {
		final State s = state.get();
		s.registerNew(collection);
	}
	
	public void removed(final OXWebdavResource resource) throws WebdavException {
		invalidate(resource.getUrl(), resource.getId(), (resource.isCollection()) ? Type.COLLECTION : Type.RESOURCE );
		final State s = state.get();
		s.remove(resource);
	}
	
	private final String normalize(String url) {
		if(url.length()==0) {
			return "/";
		}
		url = url.replaceAll("/+", "/");
		if(url.charAt(url.length()-1)=='/') {
			return url.substring(0,url.length()-1);
		}
		return url;
	}

}
