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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.mail;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderEventConstants;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.RemoveAfterAccessFolder;
import com.openexchange.folderstorage.RemoveAfterAccessFolderWrapper;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StorageParametersUtility;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.mail.contentType.DraftsContentType;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.mail.contentType.SentContentType;
import com.openexchange.folderstorage.mail.contentType.SpamContentType;
import com.openexchange.folderstorage.mail.contentType.TrashContentType;
import com.openexchange.folderstorage.type.MailType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageEnhanced;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.event.EventPool;
import com.openexchange.mail.event.PooledEvent;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.internal.RdbMailAccountStorage;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link MailFolderStorage} - The mail folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderStorage implements FolderStorage {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailFolderStorage.class));

    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    private final TObjectProcedure<MailAccess<?, ?>> procedure;

    private final MailFolderType folderType = MailFolderType.getInstance();

    private final String paramAccessFast = StorageParameters.PARAM_ACCESS_FAST;

    /**
     * Initializes a new {@link MailFolderStorage}.
     */
    public MailFolderStorage() {
        super();
        procedure = new TObjectProcedure<MailAccess<?, ?>>() {

            @Override
            public boolean execute(final MailAccess<?, ?> mailAccess) {
                closeMailAccess(mailAccess);
                return true;
            }
        };
    }

    @Override
    public void clearCache(final int userId, final int contextId) {
        /*
         * Nothing to do...
         */
    }

    @Override
    public void checkConsistency(final String treeId, final StorageParameters storageParameters) throws OXException {
        // Nothing to do
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[] {
            MailContentType.getInstance(), DraftsContentType.getInstance(), SentContentType.getInstance(), SpamContentType.getInstance(),
            TrashContentType.getInstance() };
    }

    @Override
    public ContentType getDefaultContentType() {
        return MailContentType.getInstance();
    }

    @Override
    public void commitTransaction(final StorageParameters params) throws OXException {
        /*
         * Nothing to do
         */
    }

    @Override
    public SortableId[] getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        if (!MailType.getInstance().equals(type) && !PrivateType.getInstance().equals(type)) {
            return new SortableId[0];
        }
        if (!MailContentType.getInstance().toString().equals(contentType.toString())) {
            return new SortableId[0];
        }
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            /*
             * Only primary account folders
             */
            final int accountId = MailAccount.DEFAULT_ID;
            final ServerSession session = getServerSession(storageParameters);
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, accountId);
            final Boolean accessFast = storageParameters.getParameter(folderType, paramAccessFast);
            mailAccess.connect(null == accessFast ? true : !accessFast.booleanValue());
            final List<MailFolder> folders = new ArrayList<MailFolder>(32);
            final MailFolder rootFolder = mailAccess.getRootFolder();
            folders.add(rootFolder);
            /*
             * Start recursive iteration
             */
            addSubfolders(MailFolder.DEFAULT_FOLDER_ID, folders, mailAccess.getFolderStorage());
            /*
             * Sort by name
             */
            Collections.sort(folders, new SimpleMailFolderComparator(storageParameters.getUser().getLocale()));
            final int size = folders.size();
            final List<SortableId> list = new ArrayList<SortableId>(size);
            /*
             * Add external account root folders
             */
            final FolderServiceDecorator decorator = storageParameters.getDecorator();
            if (null == decorator) {
                for (int j = 0; j < size; j++) {
                    final MailFolder mf = folders.get(j);
                    list.add(new MailId(prepareFullname(mailAccess.getAccountId(), mf.getFullname()), j).setName(mf.getName()));
                }
            } else {
                final List<MailAccount> accountList;
                final Object property = decorator.getProperty("mailRootFolders");
                if (property != null && Boolean.parseBoolean(property.toString()) && session.getUserPermissionBits().isMultipleMailAccounts()) {
                    final MailAccountStorageService mass = MailServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
                    final MailAccount[] accounts = mass.getUserMailAccounts(storageParameters.getUserId(), storageParameters.getContextId());
                    accountList = new ArrayList<MailAccount>(accounts.length);
                    for (final MailAccount mailAccount : accounts) {
                        if (!mailAccount.isDefaultAccount()) {
                            accountList.add(mailAccount);
                        }
                    }
                    Collections.sort(accountList, new MailAccountComparator(session.getUser().getLocale()));
                    if (!accountList.isEmpty() && UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(accountList.get(0).getMailProtocol())) {
                        /*
                         * Ensure Unified Mail is enabled; meaning at least one account is subscribed to Unified Mail
                         */
                        final UnifiedInboxManagement uim = MailServiceRegistry.getServiceRegistry().getService(UnifiedInboxManagement.class);
                        if (null == uim || !uim.isEnabled(session.getUserId(), session.getContextId())) {
                            accountList.remove(0);
                        } else {
                            // Add Unified Mail root folder at first position
                            final MailAccount unifiedMailAccount = accountList.remove(0);
                            list.add(0, new MailId(prepareFullname(unifiedMailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID), 0).setName(MailFolder.DEFAULT_FOLDER_NAME));
                        }
                    }
                } else {
                    accountList = Collections.emptyList();
                }
                // Add primary account's folders
                int start = list.size();
                for (int j = 0; j < size; j++) {
                    final MailFolder mf = folders.get(j);
                    list.add(new MailId(prepareFullname(mailAccess.getAccountId(), mf.getFullname()), start++).setName(mf.getName()));
                }
                // Add root folders for external accounts
                final int sz = accountList.size();
                for (int j = 0; j < sz; j++) {
                    list.add(new MailId(prepareFullname(accountList.get(j).getId(), MailFolder.DEFAULT_FOLDER_ID), start++).setName(MailFolder.DEFAULT_FOLDER_NAME));
                }
            }
            /*
             * Return
             */
            return list.toArray(new SortableId[list.size()]);
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    private static void addSubfolders(final String fullname, final List<MailFolder> folders, final IMailFolderStorage folderStorage) throws OXException {
        final MailFolder[] subfolders = folderStorage.getSubfolders(fullname, false);
        for (final MailFolder subfolder : subfolders) {
            folders.add(subfolder);
            addSubfolders(subfolder.getFullname(), folders, folderStorage);
        }
    }

    @Override
    public Folder prepareFolder(final String treeId, final Folder folder, final StorageParameters storageParameters) throws OXException {
        return folder;
    }

    @Override
    public void restore(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            final FullnameArgument argument = prepareMailFolderParam(folderId);
            final int accountId = argument.getAccountId();
            final String fullname = argument.getFullname();
            if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create();
            }
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect(false);
            /*
             * Restore if absent
             */
            if (!mailAccess.getFolderStorage().exists(fullname)) {
                recreateMailFolder(accountId, fullname, session, mailAccess);
            }
            addWarnings(mailAccess, storageParameters);
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            final FullnameArgument arg = prepareMailFolderParam(folder.getParentID());
            final int accountId = arg.getAccountId();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect(false);
            final MailFolderDescription mfd = new MailFolderDescription();
            mfd.setExists(false);
            final String parentFullName = arg.getFullname();
            mfd.setParentFullname(parentFullName);
            mfd.setParentAccountId(accountId);
            // Separator
            mfd.setSeparator(mailAccess.getFolderStorage().getFolder(arg.getFullname()).getSeparator());
            // Other
            {
                final String name = folder.getName();
                checkFolderName(name);
                mfd.setName(name);
            }
            mfd.setSubscribed(folder.isSubscribed());
            // Permissions
            final Permission[] permissions = folder.getPermissions();
            if (null != permissions && permissions.length > 0) {
                final MailPermission[] mailPermissions = new MailPermission[permissions.length];
                final MailProvider provider = MailProviderRegistry.getMailProviderBySession(session, accountId);
                for (int i = 0; i < permissions.length; i++) {
                    final Permission permission = permissions[i];
                    final MailPermission mailPerm = provider.createNewMailPermission(session, accountId);
                    mailPerm.setEntity(permission.getEntity());
                    mailPerm.setAllPermission(
                        permission.getFolderPermission(),
                        permission.getReadPermission(),
                        permission.getWritePermission(),
                        permission.getDeletePermission());
                    mailPerm.setFolderAdmin(permission.isAdmin());
                    mailPerm.setGroupPermission(permission.isGroup());
                    mailPermissions[i] = mailPerm;
                }
                mfd.addPermissions(mailPermissions);
            } else {
                if (MailFolder.DEFAULT_FOLDER_ID.equals(parentFullName)) {
                    final MailPermission[] mailPermissions = new MailPermission[1];
                    final MailProvider provider = MailProviderRegistry.getMailProviderBySession(session, accountId);
                    {
                        final MailPermission mailPerm = provider.createNewMailPermission(session, accountId);
                        mailPerm.setEntity(session.getUserId());
                        mailPerm.setAllPermission(
                            OCLPermission.ADMIN_PERMISSION,
                            OCLPermission.ADMIN_PERMISSION,
                            OCLPermission.ADMIN_PERMISSION,
                            OCLPermission.ADMIN_PERMISSION);
                        mailPerm.setFolderAdmin(true);
                        mailPerm.setGroupPermission(false);
                        mailPermissions[0] = mailPerm;
                    }
                    mfd.addPermissions(mailPermissions);
                } else {
                    final MailFolder parent = mailAccess.getFolderStorage().getFolder(parentFullName);
                    final MailPermission[] parentPermissions = parent.getPermissions();
                    final MailPermission[] mailPermissions = new MailPermission[1];
                    final MailProvider provider = MailProviderRegistry.getMailProviderBySession(session, accountId);
                    for (int i = 0; i < parentPermissions.length; i++) {
                        final MailPermission parentPerm = parentPermissions[i];
                        final MailPermission mailPerm = provider.createNewMailPermission(session, accountId);
                        mailPerm.setEntity(parentPerm.getEntity());
                        mailPerm.setAllPermission(
                            parentPerm.getFolderPermission(),
                            parentPerm.getReadPermission(),
                            parentPerm.getWritePermission(),
                            parentPerm.getDeletePermission());
                        mailPerm.setFolderAdmin(parentPerm.isFolderAdmin());
                        mailPerm.setGroupPermission(parentPerm.isGroupPermission());
                        mailPermissions[i] = mailPerm;
                    }
                    mfd.addPermissions(mailPermissions);
                }
            }
            final String fullname = mailAccess.getFolderStorage().createFolder(mfd);
            addWarnings(mailAccess, storageParameters);
            folder.setID(prepareFullname(accountId, fullname));
            postEvent(accountId, mfd.getParentFullname(), false, true, storageParameters);
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            final FullnameArgument arg = prepareMailFolderParam(folderId);
            final int accountId = arg.getAccountId();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, accountId);
            final Boolean accessFast = storageParameters.getParameter(folderType, paramAccessFast);
            mailAccess.connect(null == accessFast ? true : !accessFast.booleanValue());
            final String fullname = arg.getFullname();
            /*
             * Only backup if full name does not denote trash (sub)folder
             */
            final String trashFullname = mailAccess.getFolderStorage().getTrashFolder();
            final boolean hardDelete = fullname.startsWith(trashFullname);
            mailAccess.getFolderStorage().clearFolder(fullname, hardDelete);
            addWarnings(mailAccess, storageParameters);
            postEvent(accountId, fullname, true, storageParameters);
            if (!hardDelete) {
                postEvent(accountId, trashFullname, true, storageParameters);
            }
            try {
                /*
                 * Update message cache
                 */
                MailMessageCache.getInstance().removeFolderMessages(
                    accountId,
                    fullname,
                    storageParameters.getUserId(),
                    storageParameters.getContextId());
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
            if (fullname.startsWith(trashFullname)) {
                // Special handling
                final MailFolder[] subf = mailAccess.getFolderStorage().getSubfolders(fullname, true);
                for (int i = 0; i < subf.length; i++) {
                    final String subFullname = subf[i].getFullname();
                    mailAccess.getFolderStorage().deleteFolder(subFullname, true);
                    postEvent(accountId, subFullname, false, true, storageParameters);
                }
                postEvent(accountId, trashFullname, false, true, storageParameters);
            }
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            final FullnameArgument arg = prepareMailFolderParam(folderId);
            final int accountId = arg.getAccountId();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, accountId);
            final Boolean accessFast = storageParameters.getParameter(folderType, paramAccessFast);
            mailAccess.connect(null == accessFast ? true : !accessFast.booleanValue());
            final String fullname = arg.getFullname();
            /*
             * Only backup if full name does not denote trash (sub)folder
             */
            final String trashFullname = mailAccess.getFolderStorage().getTrashFolder();
            final boolean hardDelete = fullname.startsWith(trashFullname);
            final Map<String, Map<?, ?>> subfolders = subfolders(fullname, mailAccess);
            mailAccess.getFolderStorage().deleteFolder(fullname, hardDelete);
            addWarnings(mailAccess, storageParameters);
            postEvent(accountId, fullname, false, true, false, storageParameters);
            try {
                /*
                 * Update message cache
                 */
                MailMessageCache.getInstance().removeFolderMessages(
                    accountId,
                    fullname,
                    storageParameters.getUserId(),
                    storageParameters.getContextId());
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
            if (!hardDelete) {
                // New folder in trash folder
                postEvent(accountId, trashFullname, false, storageParameters);
            }
            postEvent4Subfolders(accountId, subfolders, storageParameters);
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        if (!(contentType instanceof MailContentType)) {
            throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(contentType.toString());
        }
        if (MailContentType.getInstance().equals(contentType)) {
            return prepareFullname(MailAccount.DEFAULT_ID, "INBOX");
        }
        MailAccess<?, ?> mailAccess = null;
        try {
            /*
             * Open mail access
             */
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, 0);
            final Boolean accessFast = storageParameters.getParameter(folderType, paramAccessFast);
            mailAccess.connect(null == accessFast ? true : !accessFast.booleanValue());
            // Return primary account's default folder
            if (DraftsContentType.getInstance().equals(contentType)) {
                return prepareFullname(MailAccount.DEFAULT_ID, mailAccess.getFolderStorage().getDraftsFolder());
            }
            if (SentContentType.getInstance().equals(contentType)) {
                return prepareFullname(MailAccount.DEFAULT_ID, mailAccess.getFolderStorage().getSentFolder());
            }
            if (SpamContentType.getInstance().equals(contentType)) {
                return prepareFullname(MailAccount.DEFAULT_ID, mailAccess.getFolderStorage().getSpamFolder());
            }
            if (TrashContentType.getInstance().equals(contentType)) {
                return prepareFullname(MailAccount.DEFAULT_ID, mailAccess.getFolderStorage().getTrashFolder());
            }
            throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(contentType.toString());
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public Type getTypeByParent(final User user, final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        return MailType.getInstance();
    }

    @Override
    public boolean containsForeignObjects(final User user, final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            final FullnameArgument argument = prepareMailFolderParam(folderId);
            final int accountId = argument.getAccountId();
            final String fullname = argument.getFullname();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect(false);
            if (!MailFolder.DEFAULT_FOLDER_ID.equals(fullname) && !mailAccess.getFolderStorage().exists(fullname)) {
                throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullname);
            }
            addWarnings(mailAccess, storageParameters);
            return false;
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public boolean isEmpty(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            final FullnameArgument argument = prepareMailFolderParam(folderId);
            final int accountId = argument.getAccountId();
            final String fullname = argument.getFullname();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, accountId);
            if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
                return true;
            }
            /*
             * Non-root folder
             */
            mailAccess.connect(false);
            final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            if (folderStorage instanceof IMailFolderStorageEnhanced) {
                return ((IMailFolderStorageEnhanced) folderStorage).getTotalCounter(fullname) > 0;
            }
            return 0 == mailAccess.getMessageStorage().searchMessages(
                fullname,
                new IndexRange(0, 1),
                MailSortField.RECEIVED_DATE,
                OrderDirection.DESC,
                null,
                new MailField[] { MailField.ID }).length;
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public void updateLastModified(final long lastModified, final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        // Nothing to do
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageParameters storageParameters) throws OXException {
        return getFolders(treeId, folderIds, StorageType.WORKING, storageParameters);
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            throw FolderExceptionErrorMessage.UNSUPPORTED_STORAGE_TYPE.create(storageType);
        }
        final TIntObjectMap<MailAccess<?, ?>> accesses = new TIntObjectHashMap<MailAccess<?, ?>>(2);
        try {
            final ServerSession session = getServerSession(storageParameters);
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final TIntObjectMap<MailAccount> accounts = new TIntObjectHashMap<MailAccount>(2);
            final List<Folder> ret = new ArrayList<Folder>(folderIds.size());
            for (final String folderId : folderIds) {
                final FullnameArgument argument = prepareMailFolderParam(folderId);
                final int accountId = argument.getAccountId();
                MailAccess<?, ?> mailAccess = accesses.get(accountId);
                if (null == mailAccess) {
                    mailAccess = MailAccess.getInstance(session, accountId);
                    accesses.put(accountId, mailAccess);
                }
                MailAccount mailAccount = accounts.get(argument.getAccountId());
                if (null == mailAccount) {
                    final MailAccountStorageService storageService =
                        MailServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
                    mailAccount = storageService.getMailAccount(accountId, storageParameters.getUserId(), storageParameters.getContextId());
                    accounts.put(accountId, mailAccount);
                }
                ret.add(getFolder(treeId, argument, storageParameters, mailAccess, session, mailAccount));
            }
            return ret;
        } finally {
            accesses.forEachValue(procedure);
        }
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            throw FolderExceptionErrorMessage.UNSUPPORTED_STORAGE_TYPE.create(storageType);
        }
        MailAccess<?, ?> mailAccess = null;
        try {
            final FullnameArgument argument = prepareMailFolderParam(folderId);
            final ServerSession session = getServerSession(storageParameters);
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final MailAccount mailAccount;
            {
                final MailAccountStorageService storageService =
                    MailServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
                mailAccount = storageService.getMailAccount(argument.getAccountId(), storageParameters.getUserId(), storageParameters.getContextId());
            }
            mailAccess = MailAccess.getInstance(session, argument.getAccountId());
            return getFolder(treeId, argument, storageParameters, mailAccess, session, mailAccount);
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    private static final Set<String> IGNORABLES = RemoveAfterAccessFolder.IGNORABLES;

    private Folder getFolder(final String treeId, final FullnameArgument argument, final StorageParameters storageParameters, final MailAccess<?, ?> mailAccess, final ServerSession session, final MailAccount mailAccount) throws OXException {
        final int accountId = argument.getAccountId();
        final String fullname = argument.getFullname();
        final Folder retval;
        final boolean hasSubfolders;
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            if (MailAccount.DEFAULT_ID == accountId) {
                final MailFolder rootFolder = mailAccess.getRootFolder();
                retval = new MailFolderImpl(rootFolder, accountId, mailAccess.getMailConfig(), storageParameters, null);
                addWarnings(mailAccess, storageParameters);
                hasSubfolders = rootFolder.hasSubfolders();
                /*
                 * This one needs sorting. Just pass null or an empty array.
                 */
                retval.setSubfolderIDs(hasSubfolders ? null : new String[0]);
            } else {
                /*
                 * An external account folder
                 */
                if (IGNORABLES.contains(mailAccount.getMailProtocol())) {
                    retval = new ExternalMailAccountRootFolder(mailAccount, mailAccess.getMailConfig(), session);
                } else {
                    retval = new RemoveAfterAccessExtRootFolder(mailAccount, mailAccess.getMailConfig(), session);
                }
                /*
                 * Load on demand (or in FolderMap)
                 */
                retval.setSubfolderIDs(null);
            }
        } else {
            final Boolean accessFast = storageParameters.getParameter(folderType, paramAccessFast);
            mailAccess.connect(null == accessFast ? true : !accessFast.booleanValue());
            final MailFolder mailFolder = getMailFolder(treeId, accountId, fullname, true, session, mailAccess);
            /*
             * Generate mail folder from loaded one
             */
            {
                final MailFolderImpl mailFolderImpl = new MailFolderImpl(mailFolder, accountId, mailAccess.getMailConfig(), storageParameters, new MailAccessFullnameProvider(mailAccess));
                if (MailAccount.DEFAULT_ID == accountId || IGNORABLES.contains(mailAccount.getMailProtocol())) {
                    retval = mailFolderImpl;
                } else {
                    retval = new RemoveAfterAccessFolderWrapper(mailFolderImpl, false, session.getUserId(), session.getContextId());
                }
            }
            hasSubfolders = mailFolder.hasSubfolders();
            /*
             * Check if denoted parent can hold default folders like Trash, Sent, etc.
             */
            if ("INBOX".equals(fullname)) {
                /*
                 * This one needs sorting. Just pass null or an empty array.
                 */
                retval.setSubfolderIDs(hasSubfolders ? null : new String[0]);
            } else {
                /*
                 * Denoted parent is not capable to hold default folders. Therefore output as it is.
                 */
                final List<MailFolder> children = new ArrayList<MailFolder>(Arrays.asList(mailAccess.getFolderStorage().getSubfolders(fullname, true)));
                /*
                 * Filter against possible POP3 storage folders
                 */
                if (MailAccount.DEFAULT_ID == accountId && MailProperties.getInstance().isHidePOP3StorageFolders()) {
                    filterPOP3StorageFolders(children, session);
                }
                Collections.sort(children, new SimpleMailFolderComparator(storageParameters.getUser().getLocale()));
                final String[] subfolderIds = new String[children.size()];
                int i = 0;
                for (final MailFolder child : children) {
                    subfolderIds[i++] = prepareFullname(accountId, child.getFullname());
                }
                retval.setSubfolderIDs(subfolderIds);
            }
            addWarnings(mailAccess, storageParameters);
        }
        retval.setTreeID(treeId);
        return retval;
    }

    private static void filterPOP3StorageFolders(final List<MailFolder> folders, final ServerSession session) throws OXException {
        final Set<String> pop3StorageFolders = RdbMailAccountStorage.getPOP3StorageFolders(session);
        for (final Iterator<MailFolder> it = folders.iterator(); it.hasNext();) {
            final MailFolder mf = it.next();
            if (pop3StorageFolders.contains(mf.getFullname())) {
                it.remove();
            }
        }
    }

    private static ServerSession getServerSession(final StorageParameters storageParameters) throws OXException, OXException {
        final Session s = storageParameters.getSession();
        if (null == s) {
            throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
        }
        if (s instanceof ServerSession) {
            return (ServerSession) s;
        }
        return ServerSessionAdapter.valueOf(s);
    }

    private static MailFolder getMailFolder(final String treeId, final int accountId, final String fullname, final boolean createIfAbsent, final Session session, final MailAccess<?, ?> mailAccess) throws OXException {
        OXException exc = null;
        final int max = 2;
        int count = 0;

        while (count++ < max) {
            try {
                return mailAccess.getFolderStorage().getFolder(fullname);
            } catch (final OXException e) {
                if (count < max && MimeMailExceptionCode.STORE_CLOSED.equals(e)) {
                    MailAccess.reconnect(mailAccess);
                    exc = e;
                } else {
                    if (!createIfAbsent) {
                        throw e;
                    }
                    if ((!e.isPrefix("MSG") || MailExceptionCode.FOLDER_NOT_FOUND.getNumber() != e.getCode()) || FolderStorage.REAL_TREE_ID.equals(treeId)) {
                        throw e;
                    }
                    return recreateMailFolder(accountId, fullname, session, mailAccess);
                }
            }
        }

        throw null == exc ? MailExceptionCode.UNEXPECTED_ERROR.create("Connection closed.") : exc;
    }

    private static MailFolder recreateMailFolder(final int accountId, final String fullname, final Session session, final MailAccess<?, ?> mailAccess) throws OXException {
        /*
         * Recreate the mail folder
         */
        final MailFolderDescription mfd = new MailFolderDescription();
        mfd.setExists(false);
        mfd.setAccountId(accountId);
        mfd.setParentAccountId(accountId);
        /*
         * Parent fullname & name
         */
        final char separator = mailAccess.getFolderStorage().getFolder("INBOX").getSeparator();
        final String[] parentAndName = splitBySeperator(fullname, separator);
        mfd.setParentFullname(parentAndName[0]);
        mfd.setName(parentAndName[1]);
        mfd.setSeparator(separator);
        {
            final MailPermission mailPerm = MailProviderRegistry.getMailProviderBySession(session, accountId).createNewMailPermission(session, accountId);
            mailPerm.setEntity(session.getUserId());
            mailPerm.setGroupPermission(false);
            mailPerm.setFolderAdmin(true);
            final int max = OCLPermission.ADMIN_PERMISSION;
            mailPerm.setAllPermission(max, max, max, max);
            mfd.addPermission(mailPerm);
        }
        mfd.setSubscribed(true);
        /*
         * Create
         */
        final String id = mailAccess.getFolderStorage().createFolder(mfd);
        return mailAccess.getFolderStorage().getFolder(id);
    }

    private static String[] splitBySeperator(final String fullname, final char sep) {
        final int pos = fullname.lastIndexOf(sep);
        if (pos < 0) {
            return new String[] { MailFolder.DEFAULT_FOLDER_ID, fullname };
        }
        return new String[] { fullname.substring(0, pos), fullname.substring(pos + 1) };
    }

    private boolean isDefaultFoldersChecked(final int accountId, final Session session) {
        final Boolean b =
            MailSessionCache.getInstance(session).getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderChecked());
        return (b != null) && b.booleanValue();
    }

    private String[] getSortedDefaultMailFolders(final int accountId, final Session session) {
        final String[] arr =
            MailSessionCache.getInstance(session).getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
        if (arr == null) {
            return new String[0];
        }
        return new String[] {
            "INBOX", arr[StorageUtility.INDEX_DRAFTS], arr[StorageUtility.INDEX_SENT], arr[StorageUtility.INDEX_SPAM],
            arr[StorageUtility.INDEX_TRASH] };
    }

    @Override
    public FolderType getFolderType() {
        return folderType;
    }

    @Override
    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        final boolean translate = !StorageParametersUtility.getBoolParameter("ignoreTranslation", storageParameters);
        return getSubfolders(parentId, storageParameters, null, translate);
    }

    private SortableId[] getSubfolders(final String parentId, final StorageParameters storageParameters, final MailAccess<?, ?> mailAccessArg, final boolean translate) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        boolean closeAccess = true;
        try {
            final ServerSession session = getServerSession(storageParameters);
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            if (PRIVATE_FOLDER_ID.equals(parentId)) {
                /*
                 * Get all user mail accounts
                 */
                final List<MailAccount> accounts;
                final ServiceRegistry serviceRegistry = MailServiceRegistry.getServiceRegistry();
                if (session.getUserPermissionBits().isMultipleMailAccounts()) {
                    final MailAccountStorageService storageService = serviceRegistry.getService(MailAccountStorageService.class, true);
                    final MailAccount[] accountsArr = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
                    final List<MailAccount> tmp = new ArrayList<MailAccount>(accountsArr.length);
                    tmp.addAll(Arrays.asList(accountsArr));
                    Collections.sort(tmp, new MailAccountComparator(session.getUser().getLocale()));
                    accounts = tmp;
                } else {
                    accounts = new ArrayList<MailAccount>(1);
                    final MailAccountStorageService storageService = serviceRegistry.getService(MailAccountStorageService.class, true);
                    accounts.add(storageService.getDefaultMailAccount(session.getUserId(), session.getContextId()));
                }
                if (!accounts.isEmpty() && UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(accounts.get(0).getMailProtocol())) {
                    /*
                     * Ensure Unified Mail is enabled; meaning at least one account is subscribed to Unified Mail
                     */
                    final UnifiedInboxManagement uim = serviceRegistry.getService(UnifiedInboxManagement.class);
                    if (null == uim || !uim.isEnabled(session.getUserId(), session.getContextId())) {
                        accounts.remove(0);
                    }
                }
                final int size = accounts.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int j = 0; j < size; j++) {
                    list.add(new MailId(prepareFullname(accounts.get(j).getId(), MailFolder.DEFAULT_FOLDER_ID), j).setName(MailFolder.DEFAULT_FOLDER_NAME));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            // A mail folder denoted by fullname
            final FullnameArgument argument = prepareMailFolderParam(parentId);
            final int accountId = argument.getAccountId();
            final String fullname = argument.getFullname();
            final Boolean accessFast = storageParameters.getParameter(folderType, paramAccessFast);
            if (null == mailAccessArg) {
                mailAccess = MailAccess.getInstance(session, accountId);
                mailAccess.connect(null == accessFast ? true : !accessFast.booleanValue());
            } else {
                mailAccess = mailAccessArg;
                if (mailAccess.isConnected()) {
                    closeAccess = false;
                } else {
                    mailAccess.connect(null == accessFast ? true : !accessFast.booleanValue());
                }
            }

            List<MailFolder> children = new ArrayList<MailFolder>(Arrays.asList(mailAccess.getFolderStorage().getSubfolders(fullname, true)));
            /*
             * Filter against possible POP3 storage folders
             */
            if (MailAccount.DEFAULT_ID == accountId && MailProperties.getInstance().isHidePOP3StorageFolders()) {
                filterPOP3StorageFolders(children, session);
            }
            addWarnings(mailAccess, storageParameters);
            /*
             * Check if denoted parent can hold default folders like Trash, Sent, etc.
             */
            if ((Boolean.TRUE.equals(accessFast)) || (!MailFolder.DEFAULT_FOLDER_ID.equals(fullname) && !"INBOX".equals(fullname))) {
                /*
                 * Denoted parent is not capable to hold default folders. Therefore output as it is.
                 */
                Collections.sort(children, new SimpleMailFolderComparator(storageParameters.getUser().getLocale()));
            } else {
                /*
                 * Ensure default folders are at first positions
                 */
                final String[] names;
                if (isDefaultFoldersChecked(accountId, storageParameters.getSession())) {
                    names = getSortedDefaultMailFolders(accountId, storageParameters.getSession());
                } else {
                    final List<String> tmp = new ArrayList<String>();
                    tmp.add("INBOX");

                    final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                    String fn = folderStorage.getDraftsFolder();
                    if (null != fn) {
                        tmp.add(fn);
                    }

                    fn = folderStorage.getSentFolder();
                    if (null != fn) {
                        tmp.add(fn);
                    }

                    fn = folderStorage.getSpamFolder();
                    if (null != fn) {
                        tmp.add(fn);
                    }

                    fn = folderStorage.getTrashFolder();
                    if (null != fn) {
                        tmp.add(fn);
                    }

                    names = tmp.toArray(new String[tmp.size()]);
                }
                /*
                 * Sort them
                 */
                final Locale locale = storageParameters.getUser().getLocale();
                children = stripNullElementsFrom(children);
                Collections.sort(children, new MailFolderComparator(names, locale));
                /*
                 * i18n INBOX name
                 */
                if (translate) {
                    for (final MailFolder child : children) {
                        if ("INBOX".equals(child.getFullname())) {
                            child.setName(StringHelper.valueOf(locale).getString(MailStrings.INBOX));
                        } else {
                            if (child.containsDefaultFolderType()) {
                                switch (child.getDefaultFolderType()) {
                                case TRASH:
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.TRASH));
                                    break;
                                case SENT:
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.SENT));
                                    break;
                                case SPAM:
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.SPAM));
                                    break;
                                case DRAFTS:
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.DRAFTS));
                                    break;
                                case CONFIRMED_SPAM:
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.CONFIRMED_SPAM));
                                    break;
                                case CONFIRMED_HAM:
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.CONFIRMED_HAM));
                                    break;
                                default:
                                    // Nope
                                }
                            } else {
                                final String fullName = child.getFullname();
                                final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                                if (fullName.equals(folderStorage.getDraftsFolder())) {
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.DRAFTS));
                                } else if (fullName.equals(folderStorage.getSentFolder())) {
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.SENT));
                                } else if (fullName.equals(folderStorage.getSpamFolder())) {
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.SPAM));
                                } else if (fullName.equals(folderStorage.getTrashFolder())) {
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.TRASH));
                                } else if (fullName.equals(folderStorage.getConfirmedSpamFolder())) {
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.CONFIRMED_SPAM));
                                } else if (fullName.equals(folderStorage.getConfirmedHamFolder())) {
                                    child.setName(StringHelper.valueOf(locale).getString(MailStrings.CONFIRMED_HAM));
                                }
                            }
                         }
                    }
                }
            }
            /*
             * Generate sorted IDs preserving order
             */
            final int size = children.size();
            final List<SortableId> list = new ArrayList<SortableId>(size);
            for (int j = 0; j < size; j++) {
                final MailFolder tmp = children.get(j);
                list.add(new MailId(prepareFullname(accountId, tmp.getFullname()), j).setName(tmp.getName()));
            }
            return list.toArray(new SortableId[list.size()]);
        } finally {
            if (closeAccess) {
                closeMailAccess(mailAccess);
            }
        }
    }

    @Override
    public void rollback(final StorageParameters params) {
        /*
         * Nothing to do
         */
    }

    @Override
    public boolean startTransaction(final StorageParameters parameters, final boolean modify) throws OXException {
        /*
         * Nothing to do
         */
        return false;
    }

    @Override
    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            return false;
        }
        MailAccess<?, ?> mailAccess = null;
        try {
            final FullnameArgument argument = prepareMailFolderParam(folderId);
            final String fullname = argument.getFullname();
            if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
                /*
                 * The default folder always exists
                 */
                return true;
            }
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, argument.getAccountId());
            final Boolean accessFast = storageParameters.getParameter(folderType, paramAccessFast);
            mailAccess.connect(null == accessFast ? true : !accessFast.booleanValue());
            final boolean exists = mailAccess.getFolderStorage().exists(fullname);
            addWarnings(mailAccess, storageParameters);
            return exists;
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) throws OXException {
        return new String[0];
    }

    @Override
    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws OXException {
        if (null == includeContentTypes || includeContentTypes.length == 0) {
            return new String[0];
        }
        final List<String> ret = new ArrayList<String>();
        final Set<ContentType> supported = new HashSet<ContentType>(Arrays.asList(getSupportedContentTypes()));
        for (final ContentType includeContentType : includeContentTypes) {
            if (supported.contains(includeContentType)) {
                final SortableId[] subfolders = getSubfolders(treeId, PRIVATE_FOLDER_ID, storageParameters);
                for (final SortableId sortableId : subfolders) {
                    ret.add(sortableId.getId());
                }
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    @Override
    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            final int accountId;
            String fullname;
            {
                final FullnameArgument argument = prepareMailFolderParam(folder.getID());
                accountId = argument.getAccountId();
                fullname = argument.getFullname();
            }
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect(false);

            final MailFolderDescription mfd = new MailFolderDescription();
            mfd.setExists(true);
            // Full name
            mfd.setFullname(fullname);
            mfd.setAccountId(accountId);
            // Parent
            if (null != folder.getParentID()) {
                final FullnameArgument parentArg = prepareMailFolderParam(folder.getParentID());
                mfd.setParentFullname(parentArg.getFullname());
                mfd.setParentAccountId(parentArg.getAccountId());
            }
            // Separator
            {
                final MailFolder mf = mailAccess.getFolderStorage().getFolder(fullname);
                mfd.setSeparator(mf.getSeparator());
            }
            // Name
            {
                final String name = folder.getName();
                if (null != name) {
                    checkFolderName(name);
                    mfd.setName(name);
                }
            }
            // Subscribed
            mfd.setSubscribed(folder.isSubscribed());
            // Permissions
            MailPermission[] mailPermissions = null;
            {
                final Permission[] permissions = folder.getPermissions();
                if (null != permissions && permissions.length > 0) {
                    mailPermissions = new MailPermission[permissions.length];
                    final MailProvider provider = MailProviderRegistry.getMailProviderBySession(session, accountId);
                    for (int i = 0; i < permissions.length; i++) {
                        final Permission permission = permissions[i];
                        final MailPermission mailPerm = provider.createNewMailPermission(session, accountId);
                        mailPerm.setEntity(permission.getEntity());
                        mailPerm.setAllPermission(
                            permission.getFolderPermission(),
                            permission.getReadPermission(),
                            permission.getWritePermission(),
                            permission.getDeletePermission());
                        mailPerm.setFolderAdmin(permission.isAdmin());
                        mailPerm.setGroupPermission(permission.isGroup());
                        mailPermissions[i] = mailPerm;
                    }
                    mfd.addPermissions(mailPermissions);
                }
            }
            final char separator = mfd.getSeparator();
            final String oldParent;
            final String oldName;
            {
                final int pos = fullname.lastIndexOf(separator);
                if (pos == -1) {
                    oldParent = "";
                    oldName = fullname;
                } else {
                    oldParent = fullname.substring(0, pos);
                    oldName = fullname.substring(pos + 1);
                }
            }
            boolean movePerformed = false;
            /*
             * Check if a move shall be performed
             */
            if (mfd.containsParentFullname()) {
                final int parentAccountID = mfd.getParentAccountId();
                if (accountId == parentAccountID) {
                    final String newParent = mfd.getParentFullname();
                    final StringBuilder newFullname = new StringBuilder(16);
                    if (!MailFolder.DEFAULT_FOLDER_ID.equals(newParent)) {
                        newFullname.append(newParent).append(mfd.getSeparator());
                    }
                    newFullname.append(mfd.containsName() ? mfd.getName() : oldName);
                    if (!newParent.equals(oldParent)) { // move & rename
                        final Map<String, Map<?, ?>> subfolders = subfolders(fullname, mailAccess);
                        final String oldFullName = fullname;
                        fullname = mailAccess.getFolderStorage().moveFolder(fullname, newFullname.toString());
                        folder.setID(prepareFullname(accountId, fullname));
                        postChangedId(fullname, oldFullName, Character.toString(separator), session);
                        postEvent4Subfolders(accountId, subfolders, storageParameters);
                        postEvent(accountId, newParent, false, storageParameters);
                        movePerformed = true;
                    }
                } else {
                    // Move to another account
                    MailAccess<?, ?> otherAccess = null;
                    try {
                        otherAccess = MailAccess.getInstance(session, parentAccountID);
                        otherAccess.connect();
                        final String newParent = mfd.getParentFullname();
                        // Check if parent mail folder exists
                        final MailFolder p = otherAccess.getFolderStorage().getFolder(newParent);
                        // Check permission on new parent
                        final MailPermission ownPermission = p.getOwnPermission();
                        if (!ownPermission.canCreateSubfolders()) {
                            throw MailExceptionCode.NO_CREATE_ACCESS.create(newParent);
                        }
                        // Check for duplicate
                        final MailFolder[] tmp = otherAccess.getFolderStorage().getSubfolders(newParent, true);
                        final String lookFor = mfd.containsName() ? mfd.getName() : oldName;
                        for (final MailFolder sub : tmp) {
                            if (sub.getName().equals(lookFor)) {
                                throw MailExceptionCode.DUPLICATE_FOLDER.create(lookFor);
                            }
                        }
                        // Copy
                        final String destFullname =
                            fullCopy(
                                mailAccess,
                                fullname,
                                otherAccess,
                                newParent,
                                p.getSeparator(),
                                storageParameters.getUserId(),
                                otherAccess.getMailConfig().getCapabilities().hasPermissions());
                        postEvent(parentAccountID, newParent, false, storageParameters);
                        // Delete source
                        final Map<String, Map<?, ?>> subfolders = subfolders(fullname, mailAccess);
                        mailAccess.getFolderStorage().deleteFolder(fullname, true);
                        // Perform other updates
                        otherAccess.getFolderStorage().updateFolder(destFullname, mfd);
                        postEvent4Subfolders(accountId, subfolders, storageParameters);
                        // Reset identifier
                        folder.setID(prepareFullname(parentAccountID, destFullname));
                        /*
                         * Handle update of permission or subscription
                         */
                        otherAccess.getFolderStorage().updateFolder(destFullname, mfd);
                        addWarnings(otherAccess, storageParameters);
                        postEvent(parentAccountID, destFullname, false, storageParameters);
                        /*
                         * Leave routine...
                         */
                        return;
                    } finally {
                        if (null != otherAccess) {
                            otherAccess.close(true);
                        }
                    }
                }
            }
            /*
             * Check if a rename shall be performed
             */
            if (!movePerformed && mfd.containsName()) {
                final String newName = mfd.getName();
                if (!newName.equals(oldName)) { // rename
                    final String oldFullName = fullname;
                    fullname = mailAccess.getFolderStorage().renameFolder(fullname, newName);
                    folder.setID(prepareFullname(accountId, fullname));
                    postChangedId(fullname, oldFullName, Character.toString(separator), session);
                    postEvent(accountId, fullname, false, storageParameters);
                }
            }
            /*
             * Handle update of permission or subscription
             */
            mailAccess.getFolderStorage().updateFolder(fullname, mfd);
            /*
             * Is hand-down?
             */
            if ((null != mailPermissions) && StorageParametersUtility.isHandDownPermissions(storageParameters)) {
                handDown(accountId, fullname, mailPermissions, mailAccess, storageParameters);
            }
            addWarnings(mailAccess, storageParameters);
            postEvent(accountId, fullname, false, storageParameters);
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    private static void handDown(final int accountId, final String parentFullName, final MailPermission[] mailPermissions, final MailAccess<?, ?> mailAccess, final StorageParameters params) throws OXException {
        final MailFolder[] subfolders = mailAccess.getFolderStorage().getSubfolders(parentFullName, true);
        for (MailFolder mailFolder : subfolders) {
            final MailFolderDescription mfd = new MailFolderDescription();
            mfd.setExists(true);
            final String childFullName = mailFolder.getFullname();
            mfd.setFullname(childFullName);
            mfd.setAccountId(accountId);
            mfd.addPermissions(mailPermissions);
            mailAccess.getFolderStorage().updateFolder(childFullName, mfd);
            postEvent(accountId, childFullName, false, params);
            // Recursive
            handDown(accountId, childFullName, mailPermissions, mailAccess, params);
        }
    }

    private static String fullCopy(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> srcAccess, final String srcFullname, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> destAccess, final String destParent, final char destSeparator, final int user, final boolean hasPermissions) throws OXException {
        // Create folder
        final MailFolder source = srcAccess.getFolderStorage().getFolder(srcFullname);
        final MailFolderDescription mfd = new MailFolderDescription();
        mfd.setName(source.getName());
        mfd.setParentFullname(destParent);
        mfd.setSeparator(destSeparator);
        mfd.setSubscribed(source.isSubscribed());
        if (hasPermissions) {
            // Copy permissions
            final MailPermission[] perms = source.getPermissions();
            try {
                for (final MailPermission perm : perms) {
                    mfd.addPermission((MailPermission) perm.clone());
                }
            } catch (final CloneNotSupportedException e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        final String destFullname = destAccess.getFolderStorage().createFolder(mfd);
        // Copy messages
        final MailMessage[] msgs =
            srcAccess.getMessageStorage().getAllMessages(
                srcFullname,
                null,
                MailSortField.RECEIVED_DATE,
                OrderDirection.ASC,
                new MailField[] { MailField.FULL });
        final IMailMessageStorage destMessageStorage = destAccess.getMessageStorage();
        // Append messages to destination account
        /* final String[] mailIds = */destMessageStorage.appendMessages(destFullname, msgs);
        /*-
         *
        // Ensure flags
        final String[] arr = new String[1];
        for (int i = 0; i < msgs.length; i++) {
            final MailMessage m = msgs[i];
            final String mailId = mailIds[i];
            if (null != m && null != mailId) {
                arr[0] = mailId;
                // System flags
                destMessageStorage.updateMessageFlags(destFullname, arr, m.getFlags(), true);
                // Color label
                if (m.containsColorLabel() && m.getColorLabel() != MailMessage.COLOR_LABEL_NONE) {
                    destMessageStorage.updateMessageColorLabel(destFullname, arr, m.getColorLabel());
                }
            }
        }
         */
        // Iterate subfolders
        final MailFolder[] tmp = srcAccess.getFolderStorage().getSubfolders(srcFullname, true);
        for (final MailFolder element : tmp) {
            fullCopy(srcAccess, element.getFullname(), destAccess, destFullname, destSeparator, user, hasPermissions);
        }
        return destFullname;
    }

    private static final class MailAccountComparator implements Comparator<MailAccount> {

        private final Collator collator;

        public MailAccountComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        @Override
        public int compare(final MailAccount o1, final MailAccount o2) {
            if (UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(o1.getMailProtocol())) {
                if (UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                    return 0;
                }
                return -1;
            } else if (UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
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

    private static final class SimpleMailFolderComparator implements Comparator<MailFolder> {

        private final Collator collator;

        public SimpleMailFolderComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        @Override
        public int compare(final MailFolder o1, final MailFolder o2) {
            return collator.compare(o1.getName(), o2.getName());
        }
    }

    private static final class MailFolderComparator implements Comparator<MailFolder> {

        private final Map<String, Integer> indexMap;

        private final Collator collator;

        private final Integer na;

        public MailFolderComparator(final String[] names, final Locale locale) {
            super();
            indexMap = new HashMap<String, Integer>(names.length);
            for (int i = 0; i < names.length; i++) {
                indexMap.put(names[i], Integer.valueOf(i));
            }
            na = Integer.valueOf(names.length);
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        private Integer getNumberOf(final String name) {
            final Integer ret = indexMap.get(name);
            if (null == ret) {
                return na;
            }
            return ret;
        }

        @Override
        public int compare(final MailFolder o1, final MailFolder o2) {
            if (o1.isDefaultFolder()) {
                if (o2.isDefaultFolder()) {
                    return getNumberOf(o1.getFullname()).compareTo(getNumberOf(o2.getFullname()));
                }
                return -1;
            }
            if (o2.isDefaultFolder()) {
                return 1;
            }
            return collator.compare(o1.getName(), o2.getName());
        }
    }

    private static void postEvent4Subfolders(final int accountId, final Map<String, Map<?, ?>> subfolders, final StorageParameters params) {
        final int size = subfolders.size();
        final Iterator<Entry<String, Map<?, ?>>> iter = subfolders.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Entry<String, Map<?, ?>> entry = iter.next();
            final @SuppressWarnings("unchecked") Map<String, Map<?, ?>> m = (Map<String, Map<?, ?>>) entry.getValue();
            if (!m.isEmpty()) {
                postEvent4Subfolders(accountId, m, params);
            }
            postEvent(accountId, entry.getKey(), false, true, false, params);
        }
    }

    private static Map<String, Map<?, ?>> subfolders(final String fullname, final MailAccess<?, ?> mailAccess) throws OXException {
        final Map<String, Map<?, ?>> m = new HashMap<String, Map<?, ?>>();
        subfoldersRecursively(fullname, m, mailAccess);
        return m;
    }

    private static void subfoldersRecursively(final String parent, final Map<String, Map<?, ?>> m, final MailAccess<?, ?> mailAccess) throws OXException {
        final MailFolder[] mailFolders = mailAccess.getFolderStorage().getSubfolders(parent, true);
        if (null == mailFolders || 0 == mailFolders.length) {
            final Map<String, Map<?, ?>> emptyMap = Collections.emptyMap();
            m.put(parent, emptyMap);
        } else {
            final Map<String, Map<?, ?>> subMap = new HashMap<String, Map<?, ?>>();
            final int size = mailFolders.length;
            for (int i = 0; i < size; i++) {
                final String fullname = mailFolders[i].getFullname();
                subfoldersRecursively(fullname, subMap, mailAccess);
            }
            m.put(parent, subMap);
        }
    }

    private static void postChangedId(final String newId, final String oldId, final String delim, final Session session) {
        final EventAdmin eventAdmin = MailServiceRegistry.getServiceRegistry().getService(EventAdmin.class);
        if (null != eventAdmin) {
            final Map<String, Object> properties = new HashMap<String, Object>(6);
            properties.put(FolderEventConstants.PROPERTY_SESSION, session);
            properties.put(FolderEventConstants.PROPERTY_CONTEXT, Integer.valueOf(session.getContextId()));
            properties.put(FolderEventConstants.PROPERTY_USER, Integer.valueOf(session.getUserId()));
            properties.put(FolderEventConstants.PROPERTY_OLD_IDENTIFIER, oldId);
            properties.put(FolderEventConstants.PROPERTY_NEW_IDENTIFIER, newId);
            properties.put(FolderEventConstants.PROPERTY_DELIMITER, delim);
            properties.put(FolderEventConstants.PROPERTY_IMMEDIATELY, Boolean.TRUE);
            final Event event = new Event(FolderEventConstants.TOPIC_IDENTIFIERS, properties);
            eventAdmin.postEvent(event);
        }
    }

    private static void postEvent(final int accountId, final String fullname, final boolean contentRelated, final StorageParameters params) {
        postEvent(accountId, fullname, contentRelated, false, params);
    }

    private static void postEvent(final int accountId, final String fullname, final boolean contentRelated, final boolean immediateDelivery, final StorageParameters params) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(
            new PooledEvent(params.getContextId(), params.getUserId(), accountId, prepareFullname(accountId, fullname), contentRelated, immediateDelivery, params.getSession()));
    }

    private static void postEvent(final String topic, final int accountId, final String fullname, final boolean contentRelated, final boolean immediateDelivery, final StorageParameters params) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(new PooledEvent(topic, params.getContextId(), params.getUserId(), accountId, prepareFullname(accountId, fullname), contentRelated, immediateDelivery, params.getSession()));
    }

    private static void postEvent(final int accountId, final String fullname, final boolean contentRelated, final boolean immediateDelivery, final boolean async, final StorageParameters params) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(
            new PooledEvent(params.getContextId(), params.getUserId(), accountId, prepareFullname(accountId, fullname), contentRelated, immediateDelivery, params.getSession()).setAsync(async));
    }

    /**
     * Closes specified {@link MailAccess} instance
     *
     * @param mailAccess The mail access to close
     */
    protected static void closeMailAccess(final MailAccess<?, ?> mailAccess) {
        if (null != mailAccess) {
            mailAccess.close(true);
        }
    }

    private static void addWarnings(final MailAccess<?, ?> mailAccess, final StorageParameters storageParameters) {
        final Collection<OXException> warnings = mailAccess.getWarnings();
        if (!warnings.isEmpty()) {
            for (final OXException OXException : warnings) {
                storageParameters.addWarning(OXException);
            }
        }
    }

    private static <E> List<E> stripNullElementsFrom(final List<E> list) {
        if (null == list || list.isEmpty()) {
            return list;
        }
        final List<E> ret = new ArrayList<E>(list.size());
        for (final E element : list) {
            if (null != element) {
                ret.add(element);
            }
        }
        return ret;
    }

    private final static String INVALID = "<>"; // "()<>;\".[]";

    private static void checkFolderName(final String name) throws OXException {
        if (isEmpty(name)) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
        }
        final int length = name.length();
        for (int i = 0; i < length; i++) {
            if (INVALID.indexOf(name.charAt(i)) >= 0) {
                throw MailExceptionCode.INVALID_FOLDER_NAME2.create(name);
            }
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
