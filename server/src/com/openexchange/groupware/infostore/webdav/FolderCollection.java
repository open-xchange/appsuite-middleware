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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.cache.FolderCacheNotEnabledException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionHolder;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.tools.oxfolder.OXFolderPermissionException;
import com.openexchange.tools.oxfolder.OXFolderTools;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.impl.AbstractCollection;

public class FolderCollection extends AbstractCollection implements OXWebdavResource {

	private static final Log LOG = LogFactory.getLog(FolderCollection.class);
	private InfostoreWebdavFactory factory;
	private String url;
	private PropertyHelper propertyHelper;
	private SessionHolder sessionHolder;
	private FolderLockHelper lockHelper;

	private FolderObject folder = null;
	private int id;
	private boolean exists;
	private boolean loaded;
	private DBProvider provider;
	private final Set<OXWebdavResource> children = new HashSet<OXWebdavResource>();
	
	private boolean loadedChildren;
	private ArrayList<OCLPermission> overrideNewACL;
	
	public FolderCollection(final String url, final InfostoreWebdavFactory factory) {
		this(url,factory,null);
	}
	
	public FolderCollection(final String url, final InfostoreWebdavFactory factory, final FolderObject folder) {
		this.url = url;
		this.factory = factory;
		this.sessionHolder = factory.getSessionHolder();
		this.propertyHelper = new PropertyHelper(factory.getFolderProperties(), sessionHolder, url);
		this.lockHelper = new FolderLockHelper(factory.getFolderLockManager(), sessionHolder, url);
		this.provider = factory.getProvider();
		if(folder!=null) {
			setId(folder.getObjectID());
			this.folder = folder;
			this.loaded = true;
			this.exists = true;
		}
	}

