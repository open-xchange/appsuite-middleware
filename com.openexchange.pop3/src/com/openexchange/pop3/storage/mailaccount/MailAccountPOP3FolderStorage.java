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

package com.openexchange.pop3.storage.mailaccount;

import static com.openexchange.pop3.storage.mailaccount.util.Utility.prependPath2Fullname;
import static com.openexchange.pop3.storage.mailaccount.util.Utility.stripPathFromFullname;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.Quota;
import com.openexchange.mail.Quota.Type;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3Exception;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.POP3StorageTrashContainer;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.NoSpamHandler;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;

/**
 * {@link MailAccountPOP3FolderStorage} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountPOP3FolderStorage implements IMailFolderStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailAccountPOP3FolderStorage.class);

    private final IMailFolderStorage delegatee;

    private final MailAccountPOP3Storage storage;

    private MailAccountPOP3MessageStorage messageStorage;

    private POP3StorageTrashContainer trashContainer;

    private POP3StorageUIDLMap uidlMap;

    private Context ctx;

    private final Session session;

    private final int accountId;

    private final String path;

    MailAccountPOP3FolderStorage(final IMailFolderStorage delegatee, final MailAccountPOP3Storage storage, final POP3Access pop3Access) {
        super();
        this.storage = storage;
        this.session = pop3Access.getSession();
        this.delegatee = delegatee;
        this.accountId = pop3Access.getAccountId();
        this.path = storage.getPath();
    }

    private MailAccountPOP3MessageStorage getMessageStorage() throws MailException {
        if (null == messageStorage) {
            messageStorage = (MailAccountPOP3MessageStorage) storage.getMessageStorage();
        }
        return messageStorage;
    }

    private POP3StorageTrashContainer getTrashContainer() throws MailException {
        if (null == trashContainer) {
            trashContainer = storage.getTrashContainer();
        }
        return trashContainer;
    }

    private POP3StorageUIDLMap getUIDLMap() throws MailException {
        if (null == uidlMap) {
            uidlMap = storage.getUIDLMap();
        }
        return uidlMap;
    }

    private MailPermission getPOP3MailPermission() {
        final MailPermission mp = new DefaultMailPermission();
        mp.setFolderPermission(MailPermission.CREATE_OBJECTS_IN_FOLDER);
        mp.setEntity(session.getUserId());
        return mp;
    }

    private Context getContext() throws MailException {
        if (null == ctx) {
            try {
                return POP3ServiceRegistry.getServiceRegistry().getService(ContextService.class, true).getContext(session.getContextId());
            } catch (final ServiceException e) {
                throw new MailException(e);
            } catch (final ContextException e) {
                throw new MailException(e);
            }
        }
        return ctx;
    }

    private String getStandardFolder(final int index) throws MailException {
        if (!isDefaultFoldersChecked()) {
            checkDefaultFolders();
        }
        final String retval = getDefaultMailFolder(index);
        if (retval != null) {
            return retval;
        }
        setDefaultFoldersChecked(false);
        checkDefaultFolders();
        return getDefaultMailFolder(index);
    }

    private String getDefaultMailFolder(final int index) {
        final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.getParamDefaultFolderArray(accountId));
        return arr == null ? null : arr[index];
    }

    private boolean isDefaultFolder(final String fullname) throws MailException {
        for (int i = 0; i < 7; i++) {
            if (getStandardFolder(i).equals(fullname)) {
                return true;
            }
        }
        return false;
    }

    public void checkDefaultFolders() throws MailException {
        if (!isDefaultFoldersChecked()) {
            synchronized (session) {
                if (isDefaultFoldersChecked()) {
                    return;
                }
                /*
                 * Load mail account
                 */
                final boolean isSpamOptionEnabled;
                final MailAccount mailAccount;
                try {
                    mailAccount = POP3ServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true).getMailAccount(
                        accountId,
                        session.getUserId(),
                        session.getContextId());
                    final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), getContext());
                    isSpamOptionEnabled = usm.isSpamOptionEnabled();
                } catch (final ServiceException e) {
                    throw new MailException(e);
                } catch (final MailAccountException e) {
                    throw new MailException(e);
                }
                /*
                 * Get default folder names
                 */
                final DefaultFolderNamesProvider defaultFolderNamesProvider = new DefaultFolderNamesProvider(
                    accountId,
                    session.getUserId(),
                    session.getContextId());
                final String[] defaultFolderFullnames = defaultFolderNamesProvider.getDefaultFolderFullnames(
                    mailAccount,
                    isSpamOptionEnabled);
                final String[] defaultFolderNames = defaultFolderNamesProvider.getDefaultFolderNames(mailAccount, isSpamOptionEnabled);
                final SpamHandler spamHandler;
                {
                    spamHandler = isSpamOptionEnabled ? SpamHandlerRegistry.getSpamHandlerBySession(session, accountId) : NoSpamHandler.getInstance();
                }
                // INBOX
                setDefaultMailFolder(StorageUtility.INDEX_INBOX, checkDefaultFolder(getRealFullname("INBOX"), storage.getSeparator(), 1));
                // Other
                for (int i = 0; i < defaultFolderNames.length; i++) {
                    final String realFullname;
                    if (null == defaultFolderFullnames[i]) {
                        realFullname = getRealFullname(defaultFolderNames[i]);
                    } else {
                        realFullname = getRealFullname(defaultFolderFullnames[i]);
                    }

                    if (StorageUtility.INDEX_CONFIRMED_HAM == i) {
                        if (spamHandler.isCreateConfirmedHam()) {
                            setDefaultMailFolder(i, checkDefaultFolder(
                                realFullname,
                                storage.getSeparator(),
                                spamHandler.isUnsubscribeSpamFolders() ? 0 : -1));
                        } else if (LOG.isDebugEnabled()) {
                            LOG.debug("Skipping check for " + defaultFolderNames[i] + " due to SpamHandler.isCreateConfirmedHam()=false");
                        }
                    } else if (StorageUtility.INDEX_CONFIRMED_SPAM == i) {
                        if (spamHandler.isCreateConfirmedSpam()) {
                            setDefaultMailFolder(i, checkDefaultFolder(
                                realFullname,
                                storage.getSeparator(),
                                spamHandler.isUnsubscribeSpamFolders() ? 0 : -1));
                        } else if (LOG.isDebugEnabled()) {
                            LOG.debug("Skipping check for " + defaultFolderNames[i] + " due to SpamHandler.isCreateConfirmedSpam()=false");
                        }
                    } else {
                        setDefaultMailFolder(i, checkDefaultFolder(realFullname, storage.getSeparator(), 1));
                    }
                }
                setDefaultFoldersChecked(true);
            }
        }
    }

    private String checkDefaultFolder(final String realFullname, final char separator, final int subscribe) throws MailException {
        /*
         * Check default folder
         */
        final String fn;
        if (delegatee.exists(realFullname)) {
            fn = realFullname;
        } else {
            final MailFolderDescription description = new MailFolderDescription();
            {
                final int pos = realFullname.lastIndexOf(separator);
                description.setName(pos == -1 ? realFullname : realFullname.substring(pos + 1));
                description.setParentFullname(pos == -1 ? "" : realFullname.substring(0, pos));
            }
            description.setSeparator(separator);
            description.setExists(false);
            description.setSubscribed(subscribe > 0);
            {
                final MailPermission mp = new DefaultMailPermission();
                mp.setEntity(session.getUserId());
                description.addPermission(mp);
            }
            fn = delegatee.createFolder(description);
        }
        return stripPathFromFullname(path, fn);
    }

    private boolean isDefaultFoldersChecked() {
        final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.getParamDefaultFolderChecked(accountId));
        return (b != null) && b.booleanValue();
    }

    private void setDefaultFoldersChecked(final boolean checked) {
        session.setParameter(MailSessionParameterNames.getParamDefaultFolderChecked(accountId), Boolean.valueOf(checked));
    }

    private void setDefaultMailFolder(final int index, final String fullname) {
        final String key = MailSessionParameterNames.getParamDefaultFolderArray(accountId);
        String[] arr = (String[]) session.getParameter(key);
        if (null == arr) {
            arr = new String[7];
            session.setParameter(key, arr);
        }
        arr[index] = fullname;
    }

    private static final MailField[] FIELDS_ID = { MailField.ID };

    public void clearFolder(final String fullname, final boolean hardDelete) throws MailException {
        if (!MailFolder.DEFAULT_FOLDER_ID.equals(fullname) && !isDefaultFolder(fullname)) {
            throw new MailException(MailException.Code.FOLDER_NOT_FOUND, fullname);
        }
        // Get affected mail IDs through loading all messages in folder
        final String[] mailIDs;
        {
            final MailMessage[] mails = getMessageStorage().getAllMessages(
                fullname,
                null,
                MailSortField.RECEIVED_DATE,
                OrderDirection.ASC,
                FIELDS_ID);
            mailIDs = new String[mails.length];
            for (int i = 0; i < mails.length; i++) {
                mailIDs[i] = mails[i].getMailId();
            }
        }
        if (hardDelete) {
            // Delegate delete to message storage which already keeps track of proper mappings
            getMessageStorage().deleteMessages(fullname, mailIDs, true);
        } else {
            // Clear folder by moving its mails to trash
            final String trashFullname = getTrashFolder();
            // Delegate move to message storage which already keeps track of proper mappings
            getMessageStorage().moveMessages(fullname, trashFullname, mailIDs, true);
        }
        // Clean from storage
        delegatee.clearFolder(getRealFullname(fullname), true);
    }

    public void clearFolder(final String fullname) throws MailException {
        clearFolder(fullname, false);
    }

    public String createFolder(final MailFolderDescription toCreate) throws MailException {
        final String realFullname = delegatee.createFolder(toCreate);
        return stripPathFromFullname(path, realFullname);
    }

    public String deleteFolder(final String fullname, final boolean hardDelete) throws MailException {
        // TODO: Shall we deny mail folder deletion in POP3 accounts?
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname) || isDefaultFolder(fullname)) {
            throw new POP3Exception(POP3Exception.Code.NO_DEFAULT_FOLDER_DELETE, fullname);
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
                    newName = tmp.append(newName).append(" (").append(++count).append(')').toString();
                }
            } while (exists);
        }
        // Soft-delete
        moveFolder0(realMailFolder, realTrashFullname, newName);
        return fullname;
    }

    private boolean performHardDelete(final String fullname) throws MailException {
        final String trashFullname = getTrashFolder();
        if (fullname.startsWith(trashFullname)) {
            // A subfolder of trash folder
            return true;
        }
        return !getFolder(trashFullname).isHoldsFolders();
    }

    private void hardDeleteFolder(final MailFolder realMailFolder) throws MailException {
        if (realMailFolder.hasSubfolders()) {
            final MailFolder[] subfolders = delegatee.getSubfolders(realMailFolder.getFullname(), true);
            for (final MailFolder subfolder : subfolders) {
                hardDeleteFolder(subfolder);
            }
        }
        // Get virtual fullname
        final String fullname = stripPathFromFullname(path, realMailFolder.getFullname());
        // Load mail IDs
        final String[] mailIDs;
        {
            final MailMessage[] mails = getMessageStorage().getAllMessages(
                fullname,
                null,
                MailSortField.RECEIVED_DATE,
                OrderDirection.ASC,
                FIELDS_ID);
            mailIDs = new String[mails.length];
            for (int i = 0; i < mails.length; i++) {
                mailIDs[i] = mails[i].getMailId();
            }
        }
        // Delegate message deletion to message storage which already keeps track of UIDLs
        getMessageStorage().deleteMessages(fullname, mailIDs, true);
    }

    private void moveFolder0(final MailFolder realMailFolder, final String newParent, final String newName) throws MailException {
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
            final MailMessage[] mails = getMessageStorage().getAllMessages(
                fullname,
                null,
                MailSortField.RECEIVED_DATE,
                OrderDirection.ASC,
                FIELDS_ID);
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

    public String deleteFolder(final String fullname) throws MailException {
        return deleteFolder(fullname, false);
    }

    public boolean exists(final String fullname) throws MailException {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname) || isDefaultFolder(fullname)) {
            return true;
        }
        return delegatee.exists(getRealFullname(fullname));
    }

    public String getConfirmedHamFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_CONFIRMED_HAM);
    }

    public String getConfirmedSpamFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_CONFIRMED_SPAM);
    }

    public String getDraftsFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_DRAFTS);
    }

    public MailFolder getFolder(final String fullname) throws MailException {
        final String realFullname = getRealFullname(fullname);
        final MailFolder mailFolder = delegatee.getFolder(realFullname);
        prepareMailFolder(mailFolder);
        return mailFolder;
    }

    public Quota getMessageQuota(final String fullname) throws MailException {
        final String realFullname = getRealFullname(fullname);
        return delegatee.getMessageQuota(realFullname);
    }

    public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
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

    public Quota[] getQuotas(final String fullname, final Type[] types) throws MailException {
        final String realFullname = getRealFullname(fullname);
        return delegatee.getQuotas(realFullname, types);
    }

    public MailFolder getRootFolder() throws MailException {
        return getFolder(MailFolder.DEFAULT_FOLDER_ID);
    }

    public String getSentFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_SENT);
    }

    public String getSpamFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_SPAM);
    }

    public Quota getStorageQuota(final String fullname) throws MailException {
        final String realFullname = getRealFullname(fullname);
        return delegatee.getStorageQuota(realFullname);
    }

    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException {
        final String parentRealFullname = getRealFullname(parentFullname);
        final MailFolder[] subfolders = delegatee.getSubfolders(parentRealFullname, all);
        for (int i = 0; i < subfolders.length; i++) {
            prepareMailFolder(subfolders[i]);
        }
        return subfolders;
    }

    public String getTrashFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_TRASH);
    }

    public String moveFolder(final String fullname, final String newFullname) throws MailException {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(newFullname)) {
            throw new POP3Exception(POP3Exception.Code.MOVE_ILLEGAL);
        } else if (isDefaultFolder(fullname)) {
            throw new POP3Exception(POP3Exception.Code.MOVE_ILLEGAL);
        } else if (isDefaultFolder(newFullname)) {
            throw new POP3Exception(POP3Exception.Code.MOVE_ILLEGAL);
        }
        final String[] parentAndName = getParentAndName(newFullname, storage.getSeparator());
        final String parentFullname = parentAndName[0];
        if (null == parentFullname) {
            throw new POP3Exception(POP3Exception.Code.MOVE_ILLEGAL);
        }
        if (delegatee.exists(getRealFullname(newFullname))) {
            throw new POP3Exception(POP3Exception.Code.MOVE_ILLEGAL);
        }
        // Move folder
        moveFolder0(delegatee.getFolder(getRealFullname(fullname)), getRealFullname(parentFullname), parentAndName[1]);
        return newFullname;
    }

    public void releaseResources() throws MailException {
        delegatee.releaseResources();
    }

    public String renameFolder(final String fullname, final String newName) throws MailException {
        final MailFolder realMailFolder = delegatee.getFolder(getRealFullname(fullname));
        final String newFullname = new StringBuilder(stripPathFromFullname(path, realMailFolder.getParentFullname())).append(
            storage.getSeparator()).append(realMailFolder.getName()).toString();
        return moveFolder(fullname, newFullname);
    }

    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws MailException {
        return stripPathFromFullname(path, delegatee.updateFolder(getRealFullname(fullname), toUpdate));
    }

    private void prepareMailFolder(final MailFolder mailFolder) throws MailException {
        mailFolder.setFullname(stripPathFromFullname(path, mailFolder.getFullname()));
        mailFolder.setRootFolder(false);
        mailFolder.setDefaultFolder(false);
        final MailPermission mp = getPOP3MailPermission();
        if (MailFolder.DEFAULT_FOLDER_ID.equals(mailFolder.getFullname())) {
            mp.setAllPermission(
                MailPermission.NO_PERMISSIONS,
                MailPermission.NO_PERMISSIONS,
                MailPermission.NO_PERMISSIONS,
                MailPermission.NO_PERMISSIONS);
            mailFolder.setRootFolder(true);
        } else if (isDefaultFolder(mailFolder.getFullname())) {
            mailFolder.setDefaultFolder(true);
        }
        mailFolder.removePermissions();
        mailFolder.removeOwnPermission();
        mailFolder.addPermission(mp);
        mailFolder.setOwnPermission(mp);
    }

    private String getRealFullname(final String fullname) throws MailException {
        return prependPath2Fullname(path, storage.getSeparator(), fullname);
    }

    private String[] getParentAndName(final String fullname, final char separator) {
        final int pos = fullname.lastIndexOf(separator);
        if (-1 == pos) {
            return new String[] { null, MailFolder.DEFAULT_FOLDER_ID };
        }
        return new String[] { fullname.substring(0, pos), fullname.substring(pos + 1) };
    }
}
