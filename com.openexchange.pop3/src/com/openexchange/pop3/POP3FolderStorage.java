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

package com.openexchange.pop3;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.converters.POP3FolderConverter;
import com.openexchange.session.Session;
import com.sun.mail.pop3.POP3Store;

/**
 * {@link POP3FolderStorage} - The IMAP folder storage implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3FolderStorage extends MailFolderStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3FolderStorage.class);

    private static final String STR_INBOX = "INBOX";

    private static final String STR_MSEC = "msec";

    private final POP3Store pop3Store;

    private final POP3Access imapAccess;

    private final int accountId;

    private final Session session;

    private final Context ctx;

    private final POP3Config imapConfig;

    private Character separator;

    /**
     * Initializes a new {@link POP3FolderStorage}
     * 
     * @param imapStore The IMAP store
     * @param imapAccess The IMAP access
     * @param session The session providing needed user data
     * @throws POP3Exception If context loading fails
     */
    public POP3FolderStorage(final POP3Store imapStore, final POP3Access imapAccess, final Session session) throws POP3Exception {
        super();
        this.pop3Store = imapStore;
        this.imapAccess = imapAccess;
        this.accountId = imapAccess.getAccountId();
        this.session = session;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new POP3Exception(e);
        }
        imapConfig = imapAccess.getPOP3Config();
    }

    private char getSeparator() throws MessagingException {
        if (null == separator) {
            separator = Character.valueOf(pop3Store.getDefaultFolder().getSeparator());
        }
        return separator.charValue();
    }

    @Override
    public boolean exists(final String fullname) throws MailException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                return true;
            }
            return pop3Store.getFolder(fullname).exists();
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public MailFolder getFolder(final String fullname) throws MailException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                return POP3FolderConverter.convertFolder(pop3Store.getDefaultFolder(), session, ctx);
            }
            final Folder f = pop3Store.getFolder(fullname);
            if (f.exists()) {
                return POP3FolderConverter.convertFolder(f, session, ctx);
            }
            throw new POP3Exception(POP3Exception.Code.FOLDER_NOT_FOUND, fullname);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    private static final String PATTERN_ALL = "%";

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException {
        try {
            Folder parent;
            if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
                parent = pop3Store.getDefaultFolder();
                final boolean subscribed = (!MailProperties.getInstance().isIgnoreSubscription() && !all);
                /*
                 * Request subfolders the usual way
                 */
                final List<Folder> subfolders = new ArrayList<Folder>();
                {
                    final Folder[] childFolders;
                    final long start = System.currentTimeMillis();
                    if (subscribed) {
                        childFolders = parent.listSubscribed(PATTERN_ALL);
                        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    } else {
                        childFolders = parent.list(PATTERN_ALL);
                        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    }
                    subfolders.addAll(Arrays.asList(childFolders));
                    boolean containsInbox = false;
                    for (int i = 0; i < childFolders.length && !containsInbox; i++) {
                        containsInbox = STR_INBOX.equals(childFolders[i].getFullName());
                    }
                    if (!containsInbox) {
                        /*
                         * Add folder INBOX manually
                         */
                        subfolders.add(0, pop3Store.getFolder(STR_INBOX));
                    }
                }
                /*
                 * Output subfolders
                 */
                final List<MailFolder> list = new ArrayList<MailFolder>(subfolders.size());
                for (final Folder subfolder : subfolders) {
                    list.add(POP3FolderConverter.convertFolder(subfolder, session, ctx));
                }
                return list.toArray(new MailFolder[list.size()]);
            }
            parent = pop3Store.getFolder(parentFullname);
            if (parent.exists()) {
                return getSubfolderArray(all, parent);
            }
            return EMPTY_PATH;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    private MailFolder[] getSubfolderArray(final boolean all, final Folder parent) throws MessagingException, MailException {
        final Folder[] subfolders;
        if (MailProperties.getInstance().isIgnoreSubscription() || all) {
            subfolders = parent.list(PATTERN_ALL);
        } else {
            subfolders = parent.listSubscribed(PATTERN_ALL);
        }
        final List<MailFolder> list = new ArrayList<MailFolder>(subfolders.length);
        for (int i = 0; i < subfolders.length; i++) {
            final MailFolder mo = POP3FolderConverter.convertFolder(subfolders[i], session, ctx);
            if (mo.exists()) {
                list.add(mo);
            }
        }
        return list.toArray(new MailFolder[list.size()]);
    }

    @Override
    public MailFolder getRootFolder() throws MailException {
        try {
            return POP3FolderConverter.convertFolder(pop3Store.getDefaultFolder(), session, ctx);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    private boolean isDefaultFoldersChecked() {
        final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.getParamDefaultFolderChecked(accountId));
        return (b != null) && b.booleanValue();
    }

    private void setDefaultFoldersChecked(final boolean checked) {
        session.setParameter(MailSessionParameterNames.getParamDefaultFolderChecked(accountId), Boolean.valueOf(checked));
    }

    /**
     * Stores specified separator character in session parameters for future look-ups
     * 
     * @param separator The separator character
     */
    private void setSeparator(final char separator) {
        session.setParameter(MailSessionParameterNames.getParamSeparator(accountId), Character.valueOf(separator));
    }

    private void setDefaultMailFolder(final int index, final String fullname) {
        final String key = MailSessionParameterNames.getParamDefaultFolderArray(accountId);
        String[] arr = (String[]) session.getParameter(key);
        if (null == arr) {
            arr = new String[6];
            session.setParameter(key, arr);
        }
        arr[index] = fullname;
    }

    @Override
    public void checkDefaultFolders() throws MailException {
        // POP3 mailbox only contains ONE folder: The INBOX folder
    }

    private static final int FOLDER_TYPE = (Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws MailException {
        throw new POP3Exception(POP3Exception.Code.FOLDER_CREATION_FAILED);
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws MailException {
        throw new POP3Exception(POP3Exception.Code.MOVE_DENIED);
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws MailException {
        throw new POP3Exception(POP3Exception.Code.UPDATE_DENIED);
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws MailException {
        throw new POP3Exception(POP3Exception.Code.DELETE_DENIED);
    }

    private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.UPDATE_DENIED);
        }
        try {
            final Folder f = pop3Store.getFolder(fullname);
            if (!f.exists()) {
                throw new POP3Exception(POP3Exception.Code.FOLDER_NOT_FOUND, fullname);
            }
            imapAccess.getMessageStorage().notifyPOP3FolderModification(fullname);
            if (!isSelectable(f)) {
                throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, f.getFullName());
            }
            f.open(Folder.READ_WRITE);
            boolean closeFolder = true;
            try {
                final int msgCount = f.getMessageCount();
                if (msgCount == 0) {
                    /*
                     * Empty folder
                     */
                    return;
                }
                final StringBuilder debug;
                if (LOG.isDebugEnabled()) {
                    debug = new StringBuilder(128);
                } else {
                    debug = null;
                }
                final long startClear = System.currentTimeMillis();
                final Message[] msgs = f.getMessages();
                for (final Message message : msgs) {
                    message.setFlags(FLAGS_DELETED, true);
                }
                final long start = System.currentTimeMillis();
                f.close(true);
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                closeFolder = false;
                if (LOG.isDebugEnabled()) {
                    debug.setLength(0);
                    LOG.info(debug.append("Folder '").append(fullname).append("' cleared in ").append(
                        System.currentTimeMillis() - startClear).append(STR_MSEC));
                }
            } finally {
                if (closeFolder) {
                    f.close(false);
                }
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        } catch (final AbstractOXException e) {
            throw new POP3Exception(e);
        }
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
        try {
            if (fullname.equals(DEFAULT_FOLDER_ID)) {
                return EMPTY_PATH;
            }
            Folder f = pop3Store.getFolder(fullname);
            if (!f.exists()) {
                throw new POP3Exception(POP3Exception.Code.FOLDER_NOT_FOUND, fullname);
            }
            final List<MailFolder> list = new ArrayList<MailFolder>();
            final String defaultFolder = pop3Store.getDefaultFolder().getFullName();
            while (!f.getFullName().equals(defaultFolder)) {
                list.add(POP3FolderConverter.convertFolder(f, session, ctx));
                f = f.getParent();
            }
            return list.toArray(new MailFolder[list.size()]);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public String getConfirmedHamFolder() throws MailException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws MailException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws MailException {
        return null;
    }

    @Override
    public String getSentFolder() throws MailException {
        return null;
    }

    @Override
    public String getSpamFolder() throws MailException {
        return null;
    }

    @Override
    public String getTrashFolder() throws MailException {
        return null;
    }

    @Override
    public void releaseResources() throws POP3Exception {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws MailException {
        return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
    }

    /*-
     * ++++++++++++++++++ Helper methods ++++++++++++++++++
     */

    /**
     * Checks if specified folder is selectable; meaning to check if it is capable to hold messages.
     * 
     * @param folder The folder to check
     * @return <code>true</code> if specified folder is selectable; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     */
    private static boolean isSelectable(final Folder folder) throws MessagingException {
        return (folder.getType() & Folder.HOLDS_MESSAGES) == Folder.HOLDS_MESSAGES;
    }

}