	@Override
	public void delete() throws WebdavException {
		if(!exists) {
			return;
		}
//		OXFolderManager oxma = new OXFolderManagerImpl(sessionHolder.getSessionObject());
//		OXFolderAction oxfa = new OXFolderAction(sessionHolder.getSessionObject());
		Connection con = null;
		try {
			con = provider.getWriteConnection(sessionHolder.getSessionObject().getContext());
			final OXFolderManager oxma = new OXFolderManagerImpl(sessionHolder.getSessionObject(), con, con);
			oxma.deleteFolder(new FolderObject(id), true, System.currentTimeMillis());
			//oxfa.deleteFolder(id, sessionHolder.getSessionObject(),con, con, true,System.currentTimeMillis()); // FIXME
			exists = false;
			factory.removed(this);
		} catch (final OXFolderException x) {
			if(isPermissionException(x)) {
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_FORBIDDEN);
			}
			throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);			
		} catch (final Exception e) {
			throw new WebdavException(e.getMessage(), e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			if(con != null) {
				provider.releaseWriteConnection(sessionHolder.getSessionObject().getContext(), con);
			}
		}
		final Set<OXWebdavResource> set = new HashSet<OXWebdavResource>(children);
		for(final OXWebdavResource res : set) {
			res.removedParent();
		}
	}
	
	private WebdavResource mergeTo(final FolderCollection to, final boolean move, final boolean overwrite) throws WebdavException {
		final StringBuilder builder = new StringBuilder(to.getUrl());
		if(!(builder.charAt(builder.length()-1)!='/')) {
			builder.append('/');
		}
		
		final int lengthUrl = getUrl().length();
		final int resetTo = builder.length();
		
		
		for(final WebdavResource res : getChildren()) {
			builder.append(res.getUrl().substring(lengthUrl));
			if(move) {
				res.move(builder.toString(), false, overwrite);
			} else {
				res.copy(builder.toString(), false, overwrite);
			}
			builder.setLength(resetTo);
		}
		
		return this;
	}
	
	@Override
	public WebdavResource move(final String dest, final boolean noroot, final boolean overwrite) throws WebdavException {
		final FolderCollection coll = (FolderCollection) factory.resolveCollection(dest);
		if(coll.exists()) {
			if(overwrite) {
				loadFolder();
				final ArrayList<OCLPermission> override = new ArrayList<OCLPermission>();
				for(final OCLPermission perm : folder.getPermissions()) {
					override.add(perm.deepClone());
				}
				coll.loadFolder();
				coll.folder.setPermissions(override);
				coll.save();
			}
			final WebdavResource moved = mergeTo(coll, true, overwrite);
			delete();
			return moved;
		}
		loadFolder();
		final int index = dest.lastIndexOf('/');
		final String name = dest.substring(index+1);
		
		folder.setFolderName(name);
		folder.setParentFolderID(((OXWebdavResource) coll.parent()).getId());
		
		
		invalidate();
		factory.invalidate(url, id, Type.COLLECTION);
		factory.invalidate(dest, id, Type.COLLECTION);
		
		
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
	public WebdavResource copy(final String dest, final boolean noroot, final boolean overwrite) throws WebdavException {
		final FolderCollection coll = (FolderCollection) factory.resolveCollection(dest);
		if(!coll.exists()) {
			loadFolder();
			final ArrayList<OCLPermission> override = new ArrayList<OCLPermission>();
			for(final OCLPermission perm : folder.getPermissions()) {
				override.add(perm.deepClone());
			}
			coll.overrideNewACL = override;
			coll.create();
			copyProperties(coll);
		} else if (overwrite) {
			final ArrayList<OCLPermission> override = new ArrayList<OCLPermission>();
			loadFolder();
			for(final OCLPermission perm : folder.getPermissions()) {
				override.add(perm.deepClone());
			}
			coll.loadFolder();
			coll.folder.setPermissions(override);
			coll.save();
		}
		return mergeTo(coll, false, overwrite);
	}

	private void copyProperties(final FolderCollection coll) throws WebdavException {
		for(final WebdavProperty prop : internalGetAllProps()) {
			coll.putProperty(prop);
		}
	}

	@Override
	protected WebdavCollection parent() throws WebdavException {
		if(url != null) {
			return super.parent();
		}
		loadFolder();
		return (WebdavCollection) factory.getCollections(Arrays.asList(folder.getParentFolderID())).iterator().next();
	}

	private void invalidate() {
		for(final OXWebdavResource res : children) {
			Type t = Type.RESOURCE;
			if(res.isCollection()) {
				((FolderCollection) res).invalidate();
				t = Type.COLLECTION;
			}
			factory.invalidate(res.getUrl(), res.getId(), t);
		}
	}

	protected void internalDelete(){
		throw new IllegalStateException("Should be called only by superclass");
	}

	@Override
	protected WebdavFactory getFactory() {
		return factory;
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
		switch(p.getId()) {
		case Protocol.GETCONTENTLANGUAGE : case Protocol.GETCONTENTLENGTH : case Protocol.GETETAG :
			return false;
		default: return !propertyHelper.isRemoved(new WebdavProperty(p.getNamespace(), p.getName()));
		}
	}

	@Override
	public void setCreationDate(final Date date) throws WebdavException {
		folder.setCreationDate(date);
	}

	public List<WebdavResource> getChildren() throws WebdavException {
		loadChildren();
		return new ArrayList<WebdavResource>(children);
	}
	public void create() throws WebdavException {
		if(exists) {
			throw new WebdavException("The directory exists already", getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
		save();
		exists=true;
		factory.created(this);
	}

	public boolean exists() throws WebdavException {
		return exists;
	}

	public Date getCreationDate() throws WebdavException {
		loadFolder();
		return folder.getCreationDate();
	}

	public String getDisplayName() throws WebdavException {
		loadFolder();
		return folder.getFolderName();
	}

	public Date getLastModified() throws WebdavException {
		loadFolder();
		return folder.getLastModified();
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
		// IGNORE
		return null;
	}

	public String getUrl() {
		if(url == null) {
			initUrl();
		}
		return url;
	}

	public void lock(final WebdavLock lock) throws WebdavException {
		lockHelper.addLock(lock);
	}
	
	public void save() throws WebdavException {
		try {
			dumpToDB();
			propertyHelper.dumpPropertiesToDB();
			lockHelper.dumpLocksToDB();
		} catch (final WebdavException x) {
			throw x;
		} catch (final Exception x) {
			throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public void setDisplayName(final String displayName) throws WebdavException {
		//loadFolder();
		//folder.setFolderName(displayName);
		//changedFields.add(FolderObject.FOLDER_NAME);
		//FIXME
	}

	public void unlock(final String token) throws WebdavException {
		lockHelper.removeLock(token);
	}

	public void setId(final int id) {
		this.id = id;
		this.propertyHelper.setId(id);
		this.lockHelper.setId(id);
	}

	public void setExists(final boolean b) {
		this.exists = b;
	}
		
	private void loadFolder() throws WebdavException {
		if(loaded) {
			return;
		}
		loaded = true;
		if(!exists) {
			folder = new FolderObject();
			return;
		}
		Connection readCon = null;
		final Context ctx = sessionHolder.getSessionObject().getContext();
		try {
			readCon = provider.getReadConnection(ctx);
			if(FolderCacheManager.isEnabled()) {
				folder = FolderCacheManager.getInstance().getFolderObject(id, false, ctx, readCon); // FIXME be smarter here
			} else {
				
				folder = FolderObject.loadFolderObjectFromDB(id, ctx, readCon);
				
			}
		} catch (final FolderCacheNotEnabledException e) {
			LOG.error("",e);
		} catch (final Exception e) {
			throw new WebdavException(e.getMessage(), e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			provider.releaseReadConnection(ctx, readCon);
		}
	}
	
	private void dumpToDB() throws WebdavException {
		//OXFolderAction oxfa = new OXFolderAction(sessionHolder.getSessionObject());
		if(!exists) {
			if(folder == null) {
				folder = new FolderObject();
			}
			initDefaultAcl(folder);
			initDefaultFields(folder);
			
			final SessionObject session = sessionHolder.getSessionObject();
			final Context ctx = session.getContext();
			
			Connection writeCon = null;
			
			try {
				writeCon = provider.getWriteConnection(ctx);
				final OXFolderManager oxma = new OXFolderManagerImpl(sessionHolder.getSessionObject(), writeCon, writeCon);
				folder = oxma.createFolder(folder, true, System.currentTimeMillis());
				//oxfa.createFolder(folder, session, true, writeCon, writeCon, true);
				setId(folder.getObjectID());
			} catch (final OXFolderException x) {
				if(isPermissionException(x)) {
					throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_FORBIDDEN);
				} else {
					throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} catch (final OXFolderPermissionException e) {
				throw new WebdavException(e.getMessage(), e, getUrl(), HttpServletResponse.SC_FORBIDDEN);
			} catch (final Exception e) {
				throw new WebdavException(e.getMessage(), e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} finally {
				provider.releaseWriteConnection(ctx, writeCon);
			}
		} else {
			if(folder == null) {
				return;
			}
			folder.setLastModified(new Date());
			folder.setModifiedBy(sessionHolder.getSessionObject().getUserObject().getId()); // Java train of death
			initParent(folder);
			final SessionObject session = sessionHolder.getSessionObject();
			final Context ctx = session.getContext();
			
			Connection writeCon = null;
			
			try {
				
				writeCon = provider.getWriteConnection(ctx);
				final OXFolderManager oxma = new OXFolderManagerImpl(sessionHolder.getSessionObject(), writeCon, writeCon);
				oxma.updateFolder(folder, true, System.currentTimeMillis());
				//oxfa.updateMoveRenameFolder(folder, session, true, folder.getLastModified().getTime(), writeCon, writeCon);
			} catch (final OXFolderException x) {
				if(isPermissionException(x)) {
					throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_FORBIDDEN);
				}
				throw new WebdavException(x.getMessage(), x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (final OXFolderPermissionException e) {
				throw new WebdavException(e.getMessage(), e, getUrl(), HttpServletResponse.SC_FORBIDDEN);
			} catch (final Exception e) {
				throw new WebdavException(e.getMessage(), e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} finally {
				provider.releaseWriteConnection(ctx, writeCon);
			}
		}
		
	}
	
	private boolean isPermissionException(final OXFolderException x) {
		return Category.PERMISSION.equals(x.getCategory());
	}

	private void initDefaultFields(final FolderObject folder) throws WebdavException {
		initParent(folder);
		folder.setType(FolderObject.PUBLIC);
		folder.setModule(FolderObject.INFOSTORE);
		if((folder.getFolderName() == null || folder.getFolderName().length() == 0) && url.contains("/")) {
			//if(url.contains("/")) {
				folder.setFolderName(url.substring(url.lastIndexOf('/')+1));
			//}
		}
	}
	
	private void initParent(final FolderObject folder) throws WebdavException{
		try {
			final FolderCollection parent = (FolderCollection) parent();
			if(!parent.exists()) {
				throw new WebdavException(getUrl(), HttpServletResponse.SC_CONFLICT);
			}
			folder.setParentFolderID(parent.id);	
		} catch (final ClassCastException x) {
			throw new WebdavException(getUrl(), HttpServletResponse.SC_CONFLICT);
		}
		
	}

	private void initDefaultAcl(final FolderObject folder) throws WebdavException {
		
		List<OCLPermission> copyPerms = null;
		
		if(this.overrideNewACL != null) {
			copyPerms = this.overrideNewACL;
		} else {
			final FolderCollection parent = (FolderCollection) parent();
			parent.loadFolder();
			final FolderObject parentFolder = parent.folder;
			copyPerms = parentFolder.getPermissions();			
		}
		
		final ArrayList<OCLPermission> newPerms = new ArrayList<OCLPermission>();

		final User owner = sessionHolder.getSessionObject().getUserObject();
		
		for(final OCLPermission perm : copyPerms) {
			if(perm.getEntity() != owner.getId()){
				newPerms.add(perm.deepClone());
			} 
		}
		
		
		// Owner has all permissions
		final OCLPermission perm = new OCLPermission();
		perm.setEntity(owner.getId());
		perm.setFolderAdmin(true);
		perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
		perm.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
		perm.setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS);
		perm.setGroupPermission(false);
		newPerms.add(perm);
		
		// All others may read and write
		
		/*OCLPermission perm2 = new OCLPermission();
		perm2.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
		perm2.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
		perm2.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
		perm2.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
		perm2.setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS); */
		folder.setPermissions(newPerms);
	}

	private void loadChildren() throws WebdavException {
		if(loadedChildren || !exists) {
			return;
		}
		loadedChildren = true;
		try {
			if(folder==null) {
				loadFolder();
			}
			final SessionObject session = sessionHolder.getSessionObject();
			final User user = session.getUserObject();
			final UserConfiguration userConfig = session.getUserConfiguration();
			final Context ctx = session.getContext();
			
			final SearchIterator iter = OXFolderTools.getVisibleSubfoldersIterator(id, user.getId(),user.getGroups(), ctx, userConfig, new Timestamp(0));
			
			final StringBuilder urlBuilder = new StringBuilder(getUrl());
			urlBuilder.append('/');
			final int resetTo = urlBuilder.length();
			
			while(iter.hasNext()) {
				final FolderObject folder = (FolderObject) iter.next();
				urlBuilder.append(folder.getFolderName());
				
				children.add(new FolderCollection(urlBuilder.toString(), factory, folder));
				
				urlBuilder.setLength(resetTo);
			}
			
			//children.addAll(factory.getCollections(folder.getSubfolderIds(true, sessionHolder.getSessionObject().getContext())));
			children.addAll(factory.getResourcesInFolder(this, folder.getObjectID()));
		} catch (final Exception e) {
			throw new WebdavException(e.getMessage(), e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		// Duplicates?
	}

	public int getId() {
		return id;
	}

	public void initUrl() {
		if(id == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
			url = "/";
			return;
		}
		try {
			url = parent().getUrl()+'/'+getDisplayName();
		} catch (final WebdavException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public void registerChild(final OXWebdavResource resource) {
		children.add(resource);
	}
	
	public void unregisterChild(final OXWebdavResource resource) {
		children.remove(resource);
	}

	public int getParentId() throws WebdavException {
		if(exists) {
			loadFolder();
			return folder.getParentFolderID();
		}
		final String url = getUrl();
		return ((OXWebdavResource) factory.resolveCollection(url.substring(0,url.lastIndexOf('/')))).getId();
	}

	public void removedParent() throws WebdavException {
		exists = false;
		factory.removed(this);
		for(final OXWebdavResource res : children) { res.removedParent(); }
	}

	public void transferLock(final WebdavLock lock) throws WebdavException {
		try {
			lockHelper.transferLock(lock);
		} catch (final OXException e) {
			throw new WebdavException(e.getMessage(), e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	@Override
	public String toString(){
		return super.toString()+" :"+id;
	}
	
}
