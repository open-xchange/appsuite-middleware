/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
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
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
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
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class InfostoreWebdavFactory extends AbstractWebdavFactory implements BulkLoader {

    private static final Protocol PROTOCOL = new Protocol();

    /**
     * Defines if WebDAV resources should be moved to trash or deleted directly
     */
    private static final Property TRASH_USE_FOR_WEBDAV = DefaultProperty.valueOf("com.openexchange.infostore.trash.useForWebdav", Boolean.TRUE);

    private WebdavFolderAliases aliases;


    private static final class State {
        public final Map<String, DocumentMetadataResource> resources = new HashMap<String, DocumentMetadataResource>();
        public final Map<String, FolderCollection> folders = new HashMap<String, FolderCollection>();

        public final Map<String, DocumentMetadataResource> newResources = new HashMap<String, DocumentMetadataResource>();
        public final Map<String, FolderCollection> newFolders = new HashMap<String, FolderCollection>();

        public final Map<String, OXWebdavResource> lockNull = new HashMap<String, OXWebdavResource>();


        public final TIntObjectMap<FolderCollection> collectionsById = new TIntObjectHashMap<FolderCollection>();
        public final TIntObjectMap<DocumentMetadataResource> resourcesById = new TIntObjectHashMap<DocumentMetadataResource>();

        /**
         * Initializes a new {@link State}.
         */
        public State() {
            super();
        }

        public void addResource(final OXWebdavResource res) {
            if (res.isCollection()) {
                addCollection((FolderCollection)res);
            } else {
                addResource((DocumentMetadataResource)res);
            }
        }

        public void addResource(final DocumentMetadataResource resource) {
            resources.put(resource.getUrl().toString(), resource);
            resourcesById.put(resource.getId(), resource);
        }

        public void addCollection(final FolderCollection collection) {
            folders.put(collection.getUrl().toString(), collection);
            collectionsById.put(collection.getId(), collection);
        }

        public void invalidate(final WebdavPath url, final int id, final Type type) {
            String key = url.toString();

            lockNull.remove(key);
            switch (type) {
            case COLLECTION:
                folders.remove(key);
                newFolders.remove(key);
                collectionsById.remove(id);
                return;
            case RESOURCE:
                resources.remove(key);
                newResources.remove(key);
                resourcesById.remove(id);
                return;
            default :
                throw new IllegalArgumentException("Unkown Type "+type);
            }
        }

        public void remove(final OXWebdavResource resource) throws WebdavProtocolException {
            final int id = resource.getParentId();
            final FolderCollection coll = getFolder(id);
            if (coll == null) {
                return;
            }
            coll.unregisterChild(resource);
        }

        public void registerNew(final OXWebdavResource resource) throws WebdavProtocolException {
            if (resource.isCollection()) {
                collectionsById.put(resource.getId(), (FolderCollection) resource);
            } else {
                resourcesById.put(resource.getId(), (DocumentMetadataResource) resource);
            }
            final int id = resource.getParentId();
            final FolderCollection coll = getFolder(id);
            if (coll == null) {
                return;
            }
            coll.registerChild(resource);


        }

        private FolderCollection getFolder(final int id) {
            return collectionsById.get(id);
        }

        public void addNewResource(final OXWebdavResource res) {
            if (res.isCollection()) {
                newFolders.put(res.getUrl().toString(), (FolderCollection) res);
            } else {
                newResources.put(res.getUrl().toString(), (DocumentMetadataResource) res);
            }
        }

        public void addLockNull(final OXWebdavResource res) {
            lockNull.put(res.getUrl().toString(), res);
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
        String key = url.toString();
        final State s = state.get();
        WebdavCollection collection = s.folders.get(key);
        if (null != collection) {
            return collection;
        }
        collection = s.newFolders.get(key);
        if (null != collection) {
            return collection;
        }
        OXWebdavResource lockNullResource = s.lockNull.get(key);
        if (null != lockNullResource) {
            InfostoreLockNullResource res = (InfostoreLockNullResource) lockNullResource;
            res.setResource(new FolderCollection(url, this));
            return res;
        }
        OXWebdavResource res;
        try {
            res =  tryLoad(url, new FolderCollection(url, this));
            if (res.isLockNull()) {
                s.addLockNull(res);
            } else if (res.exists()) {
                s.addResource(res);
            } else {
                s.addNewResource(res);
            }
        } catch (OXException e) {
            throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        if (!res.isCollection()) {
            throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_PRECONDITION_FAILED);
        }
        return (WebdavCollection) res;
    }

    @Override
    public WebdavResource resolveResource(final WebdavPath url) throws WebdavProtocolException {
        String key = url.toString();
        final State s = state.get();
        WebdavResource resource = s.resources.get(key);
        if (null != resource) {
            return resource;
        }
        resource = s.folders.get(key);
        if (null != resource) {
            return resource;
        }
        resource = s.newResources.get(key);
        if (null != resource) {
            return resource;
        }
        resource = s.newFolders.get(key);
        if (null != resource) {
            return resource;
        }
        OXWebdavResource lockNullResource = s.lockNull.get(key);
        if (null != lockNullResource) {
            InfostoreLockNullResource res = (InfostoreLockNullResource) lockNullResource;
            res.setResource(new DocumentMetadataResource(url, this));
            return res;
        }
        try {
            final OXWebdavResource res = tryLoad(url, new DocumentMetadataResource(url,this));
            if (res.isLockNull()) {
                s.addLockNull(res);
            } else if (res.exists()) {
                s.addResource(res);
            } else {
                s.addNewResource(res);
            }
            return res;
        } catch (OXException e) {
            throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Set<TransactionAware> services(){
        return this.services;
    }

    private OXWebdavResource tryLoad(final WebdavPath url, final OXWebdavResource def) throws OXException {
        final State s = state.get();
        final ServerSession session = ServerSessionAdapter.valueOf(getSession());
        final Context ctx = session.getContext();
        try {
            final Resolved resolved = resolver.resolve(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, url, session);
            if (resolved.isFolder()) {

                return loadCollection(url, resolved.getId(), s);
            }
            final DocumentMetadataResource resource = new DocumentMetadataResource(url, this);
            resource.setId(resolved.getId());
            resource.setExists(true);
            s.addResource(resource);
            return resource;
        } catch (OXException x) {
            if (x.isGeneric(Generic.NOT_FOUND)) {
                Connection readCon = null;
                try {
                    readCon = provider.getReadConnection(ctx);
                    final int lockNullId = InfostoreLockNullResource.findInfostoreLockNullResource(url, readCon, ctx);
                    if (lockNullId>0) {
                        return new InfostoreLockNullResource((AbstractResource) def, this,lockNullId);
                    }
                } catch (OXException e) {
                    throw e;
                } finally {
                    if (readCon != null) {
                        provider.releaseReadConnection(ctx, readCon);
                    }
                }
                return def;
            }
            throw x;
        }
    }

    private FolderCollection loadCollection(final WebdavPath url, final int id, final State s) {
        final FolderCollection collection = new FolderCollection(url, this);
        collection.setId(id);
        collection.setExists(true);
        s.addCollection(collection);
        if (url == null) {
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
        if (this.database != null) {
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
        if (this.security instanceof TransactionAware) {
            removeService((TransactionAware) this.security);
        }
        this.security = security;
        if (this.security instanceof TransactionAware) {
            addService((TransactionAware) this.security);
        }
       }

    public InfostoreSecurity getSecurity() {
        return security;
    }

    public void setDatabase(final InfostoreFacade database){
        if (this.sessionHolder != null) {
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

    public Collection<? extends OXWebdavResource> getCollections(final List<Integer> subfolderIds) {
        final State s = state.get();
        final Set<Integer> toLoad = new HashSet<Integer>(subfolderIds);
        final List<OXWebdavResource> retVal = new ArrayList<OXWebdavResource>(subfolderIds.size());
        for(final int id : subfolderIds) {
            if (toLoad.contains(Integer.valueOf(id)) && s.collectionsById.containsKey(id)) {
                retVal.add(s.collectionsById.get(id));
                toLoad.remove(Integer.valueOf(id));
            }
        }
        if (subfolderIds.isEmpty()) {
            return retVal;
        }

        for(final int id : toLoad) {
            retVal.add(loadCollection(null, id, s)); // FIXME 101 SELECT PROBLEM
        }

        return retVal;
    }

    public Collection<? extends OXWebdavResource> getResourcesInFolder(final FolderCollection collection, final int folderId) throws OXException {
        if (folderId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
            return new ArrayList<OXWebdavResource>();
        }
        final State s = state.get();
        final ServerSession session = ServerSessionAdapter.valueOf(getSession());
        final EffectivePermission perm = collection.getEffectivePermission();
        if (!(perm.canReadAllObjects() || perm.canReadOwnObjects())) {
            return new ArrayList<OXWebdavResource>();
        }

        SearchIterator<DocumentMetadata> iter = database.getDocuments(folderId, session).results();
        try {
            final List<OXWebdavResource> retVal = new ArrayList<OXWebdavResource>();
            while (iter.hasNext()) {
                final DocumentMetadata docMeta = iter.next();
                if (null == docMeta.getFileName() || docMeta.getFileName().equals("")) {
                    continue;
                }
                DocumentMetadataResource res = s.resourcesById.get(docMeta.getId());
                if (res == null) {
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
        if (null == service) {
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
            } catch (OXException e) {
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
            } catch (OXException e) {
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

    @Override
    public Session getSession() {
        return sessionHolder.getSessionObject();
    }

    /**
    * Returns if the trash folder should be considered when using WebDAV
    *
    * @param session The session
    * @return True, if the trash folder for WeDAV should be used, false otherwise
    * @throws OXException
    */
    public boolean isTrashEnabled(Session session) throws OXException {
        LeanConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(LeanConfigurationService.class, true);
        return configurationService.getBooleanProperty(session.getUserId(), session.getContextId(), TRASH_USE_FOR_WEBDAV);
    }
}
