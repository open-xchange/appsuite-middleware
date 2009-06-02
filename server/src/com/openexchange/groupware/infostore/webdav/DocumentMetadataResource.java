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
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.infostore.ConflictException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreException;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.GetSwitch;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurity;
import com.openexchange.groupware.infostore.database.impl.SetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.webdav.URLCache.Type;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.sessiond.impl.SessionHolder;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.impl.AbstractResource;

@OXExceptionSource(classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DOCUMENTMETADATARESOURCE, component = EnumComponent.INFOSTORE)
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

    private final InfostoreSecurity security;

    private boolean loadedMetadata;

    private boolean existsInDB;

    private final LockHelper lockHelper;

    private boolean metadataChanged;

    public DocumentMetadataResource(final WebdavPath url, final InfostoreWebdavFactory factory) {
        this.factory = factory;
        this.url = url;
        this.sessionHolder = factory.getSessionHolder();
        this.lockHelper = new EntityLockHelper(factory.getInfoLockManager(), sessionHolder, url);
        this.database = factory.getDatabase();
        this.security = factory.getSecurity();
        this.propertyHelper = new PropertyHelper(factory.getInfoProperties(), sessionHolder, url);
    }

    public DocumentMetadataResource(final WebdavPath url, final DocumentMetadata docMeta, final InfostoreWebdavFactory factory) {
        this.factory = factory;
        this.url = url;
        this.sessionHolder = factory.getSessionHolder();
        this.lockHelper = new EntityLockHelper(factory.getInfoLockManager(), sessionHolder, url);
        this.database = factory.getDatabase();
        this.security = factory.getSecurity();
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
    public boolean hasBody() throws WebdavProtocolException {
        loadMetadata();
        return metadata.getFileSize() > 0;
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
        /*
         * if(p.getId() == Protocol.GETCONTENTLANGUAGE) { return false; }
         */
        return !propertyHelper.isRemoved(new WebdavProperty(p.getNamespace(), p.getName()));
    }

    @Override
    public void setCreationDate(final Date date) throws WebdavProtocolException {
        metadata.setCreationDate(date);
        markChanged();
        markSet(Metadata.CREATION_DATE_LITERAL);
    }

    public void create() throws WebdavProtocolException {
        if (exists) {
            throw new WebdavProtocolException(
                WebdavProtocolException.Code.DIRECTORY_ALREADY_EXISTS,
                getUrl(),
                HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        save();
        exists = true;
        factory.created(this);
    }

    public void delete() throws WebdavProtocolException {
        if (exists) {
            try {
                lockHelper.deleteLocks();
                propertyHelper.deleteProperties();
                deleteMetadata();
                exists = false;
                factory.removed(this);
            } catch (final InfostoreException x) {
                if (InfostoreExceptionFactory.isPermissionException(x)) {
                    throw new WebdavProtocolException(x, getUrl(), HttpServletResponse.SC_FORBIDDEN);
                }
                throw new WebdavProtocolException(x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (final AbstractOXException x) {
                throw new WebdavProtocolException(x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (final Exception x) {
                throw new WebdavProtocolException(x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    public boolean exists() throws WebdavProtocolException {
        return exists;
    }

    public InputStream getBody() throws WebdavProtocolException {
        final ServerSession session = getSession();
        try {
            return database.getDocument(id, InfostoreFacade.CURRENT_VERSION, session.getContext(), UserStorage.getStorageUser(
                session.getUserId(),
                session.getContext()), UserConfigurationStorage.getInstance().getUserConfigurationSafe(
                session.getUserId(),
                session.getContext()));
        } catch (final AbstractOXException e) {
            throw new WebdavProtocolException(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final Exception e) {
            throw new WebdavProtocolException(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public String getContentType() throws WebdavProtocolException {
        loadMetadata();
        return metadata.getFileMIMEType();
    }

    public Date getCreationDate() throws WebdavProtocolException {
        loadMetadata();
        return metadata.getCreationDate();
    }

    public String getDisplayName() throws WebdavProtocolException {
        loadMetadata();
        return metadata.getFileName();
    }

    public String getETag() throws WebdavProtocolException {
        if (!exists && !existsInDB) {
            /*
             * try { dumpMetadataToDB(); } catch (Exception e) { throw new WebdavException(e.getMessage(), e, getUrl(),
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

    public String getLanguage() throws WebdavProtocolException {
        return null;
    }

    public Date getLastModified() throws WebdavProtocolException {
        loadMetadata();
        return metadata.getLastModified();
    }

    public Long getLength() throws WebdavProtocolException {
        loadMetadata();
        return Long.valueOf(metadata.getFileSize());
    }

    public WebdavLock getLock(final String token) throws WebdavProtocolException {
        final WebdavLock lock = lockHelper.getLock(token);
        if (lock != null) {
            return lock;
        }
        return findParentLock(token);
    }

    public List<WebdavLock> getLocks() throws WebdavProtocolException {
        final List<WebdavLock> lockList = getOwnLocks();
        addParentLocks(lockList);
        return lockList;
    }

    public WebdavLock getOwnLock(final String token) throws WebdavProtocolException {
        return lockHelper.getLock(token);
    }

    public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return lockHelper.getAllLocks();
    }

    public String getSource() throws WebdavProtocolException {
        return null;
    }

    public WebdavPath getUrl() {
        return url;
    }

    public void lock(final WebdavLock lock) throws WebdavProtocolException {
        if (!exists) {
            new InfostoreLockNullResource(this, factory).lock(lock);
            factory.invalidate(getUrl(), getId(), Type.RESOURCE);
            return;
        }
        lockHelper.addLock(lock);
        try {
            touch();
        } catch (OXException e) {
            throw new WebdavProtocolException(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void unlock(final String token) throws WebdavProtocolException {
        lockHelper.removeLock(token);
        try {
            touch();
            lockHelper.dumpLocksToDB();
        } catch (final OXException e) {
            throw new WebdavProtocolException(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void save() throws WebdavProtocolException {
        try {
            dumpMetadataToDB();
            if (propertyHelper.mustWrite()) {
                final ServerSession session = getSession();
                final Context ctx = session.getContext();
                final int userId = session.getUserId();
                final EffectiveInfostorePermission perm = security.getInfostorePermission(getId(), ctx, UserStorage.getStorageUser(
                    userId,
                    ctx), UserConfigurationStorage.getInstance().getUserConfigurationSafe(userId, ctx));
                if (!perm.canWriteObject()) {
                    throw new WebdavProtocolException(
                        WebdavProtocolException.Code.NO_WRITE_PERMISSION,
                        getUrl(),
                        HttpServletResponse.SC_FORBIDDEN);
                }
            }
            propertyHelper.dumpPropertiesToDB();
            lockHelper.dumpLocksToDB();
        } catch (final WebdavProtocolException x) {
            throw x;
        } catch (final InfostoreException x) {
            if (InfostoreExceptionFactory.isPermissionException(x)) {
                throw new WebdavProtocolException(x, getUrl(), HttpServletResponse.SC_FORBIDDEN);
            }
            throw new WebdavProtocolException(x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final AbstractOXException x) {
            throw new WebdavProtocolException(x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final Exception x) {
            throw new WebdavProtocolException(x, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void setContentType(final String type) throws WebdavProtocolException {
        metadata.setFileMIMEType(type);
        markChanged();
        markSet(Metadata.FILE_MIMETYPE_LITERAL);
    }

    public void setDisplayName(final String displayName) throws WebdavProtocolException {
        metadata.setFileName(displayName);
        markChanged();
        markSet(Metadata.FILENAME_LITERAL);
    }

    public void setLength(final Long length) throws WebdavProtocolException {
        metadata.setFileSize(length.longValue());
        markChanged();
        markSet(Metadata.FILE_SIZE_LITERAL);
    }

    public void setSource(final String source) throws WebdavProtocolException {
        // IGNORE

    }

    public void setLanguage(final String language) throws WebdavProtocolException {
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
    public WebdavResource move(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavProtocolException {
        final WebdavResource res = factory.resolveResource(dest);
        if (res.exists()) {
            if (!overwrite) {
                throw new WebdavProtocolException(
                    WebdavProtocolException.Code.FILE_ALREADY_EXISTS,
                    getUrl(),
                    HttpServletResponse.SC_PRECONDITION_FAILED,
                    dest);
            }
            res.delete();
        }
        final WebdavPath parent = dest.parent();
        final String name = dest.name();

        final FolderCollection coll = (FolderCollection) factory.resolveCollection(parent);
        if (!coll.exists()) {
            throw new WebdavProtocolException(
                WebdavProtocolException.Code.FOLDER_NOT_FOUND,
                getUrl(),
                HttpServletResponse.SC_CONFLICT,
                parent);
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
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return this;
    }

    @Override
    public WebdavResource copy(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavProtocolException {

        final WebdavPath parent = dest.parent();
        final String name = dest.name();

        final FolderCollection coll = (FolderCollection) factory.resolveCollection(parent);
        if (!coll.exists()) {
            throw new WebdavProtocolException(
                WebdavProtocolException.Code.FOLDER_NOT_FOUND,
                getUrl(),
                HttpServletResponse.SC_CONFLICT,
                parent);
        }

        final DocumentMetadataResource copy = (DocumentMetadataResource) factory.resolveResource(dest);
        if (copy.exists()) {
            if (!overwrite) {
                throw new WebdavProtocolException(
                    WebdavProtocolException.Code.FILE_ALREADY_EXISTS,
                    getUrl(),
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
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return copy;

    }

    private void initDest(final DocumentMetadataResource copy, final String name, final int parentId) {

        copy.metadata.setTitle(name);
        copy.metadata.setFileName(name);
        copy.metadata.setFolderId(parentId);

    }

    private void copyMetadata(final DocumentMetadataResource copy) throws WebdavProtocolException {
        loadMetadata();
        copy.metadata = new DocumentMetadataImpl(metadata);
        copy.metadata.setFilestoreLocation(null); // No file attachment in original version
        copy.metadata.setId(InfostoreFacade.NEW);
        copy.metadataChanged = true;
        copy.setMetadata.addAll(Metadata.VALUES);
    }

    private void copyProperties(final DocumentMetadataResource copy) throws WebdavProtocolException {
        for (final WebdavProperty prop : internalGetAllProps()) {
            copy.putProperty(prop);
        }
    }

    private void copyBody(final DocumentMetadataResource copy) throws WebdavProtocolException {
        final InputStream in = getBody();
        if (in != null) {
            copy.putBody(in);
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
            final DocumentMetadata metadata = database.getDocumentMetadata(
                id,
                InfostoreFacade.CURRENT_VERSION,
                session.getContext(),
                UserStorage.getStorageUser(session.getUserId(), session.getContext()),
                UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()));
            final SetSwitch set = new SetSwitch(this.metadata);
            final GetSwitch get = new GetSwitch(metadata);

            for (final Metadata m : toLoad) {
                set.setValue(m.doSwitch(get));
                m.doSwitch(set);
            }
        } catch (final InfostoreException x) {
            if (InfostoreExceptionFactory.isPermissionException(x)) {
                metadata.setId(getId());
                metadata.setFolderId(((OXWebdavResource) parent()).getId());
                initNameAndTitle();
            } else {
                throw new WebdavProtocolException(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (final AbstractOXException x) {
            throw new WebdavProtocolException(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final Exception x) {
            throw new WebdavProtocolException(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void markSet(final Metadata metadata) {
        setMetadata.add(metadata);
    }

    private void markChanged() {
        metadataChanged = true;
    }

    @Override
    public void putBody(final InputStream body, final boolean guessSize) throws WebdavProtocolException {
        if (!exists && !existsInDB) {
            // CREATE WITH FILE
            try {
                dumpMetadataToDB(body, guessSize);
            } catch (final WebdavProtocolException x) {
                throw x;
            } catch (final InfostoreException x) {
                if (InfostoreExceptionFactory.isPermissionException(x)) {
                    throw new WebdavProtocolException(x, url, HttpServletResponse.SC_FORBIDDEN);
                }
                throw new WebdavProtocolException(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (final Exception x) {
                throw new WebdavProtocolException(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
                database.saveDocument(metadata, body, Long.MAX_VALUE, session);
                database.commit();
            } catch (final Exception x) {
                try {
                    database.rollback();
                } catch (final TransactionException e) {
                    LOG.error("Couldn't rollback transaction. Run the recovery tool.");
                }
                if (x instanceof InfostoreException) {
                    final InfostoreException iStoreException = (InfostoreException) x;
                    if (415 == iStoreException.getDetailNumber()) {
                        throw new WebdavProtocolException(getUrl(), Protocol.SC_LOCKED);
                    }
                    if (InfostoreExceptionFactory.isPermissionException(iStoreException)) {
                        throw new WebdavProtocolException(x, url, HttpServletResponse.SC_FORBIDDEN);
                    }
                }
                throw new WebdavProtocolException(x, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                try {
                    database.finish();
                } catch (final OXException x) {
                    LOG.error("Couldn't finish transaction: ", x);
                }
            }
        }

    }

    private void dumpMetadataToDB(final InputStream fileData, final boolean guessSize) throws OXException, IllegalAccessException, ConflictException, WebdavProtocolException {
        if ((exists || existsInDB) && !metadataChanged) {
            return;
        }
        FolderCollection parent = null;
        try {
            parent = (FolderCollection) parent();
            if (!parent.exists()) {
                throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_CONFLICT);
            } else if (parent.isRoot()) {
                throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (final ClassCastException x) {
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_CONFLICT);
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
                    database.saveDocumentMetadata(metadata, InfostoreFacade.NEW, session);
                } else {
                    database.saveDocument(metadata, fileData, InfostoreFacade.NEW, session);
                }
                database.commit();
                setId(metadata.getId());
            } catch (final OXException x) {
                try {
                    database.rollback();
                } catch (final TransactionException x2) {
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
                database.saveDocumentMetadata(metadata, Long.MAX_VALUE, setMetadata.toArray(new Metadata[setMetadata.size()]), session);
                database.commit();
            } catch (final OXException x) {
                try {
                    database.rollback();
                } catch (final TransactionException x2) {
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

    private void touch() throws WebdavProtocolException, OXException {
        if (!existsInDB && ! exists) {
            return;
        }
        database.touch(getId(), getSession());
    }

    private void initNameAndTitle() {
        if (metadata.getFileName() == null || metadata.getFileName().trim().length() == 0) {
            // if(url.contains("/"))
            metadata.setFileName(url.name());
        }
        metadata.setTitle(metadata.getFileName());
    }

    private void dumpMetadataToDB() throws OXException, IllegalAccessException, ConflictException, WebdavProtocolException {
        dumpMetadataToDB(null, false);
    }

    @OXThrows(category = Category.CONCURRENT_MODIFICATION, desc = "The DocumentMetadata entry in the DB for the given resource could not be created. This is mostly due to someone else modifying the entry. This can also mean, that the entry has been deleted already.", exceptionId = 0, msg = "Could not delete DocumentMetadata %d. Please try again.")
    private void deleteMetadata() throws OXException, IllegalAccessException, WebdavProtocolException {
        final ServerSession session = getSession();
        database.startTransaction();
        try {
            final int[] nd = database.removeDocument(new int[] { id }, Long.MAX_VALUE, session);
            if (nd.length > 0) {
                database.rollback();
                throw EXCEPTIONS.create(0, Integer.valueOf(nd[0]));
            }
            database.commit();
        } catch (final OXException x) {
            database.rollback();
            throw x;
        } finally {
            database.finish();
        }
    }

    public int getId() {
        return id;
    }

    public int getParentId() throws WebdavProtocolException {
        if (metadata == null) {
            loadMetadata();
        }
        return (int) metadata.getFolderId();
    }

    public void removedParent() throws WebdavProtocolException {
        exists = false;
        factory.removed(this);
    }

    public void transferLock(final WebdavLock lock) throws WebdavProtocolException {
        try {
            lockHelper.transferLock(lock);
        } catch (final OXException e) {
            throw new WebdavProtocolException(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " :" + id;
    }

    private ServerSession getSession() throws WebdavProtocolException {
        return new ServerSessionAdapter(sessionHolder.getSessionObject(), sessionHolder.getContext());

    }
}
