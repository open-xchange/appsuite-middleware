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

package com.openexchange.folderstorage.mail;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.cache.OXCachingException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link MailFolderStorage} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderStorage implements FolderStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailFolderStorage.class);

    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    /**
     * Initializes a new {@link MailFolderStorage}.
     */
    public MailFolderStorage() {
        super();
    }

    private MailAccess<?, ?> checkMailAccess(final int accountId, final MailAccess<?, ?> openedAccess, final StorageParameters storageParameters) throws FolderException {
        if (accountId != openedAccess.getAccountId()) {
            openedAccess.close(true);
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final MailAccess<?, ?> mailAccess;
            try {
                mailAccess = initMailAccess(accountId, session);
            } catch (final MailException e) {
                throw new FolderException(e);
            }
            storageParameters.putParameter(MailFolderType.getInstance(), MailParameterConstants.PARAM_MAIL_ACCESS, mailAccess);
            return mailAccess;
        }
        return openedAccess;
    }

    private MailAccess<?, ?> initMailAccess(final int accountId, final Session session) throws MailException {
        /*
         * Fetch a mail access (either from cache or a new instance)
         */
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, accountId);
        if (!mailAccess.isConnected()) {
            /*
             * Get new mail configuration
             */
            final long start = System.currentTimeMillis();
            try {
                mailAccess.connect();
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                mailInterfaceMonitor.changeNumSuccessfulLogins(true);
            } catch (final MailException e) {
                final int number = e.getDetailNumber();
                if (number == MIMEMailException.Code.LOGIN_FAILED.getNumber() || number == MIMEMailException.Code.INVALID_CREDENTIALS.getNumber()) {
                    mailInterfaceMonitor.changeNumFailedLogins(true);
                }
                throw e;
            }
        }
        return mailAccess;
    }

    public ContentType[] getSupportedContentTypes() {
        return new ContentType[] { MailContentType.getInstance() };
    }

    public ContentType getDefaultContentType() {
        return MailContentType.getInstance();
    }

    public void commitTransaction(final StorageParameters params) throws FolderException {
        final MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) params.getParameter(
            MailFolderType.getInstance(),
            MailParameterConstants.PARAM_MAIL_ACCESS);
        if (null != mailAccess) {
            try {
                mailAccess.close(true);
            } finally {
                params.putParameter(MailFolderType.getInstance(), MailParameterConstants.PARAM_MAIL_ACCESS, null);
            }
        }
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        try {
            MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);
            if (null == mailAccess) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final MailFolderDescription mfd = new MailFolderDescription();
            mfd.setExists(false);
            // Parent
            final FullnameArgument arg = prepareMailFolderParam(folder.getParentID());
            mfd.setParentFullname(arg.getFullname());
            mfd.setParentAccountId(arg.getAccountId());
            // Separator
            {
                mailAccess = checkMailAccess(arg.getAccountId(), mailAccess, storageParameters);
                final MailFolder parent = mailAccess.getFolderStorage().getFolder(arg.getFullname());
                mfd.setSeparator(parent.getSeparator());
            }
            // Other
            mfd.setName(folder.getName());
            mfd.setSubscribed(folder.isSubscribed());
            // Permissions
            final Permission[] permissions = folder.getPermissions();
            if (null != permissions && permissions.length > 0) {
                final MailPermission[] mailPermissions = new MailPermission[permissions.length];
                final Session session = storageParameters.getSession();
                if (null == session) {
                    throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
                }
                final MailProvider provider = MailProviderRegistry.getMailProviderBySession(session, arg.getAccountId());
                for (int i = 0; i < permissions.length; i++) {
                    final Permission permission = permissions[i];
                    final MailPermission mailPerm = provider.createNewMailPermission();
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
            final String fullname = mailAccess.getFolderStorage().createFolder(mfd);
            folder.setID(prepareFullname(arg.getAccountId(), fullname));
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);
            if (null == mailAccess) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final FullnameArgument arg = prepareMailFolderParam(folderId);
            mailAccess = checkMailAccess(arg.getAccountId(), mailAccess, storageParameters);
            final String fullname = arg.getFullname();
            /*
             * Only backup if fullname does not denote trash (sub)folder
             */
            mailAccess.getFolderStorage().clearFolder(fullname, (fullname.startsWith(mailAccess.getFolderStorage().getTrashFolder())));
            try {
                /*
                 * Update message cache
                 */
                MailMessageCache.getInstance().removeFolderMessages(
                    arg.getAccountId(),
                    fullname,
                    storageParameters.getUser().getId(),
                    storageParameters.getContext().getContextId());
            } catch (final OXCachingException e) {
                LOG.error(e.getMessage(), e);
            }
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);
            if (null == mailAccess) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final FullnameArgument arg = prepareMailFolderParam(folderId);
            mailAccess = checkMailAccess(arg.getAccountId(), mailAccess, storageParameters);
            final String fullname = arg.getFullname();
            /*
             * Only backup if fullname does not denote trash (sub)folder
             */
            mailAccess.getFolderStorage().deleteFolder(fullname, (fullname.startsWith(mailAccess.getFolderStorage().getTrashFolder())));
            try {
                /*
                 * Update message cache
                 */
                MailMessageCache.getInstance().removeFolderMessages(
                    arg.getAccountId(),
                    fullname,
                    storageParameters.getUser().getId(),
                    storageParameters.getContext().getContextId());
            } catch (final OXCachingException e) {
                LOG.error(e.getMessage(), e);
            }
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final StorageParameters storageParameters) throws FolderException {
        if (!MailContentType.getInstance().equals(contentType)) {
            // TODO: Throw appropriate folder exception
        }
        // Return primary account's INBOX folder
        return prepareFullname(MailAccount.DEFAULT_ID, "INBOX");
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);
            if (null == mailAccess) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final FullnameArgument argument = prepareMailFolderParam(folderId);
            final int accountId = argument.getAccountId();
            final String fullname = argument.getFullname();

            final MailFolder mailFolder;
            if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
                if (mailAccess.getAccountId() != accountId) {
                    /*
                     * Another account
                     */
                    mailAccess.close(true);
                    mailAccess = MailAccess.getInstance(storageParameters.getSession(), accountId);
                    mailFolder = mailAccess.getRootFolder();
                    storageParameters.putParameter(MailFolderType.getInstance(), MailParameterConstants.PARAM_MAIL_ACCESS, mailAccess);
                } else {
                    mailFolder = mailAccess.getRootFolder();
                }
            } else {
                mailAccess = checkMailAccess(accountId, mailAccess, storageParameters);
                mailFolder = mailAccess.getFolderStorage().getFolder(fullname);
            }

            final MailFolderImpl retval = new MailFolderImpl(
                mailFolder,
                mailAccess.getAccountId(),
                mailAccess.getMailConfig().getCapabilities().getCapabilities());
            retval.setTreeID(treeId);

            // TODO: Fill subfolder IDs? Or leave to null to force FolderStorage.getSubfolders()?
            final List<MailFolder> children = Arrays.asList(mailAccess.getFolderStorage().getSubfolders(fullname, true));
            /*
             * Check if denoted parent can hold default folders like Trash, Sent, etc.
             */

            if (!MailFolder.DEFAULT_FOLDER_ID.equals(fullname) && !prepareMailFolderParam(
                mailAccess.getFolderStorage().getFolder("INBOX").getFullname()).equals(fullname)) {
                /*
                 * Denoted parent is not capable to hold default folders. Therefore output as it is.
                 */
                Collections.sort(children, new SimpleMailFolderComparator(storageParameters.getUser().getLocale()));
                final String[] subfolderIds = new String[children.size()];
                int i = 0;
                for (final MailFolder child : children) {
                    subfolderIds[i++] = prepareFullname(accountId, child.getFullname());
                }
                retval.setSubfolderIDs(subfolderIds);
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
                Collections.sort(children, new MailFolderComparator(names, storageParameters.getUser().getLocale()));
                final String[] subfolderIds = new String[children.size()];
                int i = 0;
                for (final MailFolder child : children) {
                    subfolderIds[i++] = prepareFullname(accountId, child.getFullname());
                }
                retval.setSubfolderIDs(subfolderIds);
            }

            return retval;
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    private boolean isDefaultFoldersChecked(final int accountId, final Session session) {
        final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.getParamDefaultFolderChecked(accountId));
        return (b != null) && b.booleanValue();
    }

    private String[] getSortedDefaultMailFolders(final int accountId, final Session session) {
        final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.getParamDefaultFolderArray(accountId));
        if (arr == null) {
            return new String[0];
        }
        return new String[] {
            "INBOX", arr[StorageUtility.INDEX_DRAFTS], arr[StorageUtility.INDEX_SENT], arr[StorageUtility.INDEX_SPAM],
            arr[StorageUtility.INDEX_TRASH] };
    }

    public FolderType getFolderType() {
        return MailFolderType.getInstance();
    }

    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters) throws FolderException {
        try {
            final ServerSession session;
            {
                final Session s = storageParameters.getSession();
                if (null == s) {
                    throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
                }
                if (s instanceof ServerSession) {
                    session = (ServerSession) s;
                } else {
                    session = new ServerSessionAdapter(s);
                }
            }

            if (PRIVATE_FOLDER_ID.equals(parentId)) {
                /*
                 * Get all user mail accounts
                 */
                final List<MailAccount> accounts;
                if (session.getUserConfiguration().isMultipleMailAccounts()) {
                    final MailAccountStorageService storageService = MailServiceRegistry.getServiceRegistry().getService(
                        MailAccountStorageService.class,
                        true);
                    final MailAccount[] accountsArr = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
                    final List<MailAccount> tmp = new ArrayList<MailAccount>(accountsArr.length);
                    tmp.addAll(Arrays.asList(accountsArr));
                    Collections.sort(tmp, new MailAccountComparator(session.getUser().getLocale()));
                    accounts = tmp;
                } else {
                    accounts = new ArrayList<MailAccount>(1);
                    final MailAccountStorageService storageService = MailServiceRegistry.getServiceRegistry().getService(
                        MailAccountStorageService.class,
                        true);
                    accounts.add(storageService.getDefaultMailAccount(session.getUserId(), session.getContextId()));
                }
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(accounts.get(0).getMailProtocol())) {
                    /*
                     * Ensure Unified INBOX is enabled; meaning at least one account is subscribed to Unified INBOX
                     */
                    final UnifiedINBOXManagement uim = MailServiceRegistry.getServiceRegistry().getService(UnifiedINBOXManagement.class);
                    if (null == uim || !uim.isEnabled(session.getUserId(), session.getContextId())) {
                        accounts.remove(0);
                    }
                }
                final int size = accounts.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int j = 0; j < size; j++) {
                    list.add(new MailId(prepareFullname(accounts.get(j).getId(), MailFolder.DEFAULT_FOLDER_ID), j));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            // A mail folder denoted by fullname
            MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);
            if (null == mailAccess) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final FullnameArgument argument = prepareMailFolderParam(parentId);
            final int accountId = argument.getAccountId();
            final String fullname = argument.getFullname();
            mailAccess = checkMailAccess(accountId, mailAccess, storageParameters);

            final List<MailFolder> children = Arrays.asList(mailAccess.getFolderStorage().getSubfolders(fullname, true));
            /*
             * Check if denoted parent can hold default folders like Trash, Sent, etc.
             */
            if (!MailFolder.DEFAULT_FOLDER_ID.equals(fullname) && !prepareMailFolderParam(
                mailAccess.getFolderStorage().getFolder("INBOX").getFullname()).equals(fullname)) {
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
                Collections.sort(children, new MailFolderComparator(names, storageParameters.getUser().getLocale()));
            }

            final List<SortableId> list = new ArrayList<SortableId>(children.size());
            final int size = children.size();
            for (int j = 0; j < size; j++) {
                list.add(new MailId(prepareFullname(mailAccess.getAccountId(), children.get(j).getFullname()), j));
            }
            return list.toArray(new SortableId[list.size()]);
        } catch (final MailException e) {
            throw new FolderException(e);
        } catch (final ContextException e) {
            throw new FolderException(e);
        } catch (final ServiceException e) {
            throw new FolderException(e);
        } catch (final MailAccountException e) {
            throw new FolderException(e);
        }
    }

    public void rollback(final StorageParameters params) {
        final MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) params.getParameter(
            MailFolderType.getInstance(),
            MailParameterConstants.PARAM_MAIL_ACCESS);
        if (null != mailAccess) {
            try {
                mailAccess.close(true);
            } finally {
                params.putParameter(MailFolderType.getInstance(), MailParameterConstants.PARAM_MAIL_ACCESS, null);
            }
        }
    }

    public StorageParameters startTransaction(final StorageParameters parameters, final boolean modify) throws FolderException {
        try {
            final Session session = parameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            parameters.putParameterIfAbsent(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS,
                MailAccess.getInstance(session));
            return parameters;
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);
            if (null == mailAccess) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final FullnameArgument argument = prepareMailFolderParam(folderId);
            mailAccess = checkMailAccess(argument.getAccountId(), mailAccess, storageParameters);

            return mailAccess.getFolderStorage().exists(folderId);
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        try {
            MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);
            if (null == mailAccess) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final int accountId;
            String fullname;
            {
                final FullnameArgument argument = prepareMailFolderParam(folder.getID());
                accountId = argument.getAccountId();
                fullname = argument.getFullname();
            }
            mailAccess = checkMailAccess(accountId, mailAccess, storageParameters);

            final MailFolderDescription mfd = new MailFolderDescription();
            mfd.setExists(true);
            // Fullname
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
            if (null != folder.getName()) {
                mfd.setName(folder.getName());
            }
            // Subscribed
            mfd.setSubscribed(folder.isSubscribed());
            // Permissions
            final Permission[] permissions = folder.getPermissions();
            if (null != permissions && permissions.length > 0) {
                final MailPermission[] mailPermissions = new MailPermission[permissions.length];
                final Session session = storageParameters.getSession();
                if (null == session) {
                    throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
                }
                final MailProvider provider = MailProviderRegistry.getMailProviderBySession(session, accountId);
                for (int i = 0; i < permissions.length; i++) {
                    final Permission permission = permissions[i];
                    final MailPermission mailPerm = provider.createNewMailPermission();
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
                    final StringBuilder newFullname = new StringBuilder(newParent).append(mfd.getSeparator());
                    if (mfd.containsName()) {
                        newFullname.append(mfd.getName());
                    } else {
                        newFullname.append(oldName);
                    }
                    if (!newParent.equals(oldParent)) { // move & rename
                        fullname = mailAccess.getFolderStorage().moveFolder(fullname, newFullname.toString());
                        movePerformed = true;
                    }
                } else {
                    // Move to another account
                    final MailAccess<?, ?> otherAccess = initMailAccess(parentAccountID, storageParameters.getSession());
                    try {
                        final String newParent = mfd.getParentFullname();
                        // Check if parent mail folder exists
                        final MailFolder p = otherAccess.getFolderStorage().getFolder(newParent);
                        // Check permission on new parent
                        final MailPermission ownPermission = p.getOwnPermission();
                        if (!ownPermission.canCreateSubfolders()) {
                            throw new MailException(MailException.Code.NO_CREATE_ACCESS, newParent);
                        }
                        // Check for duplicate
                        final MailFolder[] tmp = otherAccess.getFolderStorage().getSubfolders(newParent, true);
                        final String lookFor = mfd.containsName() ? mfd.getName() : oldName;
                        for (final MailFolder sub : tmp) {
                            if (sub.getName().equals(lookFor)) {
                                throw new MailException(MailException.Code.DUPLICATE_FOLDER, lookFor);
                            }
                        }
                        // Copy
                        final String destFullname = fullCopy(
                            mailAccess,
                            fullname,
                            otherAccess,
                            newParent,
                            p.getSeparator(),
                            storageParameters.getUser().getId(),
                            otherAccess.getMailConfig().getCapabilities().hasPermissions());
                        // Delete source
                        mailAccess.getFolderStorage().deleteFolder(fullname, true);
                        // Perform other updates
                        otherAccess.getFolderStorage().updateFolder(destFullname, mfd);
                    } finally {
                        otherAccess.close(true);
                    }
                }
            }
            /*
             * Check if a rename shall be performed
             */
            if (!movePerformed && mfd.containsName()) {
                final String newName = mfd.getName();
                if (!newName.equals(oldName)) { // rename
                    fullname = mailAccess.getFolderStorage().renameFolder(fullname, newName);
                }
            }
            /*
             * Handle update of permission or subscription
             */
            mailAccess.getFolderStorage().updateFolder(fullname, mfd);
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    private static String fullCopy(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> srcAccess, final String srcFullname, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> destAccess, final String destParent, final char destSeparator, final int user, final boolean hasPermissions) throws MailException {
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
                for (int i = 0; i < perms.length; i++) {
                    mfd.addPermission((MailPermission) perms[i].clone());
                }
            } catch (final CloneNotSupportedException e) {
                throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
            }
        }
        final String destFullname = destAccess.getFolderStorage().createFolder(mfd);
        // Copy messages
        final MailMessage[] msgs = srcAccess.getMessageStorage().getAllMessages(
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
        for (int i = 0; i < tmp.length; i++) {
            fullCopy(srcAccess, tmp[i].getFullname(), destAccess, destFullname, destSeparator, user, hasPermissions);
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

        public int compare(final MailAccount o1, final MailAccount o2) {
            if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o1.getMailProtocol())) {
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                    return 0;
                }
                return -1;
            } else if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
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

}
