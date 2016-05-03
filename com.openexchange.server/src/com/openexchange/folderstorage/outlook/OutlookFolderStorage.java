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

package com.openexchange.folderstorage.outlook;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.WarningsAware;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.AfterReadAwareFolderStorage.Mode;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StorageParametersUtility;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.database.DatabaseFolderStorage.ConnectionMode;
import com.openexchange.folderstorage.database.DatabaseFolderStorageUtility;
import com.openexchange.folderstorage.database.DatabaseFolderType;
import com.openexchange.folderstorage.database.DatabaseParameterConstants;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.internal.StorageParametersImpl;
import com.openexchange.folderstorage.internal.Tools;
import com.openexchange.folderstorage.mail.MailFolderType;
import com.openexchange.folderstorage.mail.contentType.DraftsContentType;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.mail.contentType.SentContentType;
import com.openexchange.folderstorage.mail.contentType.SpamContentType;
import com.openexchange.folderstorage.mail.contentType.TrashContentType;
import com.openexchange.folderstorage.messaging.MessagingFolderIdentifier;
import com.openexchange.folderstorage.outlook.memory.MemoryTable;
import com.openexchange.folderstorage.outlook.memory.MemoryTree;
import com.openexchange.folderstorage.outlook.osgi.Services;
import com.openexchange.folderstorage.outlook.sql.Delete;
import com.openexchange.folderstorage.outlook.sql.Insert;
import com.openexchange.folderstorage.outlook.sql.Select;
import com.openexchange.folderstorage.outlook.sql.Update;
import com.openexchange.folderstorage.outlook.sql.Utility;
import com.openexchange.folderstorage.type.MailType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.TrashType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.infostore.InfostoreFacades;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.java.CallerRunsCompletionService;
import com.openexchange.java.Collators;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.messaging.MailMessagingService;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.ServiceAware;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.Trackable;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link OutlookFolderStorage} - The MS Outlook folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OutlookFolderStorage implements FolderStorage {

    static final String PROTOCOL_UNIFIED_INBOX = UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX;

    /**
     * The constant for InfoStore's file storage service.
     */
    private static final String SERVICE_INFOSTORE = "infostore";

    /**
     * <code>"9"</code>
     */
    private static final String INFOSTORE = Integer.toString(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);

    /**
     * <code>"10"</code>
     */
    private static final String INFOSTORE_USER = Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);

    /**
     * <code>"15"</code>
     */
    private static final String INFOSTORE_PUBLIC = Integer.toString(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);

    /**
     * <code>"9"</code>, <code>"10"</code>, and <code>"15"</code>
     */
    private static final Set<String> SYSTEM_INFOSTORES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        INFOSTORE,
        INFOSTORE_PUBLIC,
        INFOSTORE_USER)));

    /**
     * The logger.
     */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OutlookFolderStorage.class);

    /**
     * The prepared full name.
     */
    static final String PREPARED_FULLNAME_INBOX = MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, "INBOX");

    /**
     * The prepared full name.
     */
    static final String PREPARED_FULLNAME_DEFAULT = MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, MailFolder.DEFAULT_FOLDER_ID);

    private static final ThreadPools.ExpectedExceptionFactory<OXException> FACTORY = new ThreadPools.ExpectedExceptionFactory<OXException>() {

        @Override
        public Class<OXException> getType() {
            return OXException.class;
        }

        @Override
        public OXException newUnexpectedError(final Throwable t) {
            return FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
        }
    };

    /**
     * The reserved tree identifier for MS Outlook folder tree: <code>"1"</code>.
     */
    public static final String OUTLOOK_TREE_ID = "1";

    /**
     * The name of Outlook root folder.
     */
    private static final String OUTLOOK_ROOT_NAME = "Hidden-Root";

    /**
     * The name of Outlook private folder.
     */
    private static final String OUTLOOK_PRIVATE_NAME = "IPM-Root";

    private static final class Key {

        private final int tree;
        private final int userId;
        private final int contextId;
        private final String id;
        private final int hash;

        protected Key(final String id, final int tree, final int userId, final int contextId) {
            super();
            this.id = id;
            this.tree = tree;
            this.userId = userId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + (null == id ? 0 : id.hashCode());
            result = prime * result + contextId;
            result = prime * result + tree;
            result = prime * result + userId;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (null == id) {
                if (null != other.id) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            if (contextId != other.contextId) {
                return false;
            }
            if (tree != other.tree) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }

    }

    private static final TimeoutConcurrentMap<Key, Future<List<SortableId>>> TCM;

    static {
        TimeoutConcurrentMap<Key, Future<List<SortableId>>> tcm;
        try {
            tcm = new TimeoutConcurrentMap<Key, Future<List<SortableId>>>(10, true);
        } catch (final OXException e) {
            LOG.error("", e);
            tcm = null;
        }
        TCM = tcm;
    }

    /**
     * Removes specified folder from TCM map.
     *
     * @param fullname The folder full name
     * @param user The user identifier
     * @param contextId The context identifier
     */
    public static void removeFromTCM(final String fullname, final int user, final int contextId) {
        final Key key = new Key(fullname, Integer.parseInt(OutlookFolderStorage.OUTLOOK_TREE_ID), user, contextId);
        TCM.remove(key);
    }

    /**
     * Clears TCM map.
     */
    public static void clearTCM() {
        TCM.clear();
    }

    private static final OutlookFolderStorage INSTANCE = new OutlookFolderStorage();

    /**
     * Gets the Outlook folder storage instance.
     *
     * @return The instance
     */
    public static OutlookFolderStorage getInstance() {
        return INSTANCE;
    }

    private static boolean showPersonalBelowInfoStore(final Session session, final boolean altNames) {
        if (!altNames) {
            return false;
        }
        final String paramName = "com.openexchange.folderstorage.outlook.showPersonalBelowInfoStore";
        final Boolean tmp = (Boolean) session.getParameter(paramName);
        if (null != tmp) {
            return tmp.booleanValue();
        }
        final ConfigViewFactory configViewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == configViewFactory) {
            return false;
        }
        try {
            final ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
            final Boolean b = view.opt(paramName, boolean.class, Boolean.FALSE);
            if (session instanceof PutIfAbsent) {
                ((PutIfAbsent) session).setParameterIfAbsent(paramName, b);
            } else {
                session.setParameter(paramName, b);
            }
            return b.booleanValue();
        } catch (final OXException e) {
            LOG.warn("", e);
            return false;
        }
    }

    private static Context getContext(final Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return null == session ? null : ContextStorage.getStorageContext(session);
    }

    /**
     * Gets the ID of the user's default infostore folder.
     *
     * @param session The session to get the default folder for
     * @return The folder ID, or <code>null</code> if not found
     */
    private static String getDefaultInfoStoreFolderId(final Session session) {
        final String paramName = "com.openexchange.folderstorage.defaultInfoStoreFolderId";
        String id = (String) session.getParameter(paramName);
        if (null == id) {
            try {
                id = Integer.toString(new OXFolderAccess(getContext(session)).getDefaultFolderID(session.getUserId(), FolderObject.INFOSTORE));
            } catch (final OXException e) {
                if (OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.equals(e)) {
                    id = "-1";
                } else {
                    LOG.error("", e);
                    return null;
                }
            }
            if (session instanceof PutIfAbsent) {
                ((PutIfAbsent) session).setParameterIfAbsent(paramName, id);
            } else {
                session.setParameter(paramName, id);
            }
        }
        return "-1".equals(id) ? null : id;
    }

    /*-
     * ----------------------- Member stuff ----------------------------
     */

    /**
     * The real tree identifier.
     */
    final String realTreeId;

    /**
     * This storage's folder type.
     */
    private final FolderType folderType;

    /**
     * The path to public mail folder
     */
    private final String publicMailFolderPath;

    /**
     * The folder storage registry.
     */
    final OutlookFolderStorageRegistry folderStorageRegistry;

    /**
     * Initializes a new {@link OutlookFolderStorage}.
     */
    private OutlookFolderStorage() {
        super();
        realTreeId = FolderStorage.REAL_TREE_ID;
        folderType = new OutlookFolderType();
        folderStorageRegistry = OutlookFolderStorageRegistry.getInstance();
        final ConfigurationService service = Services.getService(ConfigurationService.class);
        if (null == service) {
            publicMailFolderPath = null;
        } else {
            // Take from foldercache.properties
            final String property = service.getProperty("PUBLIC_MAIL_FOLDER");
            publicMailFolderPath = null == property ? null : MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, property);
        }
    }

    @Override
    public void clearCache(final int userId, final int contextId) {
        clearTCM();
    }

    /**
     * Gets the public mail folder path.
     *
     * @return The public mail folder path
     */
    public String getPublicMailFolderPath() {
        return publicMailFolderPath;
    }

    @Override
    public void checkConsistency(final String treeId, final StorageParameters storageParameters) throws OXException {
        /*
         * Initialize memory tree
         */
        final Session session = storageParameters.getSession();
        final int tree = Tools.getUnsignedInteger(treeId);
        final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(session);
        /*
         * Check for obsolete entries
         */
        final MemoryTree memoryTree = memoryTable.getTree(tree, session);
        final List<String> folderIds = memoryTree.getFolders();
        if (!folderIds.isEmpty()) {
            final List<FolderStorage> storages = new LinkedList<FolderStorage>();
            try {
                for (final String folderId : folderIds) {
                    final FolderStorage folderStorage = getOpenedStorage(folderId, realTreeId, true, storageParameters, storages);
                    try {
                        if (!folderStorage.containsFolder(realTreeId, folderId, storageParameters)) {
                            // Check if that folder has subfolders
                            final boolean restore = memoryTree.hasSubfolderIds(folderId);
                            if (restore) {
                                folderStorage.restore(realTreeId, folderId, storageParameters);
                            } else {
                                deleteFolder(treeId, folderId, storageParameters, isDatabaseFolder(folderId), memoryTable);
                                LOG.debug("Deleted absent folder '{}' from virtual folder tree as there is no real counterpart", folderId, new Throwable());
                            }
                        } else if (isDatabaseFolder(folderId)) {
                            final String parentId = memoryTree.getParentOf(folderId);
                            if (null != parentId && isDatabaseFolder(parentId)) {
                                try {
                                    final Folder fld = folderStorage.getFolder(realTreeId, folderId, storageParameters);
                                    if (parentId.equals(fld.getParentID())) {
                                        // Unnecessary entry
                                        deleteFolder(treeId, folderId, storageParameters, true, memoryTable);
                                    }
                                } catch (final Exception x) {
                                    // ignore
                                }
                            }
                        }
                    } catch (final OXException oxe) {
                        LOG.warn("Checking consistency failed for folder {} in tree {}", folderId, treeId, oxe);
                    }
                }
                // Commit
                for (final FolderStorage folderStorage : storages) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (final OXException e) {
                for (final FolderStorage folderStorage : storages) {
                    folderStorage.rollback(storageParameters);
                }
                LOG.warn("Checking consistency failed in tree {}", treeId, e);
            } catch (final RuntimeException e) {
                for (final FolderStorage folderStorage : storages) {
                    folderStorage.rollback(storageParameters);
                }
                LOG.warn("Checking consistency failed for in tree {}", treeId, e);
            }
        }
    }

    @Override
    public SortableId[] getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorageByContentType(realTreeId, contentType);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(realTreeId, contentType);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            SortableId[] ret = folderStorage.getVisibleFolders(treeId, contentType, type, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }

            if (MailContentType.getInstance().toString().equals(contentType.toString())) {
                // No primary account root folder for Outlook-style tree
                List<SortableId> tmp = new ArrayList<SortableId>(ret.length);
                String id = PREPARED_FULLNAME_DEFAULT;
                int in = 0;
                for (SortableId sortableId : ret) {
                    if (!id.equals(sortableId.getId())) {
                        tmp.add(new OutlookId(sortableId.getId(), in++, sortableId.getName()));
                    }
                }
                ret = tmp.toArray(new SortableId[tmp.size()]);
            }

            return ret;
        } catch (final OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final RuntimeException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public SortableId[] getUserSharedFolders(String treeId, ContentType contentType, StorageParameters storageParameters) throws OXException {
        FolderStorage folderStorage = folderStorageRegistry.getFolderStorageByContentType(realTreeId, contentType);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(realTreeId, contentType);
        }
        boolean ownsTransaction = folderStorage.startTransaction(storageParameters, false);
        try {
            SortableId[] sharedFolderIDs = folderStorage.getUserSharedFolders(treeId, contentType, storageParameters);
            if (ownsTransaction) {
                folderStorage.commitTransaction(storageParameters);
            }
            return sharedFolderIDs;
        } catch (OXException e) {
            if (ownsTransaction) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (RuntimeException e) {
            if (ownsTransaction) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void restore(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        // Nothing to restore, not a real storage
    }

    @Override
    public Folder prepareFolder(final String treeId, final Folder folder, final StorageParameters storageParameters) throws OXException {
        /*
         * Delegate to real storage
         */
        final String folderId = folder.getID();
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, true);
        try {
            final Folder preparedFolder = folderStorage.prepareFolder(realTreeId, folder, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            if (preparedFolder.isGlobalID() != folder.isGlobalID()) {
                TCM.clear();
            }
            return preparedFolder;
        } catch (final OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final RuntimeException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        /*
         * Delegate clear invocation to real storage
         */
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        TCM.clear();
        final boolean started = folderStorage.startTransaction(storageParameters, true);
        try {
            folderStorage.clearFolder(realTreeId, folderId, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
        } catch (final OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void commitTransaction(final StorageParameters params) {
        // Nothing to do
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        /*
         * No primary mail account root folder in this tree
         */
        if (PREPARED_FULLNAME_DEFAULT.equals(folderId)) {
            return false;
        }
        /*
         * check presence in real storage
         */
        FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            boolean contains = folderStorage.containsFolder(realTreeId, folderId, storageType, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            return contains;
        } catch (OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        TCM.clear();
        /*
         * Create only if folder could not be stored in real storage
         */
        final String folderId = folder.getID();
        final Folder realFolder;
        {
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
            }
            final boolean started = folderStorage.startTransaction(storageParameters, true);
            try {
                realFolder = folderStorage.getFolder(realTreeId, folderId, StorageType.WORKING, storageParameters);
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (final OXException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        final String parentId = folder.getParentID();
        if (realFolder.getParentID().equals(parentId)) {
            /*
             * Folder already properly created at right location in real storage
             */
            return;
        }
        final int userId = storageParameters.getUserId();
        if (null == realFolder.getLastModified()) {
            /*
             * Real folder has no last-modified time stamp, but virtual needs to have.
             */
            folder.setModifiedBy(userId);
            folder.setLastModified(new Date());
        }
        final int contextId = storageParameters.getContextId();
        final int tree = Tools.getUnsignedInteger(folder.getTreeID());
        final Connection wcon = checkWriteConnection(storageParameters);
        Insert.insertFolder(contextId, tree, userId, folder, wcon);
        final MemoryTable memoryTable = MemoryTable.optMemoryTableFor(storageParameters.getSession());
        if (null != memoryTable) {
            memoryTable.initializeTree(tree, userId, contextId, wcon);
        }
    }

    @Override
    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        TCM.clear();
        final boolean global;
        {
            final Boolean b = storageParameters.getParameter(FolderType.GLOBAL, "global");
            global = null == b ? DatabaseFolderType.getInstance().servesFolderId(folderId) : b.booleanValue();
        }
        deleteFolder(treeId, folderId, storageParameters, global, MemoryTable.optMemoryTableFor(storageParameters.getSession()));
    }

    private void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters, final boolean global, final MemoryTable memoryTable) throws OXException {
        final int tree = Tools.getUnsignedInteger(treeId);
        if (null != memoryTable) {
            final MemoryTree memoryTree = memoryTable.optTree(tree);
            if (null != memoryTree) {
                memoryTree.getCrud().remove(folderId);
            }
        }
        {
            /*
             * Cleanse from other session-bound memory tables, too
             */
            final SessiondService sessiondService = Services.getService(SessiondService.class);
            if (null != sessiondService) {
                final Session session = storageParameters.getSession();
                final Collection<Session> sessions = sessiondService.getSessions(session.getUserId(), session.getContextId());
                final Set<String> disposed = new HashSet<String>(sessions.size());
                disposed.add(session.getSessionID());
                for (final Session current : sessions) {
                    if (disposed.add(current.getSessionID())) { // Set did not already contain session ID
                        final MemoryTable memTable = MemoryTable.optMemoryTableFor(session);
                        if (null != memTable) {
                            final MemoryTree memoryTree = memTable.optTree(tree);
                            if (null != memoryTree) {
                                memoryTree.getCrud().remove(folderId);
                            }
                        }
                    }
                }
            }
        }
        /*
         * Delete from tables if present
         */
        final Connection wcon = checkWriteConnection(storageParameters);
        Delete.deleteFolder(storageParameters.getContextId(), tree, storageParameters.getUserId(), folderId, global, true, wcon);
    }

    @Override
    public ContentType getDefaultContentType() {
        return null;
    }

    @Override
    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        // Public type and public mail folder path set?
        if (PublicType.getInstance().equals(type) && null != publicMailFolderPath) {
            if (MailContentType.getInstance().toString().equals(contentType.toString())) {
                return publicMailFolderPath;
            } else if (TaskContentType.getInstance().equals(contentType)) {
                return FolderStorage.PUBLIC_ID;
            } else if (CalendarContentType.getInstance().equals(contentType)) {
                return FolderStorage.PUBLIC_ID;
            } else if (ContactContentType.getInstance().equals(contentType)) {
                return FolderStorage.PUBLIC_ID;
            }
        }
        // Get default folder
        final FolderStorage byContentType = folderStorageRegistry.getFolderStorageByContentType(realTreeId, contentType);
        if (null == byContentType) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        final boolean started = byContentType.startTransaction(storageParameters, false);
        try {
            final String defaultFolderID = byContentType.getDefaultFolderID(user, treeId, contentType, type, storageParameters);
            if (started) {
                byContentType.commitTransaction(storageParameters);
            }
            return defaultFolderID;
        } catch (final OXException e) {
            if (started) {
                byContentType.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                byContentType.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Type getTypeByParent(final User user, final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        /*
         * Usual detection
         */
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            final Type retval;
            final Type originalType = folderStorage.getTypeByParent(user, realTreeId, parentId, storageParameters);
            if (MailType.getInstance().equals(originalType)) {
                /*
                 * Special treatment for mail type
                 */
                retval = (null != publicMailFolderPath && parentId.startsWith(publicMailFolderPath, 0)) ? PublicType.getInstance() : originalType;
            } else {
                retval = originalType;
            }
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            return retval;
        } catch (final OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) {
        return new String[0];
    }

    @Override
    public boolean containsForeignObjects(final User user, final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        /*
         * Get real folder storage
         */
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            final boolean containsForeignObjects = folderStorage.containsForeignObjects(user, realTreeId, folderId, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            return containsForeignObjects;
        } catch (final OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean isEmpty(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        /*
         * Get real folder storage
         */
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            final boolean isEmpty = folderStorage.isEmpty(realTreeId, folderId, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            return isEmpty;
        } catch (final OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateLastModified(final long lastModified, final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        TCM.clear();
        final boolean started = folderStorage.startTransaction(storageParameters, true);
        try {
            final Folder realFolder = folderStorage.getFolder(realTreeId, folderId, StorageType.WORKING, storageParameters);
            if (null == realFolder.getLastModified()) {
                /*
                 * Real folder has no last-modified time stamp, but virtual needs to have.
                 */
                final int contextId = storageParameters.getContextId();
                final int tree = Tools.getUnsignedInteger(treeId);
                final int userId = storageParameters.getUserId();
                final boolean containsFolder = Select.containsFolder(
                    contextId,
                    tree,
                    userId,
                    folderId,
                    StorageType.WORKING,
                    checkReadConnection(storageParameters));
                if (containsFolder) {
                    Update.updateLastModified(contextId, tree, userId, folderId, lastModified);
                    final MemoryTable memoryTable = MemoryTable.optMemoryTableFor(storageParameters.getSession());
                    if (null != memoryTable) {
                        memoryTable.initializeFolder(folderId, tree, userId, contextId);
                    }
                }
            } else {
                folderStorage.updateLastModified(lastModified, realTreeId, folderId, storageParameters);
                final MemoryTable memoryTable = MemoryTable.optMemoryTableFor(storageParameters.getSession());
                if (null != memoryTable) {
                    memoryTable.initializeFolder(
                        folderId,
                        Tools.getUnsignedInteger(treeId),
                        storageParameters.getUserId(),
                        storageParameters.getContextId());
                }
            }
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
        } catch (final OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageParameters storageParameters) throws OXException {
        return getFolders(treeId, folderIds, StorageType.WORKING, storageParameters);
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        final Folder[] ret = new Folder[folderIds.size()];
        final TObjectIntMap<String> map = new TObjectIntHashMap<String>(folderIds.size());
        for (int i = 0; i < ret.length; i++) {
            final String folderId = folderIds.get(i);
            if (PREPARED_FULLNAME_DEFAULT.equals(folderId)) {
                throw FolderExceptionErrorMessage.NOT_FOUND.create(folderId, treeId);
            }
            if (FolderStorage.ROOT_ID.equals(folderId)) {
                ret[i] = getFolder(treeId, folderId, storageType, storageParameters);
            } else if (FolderStorage.PRIVATE_ID.equals(folderId)) {
                ret[i] = getFolder(treeId, folderId, storageType, storageParameters);
            } else if (SYSTEM_INFOSTORES.contains(folderId)) {
                ret[i] = getFolder(treeId, folderId, storageType, storageParameters);
            } else {
                map.put(folderId, i);
            }
        }
        if (!map.isEmpty()) {
            /*
             * Other folders than root or private
             */
            final User user = storageParameters.getUser();
            final int tree = Tools.getUnsignedInteger(treeId);
            final int contextId = storageParameters.getContextId();

            final List<Folder> realFolders;
            {
                /*
                 * Get real folder storage
                 */
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderIds.get(0));
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderIds.get(0));
                }
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    /*
                     * Get folders
                     */
                    realFolders = folderStorage.getFolders(realTreeId, Arrays.asList(map.keys(new String[map.size()])), storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                } catch (final OXException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            final boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
            for (final Folder realFolder : realFolders) {
                final OutlookFolder outlookFolder = new OutlookFolder(realFolder);
                outlookFolder.setTreeID(treeId);
                setSubfolders(treeId, realFolder.getID(), storageParameters, user, tree, contextId, outlookFolder, realFolder);
                /*
                 * Load folder data from database
                 */
                final Session session = storageParameters.getSession();
                final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(session);
                final boolean presentInTable = memoryTable.getTree(tree, user.getId(), contextId).fillFolder(outlookFolder);
                //
                if (!presentInTable) {
                    doModifications(outlookFolder, session, altNames);
                }

                final int index = map.get(realFolder.getID());
                ret[index] = outlookFolder;
            }
        }
        /*
         * Return
         */
        final List<Folder> l = new ArrayList<Folder>(ret.length);
        for (final Folder folder : ret) {
            if (null != folder) {
                l.add(folder);
            }
        }
        return l;
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        /*
         * Primary account's root folder does not exist in this folder tree
         */
        if (PREPARED_FULLNAME_DEFAULT.equals(folderId)) {
            throw FolderExceptionErrorMessage.NOT_FOUND.create(folderId, treeId);
        }
        /*
         * Check for root folder
         */
        if (FolderStorage.ROOT_ID.equals(folderId)) {
            final Folder rootFolder;
            {
                // Get real folder storage
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get folder
                    rootFolder = folderStorage.getFolder(realTreeId, folderId, storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                } catch (final OXException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            final OutlookFolder outlookRootFolder = new OutlookFolder(rootFolder);
            outlookRootFolder.setName(OUTLOOK_ROOT_NAME);
            outlookRootFolder.setTreeID(treeId);
            /*
             * Set subfolder IDs to null to force getSubfolders() invocation
             */
            outlookRootFolder.setSubfolderIDs(null);
            return outlookRootFolder;
        }
        /*
         * Check for private folder
         */
        if (FolderStorage.PRIVATE_ID.equals(folderId)) {
            final Folder privateFolder;
            {
                // Get real folder storage
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get folder
                    privateFolder = folderStorage.getFolder(realTreeId, folderId, storageParameters);
                    folderStorage.commitTransaction(storageParameters);
                } catch (final OXException e) {
                    folderStorage.rollback(storageParameters);
                    throw e;
                } catch (final Exception e) {
                    folderStorage.rollback(storageParameters);
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            final OutlookFolder outlookPrivateFolder = new OutlookFolder(privateFolder);
            outlookPrivateFolder.setName(OUTLOOK_PRIVATE_NAME);
            outlookPrivateFolder.setTreeID(treeId);
            outlookPrivateFolder.setSubfolderIDs(null);
            return outlookPrivateFolder;
        }
        /*
         * Other folder than root or private
         */
        final User user = storageParameters.getUser();
        final int tree = Tools.getUnsignedInteger(treeId);
        final int contextId = storageParameters.getContextId();

        final OutlookFolder outlookFolder;
        final Session session = storageParameters.getSession();
        {
            final Folder realFolder;
            {
                /*
                 * Get real folder storage
                 */
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    /*
                     * Get folder
                     */
                    realFolder = folderStorage.getFolder(realTreeId, folderId, storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                } catch (final OXException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    /*
                     * Check consistency
                     */
                    final MemoryTable memoryTable = MemoryTable.optMemoryTableFor(session);
                    if (null == memoryTable) {
                        final Connection wcon = checkWriteConnection(storageParameters);
                        if (Select.containsFolder(contextId, tree, user.getId(), folderId, StorageType.WORKING, wcon)) {
                            /*
                             * In virtual tree table, but shouldn't
                             */
                            Delete.deleteFolder(contextId, tree, user.getId(), folderId, false, false, wcon);
                            throw FolderExceptionErrorMessage.TEMPORARY_ERROR.create(e, new Object[0]);
                        }
                    } else {
                        final MemoryTree memoryTree = memoryTable.getTree(tree, user.getId(), contextId);
                        if (memoryTree.containsFolder(folderId)) {
                            /*
                             * In virtual tree table, but shouldn't
                             */
                            Delete.deleteFolder(
                                contextId,
                                tree,
                                user.getId(),
                                folderId,
                                false,
                                false,
                                checkWriteConnection(storageParameters));
                            memoryTree.getCrud().remove(folderId);
                            throw FolderExceptionErrorMessage.TEMPORARY_ERROR.create(e, new Object[0]);
                        }
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            outlookFolder = new OutlookFolder(realFolder);
            outlookFolder.setTreeID(treeId);
            if (SYSTEM_INFOSTORES.contains(folderId)) {
                if (INFOSTORE.equals(folderId) && !InfostoreFacades.isInfoStoreAvailable()) {
                    final FileStorageAccount defaultAccount = getDefaultFileStorageAccess(session);
                    if (null != defaultAccount) {
                        // Rename to default account name
                        outlookFolder.setName(defaultAccount.getDisplayName());
                    }
                }
                if (!INFOSTORE_PUBLIC.equals(folderId)) {
                    // Force invocation of getSubfolders() through setting to null
                    outlookFolder.setSubfolderIDs(null);
                }
            } else {
                setSubfolders(treeId, folderId, storageParameters, user, tree, contextId, outlookFolder, realFolder);
            }
        }
        /*
         * Load folder data from database
         */
        final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(session);
        final boolean presentInTable = memoryTable.getTree(tree, user.getId(), contextId).fillFolder(outlookFolder);
        //
        if (!presentInTable) {
            final boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
            doModifications(outlookFolder, session, altNames);
        }
        return outlookFolder;
    }

    private void setSubfolders(final String treeId, final String folderId, final StorageParameters storageParameters, final User user, final int tree, final int contextId, final OutlookFolder outlookFolder, final Folder realFolder) throws OXException {
        /*
         * Set subfolders
         */
        if (PREPARED_FULLNAME_INBOX.equals(folderId)) {
            /*
             * Special treatment for INBOX
             */
            final SortableId[] inboxSubfolders = getINBOXSubfolders(treeId, storageParameters, user, user.getLocale(), contextId, tree);
            final String[] subs = new String[inboxSubfolders.length];
            for (int i = 0; i < subs.length; i++) {
                subs[i] = inboxSubfolders[i].getId();
            }
            outlookFolder.setSubfolderIDs(subs);
        } else {
            final String[] realSubfolderIDs = realFolder.getSubfolderIDs();
            if (null == realSubfolderIDs) {
                /*
                 * Subfolders available; set to null to indicate this condition since AbstractUserizedFolderPerformer#getUserizedFolder()
                 * interprets null as if subfolders are present.
                 */
                outlookFolder.setSubfolderIDs(null);
            } else {
                final int userId = user.getId();
                if (0 == realSubfolderIDs.length) {
                    /*
                     * Folder indicates to hold no subfolders; verify against virtual tree
                     */
                    final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(storageParameters.getSession());
                    final MemoryTree memoryTree = memoryTable.getTree(tree, userId, contextId);
                    final boolean contains = memoryTree.containsParent(folderId);
                    if (contains) {
                        outlookFolder.setSubfolderIDs(null);
                        outlookFolder.setSubscribedSubfolders(true);
                    } else {
                        outlookFolder.setSubfolderIDs(realSubfolderIDs); // Zero-length array => No subfolders
                    }
                } else {
                    if (realFolder.isDefault() || FolderStorage.PUBLIC_ID.equals(realFolder.getID())) {
                        final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(storageParameters.getSession());
                        final MemoryTree memoryTree = memoryTable.getTree(tree, userId, contextId);
                        if (memoryTree.containsParent(folderId)) {
                            /*
                             * There's a virtual child
                             */
                            outlookFolder.setSubfolderIDs(null);
                            outlookFolder.setSubscribedSubfolders(true);
                        } else {
                            /*
                             * Filter children kept in virtual table
                             */
                            final boolean[] contained = memoryTree.containsFolders(realSubfolderIDs);
                            final List<String> filtered = new ArrayList<String>(realSubfolderIDs.length);
                            for (int i = 0; i < realSubfolderIDs.length; i++) {
                                if (!contained[i]) {
                                    filtered.add(realSubfolderIDs[i]);
                                }
                            }
                            /*-
                             * TODO: If sorting needed:
                             *
                             * outlookFolder.setSubfolderIDs(null);
                             * outlookFolder.setSubscribedSubfolders(true);
                             */
                            if (!filtered.isEmpty()) {
                                outlookFolder.setSubfolderIDs(filtered.toArray(new String[filtered.size()]));
                                outlookFolder.setSubscribedSubfolders(true);
                            } else {
                                outlookFolder.setSubfolderIDs(new String[0]);
                                outlookFolder.setSubscribedSubfolders(false);
                            }
                        }
                    } else {
                        /*
                         * Check if there's a parent in memory table
                         */
                        final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(storageParameters.getSession());
                        final MemoryTree memoryTree = memoryTable.getTree(tree, userId, contextId);
                        if (memoryTree.containsParent(folderId)) {
                            /*
                             * There's a virtual child
                             */
                            outlookFolder.setSubfolderIDs(null);
                            outlookFolder.setSubscribedSubfolders(true);
                        } else {
                            /*
                             * There is no virtual child, thus real subfolders are equal to virtual ones
                             */
                            outlookFolder.setSubfolderIDs(realSubfolderIDs);
                        }
                    }
                }
            }
        }
    }

    @Override
    public FolderType getFolderType() {
        return folderType;
    }

    @Override
    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws OXException {
        if (null == includeContentTypes || includeContentTypes.length == 0) {
            return new String[0];
        }
        boolean containsMail = false;
        for (final ContentType contentType : includeContentTypes) {
            if (MailContentType.getInstance().equals(contentType)) {
                containsMail = true;
                break;
            }
        }
        if (containsMail) {
            final User user = storageParameters.getUser();
            final Locale locale = user.getLocale();
            final int contextId = storageParameters.getContextId();
            final int tree = Tools.getUnsignedInteger(treeId);
            final FolderNameComparator comparator = new FolderNameComparator(locale);
            final List<TreeMap<String, List<String>>> maps = new ArrayList<TreeMap<String, List<String>>>(2);
            /*
             * From primary mail folder
             */
            maps.add(new MailFolderCallable(comparator, locale, user, contextId, tree, storageParameters).call());
            /*
             * Callable for the ones from virtual table
             */
            maps.add(new TrackableCallable<TreeMap<String, List<String>>>() {

                @Override
                public TreeMap<String, List<String>> call() throws OXException {
                    /*
                     * Get the ones from virtual table
                     */
                    final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(storageParameters.getSession());
                    final List<String[]> l = memoryTable.getTree(tree, user.getId(), contextId).getSubfolderIds(FolderStorage.PRIVATE_ID);
                    /*
                     * Filter only mail folders
                     */
                    final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                    for (final String[] idAndName : l) {
                        final String id = idAndName[0];
                        if (MailFolderType.getInstance().servesFolderId(id)) {
                            put2TreeMap(idAndName[1], id, treeMap);
                        }
                    }
                    return treeMap;
                }
            }.call());
            /*
             * Merge
             */
            final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
            for (final TreeMap<String, List<String>> tm : maps) {
                for (final Entry<String, List<String>> entry : tm.entrySet()) {
                    final String key = entry.getKey();
                    final List<String> list = treeMap.get(key);
                    if (null == list) {
                        treeMap.put(key, entry.getValue());
                    } else {
                        list.addAll(entry.getValue());
                    }
                }
            }
            /*
             * Return
             */
            final Collection<List<String>> values = treeMap.values();
            final List<String> ret = new ArrayList<String>(values.size());
            for (final List<String> list : values) {
                for (final String id : list) {
                    ret.add(id);
                }
            }
            return ret.toArray(new String[ret.size()]);
        }
        /*
         * Empty array
         */
        return new String[0];
    }

    protected static boolean supportsMail(final ContentType[] types) {
        for (final ContentType contentType : types) {
            if (MailContentType.getInstance().equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    @Override
    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        /*
         * Root folder
         */
        if (FolderStorage.ROOT_ID.equals(parentId)) {
            return getRootFolderSubfolders(storageParameters);
        }
        final User user = storageParameters.getUser();
        final Locale locale = user.getLocale();
        final int contextId = storageParameters.getContextId();
        final int tree = Tools.getUnsignedInteger(treeId);
        /*
         * INBOX folder
         */
        if (PREPARED_FULLNAME_INBOX.equals(parentId)) {
            return getINBOXSubfolders(treeId, storageParameters, user, locale, contextId, tree);
        }
        /*
         * Check for private folder
         */
        if (FolderStorage.PRIVATE_ID.equals(parentId)) {
            return getPrivateFolderSubfolders(parentId, tree, storageParameters, user, locale, contextId);
        }
        final Session session = storageParameters.getSession();
        /*
         * Is InfoStore active?
         */
        if (!InfostoreFacades.isInfoStoreAvailable()) {
            /*
             * Check for InfoStore user folder (10)
             */
            if (INFOSTORE_USER.equals(parentId)) {
                /*
                 * A default account available from any other file storage?
                 */
                final FileStorageAccount defaultAccount = getDefaultFileStorageAccess(session);
                if (null != defaultAccount) {
                    final FileStorageService fileStorageService = defaultAccount.getFileStorageService();
                    final String defaultId = FileStorageAccount.DEFAULT_ID;
                    final FileStorageAccountAccess defaultFileStorageAccess = fileStorageService.getAccountAccess(defaultId, session);
                    defaultFileStorageAccess.connect();
                    try {
                        final FileStorageFolder personalFolder = defaultFileStorageAccess.getFolderAccess().getPersonalFolder();
                        if (defaultFileStorageAccess instanceof WarningsAware) {
                            addWarnings(storageParameters, (WarningsAware) defaultFileStorageAccess);
                        }
                        return new SortableId[] { new OutlookId(new FolderID(fileStorageService.getId(), defaultAccount.getId(), personalFolder.getId()).toUniqueID(), 0, personalFolder.getName()) };
                        // TODO: Shared?
                    } finally {
                        defaultFileStorageAccess.close();
                    }
                }
            }
            /*
             * Check for InfoStore public folder (15)
             */
            if (INFOSTORE_PUBLIC.equals(parentId)) {
                /*
                 * A default account available from any other file storage?
                 */
                final FileStorageAccount defaultAccount = getDefaultFileStorageAccess(session);
                if (null != defaultAccount) {
                    final FileStorageService fileStorageService = defaultAccount.getFileStorageService();
                    final String defaultId = FileStorageAccount.DEFAULT_ID;
                    final FileStorageAccountAccess defaultFileStorageAccess = fileStorageService.getAccountAccess(defaultId, session);
                    defaultFileStorageAccess.connect();
                    try {
                        final FileStorageFolder[] publicFolders = defaultFileStorageAccess.getFolderAccess().getPublicFolders();
                        final SortableId[] ret = new SortableId[publicFolders.length];
                        final String serviceId = fileStorageService.getId();
                        final String accountId = defaultAccount.getId();
                        for (int i = 0; i < publicFolders.length; i++) {
                            final FileStorageFolder folder = publicFolders[i];
                            ret[i] = new OutlookId(new FolderID(serviceId, accountId, folder.getId()).toUniqueID(), i, folder.getName());
                        }
                        if (defaultFileStorageAccess instanceof WarningsAware) {
                            addWarnings(storageParameters, (WarningsAware) defaultFileStorageAccess);
                        }
                        return ret;
                    } finally {
                        defaultFileStorageAccess.close();
                    }
                }
            }
        }
        /*
         * Others...
         */
        final List<String[]> l;
        final Map<String, String> id2name;
        {
            // Get real folder storage
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
            }
            final boolean started = folderStorage.startTransaction(storageParameters, false);
            try {
                // Get real subfolders
                final Folder parentFolder = folderStorage.getFolder(realTreeId, parentId, storageParameters);
                final SortableId[] realSubfolderIds = getSubfolderIDs(parentFolder, folderStorage, storageParameters);
                l = new ArrayList<String[]>(realSubfolderIds.length);
                id2name = new HashMap<String, String>(realSubfolderIds.length);
                if (parentFolder.isDefault() || FolderStorage.PUBLIC_ID.equals(parentId)) {
                    /*
                     * Strip subfolders occurring at another location in folder tree
                     */
                    final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(session);
                    final boolean[] contained = memoryTable.getTree(tree, user.getId(), contextId).containsFolders(realSubfolderIds);
                    for (int k = 0; k < realSubfolderIds.length; k++) {
                        final SortableId realSubfolderId = realSubfolderIds[k];
                        if (!contained[k]) {
                            final String id = realSubfolderId.getId();
                            final String name = realSubfolderId.getName();
                            final String fName = name == null ? folderStorage.getFolder(realTreeId, id, storageParameters).getName() : name;
                            l.add(new String[] { id, fName });
                            id2name.put(id, fName);

                        }
                    }
                } else {
                    for (final SortableId realSubfolderId : realSubfolderIds) {
                        final String id = realSubfolderId.getId();
                        final String name = realSubfolderId.getName();
                        final String fName = name == null ? folderStorage.getFolder(realTreeId, id, storageParameters).getName() : name;
                        l.add(new String[] { id, fName });
                        id2name.put(id, fName);
                    }
                }
                /*
                 * Add file storage root folders below "infostore" folder
                 */
                if (INFOSTORE.equals(parentId)) {
                    /*
                     * Obtain file storage accounts with running thread
                     */
                    {
                        /*
                         * File storage accounts
                         */
                        final Queue<FileStorageAccount> fsAccounts = new ConcurrentLinkedQueue<FileStorageAccount>();
                        final FileStorageServiceRegistry fsr = Services.getService(FileStorageServiceRegistry.class);
                        if (null == fsr) {
                            // Do nothing
                        } else {
                            CompletionService<Void> completionService = new CallerRunsCompletionService<Void>();
                            int taskCount = 0;
                            try {
                                final List<FileStorageService> allServices = fsr.getAllServices();
                                for (final FileStorageService fsService : allServices) {
                                    Callable<Void> task = new Callable<Void>() {

                                        @Override
                                        public Void call() throws Exception {
                                            /*
                                             * Check if file storage service provides a root folder
                                             */
                                            List<FileStorageAccount> userAccounts = null;
                                            if (fsService instanceof AccountAware) {
                                                userAccounts = ((AccountAware) fsService).getAccounts(session);
                                            }
                                            if (null == userAccounts) {
                                                userAccounts = fsService.getAccountManager().getAccounts(session);
                                            }
                                            for (final FileStorageAccount userAccount : userAccounts) {
                                                if (SERVICE_INFOSTORE.equals(userAccount.getId()) || FileStorageAccount.DEFAULT_ID.equals(userAccount.getId())) {
                                                    // Ignore infostore file storage and default account
                                                    continue;
                                                }
                                                fsAccounts.add(userAccount);
                                            }
                                            return null;
                                        }
                                    };
                                    completionService.submit(task);
                                    taskCount++;
                                }
                            } catch (final OXException e) {
                                LOG.error("", e);
                            }
                            for (int i = taskCount; i-- > 0;) {
                                completionService.take();
                            }
                            if (fsAccounts.isEmpty()) {
                                // Do nothing
                            } else {
                                List<FileStorageAccount> accountList = new ArrayList<FileStorageAccount>(fsAccounts);
                                Collections.sort(accountList, new FileStorageAccountComparator(locale));
                                final int sz = accountList.size();
                                final String fid = FileStorageFolder.ROOT_FULLNAME;
                                for (int i = 0; i < sz; i++) {
                                    final FileStorageAccount fsa = accountList.get(i);
                                    final String serviceId;
                                    if (fsa instanceof com.openexchange.file.storage.ServiceAware) {
                                        serviceId = ((com.openexchange.file.storage.ServiceAware) fsa).getServiceId();
                                    } else {
                                        final FileStorageService tmp = fsa.getFileStorageService();
                                        serviceId = null == tmp ? null : tmp.getId();
                                    }
                                    FolderID folderID = new FolderID(serviceId, fsa.getId(), fid);
                                    l.add(new String[] { folderID.toUniqueID(), fsa.getDisplayName() });
                                    id2name.put(folderID.toUniqueID(), fsa.getDisplayName());
                                }
                            }
                        }
                    }
                }
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (final OXException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        // Load folder data from database
        final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(session);
        final String[] ids = memoryTable.getTree(tree, user.getId(), contextId).getSubfolderIds(locale, parentId, l);
        final boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
        if (SYSTEM_INFOSTORES.contains(parentId) && showPersonalBelowInfoStore(session, altNames)) {
            if (INFOSTORE.equals(parentId)) {
                // Get personal InfoStore folder
                final String defaultFolderId = getDefaultInfoStoreFolderId(session);
                if (null != defaultFolderId) {
                    // Create return array
                    final List<SortableId> ret = new ArrayList<SortableId>(ids.length + 1);
                    int ordinal = 0;
                    ret.add(new OutlookId(defaultFolderId, ordinal++, FolderStrings.DEFAULT_FILES_FOLDER_NAME));
                    for (int i = 0; i < ids.length; i++) {
                        final String id = ids[i];
                        ret.add(new OutlookId(id, ordinal++, id2name.get(id)));
                    }
                    return ret.toArray(new SortableId[ret.size()]);
                }
            } else if (INFOSTORE_USER.equals(parentId)) {
                // Get personal InfoStore folder
                final String defaultFolderId = getDefaultInfoStoreFolderId(session);
                if (null != defaultFolderId) {
                    // Create return array
                    final List<SortableId> ret = new ArrayList<SortableId>(ids.length);
                    int ordinal = 0;
                    for (int i = 0; i < ids.length; i++) {
                        final String id = ids[i];
                        if (!defaultFolderId.equals(id)) {
                            ret.add(new OutlookId(id, ordinal++, id2name.get(id)));
                        }
                    }
                    return ret.toArray(new SortableId[ret.size()]);
                }
            }
        }
        // No special treatment for InfoStore folders
        final SortableId[] ret = new SortableId[ids.length];
        for (int i = 0; i < ids.length; i++) {
            final String id = ids[i];
            ret[i] = new OutlookId(id, i, id2name.get(id));
        }
        return ret;
    }

    /**
     * The identifier for default/primary file storage account.
     */
    private static final String DEFAULT_ID = FileStorageAccount.DEFAULT_ID;

    private FileStorageAccount getDefaultFileStorageAccess(final Session session) throws OXException {
        final FileStorageAccountManagerLookupService lookupService = ServerServiceRegistry.getInstance().getService(
            FileStorageAccountManagerLookupService.class);
        if (null == lookupService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(FileStorageAccountManagerLookupService.class.getName());
        }
        final FileStorageAccountManager defaultAccountManager = lookupService.getAccountManager(DEFAULT_ID, session);
        if (null != defaultAccountManager) {
            return defaultAccountManager.getAccount(DEFAULT_ID, session);
        }
        return null;
    }

    FileStorageAccountAccess getFSAccountAccess(final StorageParameters storageParameters, final FileStorageAccount userAccount) throws OXException {
        FileStorageService fileStorageService = userAccount.getFileStorageService();
        if (null == fileStorageService) {
            if (!(userAccount instanceof com.openexchange.file.storage.ServiceAware)) {
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Missing FileStorageService instance.");
            }
            final String serviceId = ((com.openexchange.file.storage.ServiceAware) userAccount).getServiceId();
            fileStorageService = Services.getService(FileStorageServiceRegistry.class).getFileStorageService(serviceId);
        }
        return fileStorageService.getAccountAccess(userAccount.getId(), storageParameters.getSession());
    }

    SortableId[] getSubfolders(final String id, final String treeId, final FolderStorage folderStorage, final StorageParameters storageParameters) throws OXException {
        final Key key = new Key(id, Integer.parseInt(treeId), storageParameters.getUserId(), storageParameters.getContextId());
        Future<List<SortableId>> f = TCM.get(key);
        if (null == f) {
            final FutureTask<List<SortableId>> ft = new FutureTask<List<SortableId>>(new TrackableCallable<List<SortableId>>() {

                @Override
                public List<SortableId> call() throws Exception {
                    return Arrays.asList(folderStorage.getSubfolders(realTreeId, id, storageParameters));
                }
            });
            f = TCM.putIfAbsent(key, ft, 60);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }
        try {
            final List<SortableId> sortedIDs = f.get();
            return sortedIDs.toArray(new SortableId[sortedIDs.size()]);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

    private SortableId[] getINBOXSubfolders(final String treeId, final StorageParameters storageParameters, final User user, final Locale locale, final int contextId, final int tree) throws OXException {
        final Key key = new Key(PREPARED_FULLNAME_INBOX, tree, user.getId(), contextId);
        Future<List<SortableId>> f = TCM.get(key);
        if (null == f) {
            final FutureTask<List<SortableId>> ft = new FutureTask<List<SortableId>>(new TrackableCallable<List<SortableId>>() {

                @Override
                public List<SortableId> call() throws OXException {
                    /*
                     * Get real folder storage for primary mail folder
                     */
                    final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, PREPARED_FULLNAME_INBOX);
                    if (null == folderStorage) {
                        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, PREPARED_FULLNAME_INBOX);
                    }
                    final boolean started = folderStorage.startTransaction(storageParameters, false);
                    try {
                        final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(new FolderNameComparator(locale));
                        /*
                         * Add default folders: Trash, Sent, Drafts, ...
                         */
                        final Set<String> defIds;
                        {
                            defIds = new HashSet<String>(6);
                            final MailType mailType = MailType.getInstance();
                            defIds.add(folderStorage.getDefaultFolderID(
                                user,
                                treeId,
                                TrashContentType.getInstance(),
                                mailType,
                                storageParameters));
                            defIds.add(folderStorage.getDefaultFolderID(
                                user,
                                treeId,
                                DraftsContentType.getInstance(),
                                mailType,
                                storageParameters));
                            defIds.add(folderStorage.getDefaultFolderID(
                                user,
                                treeId,
                                SentContentType.getInstance(),
                                mailType,
                                storageParameters));
                            defIds.add(folderStorage.getDefaultFolderID(
                                user,
                                treeId,
                                SpamContentType.getInstance(),
                                mailType,
                                storageParameters));
                        }
                        final SortableId[] inboxSubfolders = folderStorage.getSubfolders(
                            realTreeId,
                            PREPARED_FULLNAME_INBOX,
                            storageParameters);
                        /*
                         * Filter those mail folders which denote a virtual one
                         */
                        final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(storageParameters.getSession());
                        final MemoryTree memoryTree = memoryTable.getTree(tree, user.getId(), contextId);
                        final boolean[] contained = memoryTree.containsFolders(inboxSubfolders);
                        for (int i = 0; i < inboxSubfolders.length; i++) {
                            if (!contained[i]) {
                                final SortableId sortableId = inboxSubfolders[i];
                                final String id = sortableId.getId();
                                if (!defIds.contains(id)) {
                                    final String name = sortableId.getName();
                                    if (null == name) {
                                        final String localizedName = getLocalizedName(id, tree, locale, folderStorage, storageParameters);
                                        put2TreeMap(localizedName, id, treeMap);
                                    } else {
                                        put2TreeMap(name, id, treeMap);
                                    }
                                }
                            }
                        }
                        /*
                         * Get virtual subfolders
                         */
                        final List<String[]> ids = memoryTree.getSubfolderIds(PREPARED_FULLNAME_INBOX);
                        /*
                         * Merge them into tree map
                         */
                        for (final String[] idAndName : ids) {
                            /*
                             * Names loaded from DB have no locale-sensitive string
                             */
                            put2TreeMap(idAndName[1], idAndName[0], treeMap);
                        }
                        if (started) {
                            folderStorage.commitTransaction(storageParameters);
                        }
                        /*
                         * Compose list
                         */
                        final List<SortableId> sortedIDs;
                        {
                            final Collection<List<String>> values = treeMap.values();
                            sortedIDs = new ArrayList<SortableId>(values.size());
                            int i = 0;
                            for (final List<String> list : values) {
                                for (final String id : list) {
                                    sortedIDs.add(new OutlookId(id, i++, null));
                                }
                            }
                        }
                        return sortedIDs;
                    } catch (final OXException e) {
                        if (started) {
                            folderStorage.rollback(storageParameters);
                        }
                        throw e;
                    } catch (final Exception e) {
                        if (started) {
                            folderStorage.rollback(storageParameters);
                        }
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                }
            });
            f = TCM.putIfAbsent(key, ft, 60);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }
        try {
            final List<SortableId> sortedIDs = f.get();
            return sortedIDs.isEmpty() ? new SortableId[0] : sortedIDs.toArray(new SortableId[sortedIDs.size()]);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

    private SortableId[] getRootFolderSubfolders(final StorageParameters storageParameters) throws OXException {
        final SortableId[] ids;
        {
            /*
             * Get real folder storage
             */
            final String parentId = ROOT_ID;
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
            }
            final boolean started = folderStorage.startTransaction(storageParameters, false);
            try {
                /*
                 * Get subfolders
                 */
                final SortableId[] subfolders = folderStorage.getSubfolders(realTreeId, parentId, storageParameters);
                /*
                 * Get only private folder
                 */
                ids = new SortableId[1];
                boolean b = false;
                for (int i = 0; !b && i < subfolders.length; i++) {
                    final SortableId si = subfolders[i];
                    if (FolderStorage.PRIVATE_ID.equals(si.getId())) {
                        ids[0] = si;
                        b = true;
                    }
                }
                if (!b) {
                    // Missing private folder
                    throw FolderExceptionErrorMessage.NOT_FOUND.create(FolderStorage.PRIVATE_ID, OUTLOOK_TREE_ID);
                }
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (final OXException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        final List<String> subfolderIDs = toIDList(ids);
        final SortableId[] ret = new SortableId[subfolderIDs.size()];
        int i = 0;
        for (final String id : subfolderIDs) {
            ret[i] = new OutlookId(id, i, null);
            i++;
        }
        return ret;
    }

    private SortableId[] getPrivateFolderSubfolders(final String parentId, final int tree, final StorageParameters parameters, final User user, final Locale locale, final int contextId) throws OXException {
        final CompletionService<TreeMap<String, List<String>>> completionService;
        {
            completionService = new ThreadPoolCompletionService<TreeMap<String, List<String>>>(Services.getService(ThreadPoolService.class));
        }
        int taskCount = 0;
        final FolderNameComparator comparator = new FolderNameComparator(locale);
        /*
         * Callable for real folder storage
         */
        completionService.submit(new TrackableCallable<TreeMap<String, List<String>>>() {

            @Override
            public TreeMap<String, List<String>> call() throws OXException {
                /*
                 * Get real folder storage
                 */
                final FolderStorage folderStorage = folderStorageRegistry.getDedicatedFolderStorage(realTreeId, parentId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
                }
                final StorageParameters storageParameters = newStorageParameters(parameters);
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    /*
                     * Get folder
                     */
                    final SortableId[] ids = folderStorage.getSubfolders(realTreeId, parentId, storageParameters);
                    final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                    for (final SortableId sortableId : ids) {
                        final String id = sortableId.getId();
                        final String name = sortableId.getName();
                        if (null == name) {
                            put2TreeMap(getLocalizedName(id, tree, locale, folderStorage, storageParameters), id, treeMap);
                        } else {
                            put2TreeMap(name, sortableId.getId(), treeMap);
                        }
                    }
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                    return treeMap;
                } catch (final OXException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        });
        taskCount++;
        /*
         * Callable for primary mail folder
         */
        if (null == parameters.getSession() || ServerSessionAdapter.valueOf(parameters.getSession()).getUserConfiguration().hasWebMail()) {
            completionService.submit(new MailFolderCallable(comparator, locale, user, contextId, tree, parameters));
            taskCount++;
        }
        /*
         * Callable for the ones from virtual table
         */
        completionService.submit(new TrackableCallable<TreeMap<String, List<String>>>() {

            @Override
            public TreeMap<String, List<String>> call() throws OXException {
                /*
                 * Get the ones from virtual table
                 */
                final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(parameters.getSession());
                final List<String[]> l = memoryTable.getTree(tree, user.getId(), contextId).getSubfolderIds(parentId);
                final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                for (final String[] idAndName : l) {
                    put2TreeMap(idAndName[1], idAndName[0], treeMap);
                }
                return treeMap;
            }
        });
        taskCount++;
        /*
         * Callable for other top-level folders: shared + public
         */
        completionService.submit(new TrackableCallable<TreeMap<String, List<String>>>() {

            @Override
            public TreeMap<String, List<String>> call() throws OXException {
                // Get other top-level folders: shared + public
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
                }
                final StorageParameters storageParameters = newStorageParameters(parameters);
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get subfolders
                    final SortableId[] subfolders = folderStorage.getSubfolders(realTreeId, FolderStorage.ROOT_ID, storageParameters);
                    final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                    for (final SortableId subfolder : subfolders) {
                        final String id = subfolder.getId();
                        if (!FolderStorage.PRIVATE_ID.equals(id)) { // Exclude private folder
                            final String name = subfolder.getName();
                            if (null == name) {
                                put2TreeMap(getLocalizedName(id, tree, locale, folderStorage, storageParameters), id, treeMap);
                            } else {
                                put2TreeMap(name, id, treeMap);
                            }
                        }
                    }
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                    return treeMap;
                } catch (final OXException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        });
        taskCount++;
        /*
         * User configuration
         */
        final UserPermissionBits userPermissionBits;
        {
            final Session s = parameters.getSession();
            if (s instanceof ServerSession) {
                userPermissionBits = ((ServerSession) s).getUserPermissionBits();
            } else {
                userPermissionBits = UserPermissionBitsStorage.getInstance().getUserPermissionBits(user.getId(), parameters.getContext());
            }
        }
        /*
         * Obtain external mail accounts with running thread
         */
        final List<String> accountSubfolderIDs;
        int unifiedMailIndex = -1;
        if (userPermissionBits.isMultipleMailAccounts()) {
            final MailAccountFacade maf = Services.getService(MailAccountFacade.class);
            if (null == maf) {
                accountSubfolderIDs = Collections.emptyList();
            } else {
                final List<MailAccount> accounts = Arrays.asList(maf.getUserMailAccounts(user.getId(), contextId));
                Collections.sort(accounts, new MailAccountComparator(locale));
                if (accounts.isEmpty()) {
                    accountSubfolderIDs = Collections.emptyList();
                } else {
                    final boolean suppressUnifiedMail = StorageParametersUtility.getBoolParameter("suppressUnifiedMail", parameters);
                    accountSubfolderIDs = new ArrayList<String>(accounts.size());
                    if (suppressUnifiedMail) {
                        for (final MailAccount mailAccount : accounts) {
                            if (!mailAccount.isDefaultAccount() && !PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                                accountSubfolderIDs.add(MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID));
                            }
                        }
                    } else {
                        for (final MailAccount mailAccount : accounts) {
                            if (!mailAccount.isDefaultAccount()) {
                                if (PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                                    /*
                                     * Ensure Unified Mail is enabled; meaning at least one account is subscribed to Unified Mail
                                     */
                                    final UnifiedInboxManagement uim = Services.getService(UnifiedInboxManagement.class);
                                    try {
                                        if (null != uim && uim.isEnabled(user.getId(), contextId)) {
                                            accountSubfolderIDs.add(MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID));
                                            unifiedMailIndex = accountSubfolderIDs.size() - 1;
                                        }
                                    } catch (final OXException e) {
                                        LOG.error("", e);
                                    }
                                } else {
                                    accountSubfolderIDs.add(MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID));
                                }
                            }
                        }
                    }
                }
            }
        } else {
            accountSubfolderIDs = Collections.emptyList();
        }
        /*
         * Obtain external messaging accounts with running thread
         */
        final List<String> messagingSubfolderIDs;
        {
            /*
             * Messaging accounts; except mail
             */
            final List<MessagingAccount> messagingAccounts = new ArrayList<MessagingAccount>();
            final MessagingServiceRegistry msr = Services.getService(MessagingServiceRegistry.class);
            if (null == msr) {
                messagingSubfolderIDs = Collections.emptyList();
            } else {
                try {
                    final List<MessagingService> allServices = msr.getAllServices(user.getId(), contextId);
                    for (final MessagingService messagingService : allServices) {
                        if (!messagingService.getId().equals(MailMessagingService.ID)) {
                            /*
                             * Only non-mail services
                             */
                            try {
                                messagingAccounts.addAll(messagingService.getAccountManager().getAccounts(parameters.getSession()));
                            } catch (final OXException e) {
                                LOG.error("", e);
                            }
                        }
                    }
                } catch (final OXException e) {
                    LOG.error("", e);
                }
                if (messagingAccounts.isEmpty()) {
                    messagingSubfolderIDs = Collections.emptyList();
                } else {
                    Collections.sort(messagingAccounts, new MessagingAccountComparator(locale));
                    final int sz = messagingAccounts.size();
                    messagingSubfolderIDs = new ArrayList<String>(sz);
                    final String fullname = MessagingFolder.ROOT_FULLNAME;
                    for (int i = 0; i < sz; i++) {
                        final MessagingAccount ma = messagingAccounts.get(i);
                        final String serviceId;
                        if (ma instanceof ServiceAware) {
                            serviceId = ((ServiceAware) ma).getServiceId();
                        } else {
                            final MessagingService tmp = ma.getMessagingService();
                            serviceId = null == tmp ? null : tmp.getId();
                        }
                        final MessagingFolderIdentifier mfi = new MessagingFolderIdentifier(serviceId, ma.getId(), fullname);
                        messagingSubfolderIDs.add(mfi.toString());
                    }
                }
            }
        }

        /*
         * Wait for completion
         */
        final List<String> sortedIDs;
        {
            final List<TreeMap<String, List<String>>> taken = ThreadPools.takeCompletionService(completionService, taskCount, FACTORY);
            final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
            for (final TreeMap<String, List<String>> tm : taken) {
                for (final Entry<String, List<String>> entry : tm.entrySet()) {
                    final String key = entry.getKey();
                    final List<String> list = treeMap.get(key);
                    if (null == list) {
                        treeMap.put(key, entry.getValue());
                    } else {
                        list.addAll(entry.getValue());
                    }
                }
            }
            /*
             * Get sorted values
             */
            final Collection<List<String>> values = treeMap.values();
            sortedIDs = new ArrayList<String>(values.size() + accountSubfolderIDs.size());
            for (final List<String> list : values) {
                for (final String id : list) {
                    sortedIDs.add(id);
                }
            }
        }
        /*
         * Add external mail accounts
         */
        if (unifiedMailIndex >= 0) {
            sortedIDs.add(0, accountSubfolderIDs.remove(unifiedMailIndex));
        }
        sortedIDs.addAll(accountSubfolderIDs);
        /*
         * Add external messaging accounts/file storage accounts
         */
        sortedIDs.addAll(messagingSubfolderIDs);
        final int size = sortedIDs.size();
        final SortableId[] ret = new SortableId[size];
        for (int i = 0; i < size; i++) {
            ret[i] = new OutlookId(sortedIDs.get(i), i, null);
        }
        return ret;
    }

    static void put2TreeMap(final String localizedName, final String id, final TreeMap<String, List<String>> treeMap) {
        List<String> list = treeMap.get(localizedName);
        if (null == list) {
            list = new ArrayList<String>(2);
            treeMap.put(localizedName, list);
        }
        list.add(id);
    }

    /**
     * Creates a new storage parameter instance.
     */
    static StorageParameters newStorageParameters(final StorageParameters source) {
        StorageParametersImpl parameters;
        final Session session = source.getSession();
        if (null == session) {
            parameters = new StorageParametersImpl(source.getUser(), source.getContext());
        } else {
            parameters = new StorageParametersImpl((ServerSession) session, source.getUser(), source.getContext());
        }

        FolderServiceDecorator decorator = source.getDecorator();
        if (decorator != null) {
            try {
                parameters.setDecorator(decorator.clone());
            } catch (CloneNotSupportedException e) {
                // ignore
            }
        }
        return parameters;
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[0];
    }

    @Override
    public void rollback(final StorageParameters params) {
        // Nothing to do
    }

    @Override
    public boolean startTransaction(final StorageParameters parameters, final boolean modify) {
        return false;
    }

    @Override
    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        TCM.clear();
        /*
         * Update only if folder is contained
         */
        final int contextId = storageParameters.getContextId();
        final int tree = Tools.getUnsignedInteger(folder.getTreeID());
        final int userId = storageParameters.getUserId();
        final String folderId = folder.getID();
        final boolean contains = Select.containsFolder(
            contextId,
            tree,
            userId,
            folderId,
            StorageType.WORKING,
            checkReadConnection(storageParameters));
        if (contains) {
            /*
             * Get a connection
             */
            final Connection wcon = checkWriteConnection(storageParameters);
            if (wcon == null) {
                final DatabaseService databaseService = Utility.getDatabaseService();
                final Connection con;
                try {
                    con = databaseService.getWritable(contextId);
                    con.setAutoCommit(false); // BEGIN
                } catch (final SQLException e) {
                    throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
                }
                try {
                    final String name = folder.getName();
                    if (name != null) {
                        Update.updateName(contextId, tree, userId, folderId, name, con);
                    }
                    final String parentId = folder.getParentID();
                    if (parentId != null) {
                        Update.updateParent(contextId, tree, userId, folderId, parentId, con);
                    }
                    final String newId = folder.getNewID();
                    if (newId != null) {
                        Update.updateId(contextId, tree, userId, folderId, newId, con);
                    }
                    Update.updateLastModified(contextId, tree, userId, folderId, System.currentTimeMillis(), con);
                    final MemoryTable memoryTable = MemoryTable.optMemoryTableFor(storageParameters.getSession());
                    if (null != memoryTable) {
                        memoryTable.initializeTree(tree, userId, contextId, con);
                    }
                    con.commit(); // COMMIT
                } catch (final SQLException e) {
                    DBUtils.rollback(con); // ROLLBACK
                    throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
                } catch (final OXException e) {
                    DBUtils.rollback(con); // ROLLBACK
                    throw e;
                } catch (final Exception e) {
                    DBUtils.rollback(con); // ROLLBACK
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                } finally {
                    DBUtils.autocommit(con);
                    databaseService.backWritable(contextId, con);
                }
            } else {
                try {
                    final String name = folder.getName();
                    if (name != null) {
                        Update.updateName(contextId, tree, userId, folderId, name, wcon);
                    }
                    final String parentId = folder.getParentID();
                    if (parentId != null) {
                        Update.updateParent(contextId, tree, userId, folderId, parentId, wcon);
                    }
                    final String newId = folder.getNewID();
                    if (newId != null) {
                        Update.updateId(contextId, tree, userId, folderId, newId, wcon);
                    }
                    Update.updateLastModified(contextId, tree, userId, folderId, System.currentTimeMillis(), wcon);
                    final MemoryTable memoryTable = MemoryTable.optMemoryTableFor(storageParameters.getSession());
                    if (null != memoryTable) {
                        memoryTable.initializeTree(tree, userId, contextId, wcon);
                    }
                } catch (final OXException e) {
                    throw e;
                } catch (final Exception e) {
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        }
    }

    private final class MailFolderCallable implements Callable<TreeMap<String, List<String>>>, Trackable {

        private final FolderNameComparator comparator;
        private final Locale locale;
        private final User user;
        private final int contextId;
        private final int tree;
        private final StorageParameters parameters;

        public MailFolderCallable(final FolderNameComparator comparator, final Locale locale, final User user, final int contextId, final int tree, final StorageParameters parameters) {
            super();
            this.comparator = comparator;
            this.locale = locale == null ? Locale.US : locale;
            this.user = user;
            this.contextId = contextId;
            this.tree = tree;
            this.parameters = parameters;
        }

        @Override
        public TreeMap<String, List<String>> call() throws OXException {
            /*
             * Get real folder storage for primary mail folder
             */
            final String fullname = PREPARED_FULLNAME_DEFAULT;
            final FolderStorage realFolderStorage = folderStorageRegistry.getFolderStorage(realTreeId, fullname);
            if (null == realFolderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, fullname);
            }
            final StorageParameters storageParameters = newStorageParameters(parameters);
            final boolean started = realFolderStorage.startTransaction(storageParameters, false);
            try {
                /*
                 * Get IDs
                 */
                final SortableId[] mailIDs;
                try {
                    mailIDs = getSubfolders(fullname, realTreeId, realFolderStorage, storageParameters);
                    final Set<OXException> warnings = storageParameters.getWarnings();
                    if (!warnings.isEmpty()) {
                        for (final OXException warning : warnings) {
                            parameters.addWarning(warning);
                        }
                    }
                } catch (final OXException e) {
                    if (MailExceptionCode.UNKNOWN_PROTOCOL.equals(e)) {
                        LOG.debug("", e);
                        parameters.addWarning(e);
                        /*
                         * Return empty map
                         */
                        return new TreeMap<String, List<String>>(comparator);
                    } else if (MailExceptionCode.ACCOUNT_DOES_NOT_EXIST.equals(e)) {
                        LOG.debug("", e);
                        parameters.addWarning(e);
                        /*
                         * Return empty map
                         */
                        return new TreeMap<String, List<String>>(comparator);
                    } else if (MimeMailExceptionCode.INVALID_CREDENTIALS.equals(e) || MimeMailExceptionCode.LOGIN_FAILED.equals(e)) {
                        LOG.debug("", e);
                        parameters.addWarning(e);
                        /*
                         * Return empty map
                         */
                        return new TreeMap<String, List<String>>(comparator);
                    } else if (MimeMailExceptionCode.CONNECT_ERROR.equals(e) || MimeMailExceptionCode.UNKNOWN_HOST.equals(e)) {
                        LOG.debug("", e);
                        parameters.addWarning(e);
                        /*
                         * Return empty map
                         */
                        return new TreeMap<String, List<String>>(comparator);
                    }
                    throw e;
                }
                final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                if (mailIDs.length == 0) {
                    // If no subfolders exist in the mail account, loading the default folders fails.
                    // Therefore we need to return early here.
                    return treeMap;
                }

                final String publicFolderPath = getPublicMailFolderPath();
                for (final SortableId sortableId : mailIDs) {
                    /*
                     * Ignore special public mail folder
                     */
                    final String id = sortableId.getId();
                    if (!id.equals(publicFolderPath)) {
                        /*
                         * Mail folders have no locale-sensitive name
                         */
                        final String name = sortableId.getName();
                        if (null == name) {
                            put2TreeMap(getLocalizedName(id, tree, locale, realFolderStorage, storageParameters), id, treeMap);
                        } else {
                            put2TreeMap(name, id, treeMap);
                        }
                    }
                }
                /*
                 * Add default folders: Trash, Sent, Drafts, ...
                 */
                final Set<String> defIds;
                {
                    defIds = new HashSet<String>(6);
                    final String treeId = realTreeId;
                    final MailType mailType = MailType.getInstance();
                    defIds.add(realFolderStorage.getDefaultFolderID(
                        user,
                        treeId,
                        TrashContentType.getInstance(),
                        mailType,
                        storageParameters));
                    defIds.add(realFolderStorage.getDefaultFolderID(
                        user,
                        treeId,
                        DraftsContentType.getInstance(),
                        mailType,
                        storageParameters));
                    defIds.add(realFolderStorage.getDefaultFolderID(
                        user,
                        treeId,
                        SentContentType.getInstance(),
                        mailType,
                        storageParameters));
                    defIds.add(realFolderStorage.getDefaultFolderID(
                        user,
                        treeId,
                        SpamContentType.getInstance(),
                        mailType,
                        storageParameters));
                }
                final SortableId[] inboxSubfolders = realFolderStorage.getSubfolders(realTreeId, PREPARED_FULLNAME_INBOX, storageParameters);
                final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(storageParameters.getSession());
                final boolean[] contained = memoryTable.getTree(tree, user.getId(), contextId).containsFolders(inboxSubfolders);
                for (int i = 0; i < inboxSubfolders.length; i++) {
                    if (!contained[i]) {
                        final SortableId sortableId = inboxSubfolders[i];
                        final String id = sortableId.getId();
                        if (defIds.contains(id)) {
                            final String name = sortableId.getName();
                            if (null == name) {
                                put2TreeMap(getLocalizedName(id, tree, locale, realFolderStorage, storageParameters), id, treeMap);
                            } else {
                                put2TreeMap(name, sortableId.getId(), treeMap);
                            }
                        }
                    }
                }
                if (started) {
                    realFolderStorage.commitTransaction(storageParameters);
                }
                return treeMap;
            } catch (final OXException e) {
                if (started) {
                    realFolderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    realFolderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }

    } // End of MailFolderCallable member class

    private static final class MailAccountComparator implements Comparator<MailAccount> {

        private final Collator collator;

        public MailAccountComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale == null ? Locale.US : locale);
        }

        @Override
        public int compare(final MailAccount o1, final MailAccount o2) {
            if (PROTOCOL_UNIFIED_INBOX.equals(o1.getMailProtocol())) {
                if (PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                    return 0;
                }
                return -1;
            } else if (PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                return 1;
            }
            if (o1.isDefaultAccount()) {
                if (o2.isDefaultAccount()) {
                    return 0;
                }
                return -1;
            } else if (o2.isDefaultAccount()) {
                return 1;
            }
            return collator.compare(o1.getName(), o2.getName());
        }

    } // End of MailAccountComparator

    private static final class MessagingAccountComparator implements Comparator<MessagingAccount> {

        private final Collator collator;

        public MessagingAccountComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale == null ? Locale.US : locale);
        }

        @Override
        public int compare(final MessagingAccount o1, final MessagingAccount o2) {
            return collator.compare(o1.getDisplayName(), o2.getDisplayName());
        }

    } // End of MessagingAccountComparator

    private static final class FileStorageAccountComparator implements Comparator<FileStorageAccount> {

        private final Collator collator;

        public FileStorageAccountComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final FileStorageAccount o1, final FileStorageAccount o2) {
            return collator.compare(o1.getDisplayName(), o2.getDisplayName());
        }

    } // End of FileStorageAccountComparator

    private static final class FolderNameComparator implements Comparator<String> {

        private final Collator collator;

        public FolderNameComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale == null ? Locale.US : locale);
        }

        @Override
        public int compare(final String o1, final String o2) {
            return collator.compare(o1, o2);
        }

    } // End of FolderNameComparator

    private static List<String> toIDList(final SortableId[] ids) {
        final List<String> l = new ArrayList<String>(ids.length);
        for (final SortableId id : ids) {
            l.add(id.getId());
        }
        return l;
    }

    private static SortableId[] getSubfolderIDs(final Folder realFolder, final FolderStorage folderStorage, final StorageParameters storageParameters) throws OXException {
        /*-
         *
        final String[] ids = realFolder.getSubfolderIDs();
        if (null != ids) {
            return ids;
        }
         */
        return folderStorage.getSubfolders(realFolder.getTreeID(), realFolder.getID(), storageParameters);
    }

    String getLocalizedName(final String id, final int tree, final Locale locale, final FolderStorage folderStorage, final StorageParameters storageParameters) throws OXException {
        final Session session = storageParameters.getSession();
        final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(session);
        final MemoryTree memoryTree = memoryTable.getTree(tree, session);
        final String name = memoryTree.getFolderName(id);
        if (null != name) {
            /*
             * If name is held in virtual tree, it has no locale-sensitive string
             */
            return name;
        }
        return folderStorage.getFolder(realTreeId, id, storageParameters).getLocalizedName(locale);
    }

    static Connection checkReadConnection(final StorageParameters storageParameters) {
        final ConnectionMode con = storageParameters.<ConnectionMode> getParameter(
            DatabaseFolderType.getInstance(),
            DatabaseParameterConstants.PARAM_CONNECTION);
        return null == con ? null : con.connection;
    }

    static Connection checkWriteConnection(final StorageParameters storageParameters) {
        final ConnectionMode con = storageParameters.<ConnectionMode> getParameter(
            DatabaseFolderType.getInstance(),
            DatabaseParameterConstants.PARAM_CONNECTION);
        if (null != con && con.supports(Mode.WRITE)) {
            return con.connection;
        }
        return null;
    }

    private static void doModifications(final OutlookFolder folder, final Session session, final boolean altNames) {
        final String id = folder.getID();
        if (FolderStorage.PUBLIC_ID.equals(id)) {
            folder.setParentID(FolderStorage.PRIVATE_ID);
        } else if (FolderStorage.SHARED_ID.equals(id)) {
            folder.setParentID(FolderStorage.PRIVATE_ID);
        } else if (INFOSTORE.equals(id)) { // InfoStore folder
            folder.setParentID(FolderStorage.PRIVATE_ID);
            folder.setSubfolderIDs(null);
        } else if (isDefaultMailFolder(folder)) {
            folder.setParentID(FolderStorage.PRIVATE_ID);
        } else if (isNonPrimaryMailAccountFolder(folder)) {
            folder.setParentID(FolderStorage.PRIVATE_ID);
        } else if (PREPARED_FULLNAME_DEFAULT.equals(folder.getParentID())) {
            folder.setParentID(FolderStorage.PRIVATE_ID);
        } else if (isDefaultFileStorageFolder(folder)) {
            folder.setParentID(INFOSTORE);
        } else if (showPersonalBelowInfoStore(session, altNames) && id.equals(getDefaultInfoStoreFolderId(session))) {
            folder.setParentID(INFOSTORE);
            folder.setName(FolderStrings.DEFAULT_FILES_FOLDER_NAME);
        } else if (MODULE_FILE == folder.getContentType().getModule() && TrashType.getInstance().equals(folder.getType()) && folder.isDefault()) {
            folder.setParentID(INFOSTORE);
            folder.setName(altNames ? FolderStrings.SYSTEM_TRASH_FILES_FOLDER_NAME : FolderStrings.SYSTEM_TRASH_INFOSTORE_FOLDER_NAME);
        }
    }

    private static final int MODULE_FILE = FileStorageContentType.getInstance().getModule();

    private static boolean isDefaultFileStorageFolder(final OutlookFolder folder) {
        if (MODULE_FILE != folder.getContentType().getModule()) {
            return false;
        }
        try {
            FolderID folderID = new FolderID(folder.getID());
            // FileStorage root full name has zero length
            return 0 == folderID.getFolderId().length() && folderID.getService().indexOf(SERVICE_INFOSTORE) < 0;
        } catch (final Exception e) {
            /*
             * Parsing failed
             */
            return false;
        }
    }

    private static final int MODULE_MAIL = MailContentType.getInstance().getModule();

    private static boolean isDefaultMailFolder(final OutlookFolder folder) {
        if (!folder.isDefault() || (MODULE_MAIL != folder.getContentType().getModule())) {
            return false;
        }
        /*
         * Check for primary account
         */
        final String id = folder.getID();
        try {
            final FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(id);
            return (MailAccount.DEFAULT_ID == arg.getAccountId());
        } catch (final RuntimeException e) {
            /*
             * Parsing failed
             */
            return false;
        }
    }

    private static boolean isNonPrimaryMailAccountFolder(final OutlookFolder folder) {
        final String id = folder.getID();
        if (!id.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
            return false;
        }
        try {
            final FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(id);
            return MailFolder.DEFAULT_FOLDER_ID.equals(arg.getFullname()) && arg.getAccountId() != MailAccount.DEFAULT_ID;
        } catch (final RuntimeException e) {
            /*
             * Parsing failed
             */
            return false;
        }
    }

    /**
     * Combines Callable and Trackable
     */
    private static abstract class TrackableCallable<V> implements Callable<V>, Trackable {

        TrackableCallable() {
            super();
        }
    }

    private FolderStorage getOpenedStorage(final String id, final String treeId, final boolean modify, final StorageParameters storageParameters, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        for (final FolderStorage ps : openedStorages) {
            if (ps.getFolderType().servesFolderId(id)) {
                // Found an already opened storage which is capable to server given folderId-treeId-pair
                return ps;
            }
        }
        // None opened storage is capable to server given folderId-treeId-pair
        final FolderStorage tmp = folderStorageRegistry.getFolderStorage(treeId, id);
        if (null == tmp) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
        }
        // Open storage and add to list of opened storages
        if (tmp.startTransaction(storageParameters, modify)) {
            openedStorages.add(tmp);
        }
        return tmp;
    }

    private boolean isDatabaseFolder(final String folderId) {
        return DatabaseFolderStorageUtility.getUnsignedInteger(folderId) >= 0;
    }

    static void addWarnings(final StorageParameters storageParameters, final WarningsAware warningsAware) {
        final List<OXException> list = warningsAware.getAndFlushWarnings();
        if (null != list && !list.isEmpty()) {
            for (OXException warning : list) {
                storageParameters.addWarning(warning);
            }
        }
    }

}
