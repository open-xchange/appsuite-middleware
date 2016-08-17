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

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.EffectiveInfostoreFolderPermission;
import com.openexchange.groupware.infostore.WebdavFolderAliases;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurity;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;

public class FolderCollection extends AbstractCollection implements OXWebdavResource {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderCollection.class);
	private final InfostoreWebdavFactory factory;
	private WebdavPath url;
	private final PropertyHelper propertyHelper;
	private final SessionHolder sessionHolder;
	private final FolderLockHelper lockHelper;

	private FolderObject folder;
	private int id;
	private boolean exists;
	private boolean loaded;
	private final DBProvider provider;
	private final Set<OXWebdavResource> children = new HashSet<OXWebdavResource>();

	private boolean loadedChildren;
	private ArrayList<OCLPermission> overrideNewACL;
    private final InfostoreSecurity security;

    private final WebdavFolderAliases aliases;

    public FolderCollection(final WebdavPath url, final InfostoreWebdavFactory factory) {
		this(url,factory,null);
	}

	public FolderCollection(final WebdavPath url, final InfostoreWebdavFactory factory, final FolderObject folder) {
		this.url = url;
		this.factory = factory;
		this.sessionHolder = factory.getSessionHolder();
		this.propertyHelper = new PropertyHelper(factory.getFolderProperties(), sessionHolder, url);
		this.lockHelper = new FolderLockHelper(factory.getFolderLockManager(), sessionHolder, url);
        this.security = factory.getSecurity();
        this.provider = factory.getProvider();
        this.aliases = factory.getAliases();
        if(folder!=null) {
			setId(folder.getObjectID());
			this.folder = folder;
			this.loaded = true;
			this.exists = true;
		}
	}

	@Override
	public void delete() throws WebdavProtocolException {
		if(!exists) {
			return;
		}
		Connection con = null;
		try {
			con = provider.getWriteConnection(getSession().getContext());
			final OXFolderManager oxma = OXFolderManager.getInstance(getSession(), con, con);
			oxma.deleteFolder(new FolderObject(id), true, System.currentTimeMillis());
			exists = false;
			factory.removed(this);
		} catch (final OXException x) {
			if(isPermissionException(x)) {
			    throw WebdavProtocolException.generalError(x, url, HttpServletResponse.SC_FORBIDDEN);
			}
			throw WebdavProtocolException.generalError(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (final Exception e) {
		    throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			if(con != null) {
				provider.releaseWriteConnection(getSession().getContext(), con);
			}
		}
		final Set<OXWebdavResource> set = new HashSet<OXWebdavResource>(children);
		for(final OXWebdavResource res : set) {
			res.removedParent();
		}
	}

	private WebdavResource mergeTo(final FolderCollection to, final boolean move, final boolean overwrite) throws WebdavProtocolException {


		final int lengthUrl = getUrl().size();

		for(final WebdavResource res : getChildren()) {
			final WebdavPath toUrl = to.getUrl().dup().append(res.getUrl().subpath(lengthUrl));
			if(move) {
				res.move(toUrl, false, overwrite);
			} else {
				res.copy(toUrl, false, overwrite);
			}

		}

		return this;
	}

	@Override
	public WebdavResource move(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavProtocolException {
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
		final String name = dest.name();
		final int parentId =  ((OXWebdavResource) coll.parent()).getId();


        folder.setFolderName(name);
		folder.setParentFolderID(parentId);


		invalidate();
		factory.invalidate(url, id, Type.COLLECTION);
		factory.invalidate(dest, id, Type.COLLECTION);


		url = dest;
		save();
		try {
			lockHelper.deleteLocks();
		} catch (final OXException e) {
			throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this;
	}



	@Override
	public WebdavResource copy(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavProtocolException {
		final FolderCollection coll = (FolderCollection) factory.resolveCollection(dest);
		if(coll.exists()) {
			if (overwrite) {
				final ArrayList<OCLPermission> override = new ArrayList<OCLPermission>();
				loadFolder();
				for(final OCLPermission perm : folder.getPermissions()) {
					override.add(perm.deepClone());
				}
				coll.loadFolder();
				coll.folder.setPermissions(override);
				coll.save();
			}
		} else {
			loadFolder();
			final ArrayList<OCLPermission> override = new ArrayList<OCLPermission>();
			for(final OCLPermission perm : folder.getPermissions()) {
				override.add(perm.deepClone());
			}
			coll.overrideNewACL = override;
			coll.create();
			copyProperties(coll);
		}
		return mergeTo(coll, false, overwrite);
	}

	private void copyProperties(final FolderCollection coll) throws WebdavProtocolException {
		for(final WebdavProperty prop : internalGetAllProps()) {
			coll.putProperty(prop);
		}
	}

	@Override
	protected WebdavCollection parent() throws WebdavProtocolException {
		if(url != null) {
			return super.parent();
		}
		loadFolder();
		return (WebdavCollection) factory.getCollections(Arrays.asList(Integer.valueOf(folder.getParentFolderID()))).iterator().next();
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

	@Override
	protected void internalDelete(){
		throw new IllegalStateException("Should be called only by superclass");
	}

	@Override
	protected WebdavFactory getFactory() {
		return factory;
	}

	@Override
	protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
		return propertyHelper.getAllProps();
	}

	@Override
	protected WebdavProperty internalGetProperty(final String namespace, final String name) throws WebdavProtocolException {
		return propertyHelper.getProperty(namespace, name);
	}

	@Override
	protected void internalPutProperty(final WebdavProperty prop) throws WebdavProtocolException {
		propertyHelper.putProperty(prop);
	}

	@Override
	protected void internalRemoveProperty(final String namespace, final String name) throws WebdavProtocolException {
		propertyHelper.removeProperty(namespace, name);
	}

	@Override
	protected boolean isset(final Property p) {
		if (p.getId() == Protocol.GETCONTENTLANGUAGE || p.getId() == Protocol.GETCONTENTLENGTH || p.getId() == Protocol.GETETAG) {
			return false;
		}
		return !propertyHelper.isRemoved(new WebdavProperty(p.getNamespace(), p.getName()));
	}

	@Override
	public void setCreationDate(final Date date) throws WebdavProtocolException {
		folder.setCreationDate(date);
	}

	@Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
		loadChildren();
		return new ArrayList<WebdavResource>(children);
	}
	@Override
    public void create() throws WebdavProtocolException {
		if(exists) {
		    throw WebdavProtocolException.Code.DIRECTORY_ALREADY_EXISTS.create(getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
		save();
		exists=true;
		factory.created(this);
	}

	@Override
    public boolean exists() throws WebdavProtocolException {
		return exists;
	}

	@Override
    public Date getCreationDate() throws WebdavProtocolException {
		loadFolder();
		return folder.getCreationDate();
	}

	@Override
    public String getDisplayName() throws WebdavProtocolException {
		loadFolder();
		return getFolderName(folder);
	}

	@Override
    public Date getLastModified() throws WebdavProtocolException {
		loadFolder();
		return folder.getLastModified();
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
		// IGNORE
		return null;
	}

	@Override
    public WebdavPath getUrl() {
		if(url == null) {
			initUrl();
		}
		return url;
	}

	@Override
    public void lock(final WebdavLock lock) throws WebdavProtocolException {
		lockHelper.addLock(lock);
	}

	@Override
    public void save() throws WebdavProtocolException {
		try {
			dumpToDB();
            if(propertyHelper.mustWrite()) {
                final ServerSession session = getSession();
                final EffectiveInfostoreFolderPermission perm = security.getFolderPermission(getId(),session.getContext(), UserStorage.getInstance().getUser(session.getUserId(), session.getContext()),
					UserPermissionBitsStorage.getInstance().getUserPermissionBits(session.getUserId(), session.getContext()));
                if(!perm.isFolderAdmin()) {
                    throw WebdavProtocolException.Code.NO_WRITE_PERMISSION.create(getUrl(), HttpServletResponse.SC_FORBIDDEN);
                }
            }
            propertyHelper.dumpPropertiesToDB();
			lockHelper.dumpLocksToDB();
			exists = true;
		} catch (WebdavProtocolException x) {
			throw x;
		} catch (final Exception x) {
		    throw WebdavProtocolException.generalError(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
    public void setDisplayName(final String displayName) throws WebdavProtocolException {
		//loadFolder();
		//folder.setFolderName(displayName);
		//changedFields.add(FolderObject.FOLDER_NAME);
		//FIXME
	}

	@Override
    public void unlock(final String token) throws WebdavProtocolException {
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

	private void loadFolder() throws WebdavProtocolException {
		if(loaded) {
			return;
		}
		loaded = true;
		if(!exists) {
			folder = new FolderObject();
			return;
		}
		Connection readCon = null;
		final Context ctx = getSession().getContext();
		try {
			readCon = provider.getReadConnection(ctx);
			if(FolderCacheManager.isEnabled()) {
				folder = FolderCacheManager.getInstance().getFolderObject(id, true, ctx, readCon); // FIXME be smarter here
			} else {

				folder = FolderObject.loadFolderObjectFromDB(id, ctx, readCon);

			}
		} catch (final Exception e) {
		    throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			provider.releaseReadConnection(ctx, readCon);
		}
	}

	private void dumpToDB() throws WebdavProtocolException {
		//OXFolderAction oxfa = new OXFolderAction(getSession());
		if(exists) {
			if(folder == null) {
				return;
			}
			folder.setLastModified(new Date());
			folder.setModifiedBy(getSession().getUserId()); // Java train of death
			initParent(folder);
			final ServerSession session = getSession();
			final Context ctx = session.getContext();

			Connection writeCon = null;

			try {

				writeCon = provider.getWriteConnection(ctx);
				final OXFolderManager oxma = OXFolderManager.getInstance(getSession(), writeCon, writeCon);
				oxma.updateFolder(folder, true, false, System.currentTimeMillis());
				//oxfa.updateMoveRenameFolder(folder, session, true, folder.getLastModified().getTime(), writeCon, writeCon);
			} catch (final OXException x) {
				if(isPermissionException(x)) {
				    throw WebdavProtocolException.generalError(x, url, HttpServletResponse.SC_FORBIDDEN);
				}
				throw WebdavProtocolException.generalError(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (final Exception e) {
			    throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} finally {
				provider.releaseWriteConnection(ctx, writeCon);
			}
		} else {
			if(folder == null) {
				folder = new FolderObject();
			}

			Context ctx = null;
			Connection writeCon = null;
			try {
			    initDefaultAcl(folder);
			    initDefaultFields(folder);

			    final ServerSession session = getSession();
                ctx = session.getContext();
				writeCon = provider.getWriteConnection(ctx);
				final OXFolderManager oxma = OXFolderManager.getInstance(getSession(), writeCon, writeCon);
				folder = oxma.createFolder(folder, true, System.currentTimeMillis());
				//oxfa.createFolder(folder, session, true, writeCon, writeCon, true);
				setId(folder.getObjectID());
            } catch (WebdavProtocolException x) {
                throw x; // re-throw
			} catch (final OXException x) {
				if(isPermissionException(x)) {
				    throw WebdavProtocolException.generalError(x, url, HttpServletResponse.SC_FORBIDDEN);
				}
				throw WebdavProtocolException.generalError(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (final Exception e) {
			    throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} finally {
				provider.releaseWriteConnection(ctx, writeCon);
			}
		}

	}

	private boolean isPermissionException(final OXException x) {
		return Category.CATEGORY_PERMISSION_DENIED.equals(x.getCategory());
	}

	private void initDefaultFields(final FolderObject folder) throws WebdavProtocolException {
		initParent(folder);
		folder.setModule(FolderObject.INFOSTORE);
		if (folder.getFolderName() == null || folder.getFolderName().length() == 0) {
			//if(url.contains("/")) {
				folder.setFolderName(url.name());
			//}
		}
        folder.removeObjectID();
    }

	private void initParent(final FolderObject folder) throws WebdavProtocolException{
		try {
			final FolderCollection parent = (FolderCollection) parent();
			if(!parent.exists()) {
				throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_CONFLICT);
			}
			folder.setParentFolderID(parent.id);
            if (null != parent.folder && FolderObject.SYSTEM_TYPE != parent.folder.getType()) {
                folder.setType(parent.folder.getType());
            } else {
                folder.setType(FolderObject.PUBLIC);
            }
		} catch (final ClassCastException x) {
			throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_CONFLICT);
		}

	}

	private void initDefaultAcl(final FolderObject folder) throws OXException {

		final List<OCLPermission> copyPerms;

		if(this.overrideNewACL == null) {
			final FolderCollection parent = (FolderCollection) parent();
			parent.loadFolder();
			final FolderObject parentFolder = parent.folder;
			if(FolderObject.SYSTEM_MODULE == parentFolder.getType()) {
			    copyPerms = Collections.emptyList();
			} else {
	            copyPerms = parentFolder.getPermissions();
			}
		} else {
			copyPerms = this.overrideNewACL;
		}

		final ArrayList<OCLPermission> newPerms = new ArrayList<OCLPermission>();

		final User owner = UserStorage.getInstance().getUser(getSession().getUserId(), getSession().getContext());

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

	private void loadChildren() throws WebdavProtocolException {
		if(loadedChildren || !exists) {
			return;
		}
		loadedChildren = true;
		SearchIterator<FolderObject> iter = null;
        try {
            if (folder == null) {
                loadFolder();
            }
            final ServerSession session = getSession();
            final User user = UserStorage.getInstance().getUser(session.getUserId(), session.getContext());
            final UserPermissionBits userPermissionBits = UserPermissionBitsStorage.getInstance().getUserPermissionBits(session.getUserId(), session.getContext());
            final Context ctx = session.getContext();

            iter = OXFolderIteratorSQL.getVisibleSubfoldersIterator(id, user.getId(), user.getGroups(), ctx, userPermissionBits, new Timestamp(0));
            while (iter.hasNext()) {
                final FolderObject folder = iter.next();
                if (FolderObject.TRASH == folder.getType()) {
                    continue; // skip trash folder
                }
                final WebdavPath newUrl = getUrl().dup().append(getFolderName(folder));
                children.add(new FolderCollection(newUrl, factory, folder));
            }
            children.addAll(factory.getResourcesInFolder(this, folder.getObjectID()));
        } catch (final WebdavProtocolException e) {
            throw e;
        } catch (final Exception e) {
		    throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
		    SearchIterators.close(iter);
		}
		// Duplicates?
	}

	@Override
    public int getId() {
		return id;
	}

	public void initUrl() {
		if(id == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
			url = new WebdavPath();
			return;
		}
		try {
			url = parent().getUrl().dup().append(getDisplayName());
		} catch (final OXException e) {
			LOG.error("", e);
		}
	}

	public void registerChild(final OXWebdavResource resource) {
		children.add(resource);
	}

	public void unregisterChild(final OXWebdavResource resource) {
		children.remove(resource);
	}

	@Override
    public int getParentId() throws WebdavProtocolException {
		if(exists) {
			loadFolder();
			return folder.getParentFolderID();
		}
		final WebdavPath url = getUrl();
		return ((OXWebdavResource) factory.resolveCollection(url.parent())).getId();
	}

	@Override
    public void removedParent() throws WebdavProtocolException {
		exists = false;
		factory.removed(this);
		for(final OXWebdavResource res : children) { res.removedParent(); }
	}

	@Override
    public void transferLock(final WebdavLock lock) throws WebdavProtocolException {
		try {
			lockHelper.transferLock(lock);
		} catch (final OXException e) {
		    throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public String toString(){
		return super.toString()+" :"+id;
	}

    public String getFolderName(final FolderObject folder) {
        if(aliases != null) {
            final String alias = aliases.getAlias(folder.getObjectID());
            if(alias != null) {
                return alias;
            }
        }
        return folder.getFolderName();
    }

    public EffectivePermission getEffectivePermission() throws WebdavProtocolException {
        loadFolder();
        final ServerSession session = getSession();

        final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext());
        final Context ctx = session.getContext();

        Connection con = null;
        try {
            con = provider.getReadConnection(ctx);
            return folder.getEffectiveUserPermission(session.getUserId(), userConfig, con);
        } catch (final Exception e) {
            throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {

            if (con != null) {
                provider.releaseReadConnection(ctx, con);
            }
        }

    }

    public boolean isRoot() {
        return id == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID;
    }

     private ServerSession getSession() {
        return ServerSessionAdapter.valueOf(sessionHolder.getSessionObject(), sessionHolder.getContext());
    }
}
