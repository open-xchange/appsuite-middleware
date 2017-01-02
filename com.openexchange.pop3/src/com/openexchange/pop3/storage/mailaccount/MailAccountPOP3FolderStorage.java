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

package com.openexchange.pop3.storage.mailaccount;

import static com.openexchange.pop3.storage.mailaccount.util.Utility.prependPath2Fullname;
import static com.openexchange.pop3.storage.mailaccount.util.Utility.stripPathFromFullname;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.Quota;
import com.openexchange.mail.Quota.Type;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageInfoSupport;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailFolderInfo;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.NoSpamHandler;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailAccountPOP3FolderStorage} - The folder storage for (primary) mail account POP3 storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountPOP3FolderStorage implements IMailFolderStorage, IMailFolderStorageInfoSupport {

    static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(MailAccountPOP3FolderStorage.class);

    private final IMailFolderStorage delegatee;

    /**
     * Associated POP3 storage.
     */
    final MailAccountPOP3Storage storage;
    private MailAccountPOP3MessageStorage messageStorage;
    private volatile Context ctx;

    /**
     * The session.
     */
    final Session session;

    /**
     * The account identifier
     */
    final int accountId;

    /**
     * The POP3 account's path.
     */
    final String path;

    MailAccountPOP3FolderStorage(final IMailFolderStorage delegatee, final MailAccountPOP3Storage storage, final POP3Access pop3Access) {
        super();
        this.storage = storage;
        session = pop3Access.getSession();
        this.delegatee = delegatee;
        accountId = pop3Access.getAccountId();
        path = storage.getPath();
    }

    private MailAccountPOP3MessageStorage getMessageStorage() throws OXException {
        if (null == messageStorage) {
            messageStorage = (MailAccountPOP3MessageStorage) storage.getMessageStorage();
        }
        return messageStorage;
    }

    private MailPermission getPOP3MailPermission() {
        final MailPermission mp = new DefaultMailPermission();
        mp.setEntity(session.getUserId());
        return mp;
    }

    /**
     * Gets the context.
     *
     * @return The context
     * @throws OXException If context cannot be returned
     */
    Context getContext() throws OXException {
        Context ctx = this.ctx;
        if (null == ctx) {
            synchronized (this) {
                ctx = this.ctx;
                if (null == ctx) {
                    if (session instanceof ServerSession) {
                        this.ctx = ctx = ((ServerSession) session).getContext();
                    } else {
                        try {
                            this.ctx = ctx = POP3ServiceRegistry.getServiceRegistry().getService(ContextService.class, true).getContext(session.getContextId());
                        } catch (final OXException e) {
                            throw e;
                        }
                    }
                }
            } // synchronized
        }
        return ctx;
    }

    private String getStandardFolder(final int index) throws OXException {
        final MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        if (!isDefaultFoldersChecked(mailSessionCache)) {
            checkDefaultFolders();
        }
        final String[] arr =
            MailSessionCache.getInstance(session).getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
        if (null == arr) {
            setDefaultFoldersChecked(false, mailSessionCache);
            checkDefaultFolders();
            return getDefaultMailFolder(index);
        }
        return arr[index];
    }

    private String getDefaultMailFolder(final int index) {
        final String[] arr =
            MailSessionCache.getInstance(session).getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
        return arr == null ? null : arr[index];
    }

    private boolean isDefaultFolder(final String fullname) throws OXException {
        for (int i = 0; i < 7; i++) {
            final String stdFullname = getStandardFolder(i);
            if (null != stdFullname && stdFullname.equals(fullname)) {
                return true;
            }
        }
        return false;
    }

    private static final DefaultFolderType[] TYPES =
        {
            DefaultFolderType.DRAFTS, DefaultFolderType.SENT, DefaultFolderType.SPAM, DefaultFolderType.TRASH,
            DefaultFolderType.CONFIRMED_SPAM, DefaultFolderType.CONFIRMED_HAM, DefaultFolderType.INBOX };

    private void setDefaultFolderInfo(final MailFolder mailFolder) throws OXException {
        final String fullname = mailFolder.getFullname();
        for (int i = 0; i < 7; i++) {
            final String stdFullname = getStandardFolder(i);
            if (null != stdFullname && stdFullname.equals(fullname)) {
                mailFolder.setDefaultFolder(true);
                mailFolder.setDefaultFolderType(TYPES[i]);
                return;
            }
        }
        mailFolder.setDefaultFolder(false);
        mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
    }

    private <V> V performSynchronized(final Callable<V> task, final Session session) throws Exception {
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null == lock) {
            lock = Session.EMPTY_LOCK;
        }
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isInfoSupported() throws OXException {
        return (delegatee instanceof IMailFolderStorageInfoSupport) && ((IMailFolderStorageInfoSupport) delegatee).isInfoSupported();
    }

    @Override
    public MailFolderInfo getFolderInfo(final String fullName) throws OXException {
        if (delegatee instanceof IMailFolderStorageInfoSupport) {
            final IMailFolderStorageInfoSupport infoSupport = ((IMailFolderStorageInfoSupport) delegatee);
            if (infoSupport.isInfoSupported()) {
                return infoSupport.getFolderInfo(fullName);
            }
        }
        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public List<MailFolderInfo> getAllFolderInfos(final boolean subscribedOnly) throws OXException {
        return getFolderInfos(null, subscribedOnly);
    }

    @Override
    public List<MailFolderInfo> getFolderInfos(final String optParentFullName, final boolean subscribedOnly) throws OXException {
        if (delegatee instanceof IMailFolderStorageInfoSupport) {
            final IMailFolderStorageInfoSupport infoSupport = ((IMailFolderStorageInfoSupport) delegatee);
            if (infoSupport.isInfoSupported()) {
                return infoSupport.getFolderInfos(optParentFullName, subscribedOnly);
            }
        }
        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public String getDefaultFolderPrefix() throws OXException {
        return "";
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        final MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        if (!isDefaultFoldersChecked(mailSessionCache)) {
            final Callable<Void> task = new Callable<Void>() {

                @Override
                public Void call() throws OXException {
                    if (isDefaultFoldersChecked(mailSessionCache)) {
                        return null;
                    }
                    /*
                     * Load mail account
                     */
                    final boolean isSpamOptionEnabled;
                    final MailAccount mailAccount;
                    try {
                        mailAccount =
                            POP3ServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true).getMailAccount(
                                accountId,
                                session.getUserId(),
                                session.getContextId());
                        final UserSettingMail usm =
                            UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), getContext());
                        isSpamOptionEnabled = usm.isSpamOptionEnabled();
                    } catch (final OXException e) {
                        throw e;
                    }
                    /*
                     * Get default folder names
                     */
                    final DefaultFolderNamesProvider defaultFolderNamesProvider =
                        new DefaultFolderNamesProvider(accountId, session.getUserId(), session.getContextId());
                    final String[] defaultFolderFullnames =
                        defaultFolderNamesProvider.getDefaultFolderFullnames(mailAccount, isSpamOptionEnabled);
                    final String[] defaultFolderNames = defaultFolderNamesProvider.getDefaultFolderNames(mailAccount, isSpamOptionEnabled);
                    final SpamHandler spamHandler;
                    {
                        spamHandler =
                            isSpamOptionEnabled ? SpamHandlerRegistry.getSpamHandlerBySession(session, accountId) : NoSpamHandler.getInstance();
                    }
                    // INBOX
                    setDefaultMailFolder(StorageUtility.INDEX_INBOX, checkDefaultFolder(getRealFullname("INBOX"), storage.getSeparator()));
                    // Other
                    for (int i = 0; i < defaultFolderNames.length; i++) {
                        final String realFullname;
                        if (null == defaultFolderFullnames[i]) {
                            realFullname = getRealFullname(defaultFolderNames[i]);
                        } else {
                            realFullname = getRealFullname(defaultFolderFullnames[i]);
                        }

                        if (StorageUtility.INDEX_CONFIRMED_HAM == i) {
                            if (spamHandler.isCreateConfirmedHam(session)) {
                                setDefaultMailFolder(i, checkDefaultFolder(realFullname, storage.getSeparator()));
                            } else {
                                LOG.debug("Skipping check for {} due to SpamHandler.isCreateConfirmedHam()=false", defaultFolderNames[i]);
                            }
                        } else if (StorageUtility.INDEX_CONFIRMED_SPAM == i) {
                            if (spamHandler.isCreateConfirmedSpam(session)) {
                                setDefaultMailFolder(i, checkDefaultFolder(realFullname, storage.getSeparator()));
                            } else {
                                LOG.debug("Skipping check for {} due to SpamHandler.isCreateConfirmedSpam()=false", defaultFolderNames[i]);
                            }
                        } else {
                            setDefaultMailFolder(i, checkDefaultFolder(realFullname, storage.getSeparator()));
                        }
                    }
                    setDefaultFoldersChecked(true, mailSessionCache);
                    return null;
                }
            };
            try {
                performSynchronized(task, session);
            } catch (final OXException e) {
                throw e;
            } catch (final Exception e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    String checkDefaultFolder(final String realFullname, final char separator) throws OXException {
        /*
         * Try to create folder
         */
        final MailFolderDescription description = new MailFolderDescription();
        {
            final int pos = realFullname.lastIndexOf(separator);
            description.setName(pos == -1 ? realFullname : realFullname.substring(pos + 1));
            description.setParentFullname(pos == -1 ? "" : realFullname.substring(0, pos));
        }
        description.setSeparator(separator);
        description.setExists(false);
        description.setSubscribed(false);
        {
            final MailPermission mp = new DefaultMailPermission();
            mp.setEntity(session.getUserId());
            description.addPermission(mp);
        }
        String fn;
        try {
            fn = delegatee.createFolder(description);
        } catch (final OXException e) {
            /*-
             * Expect creation failed because already existent
             *
             * Ensure folder is unsubscribed in primary account
             */
            unsubscribe(realFullname);
            fn = realFullname;
        }
        /*
         * Return
         */
        return stripPathFromFullname(path, fn);
    }

    boolean isDefaultFoldersChecked(final MailSessionCache mailSessionCache) {
        final Boolean b = mailSessionCache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderChecked());
        return (b != null) && b.booleanValue();
    }

    void setDefaultFoldersChecked(final boolean checked, final MailSessionCache mailSessionCache) {
        mailSessionCache.putParameter(accountId, MailSessionParameterNames.getParamDefaultFolderChecked(), Boolean.valueOf(checked));
    }

    void setDefaultMailFolder(final int index, final String fullname) {
        final MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        final String key = MailSessionParameterNames.getParamDefaultFolderArray();
        String[] arr = mailSessionCache.getParameter(accountId, key);
        if (null == arr) {
            arr = new String[7];
            mailSessionCache.putParameter(accountId, key, arr);
        }
        arr[index] = fullname;
    }

    private static final MailField[] FIELDS_ID = { MailField.ID };

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws OXException {
        if (!MailFolder.DEFAULT_FOLDER_ID.equals(fullname) && !isDefaultFolder(fullname)) {
            throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullname);
        }
        // Get affected mail IDs through loading all messages in folder
        final String[] mailIDs;
        {
            final MailMessage[] mails =
                getMessageStorage().getAllMessages(fullname, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, FIELDS_ID);
            mailIDs = new String[mails.length];
            for (int i = 0; i < mails.length; i++) {
                mailIDs[i] = mails[i].getMailId();
            }
        }
        if (hardDelete || performHardDelete(fullname)) {
            // Delegate delete to message storage which already keeps track of proper mappings
            getMessageStorage().deleteMessages(fullname, mailIDs, true);
            // Check for trash folder
            if (getTrashFolder().equals(fullname)) {
                final MailFolder[] trashSubfolders = getSubfolders(getTrashFolder(), true);
                for (final MailFolder trashSubfolder : trashSubfolders) {
                    deleteFolder(trashSubfolder.getFullname(), true);
                }
            }
        } else {
            // Clear folder by moving its mails to trash
            final String trashFullname = getTrashFolder();
            // Delegate move to message storage which already keeps track of proper mappings
            getMessageStorage().moveMessages(fullname, trashFullname, mailIDs, true);
        }
        // Clean from storage
        delegatee.clearFolder(getRealFullname(fullname), true);
    }

    @Override
    public void clearFolder(final String fullname) throws OXException {
        clearFolder(fullname, false);
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        toCreate.setParentFullname(getRealFullname(toCreate.getParentFullname()));
        toCreate.setSubscribed(false);
        final String realFullname = delegatee.createFolder(toCreate);
        unsubscribe(realFullname);
        return stripPathFromFullname(path, realFullname);
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws OXException {
        // TODO: Shall we deny mail folder deletion in POP3 accounts?
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname) || isDefaultFolder(fullname)) {
            throw POP3ExceptionCode.NO_DEFAULT_FOLDER_DELETE.create(fullname);
        }
        final String realFullname = getRealFullname(fullname);
        if (hardDelete || performHardDelete(fullname)) {
            // Hard-delete
            hardDeleteFolder(delegatee.getFolder(realFullname));
            return fullname;
        }
        // Load trash's subfolders to compose unique name
        final String realTrashFullname = getRealFullname(getTrashFolder());
        final MailFolder realMailFolder = delegatee.getFolder(realFullname);
        String newName = realMailFolder.getName();
        {
            final StringBuilder tmp = new StringBuilder(32);
            boolean exists = false;
            int count = 1;
            do {
                tmp.setLength(0);
                exists = delegatee.exists(tmp.append(realTrashFullname).append(storage.getSeparator()).append(newName).toString());
                if (exists) {
                    tmp.setLength(0);
                    newName = tmp.append(newName).append('_').append(++count).toString();
                }
            } while (exists);
        }
        // Soft-delete
        moveFolder0(realMailFolder, realTrashFullname, newName);
        return fullname;
    }

    private boolean performHardDelete(final String fullname) throws OXException {
        final String trashFullname = getTrashFolder();
        if (fullname.startsWith(trashFullname)) {
            // A subfolder of trash folder
            return true;
        }
        return !getFolder(trashFullname).isHoldsFolders();
    }

    private void hardDeleteFolder(final MailFolder realMailFolder) throws OXException {
        final String realFullname = realMailFolder.getFullname();
        if (realMailFolder.hasSubfolders()) {
            final MailFolder[] subfolders = delegatee.getSubfolders(realFullname, true);
            for (final MailFolder subfolder : subfolders) {
                hardDeleteFolder(subfolder);
            }
        }
        // Get virtual fullname
        final String fullname = stripPathFromFullname(path, realFullname);
        // Load mail IDs
        final String[] mailIDs;
        {
            final MailMessage[] mails =
                getMessageStorage().getAllMessages(fullname, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, FIELDS_ID);
            mailIDs = new String[mails.length];
            for (int i = 0; i < mails.length; i++) {
                mailIDs[i] = mails[i].getMailId();
            }
        }
        // Delegate message deletion to message storage which already keeps track of UIDLs
        getMessageStorage().deleteMessages(fullname, mailIDs, true);
        // Finally delete folder
        delegatee.deleteFolder(realFullname, true);
    }

    private void moveFolder0(final MailFolder realMailFolder, final String newParent, final String newName) throws OXException {
        // Create backup folder
        final String newSubfolderParent;
        {
            final MailFolderDescription toCreate = new MailFolderDescription();
            toCreate.setName(newName);
            toCreate.setParentFullname(newParent);
            toCreate.setSeparator(storage.getSeparator());
            toCreate.setExists(false);
            toCreate.setSubscribed(true);
            {
                final MailPermission mp = new DefaultMailPermission();
                mp.setEntity(session.getUserId());
                toCreate.addPermission(mp);
            }
            newSubfolderParent = delegatee.createFolder(toCreate);
        }
        final String realFullname = realMailFolder.getFullname();
        // Get virtual fullname
        final String fullname = stripPathFromFullname(path, realFullname);
        // Get affected mail IDs
        final String[] mailIDs;
        {
            final MailMessage[] mails =
                getMessageStorage().getAllMessages(fullname, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, FIELDS_ID);
            mailIDs = new String[mails.length];
            for (int i = 0; i < mails.length; i++) {
                mailIDs[i] = mails[i].getMailId();
            }
        }
        // Move messages to new folder through message storage which already keeps track of UIDLs
        getMessageStorage().moveMessages(fullname, stripPathFromFullname(path, newSubfolderParent), mailIDs, true);
        // Iterate possible subfolders
        final MailFolder[] subfolders = delegatee.getSubfolders(realFullname, true);
        for (final MailFolder subfolder : subfolders) {
            moveFolder0(subfolder, newSubfolderParent, subfolder.getName());
        }
        // Finally delete folder
        delegatee.deleteFolder(realFullname, true);
    }

    @Override
    public String deleteFolder(final String fullname) throws OXException {
        return deleteFolder(fullname, false);
    }

    @Override
    public boolean exists(final String fullname) throws OXException {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname) || isDefaultFolder(fullname)) {
            return true;
        }
        return delegatee.exists(getRealFullname(fullname));
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return getStandardFolder(StorageUtility.INDEX_CONFIRMED_HAM);
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return getStandardFolder(StorageUtility.INDEX_CONFIRMED_SPAM);
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return getStandardFolder(StorageUtility.INDEX_DRAFTS);
    }

    @Override
    public MailFolder getFolder(final String fullname) throws OXException {
        final String realFullname = getRealFullname(fullname);
        final MailFolder mailFolder = delegatee.getFolder(realFullname);
        unsubscribe(mailFolder.getFullname());
        prepareMailFolder(mailFolder);
        return mailFolder;
    }

    @Override
    public Quota getMessageQuota(final String fullname) throws OXException {
        final String realFullname = getRealFullname(fullname);
        return delegatee.getMessageQuota(realFullname);
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws OXException {
        final String realFullname = getRealFullname(fullname);
        final MailFolder[] folders = delegatee.getPath2DefaultFolder(realFullname);
        final List<MailFolder> tmp = new ArrayList<MailFolder>(folders.length);
        boolean stop = false;
        for (int i = 0; i < folders.length && !stop; i++) {
            final MailFolder mailFolder = folders[i];
            prepareMailFolder(mailFolder);
            stop = (mailFolder.getFullname().equals(MailFolder.DEFAULT_FOLDER_ID));
        }
        return tmp.toArray(new MailFolder[tmp.size()]);
    }

    @Override
    public Quota[] getQuotas(final String fullname, final Type[] types) throws OXException {
        final String realFullname = getRealFullname(fullname);
        return delegatee.getQuotas(realFullname, types);
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return getFolder(MailFolder.DEFAULT_FOLDER_ID);
    }

    @Override
    public String getSentFolder() throws OXException {
        return getStandardFolder(StorageUtility.INDEX_SENT);
    }

    @Override
    public String getSpamFolder() throws OXException {
        return getStandardFolder(StorageUtility.INDEX_SPAM);
    }

    @Override
    public Quota getStorageQuota(final String fullname) throws OXException {
        final String realFullname = getRealFullname(fullname);
        return delegatee.getStorageQuota(realFullname);
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws OXException {
        final String parentRealFullname = getRealFullname(parentFullname);
        final MailFolder[] subfolders = delegatee.getSubfolders(parentRealFullname, true);
        for (final MailFolder mailFolder : subfolders) {
            unsubscribe(mailFolder.getFullname());
            prepareMailFolder(mailFolder);
        }
        return subfolders;
    }

    @Override
    public String getTrashFolder() throws OXException {
        return getStandardFolder(StorageUtility.INDEX_TRASH);
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws OXException {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(newFullname)) {
            throw POP3ExceptionCode.MOVE_ILLEGAL.create();
        } else if (isDefaultFolder(fullname)) {
            throw POP3ExceptionCode.MOVE_ILLEGAL.create();
        } else if (isDefaultFolder(newFullname)) {
            throw POP3ExceptionCode.MOVE_ILLEGAL.create();
        }
        final String[] parentAndName = getParentAndName(newFullname, storage.getSeparator());
        final String parentFullname = parentAndName[0];
        if (null == parentFullname) {
            throw POP3ExceptionCode.MOVE_ILLEGAL.create();
        }
        if (delegatee.exists(getRealFullname(newFullname))) {
            throw POP3ExceptionCode.MOVE_ILLEGAL.create();
        }
        // Move folder
        moveFolder0(delegatee.getFolder(getRealFullname(fullname)), getRealFullname(parentFullname), parentAndName[1]);
        return newFullname;
    }

    @Override
    public void releaseResources() throws OXException {
        delegatee.releaseResources();
    }

    @Override
    public String renameFolder(final String fullname, final String newName) throws OXException {
        final MailFolder realMailFolder = delegatee.getFolder(getRealFullname(fullname));
        final String newFullname;
        {
            final String parent = stripPathFromFullname(path, realMailFolder.getParentFullname());
            if (MailFolder.DEFAULT_FOLDER_ID.equals(parent)) {
                newFullname = newName;
            } else {
                newFullname = new StringBuilder(parent).append(storage.getSeparator()).append(newName).toString();
            }
        }
        return moveFolder(fullname, newFullname);
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws OXException {
        final String realFullname = getRealFullname(fullname);
        toUpdate.setFullname(realFullname);
        if (toUpdate.containsParentFullname()) {
            final String realParentFullname = getRealFullname(toUpdate.getParentFullname());
            toUpdate.setParentFullname(realParentFullname);
        }
        if (toUpdate.containsSubscribed()) {
            /*
             * POP3 does not support subscription; unsubscribe in backing primary account
             */
            toUpdate.setSubscribed(false);
        }
        if (!toUpdate.containsExists()) {
            toUpdate.setExists(true); // Obviously
        }
        return stripPathFromFullname(path, delegatee.updateFolder(realFullname, toUpdate));
    }

    private void prepareMailFolder(final MailFolder mailFolder) throws OXException {
        mailFolder.setFullname(stripPathFromFullname(path, mailFolder.getFullname()));
        mailFolder.setParentFullname(stripPathFromFullname(path, mailFolder.getParentFullname()));
        mailFolder.setRootFolder(false);
        mailFolder.setDefaultFolder(false);
        mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
        final MailPermission mp = getPOP3MailPermission();
        if (MailFolder.DEFAULT_FOLDER_ID.equals(mailFolder.getFullname())) {
            mp.setAllPermission(
                OCLPermission.CREATE_SUB_FOLDERS,
                OCLPermission.NO_PERMISSIONS,
                OCLPermission.NO_PERMISSIONS,
                OCLPermission.NO_PERMISSIONS);
            mailFolder.setRootFolder(true);
        } else {
            setDefaultFolderInfo(mailFolder);
        }
        mailFolder.removePermissions();
        mailFolder.removeOwnPermission();
        mailFolder.addPermission(mp);
        mailFolder.setOwnPermission(mp);
        mailFolder.setShared(false);
        // POP3 does not support subscription
        mailFolder.setSubscribed(true);
        mailFolder.setSubscribedSubfolders(mailFolder.hasSubfolders());
    }

    String getRealFullname(final String fullname) throws OXException {
        return prependPath2Fullname(path, storage.getSeparator(), fullname);
    }

    private String[] getParentAndName(final String fullname, final char separator) {
        final int pos = fullname.lastIndexOf(separator);
        if (-1 == pos) {
            return new String[] { MailFolder.DEFAULT_FOLDER_ID, fullname };
        }
        return new String[] { fullname.substring(0, pos), fullname.substring(pos + 1) };
    }

    private void unsubscribe(final String realFullName) throws OXException {
        final MailFolderDescription description = new MailFolderDescription();
        description.setFullname(realFullName);
        description.setSubscribed(false);
        delegatee.updateFolder(realFullName, description);
    }

}
