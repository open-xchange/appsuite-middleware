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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
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
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBProviderUser;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.FolderLockManager;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.PathResolver;
import com.openexchange.groupware.infostore.Resolved;
import com.openexchange.groupware.infostore.WebdavFolderAliases;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurity;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tx.TransactionAware;
import com.openexchange.webdav.loader.BulkLoader;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;
import com.openexchange.webdav.protocol.helpers.AbstractWebdavFactory;

public class InfostoreWebdavFactory extends AbstractWebdavFactory implements BulkLoader {

    private static final Protocol PROTOCOL = new Protocol();
    private WebdavFolderAliases aliases;


    private static final class State {
        public final Map<WebdavPath, DocumentMetadataResource> resources = new HashMap<WebdavPath, DocumentMetadataResource>();
        public final Map<WebdavPath, FolderCollection> folders = new HashMap<WebdavPath, FolderCollection>();

        public final Map<WebdavPath, DocumentMetadataResource> newResources = new HashMap<WebdavPath, DocumentMetadataResource>();
        public final Map<WebdavPath, FolderCollection> newFolders = new HashMap<WebdavPath, FolderCollection>();

        public final Map<WebdavPath, OXWebdavResource> lockNull = new HashMap<WebdavPath, OXWebdavResource>();


        public final TIntObjectMap<FolderCollection> collectionsById = new TIntObjectHashMap<FolderCollection>();
        public final TIntObjectMap<DocumentMetadataResource> resourcesById = new TIntObjectHashMap<DocumentMetadataResource>();

        public void addResource(final OXWebdavResource res) {
            if(res.isCollection()) {
                addCollection((FolderCollection)res);
            } else {
                addResource((DocumentMetadataResource)res);
            }
        }

        public void addResource(final DocumentMetadataResource resource) {
            resources.put(resource.getUrl(), resource);
            resourcesById.put(resource.getId(), resource);
        }

        public void addCollection(final FolderCollection collection) {
            folders.put(collection.getUrl(), collection);
            collectionsById.put(collection.getId(), collection);
        }

        public void invalidate(final WebdavPath url, final int id, final Type type) {

            lockNull.remove(url);
            switch(type) {
            case COLLECTION:
                folders.remove(url);
                newFolders.remove(url);
                collectionsById.remove(id);
                return;
            case RESOURCE:
                resources.remove(url);
                newResources.remove(url);
                resourcesById.remove(id);
                return;
            default :
                throw new IllegalArgumentException("Unkown Type "+type);
            }
        }

        public void remove(final OXWebdavResource resource) throws WebdavProtocolException {
            final int id = resource.getParentId();
            final FolderCollection coll = getFolder(id);
            if(coll == null) {
                return;
            }
            coll.unregisterChild(resource);
        }

        public void registerNew(final OXWebdavResource resource) throws WebdavProtocolException {
            if(resource.isCollection()) {
                collectionsById.put(resource.getId(), (FolderCollection) resource);
            } else {
                resourcesById.put(resource.getId(), (DocumentMetadataResource) resource);
            }
            final int id = resource.getParentId();
            final FolderCollection coll = getFolder(id);
            if(coll == null) {
                return;
            }
            coll.registerChild(resource);


        }

        private FolderCollection getFolder(final int id) {
            return collectionsById.get(id);
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

    private final Set<TransactionAware> services = new HashSet<TransactionAware>();

    private final ThreadLocal<State> state = new ThreadLocal<State>();
    private PathResolver resolver;
    private SessionHolder sessionHolder;
    private EntityLockManager lockNullLockManager;
    private EntityLockManager infoLockManager;
    private FolderLockManager folderLockManager;
    private PropertyStore infoProperties;
    private PropertyStore folderProperties;
    private InfostoreFacade database;
    private InfostoreSecurity security;
    private DBProvider provider;

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreWebdavFactory.class);


    @Override
    public Protocol getProtocol() {
        return PROTOCOL;
    }

    @Override
    public WebdavCollection resolveCollection(final WebdavPath url) throws WebdavProtocolException {
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
            throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        if(!res.isCollection()) {
            throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_PRECONDITION_FAILED);
        }
        return (WebdavCollection) res;
    }

    @Override
    public WebdavResource resolveResource(final WebdavPath url) throws WebdavProtocolException {
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
            return res;
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
            throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }



    private Set<TransactionAware> services(){
        return this.services;
    }

    private OXWebdavResource tryLoad(final WebdavPath url, final OXWebdavResource def) throws OXException {
        final State s = state.get();
        final ServerSession session = getSession();
        final Context ctx = session.getContext();
        try {
            final Resolved resolved = resolver.resolve(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, url, session);
            if(resolved.isFolder()) {

                return loadCollection(url, resolved.getId(), s);
            }
            final DocumentMetadataResource resource = new DocumentMetadataResource(url, this);
            resource.setId(resolved.getId());
            resource.setExists(true);
            s.addResource(resource);
            return resource;
        } catch (final OXException x) {
            if (x.isGeneric(Generic.NOT_FOUND)) {
                Connection readCon = null;
                try {
                    readCon = provider.getReadConnection(ctx);
                    final int lockNullId = InfostoreLockNullResource.findInfostoreLockNullResource(url, readCon, ctx);
                    if(lockNullId>0) {
                        return new InfostoreLockNullResource((AbstractResource) def, this,lockNullId);
                    }
                } catch (final OXException e) {
                    throw e;
                } finally {
                    if(readCon != null) {
                        provider.releaseReadConnection(ctx, readCon);
                    }
                }
                return def;
            }
            throw x;
        }
    }

