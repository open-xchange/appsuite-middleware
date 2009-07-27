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

package com.openexchange.folderstorage.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.openexchange.api2.OXException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.database.type.PrivateType;
import com.openexchange.folderstorage.database.type.PublicType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.server.ServiceException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DatabaseFolderStorage} - The database folder storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseFolderStorage implements FolderStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(DatabaseFolderStorage.class);

    private final String treeId;

    /**
     * Initializes a new {@link DatabaseFolderStorage}.
     * 
     * @param treeId The tree identifier
     */
    public DatabaseFolderStorage(final String treeId) {
        super();
        this.treeId = treeId;
    }

    public void commitTransaction(final StorageParameters params) throws FolderException {
        final Connection con;
        final Boolean writable;
        try {
            con = getParameter(Connection.class, DatabaseParameterConstants.PARAM_CONNECTION, params);
            writable = getParameter(Boolean.class, DatabaseParameterConstants.PARAM_WRITABLE, params);
        } catch (final FolderException e) {
            LOG.error(e.getMessage(), e);
            return;
        }
        try {
            con.commit();
        } catch (final SQLException e) {
            throw new FolderException(new OXFolderException(
                OXFolderException.FolderCode.SQL_ERROR,
                e,
                Integer.valueOf(params.getContext().getContextId())));
        } finally {
            DBUtils.autocommit(con);
            final DatabaseService databaseService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class);
            if (null != databaseService) {
                if (writable.booleanValue()) {
                    databaseService.backWritable(params.getContext(), con);
                } else {
                    databaseService.backReadOnly(params.getContext(), con);
                }
            }
        }
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        try {
            final Session session = storageParameters.getSession();
            final long millis = System.currentTimeMillis();

            final FolderObject createMe = new FolderObject();
            createMe.setCreatedBy(session.getUserId());
            createMe.setCreationDate(new Date(millis));
            createMe.setCreator(session.getUserId());
            createMe.setDefaultFolder(false);
            {
                final String name = folder.getName();
                if (null != name) {
                    createMe.setFolderName(name);
                }
            }
            createMe.setLastModified(new Date(millis));
            createMe.setModifiedBy(session.getUserId());
            {
                final ContentType ct = folder.getContentType();
                if (null != ct) {
                    createMe.setModule(getModuleByContentType(ct));
                }
            }
            {
                final String parentId = folder.getParentID();
                if (null != parentId) {
                    createMe.setParentFolderID(Integer.parseInt(parentId));
                }
            }
            {
                final Type t = folder.getType();
                if (null != t) {
                    createMe.setType(getTypeByFolderType(t));
                }
            }
            // Permissions
            final Permission[] perms = folder.getPermissions();
            if (null != perms) {
                final OCLPermission[] oclPermissions = new OCLPermission[perms.length];
                for (int i = 0; i < perms.length; i++) {
                    final Permission p = perms[i];
                    final OCLPermission oclPerm = new OCLPermission();
                    oclPerm.setEntity(p.getEntity());
                    oclPerm.setGroupPermission(p.isGroup());
                    oclPerm.setFolderAdmin(p.isAdmin());
                    oclPerm.setAllPermission(
                        p.getFolderPermission(),
                        p.getReadPermission(),
                        p.getWritePermission(),
                        p.getDeletePermission());
                    oclPerm.setSystem(p.getSystem());
                    oclPermissions[i] = oclPerm;
                }
            }
            // Create
            final OXFolderManager folderManager = OXFolderManager.getInstance(session);
            folderManager.createFolder(createMe, true, millis);
        } catch (final OXException e) {
            throw new FolderException(e);
        }
    }

    public void deleteFolder(final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            final FolderObject fo = new FolderObject();
            fo.setObjectID(Integer.parseInt(folderId));

            final OXFolderManager folderManager = OXFolderManager.getInstance(storageParameters.getSession());
            folderManager.deleteFolder(fo, true, System.currentTimeMillis());
        } catch (final OXFolderException e) {
            throw new FolderException(e);
        } catch (final OXException e) {
            throw new FolderException(e);
        }
    }

    public Folder getDefaultFolder(final int entity, final ContentType contentType, final StorageParameters storageParameters) throws FolderException {
        final Context context = storageParameters.getContext();
        try {
            final Connection con = getParameter(Connection.class, DatabaseParameterConstants.PARAM_CONNECTION, storageParameters);
            final Session session = storageParameters.getSession();
            final int folderId;
            if (TaskContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.TASK, con, context);
            } else if (CalendarContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.CALENDAR, con, context);
            } else if (ContactContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.CONTACT, con, context);
            } else if (InfostoreContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.INFOSTORE, con, context);
            } else {
                throw new FolderException(new OXFolderException(
                    OXFolderException.FolderCode.UNKNOWN_MODULE,
                    contentType.toString(),
                    Integer.valueOf(context.getContextId())));
            }
            return getFolder(String.valueOf(folderId), storageParameters);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        } catch (final SQLException e) {
            throw new FolderException(new OXFolderException(
                OXFolderException.FolderCode.SQL_ERROR,
                e,
                Integer.valueOf(context.getContextId())));
        }
    }

    public Folder getFolder(final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            final Connection con = getParameter(Connection.class, DatabaseParameterConstants.PARAM_CONNECTION, storageParameters);

            final FolderObject fo = FolderObject.loadFolderObjectFromDB(Integer.parseInt(folderId), storageParameters.getContext(), con);
            final DatabaseFolder retval = new DatabaseFolder(fo);
            retval.setTreeID(treeId);

            final List<Integer> subfolderIds = FolderObject.getSubfolderIds(Integer.parseInt(folderId), storageParameters.getContext(), con);
            final List<String> subfolderIdentifies = new ArrayList<String>(subfolderIds.size());
            for (final Integer id : subfolderIds) {
                subfolderIdentifies.add(id.toString());
            }
            retval.setSubfolderIDs(subfolderIdentifies.toArray(new String[subfolderIdentifies.size()]));

            // TODO: Subscribed?

            return retval;
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        } catch (final OXException e) {
            throw new FolderException(e);
        } catch (final SQLException e) {
            throw new FolderException(new OXFolderException(
                OXFolderException.FolderCode.SQL_ERROR,
                e,
                Integer.valueOf(storageParameters.getContext().getContextId())));
        }
    }

    public FolderType getFolderType() {
        return DatabaseFolderType.getInstance();
    }

    public SortableId[] getSubfolders(final String parentId, final StorageParameters storageParameters) throws FolderException {
        try {
            final Connection con = getParameter(Connection.class, DatabaseParameterConstants.PARAM_CONNECTION, storageParameters);

            final List<Integer> subfolderIds = FolderObject.getSubfolderIds(Integer.parseInt(parentId), storageParameters.getContext(), con);
            final List<FolderObject> subfolders = new ArrayList<FolderObject>(subfolderIds.size());
            for (final Integer folderId : subfolderIds) {
                subfolders.add(FolderObject.loadFolderObjectFromDB(folderId.intValue(), storageParameters.getContext(), con, false, false));
            }
            final ServerSession session;
            {
                final Session s = storageParameters.getSession();
                if (s instanceof ServerSession) {
                    session = (ServerSession) s;
                } else {
                    session = new ServerSessionAdapter(s);
                }
            }
            Collections.sort(subfolders, new FolderObjectComparator(session.getUser().getLocale()));

            final int size = subfolders.size();
            final List<SortableId> list = new ArrayList<SortableId>(size);
            for (int i = 0; i < size; i++) {
                list.add(new DatabaseId(subfolders.get(i).getObjectID(), i));
            }
            return list.toArray(new SortableId[size]);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        } catch (final SQLException e) {
            throw new FolderException(new OXFolderException(
                OXFolderException.FolderCode.SQL_ERROR,
                e,
                Integer.valueOf(storageParameters.getContext().getContextId())));
        } catch (final OXException e) {
            throw new FolderException(e);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public void rollback(final StorageParameters params) {
        final Connection con;
        final Boolean writable;
        try {
            con = getParameter(Connection.class, DatabaseParameterConstants.PARAM_CONNECTION, params);
            writable = getParameter(Boolean.class, DatabaseParameterConstants.PARAM_WRITABLE, params);
        } catch (final FolderException e) {
            LOG.error(e.getMessage(), e);
            return;
        }
        try {
            DBUtils.rollback(con);
        } finally {
            DBUtils.autocommit(con);
            final DatabaseService databaseService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class);
            if (null != databaseService) {
                if (writable.booleanValue()) {
                    databaseService.backWritable(params.getContext(), con);
                } else {
                    databaseService.backReadOnly(params.getContext(), con);
                }
            }
        }
    }

    public StorageParameters startTransaction(final StorageParameters parameters, final boolean modify) throws FolderException {
        try {
            final DatabaseService databaseService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
            final Connection con = modify ? databaseService.getWritable(parameters.getContext()) : databaseService.getReadOnly(parameters.getContext());
            con.setAutoCommit(false);
            // Put to parameters
            parameters.putParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_CONNECTION, con);
            parameters.putParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_WRITABLE, Boolean.valueOf(modify));
            return parameters;
        } catch (final ServiceException e) {
            throw new FolderException(e);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        } catch (final SQLException e) {
            throw new FolderException(new OXFolderException(
                OXFolderException.FolderCode.SQL_ERROR,
                e,
                Integer.valueOf(parameters.getContext().getContextId())));
        }
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        try {
            final Session session = storageParameters.getSession();
            final long millis = System.currentTimeMillis();

            final FolderObject updateMe = new FolderObject();
            updateMe.setObjectID(Integer.parseInt(folder.getID()));
            updateMe.setDefaultFolder(false);
            {
                final String name = folder.getName();
                if (null != name) {
                    updateMe.setFolderName(name);
                }
            }
            updateMe.setLastModified(new Date(millis));
            updateMe.setModifiedBy(session.getUserId());
            {
                final ContentType ct = folder.getContentType();
                if (null != ct) {
                    updateMe.setModule(getModuleByContentType(ct));
                }
            }
            {
                final String parentId = folder.getParentID();
                if (null != parentId) {
                    updateMe.setParentFolderID(Integer.parseInt(parentId));
                }
            }
            {
                final Type t = folder.getType();
                if (null != t) {
                    updateMe.setType(getTypeByFolderType(t));
                }
            }
            // Permissions
            final Permission[] perms = folder.getPermissions();
            if (null != perms) {
                final OCLPermission[] oclPermissions = new OCLPermission[perms.length];
                for (int i = 0; i < perms.length; i++) {
                    final Permission p = perms[i];
                    final OCLPermission oclPerm = new OCLPermission();
                    oclPerm.setEntity(p.getEntity());
                    oclPerm.setGroupPermission(p.isGroup());
                    oclPerm.setFolderAdmin(p.isAdmin());
                    oclPerm.setAllPermission(
                        p.getFolderPermission(),
                        p.getReadPermission(),
                        p.getWritePermission(),
                        p.getDeletePermission());
                    oclPerm.setSystem(p.getSystem());
                    oclPermissions[i] = oclPerm;
                }
            }
            final OXFolderManager folderManager = OXFolderManager.getInstance(session);
            folderManager.updateFolder(updateMe, true, millis);
        } catch (final OXException e) {
            throw new FolderException(e);
        }
    }

    /*-
     * ############################# HELPER METHODS #############################
     */

    private static <T> T getParameter(final Class<T> clazz, final String name, final StorageParameters parameters) throws FolderException {
        final Object obj = parameters.getParameter(DatabaseFolderType.getInstance(), name);
        if (null == obj) {
            throw new FolderException(new OXFolderException(OXFolderException.FolderCode.MISSING_PARAMETER, name));
        }
        try {
            return clazz.cast(obj);
        } catch (final ClassCastException e) {
            throw new FolderException(new OXFolderException(OXFolderException.FolderCode.MISSING_PARAMETER, e, name));
        }
    }

    private static int getModuleByContentType(final ContentType contentType) {
        if (TaskContentType.getInstance().equals(contentType)) {
            return FolderObject.TASK;
        }
        if (CalendarContentType.getInstance().equals(contentType)) {
            return FolderObject.CALENDAR;
        }
        if (ContactContentType.getInstance().equals(contentType)) {
            return FolderObject.CONTACT;
        }
        if (InfostoreContentType.getInstance().equals(contentType)) {
            return FolderObject.INFOSTORE;
        }
        return FolderObject.UNBOUND;
    }

    private static int getTypeByFolderType(final Type type) {
        if (PrivateType.getInstance().equals(type)) {
            return FolderObject.PRIVATE;
        }
        if (PublicType.getInstance().equals(type)) {
            return FolderObject.PUBLIC;
        }
        return FolderObject.SYSTEM_TYPE;
    }

    private static final class FolderObjectComparator implements Comparator<FolderObject> {

        private final Collator collator;

        public FolderObjectComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        public int compare(final FolderObject o1, final FolderObject o2) {
            if (o1.isDefaultFolder()) {
                if (o2.isDefaultFolder()) {
                    return compareById(o1.getObjectID(), o2.getObjectID());
                }
                return -1;
            } else if (o2.isDefaultFolder()) {
                return 1;
            }
            // Compare by name
            return collator.compare(o1.getFolderName(), o2.getFolderName());
        }

        private int compareById(final int id1, final int id2) {
            return (id1 < id2 ? -1 : (id1 == id2 ? 0 : 1));
        }

    } // End of MailAccountComparator

}
