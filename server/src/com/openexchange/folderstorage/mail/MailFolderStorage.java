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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import com.openexchange.api2.OXException;
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
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
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

    public ContentType[] getSupportedContentTypes() {
        return new ContentType[] { MailContentType.getInstance() };
    }

    public void commitTransaction(final StorageParameters params) throws FolderException {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) params.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);
            if (null != mailServletInterface) {
                mailServletInterface.close(true);
            }
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);

            if (null == mailServletInterface) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final MailFolderDescription mfd = new MailFolderDescription();
            mfd.setExists(false);
            // Parent
            final FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(folder.getParentID());
            mfd.setParentFullname(arg.getFullname());
            mfd.setParentAccountId(arg.getAccountId());
            // Separator
            {
                final MailFolder parent = mailServletInterface.getFolder(folder.getParentID(), true);
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
            mailServletInterface.saveFolder(mfd);

        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);

            if (null == mailServletInterface) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            mailServletInterface.deleteFolder(folderId);
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final StorageParameters storageParameters) throws FolderException {
        if (!MailContentType.getInstance().equals(contentType)) {
            // TODO: Throw appropriate folder exception
        }
        // Return primary account's INBOX folder
        return MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, "INBOX");
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);

            if (null == mailServletInterface) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final MailFolder mailFolder = mailServletInterface.getFolder(folderId, true);
            final MailFolderImpl retval = new MailFolderImpl(mailFolder, mailServletInterface.getAccountID());
            retval.setTreeID(treeId);

            // TODO: Fill subfolder IDs? Or leave to null to force FolderStorage.getSubfolders()?
            final SearchIterator<MailFolder> iter = mailServletInterface.getChildFolders(MailFolderUtility.prepareFullname(
                mailServletInterface.getAccountID(),
                mailFolder.getFullname()), true);
            try {
                final String[] subfolderIds = new String[iter.size()];
                for (int i = 0; i < subfolderIds.length; i++) {
                    subfolderIds[i] = MailFolderUtility.prepareFullname(mailServletInterface.getAccountID(), iter.next().getFullname());
                }
            } catch (final SearchIteratorException e) {
                throw new FolderException(e);
            } catch (final OXException e) {
                throw new FolderException(e);
            } finally {
                try {
                    iter.close();
                } catch (final SearchIteratorException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            return retval;
        } catch (final MailException e) {
            throw new FolderException(e);
        }
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
                    list.add(new MailId(MailFolderUtility.prepareFullname(accounts.get(j).getId(), MailFolder.DEFAULT_FOLDER_ID), j));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            // A mail folder denoted by fullname
            final MailServletInterface mailServletInterface = (MailServletInterface) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);

            if (null == mailServletInterface) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final SearchIterator<MailFolder> iter = mailServletInterface.getChildFolders(parentId, true);
            try {
                final int size = iter.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int j = 0; j < size; j++) {
                    list.add(new MailId(
                        MailFolderUtility.prepareFullname(mailServletInterface.getAccountID(), iter.next().getFullname()),
                        j));
                }
                return list.toArray(new SortableId[list.size()]);
            } catch (final SearchIteratorException e) {
                throw new FolderException(e);
            } catch (final OXException e) {
                throw new FolderException(e);
            } finally {
                try {
                    iter.close();
                } catch (final SearchIteratorException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
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
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) params.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);
            if (null != mailServletInterface) {
                mailServletInterface.close(true);
            }
        } catch (final MailException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public StorageParameters startTransaction(final StorageParameters parameters, final boolean modify) throws FolderException {
        try {
            final Session session = parameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            parameters.putParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS,
                MailServletInterface.getInstance(session));
            return parameters;
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailParameterConstants.PARAM_MAIL_ACCESS);

            if (null == mailServletInterface) {
                throw new FolderException(new MailException(MailException.Code.MISSING_PARAM, MailParameterConstants.PARAM_MAIL_ACCESS));
            }

            final MailFolderDescription mfd = new MailFolderDescription();
            mfd.setExists(true);
            // Fullname
            final FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(folder.getID());
            mfd.setFullname(arg.getFullname());
            mfd.setAccountId(arg.getAccountId());
            // Separator
            {
                final MailFolder mf = mailServletInterface.getFolder(folder.getID(), true);
                mfd.setSeparator(mf.getSeparator());
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
            mailServletInterface.saveFolder(mfd);
        } catch (final MailException e) {
            throw new FolderException(e);
        }
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

    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

}