    private FolderCollection loadCollection(final WebdavPath url, final int id, final State s) throws WebdavProtocolException {
        final FolderCollection collection = new FolderCollection(url, this);
        collection.setId(id);
        collection.setExists(true);
        s.addCollection(collection);
        if(url == null) {
            collection.initUrl();
        }
        return collection;
    }

    @Override
    public void load(final LoadingHints loading) {
        load(Arrays.asList(loading));
    }

    @Override
    public void load(final List<LoadingHints> hints) {
        // Nothing to do

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
        if(this.database != null) {
            this.database.setSessionHolder(sessionHolder);
        }
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

    public void setSecurity(final InfostoreSecurity security){
        if(this.security instanceof TransactionAware) {
            removeService((TransactionAware) this.security);
        }
        this.security = security;
        if(this.security instanceof TransactionAware) {
            addService((TransactionAware) this.security);
        }
       }

    public InfostoreSecurity getSecurity() {
        return security;
    }

    public void setDatabase(final InfostoreFacade database){
        if(this.sessionHolder != null) {
            database.setSessionHolder(sessionHolder);
        }
        removeService(this.database);
        this.database=database;

        addService(this.database);
    }

    public void setAliases(final WebdavFolderAliases aliases) {
        this.aliases = aliases;
    }

    public WebdavFolderAliases getAliases() {
        return aliases;
    }

    public Collection<? extends OXWebdavResource> getCollections(final List<Integer> subfolderIds) throws WebdavProtocolException {
        final State s = state.get();
        final Set<Integer> toLoad = new HashSet<Integer>(subfolderIds);
        final List<OXWebdavResource> retVal = new ArrayList<OXWebdavResource>(subfolderIds.size());
        for(final int id : subfolderIds) {
            if(toLoad.contains(Integer.valueOf(id)) && s.collectionsById.containsKey(id)) {
                retVal.add(s.collectionsById.get(id));
                toLoad.remove(Integer.valueOf(id));
            }
        }
        if(subfolderIds.isEmpty()) {
            return retVal;
        }

        for(final int id : toLoad) {
            try {
                retVal.add(loadCollection(null, id, s)); // FIXME 101 SELECT PROBLEM
            } catch (final WebdavProtocolException x) {
                //System.out.println(x.getStatus());
                if(x.getStatus() != HttpServletResponse.SC_FORBIDDEN) {
                    throw x;
                }
            }
        }

        return retVal;
    }

    public Collection<? extends OXWebdavResource> getResourcesInFolder(final FolderCollection collection, final int folderId) throws OXException, IllegalAccessException, OXException {
        if(folderId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
            return new ArrayList<OXWebdavResource>();
        }
        final State s = state.get();
        final ServerSession session = getSession();
        final EffectivePermission perm = collection.getEffectivePermission();
        if(!(perm.canReadAllObjects() || perm.canReadOwnObjects())) {
            return new ArrayList<OXWebdavResource>();
        }

        SearchIterator<DocumentMetadata> iter = database.getDocuments(folderId, session).results();
        try {
            final List<OXWebdavResource> retVal = new ArrayList<OXWebdavResource>();
            while(iter.hasNext()) {
                final DocumentMetadata docMeta = iter.next();
                if(null == docMeta.getFileName() || docMeta.getFileName().equals("")) {
                    continue;
                }
                DocumentMetadataResource res = s.resourcesById.get(docMeta.getId());
                if(res == null) {
                    res = new DocumentMetadataResource(collection.getUrl().dup().append(docMeta.getFileName()), docMeta, this);
                    s.addResource(res);
                }
                retVal.add(res);
            }
            return retVal;
        } finally {
            SearchIterators.close(iter);
        }
    }

    private void addService(final TransactionAware service) {
        services.add(service);
        if (service instanceof DBProviderUser) {
            final DBProviderUser defService = (DBProviderUser) service;
            defService.setProvider(getProvider());
        }
    }

    private void removeService(final TransactionAware service) {
        if(null == service) {
            return;
        }
        services.remove(service);
    }

    @Override
    public void beginRequest() {
        state.set(new State());
        for(final TransactionAware service : services()) {
            try {
                service.startTransaction();
            } catch (final OXException e) {
                LOG.error("",e);
            }
        }
    }

    @Override
    public void endRequest(final int status) {
        state.set(null);
        for (final TransactionAware service : services()) {
            try {
                service.finish();
            } catch (final OXException e) {
                LOG.error("",e);
            }
        }
    }

    public DBProvider getProvider() {
        return provider;
    }

    public void setProvider(final DBProvider provider) {
        this.provider = provider;
        for(final TransactionAware service : services()) {
            if (service instanceof DBProviderUser) {
                final DBProviderUser defService = (DBProviderUser) service;
                defService.setProvider(getProvider());
            }
        }
    }

    public void invalidate(final WebdavPath url, final int id, final Type type) {
        final State s = state.get();
        s.invalidate(url,id,type);

        for(final TransactionAware service : services) {
            if (service instanceof URLCache) {
                final URLCache urlCache = (URLCache) service;
                urlCache.invalidate(url,id,type);
            }
        }
    }

    public void created(final DocumentMetadataResource resource) throws WebdavProtocolException {
        final State s = state.get();
        s.registerNew(resource);
    }

    public void created(final FolderCollection collection) throws WebdavProtocolException {
        final State s = state.get();
        s.registerNew(collection);
    }

    public void removed(final OXWebdavResource resource) throws WebdavProtocolException {
        invalidate(resource.getUrl(), resource.getId(), (resource.isCollection()) ? Type.COLLECTION : Type.RESOURCE );
        final State s = state.get();
        s.remove(resource);
    }

    public ServerSession getSession() throws OXException {
        try {
            return ServerSessionAdapter.valueOf(sessionHolder.getSessionObject());
        } catch (final OXException e) {
            throw e;
        }
    }

}
