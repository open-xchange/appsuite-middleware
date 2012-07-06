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

package com.openexchange.pop3.storage.mailaccount;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.connect.POP3StoreConnector;
import com.openexchange.pop3.connect.POP3StoreConnector.POP3StoreResult;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.pop3.storage.POP3StoragePropertyNames;
import com.openexchange.pop3.storage.POP3StorageTrashContainer;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.pop3.storage.mailaccount.util.Utility;
import com.openexchange.session.Session;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Message;
import com.sun.mail.pop3.POP3Store;

/**
 * {@link MailAccountPOP3Storage} - The built-in mail account POP3 storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountPOP3Storage implements POP3Storage {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailAccountPOP3Storage.class));

    /*-
     * Member section
     */

    private final POP3StorageProperties properties;

    private String path;

    private final POP3Access pop3Access;

    private MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> defaultMailAccess;

    private final int pop3AccountId;

    private MailAccountPOP3MessageStorage messageStorage;

    private MailAccountPOP3FolderStorage folderStorage;

    private char separator;

    private final Collection<OXException> warnings;

    MailAccountPOP3Storage(final POP3Access pop3Access, final POP3StorageProperties properties) throws OXException {
        super();
        warnings = new ArrayList<OXException>(2);
        this.pop3Access = pop3Access;
        pop3AccountId = pop3Access.getAccountId();
        final Session session = pop3Access.getSession();
        // defaultMailAccess = MailAccess.getInstance(session);
        this.properties = properties;
        {
            String tmp = properties.getProperty(POP3StoragePropertyNames.PROPERTY_PATH);
            if (null == tmp) {
                final OXException e = POP3ExceptionCode.MISSING_PATH.create(Integer.valueOf(session.getUserId()),
                    Integer.valueOf(session.getContextId()));
                LOG.debug("Path is null. Error: " + e.getMessage(), e);
                // Try to compose path
                tmp = composeUniquePath(pop3Access.getAccountId(), session.getUserId(), session.getContextId());
                // Add to properties
                properties.addProperty(POP3StoragePropertyNames.PROPERTY_PATH, tmp);
            }
            path = tmp;
        }
        if (null == path) {
            throw POP3ExceptionCode.MISSING_PATH.create(Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }
        separator = 0;
    }

    private MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getDefaultMailAccess() throws OXException {
        if (null == defaultMailAccess) {
            defaultMailAccess = MailAccess.getInstance(pop3Access.getSession());
        }
        return defaultMailAccess;
    }

    private String composeUniquePath(final int pop3AccountId, final int user, final int cid) throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> defaultMailAccess = getDefaultMailAccess();
        defaultMailAccess.connect(false);
        try {
            final String trashFullname = defaultMailAccess.getFolderStorage().getTrashFolder();
            final char sep = defaultMailAccess.getFolderStorage().getFolder("INBOX").getSeparator();
            /*
             * Check location of trash folder: beside or below INBOX folder?
             */
            final int pos = trashFullname.lastIndexOf(sep);
            final String accountName;
            try {
                final MailAccountStorageService storageService = POP3ServiceRegistry.getServiceRegistry().getService(
                    MailAccountStorageService.class,
                    true);
                accountName = stripSpecials(storageService.getMailAccount(pop3AccountId, user, cid).getName());
            } catch (final OXException e) {
                throw e;
            }
            String fullname;
            if (pos == -1) {
                /*
                 * Beside INBOX folder
                 */
                fullname = accountName;
            } else {
                /*
                 * Below INBOX folder but beside trash folder
                 */
                fullname = new StringBuilder(16).append(trashFullname.substring(0, pos)).append(sep).append(accountName).toString();
            }
            /*
             * Check existence
             */
            if (defaultMailAccess.getFolderStorage().exists(fullname)) {
                final String pre = fullname;
                final SecureRandom secureRandom = new SecureRandom();
                do {
                    fullname = pre + stripSpecials(String.valueOf(secureRandom.nextInt(1000)));
                } while (defaultMailAccess.getFolderStorage().exists(fullname));
            }
            /*
             * Return unique path
             */
            return fullname;
        } finally {
            defaultMailAccess.close(true);
        }
    }

    private static String stripSpecials(final String src) {
        if (null == src || src.length() == 0) {
            return Long.toString(System.currentTimeMillis());
        }
        final char[] chars = src.toCharArray();
        final StringBuilder sb = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public Collection<OXException> getWarnings() {
        return Collections.unmodifiableCollection(warnings);
    }

    /**
     * Gets the separator character of underlying mail account.
     *
     * @return The separator character of underlying mail account
     * @throws OXException If separator character cannot be returned
     */
    public char getSeparator() throws OXException {
        if (0 == separator) {
            separator = getDefaultMailAccess().getFolderStorage().getFolder("INBOX").getSeparator();
        }
        return separator;
    }

    @Override
    public void drop() throws OXException {
        if (null != path) {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> defaultMailAccess = getDefaultMailAccess();
            if (defaultMailAccess.isConnected()) {
                try {
                    defaultMailAccess.getFolderStorage().deleteFolder(path, true);
                } catch (final OXException e) {
                    if (MimeMailExceptionCode.FOLDER_NOT_FOUND.equals(e)) {
                        // Ignore
                        LOG.trace(e.getMessage(), e);
                    } else {
                        throw e;
                    }
                }
            } else {
                defaultMailAccess.connect(false);
                try {
                    defaultMailAccess.getFolderStorage().deleteFolder(path, true);
                } catch (final OXException e) {
                    if (MimeMailExceptionCode.FOLDER_NOT_FOUND.equals(e)) {
                        // Ignore
                        LOG.trace(e.getMessage(), e);
                    } else {
                        throw e;
                    }
                } finally {
                    defaultMailAccess.close(true);
                }
            }
        }
    }

    /**
     * Gets the path to virtual root folder.
     *
     * @return The path to virtual root folder
     */
    public String getPath() {
        return path;
    }

    @Override
    public void close() {
        if (null != messageStorage) {
            try {
                messageStorage.releaseResources();
            } catch (final Exception e) {
                // Ignore
            }
            messageStorage = null;
        }
        if (null != folderStorage) {
            try {
                folderStorage.releaseResources();
            } catch (final Exception e) {
                // Ignore
            }
            folderStorage = null;
        }
        if (null != defaultMailAccess) {
            try {
                defaultMailAccess.close(true);
            } catch (final Exception e) {
                // Ignore
            }
            defaultMailAccess = null;
        }
    }

    @Override
    public int getUnreadMessagesCount(final String fullname) throws OXException {
        final String realFullname = getRealFullname(fullname);
        return getDefaultMailAccess().getUnreadMessagesCount(realFullname);
    }

    @Override
    public void connect() throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> defaultMailAccess = getDefaultMailAccess();
        defaultMailAccess.connect(false);
        try {
            // Check path existence
            final IMailFolderStorage fs = defaultMailAccess.getFolderStorage();
            if (!fs.exists(path)) {
                final MailFolderDescription toCreate = new MailFolderDescription();

                final MailPermission mp = new DefaultMailPermission();
                final Session session = pop3Access.getSession();
                mp.setEntity(session.getUserId());

                toCreate.addPermission(mp);
                toCreate.setExists(false);

                /*
                 * Determine where to create
                 */

                final MailFolder inboxFolder = fs.getFolder("INBOX");
                final char separator = inboxFolder.getSeparator();
                final String[] parentAndName = parseFullname(path, separator);
                /*
                 * Check CREATE permission
                 */
                final MailPermission ownPermission;
                String parentFullname = parentAndName[0];
                if (0 == parentFullname.length()) {
                    parentFullname = MailFolder.DEFAULT_FOLDER_ID;
                }
                if ("INBOX".equals(parentFullname)) {
                    ownPermission = inboxFolder.getOwnPermission();
                } else {
                    ownPermission = fs.getFolder(parentFullname).getOwnPermission();
                }
                if (null == ownPermission || ownPermission.canCreateSubfolders()) { // null is allowed for root folder
                    /*
                     * Set parent to current path's parent
                     */
                    toCreate.setParentFullname(parentFullname);
                } else {
                    /*
                     * Path is invalid! Change path
                     */
                    final String newParentFullname;
                    {
                        final String[] trashParentAndName = parseFullname(fs.getTrashFolder(), separator);
                        newParentFullname = trashParentAndName[0];
                    }
                    toCreate.setParentFullname(newParentFullname);
                    /*
                     * Compose new path
                     */
                    final StringBuilder sb = new StringBuilder();
                    if (!MailFolder.DEFAULT_FOLDER_ID.equals(newParentFullname)) {
                        sb.append(newParentFullname);
                    }
                    sb.append(separator);
                    sb.append(parentAndName[1]);
                    path = sb.toString();
                    // Update in properties
                    properties.addProperty(POP3StoragePropertyNames.PROPERTY_PATH, path);
                    if (fs.exists(path)) {
                        /*
                         * Check default folders
                         */
                        getFolderStorage().checkDefaultFolders();
                        return;
                    }
                }
                toCreate.setName(parentAndName[1]); // Set name
                toCreate.setSeparator(separator); // Set separator

                // Unsubscribe
                toCreate.setSubscribed(false);

                try {
                    fs.createFolder(toCreate);
                } catch (final OXException e) {
                    throw POP3ExceptionCode.ILLEGAL_PATH.create(e,
                        path,
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }

                /*
                 * Check default folders
                 */
                getFolderStorage().checkDefaultFolders();
            }
        } catch (final OXException e) {
            /*
             * Close on error
             */
            defaultMailAccess.close(true);
            throw e;
        } catch (final Exception e) {
            /*
             * Close on error
             */
            defaultMailAccess.close(true);
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static String[] parseFullname(final String fullname, final char separator) {
        final int pos = fullname.lastIndexOf(separator);
        if (-1 == pos) {
            return new String[] { MailFolder.DEFAULT_FOLDER_ID, fullname };
        }
        return new String[] { fullname.substring(0, pos), fullname.substring(pos + 1) };
    }

    @Override
    public IMailFolderStorage getFolderStorage() throws OXException {
        if (null == folderStorage) {
            folderStorage = new MailAccountPOP3FolderStorage(getDefaultMailAccess().getFolderStorage(), this, pop3Access);
        }
        return folderStorage;
    }

    @Override
    public IMailMessageStorage getMessageStorage() throws OXException {
        if (null == messageStorage) {
            messageStorage = new MailAccountPOP3MessageStorage(
                getDefaultMailAccess().getMessageStorage(),
                this,
                pop3AccountId,
                pop3Access.getSession());
        }
        return messageStorage;
    }

    IMailMessageStorage getInternalMessageStorage() throws OXException {
        return getDefaultMailAccess().getMessageStorage();
    }

    @Override
    public void releaseResources() {
        try {
            getFolderStorage().releaseResources();
        } catch (final OXException e) {
            LOG.debug(new StringBuilder("Error while closing POP3 folder storage: ").append(e.getMessage()).toString(), e);
        }
        try {
            getMessageStorage().releaseResources();
        } catch (final OXException e) {
            LOG.debug(new StringBuilder("Error while closing POP3 message storage: ").append(e.getMessage()).toString(), e);
        }
        /*-
         * TODO:
         * if (logicTools != null) {
         *  logicTools = null;
         * }
         */
    }

    private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

    private static final FetchProfile FETCH_PROFILE_UID = new FetchProfile() {

        // Unnamed block
        {
            add(UIDFolder.FetchProfileItem.UID);
        }
    };

    @Override
    public void syncMessages(final boolean expunge) throws OXException {
        POP3Store pop3Store = null;
        try {
            final POP3StoreResult result = POP3StoreConnector.getPOP3Store(
                pop3Access.getPOP3Config(),
                pop3Access.getMailProperties(),
                false,
                pop3Access.getSession(),
                !expunge);
            pop3Store = result.getPop3Store();
            final boolean containsWarnings = result.containsWarnings();
            if (containsWarnings) {
                warnings.addAll(result.getWarnings());
            }
            /*
             * Increase counter
             */
            final POP3Folder inbox = (POP3Folder) pop3Store.getFolder("INBOX");
            boolean doExpunge = false;
            /*
             * Get message count
             */
            final int messageCount;
            inbox.open(Folder.READ_WRITE);
            try {
                synchronized (inbox) {
                    messageCount = inbox.getMessageCount();
                    /*
                     * Empty?
                     */
                    if (0 == messageCount) {
                        // Nothing to synchronize
                        return;
                    }
                    final Vector<POP3Message> messageCache = getMessageCache(inbox);
                    final TIntObjectMap<String> seqnum2uidl;
                    {
                        /*
                         * The current UIDLs
                         */
                        final Message[] all = inbox.getMessages();
                        seqnum2uidl = new TIntObjectHashMap<String>(all.length);
                        final long startMillis = System.currentTimeMillis();
                        inbox.fetch(all, FETCH_PROFILE_UID);
                        MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - startMillis);
                        for (int i = 0; i < all.length; i++) {
                            final Message message = all[i];
                            final String uidl = inbox.getUID(message);
                            seqnum2uidl.put(message.getMessageNumber(), uidl);
                        }
                        messageCache.clear();
                        messageCache.setSize(messageCount);
                    }
                    /*
                     * Block-wise processing
                     */
                    final int blockSize;
                    {
                        final int configuredBlockSize = pop3Access.getPOP3Config().getPOP3Properties().getPOP3BlockSize();
                        blockSize = configuredBlockSize > messageCount ? messageCount : configuredBlockSize;
                    }
                    int start = 1;
                    while (start <= messageCount) {
                        final int num = add2Storage(inbox, start, blockSize, containsWarnings, messageCount, seqnum2uidl);
                        start += num;
                        messageCache.clear();
                        messageCache.setSize(messageCount);
                    }
                    /*
                     * Expunge if necessary
                     */
                    if (containsWarnings || expunge) {
                        /*
                         * Expunge all messages
                         */
                        final Message[] messages = inbox.getMessages();
                        for (int i = 0; i < messages.length; i++) {
                            messages[i].setFlags(FLAGS_DELETED, true);
                        }
                        doExpunge = true;
                    } else if (isDeleteWriteThrough()) {
                        /*
                         * Expunge trashed messages
                         */
                        final Message[] messages = inbox.getMessages();
                        final Set<String> trashedUIDLs = getTrashContainer().getUIDLs();
                        for (int i = 0; i < messages.length; i++) {
                            final Message message = messages[i];
                            final String uidl = seqnum2uidl.get(message.getMessageNumber());
                            if (trashedUIDLs.contains(uidl)) {
                                message.setFlags(FLAGS_DELETED, true);
                            }
                        }
                        doExpunge = true;
                    }
                }
            } finally {
                try {
                    if (inbox.isOpen()) {
                        inbox.close(doExpunge);
                    }
                } catch (final Exception e) {
                    final POP3Config pop3Config = pop3Access.getPOP3Config();
                    LOG.warn(
                        "POP3 mailbox " + pop3Config.getServer() + " could not be expunged/closed for login " + pop3Config.getLogin(),
                        e);
                }
                // Trashed UIDLs not needed anymore
                if (doExpunge) {
                    getTrashContainer().clear();
                }
            }
        } catch (final MessagingException e) {
            final Exception nested = e.getNextException();
            if (nested instanceof IOException) {
                LOG.warn("Connect to POP3 account failed: " + nested.getMessage(), nested);
                warnings.add(MailExceptionCode.IO_ERROR.create(nested, nested.getMessage()));
            } else {
                LOG.warn("Connect to POP3 account failed: " + e.getMessage(), e);
                warnings.add(MimeMailException.handleMessagingException(e, pop3Access.getPOP3Config(), pop3Access.getSession()));
            }
        } catch (final OXException e) {
            if (MimeMailExceptionCode.LOGIN_FAILED.equals(e) || MimeMailExceptionCode.INVALID_CREDENTIALS.equals(e)) {
                throw e;
            }
            LOG.warn("Connect to POP3 account failed: " + e.getMessage(), e);
            warnings.add(e);
        } finally {
            try {
                if (null != pop3Store) {
                    pop3Store.close();
                }
            } catch (final MessagingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private boolean isDeleteWriteThrough() throws OXException {
        final String property = properties.getProperty(POP3StoragePropertyNames.PROPERTY_DELETE_WRITE_THROUGH);
        return null == property ? false : Boolean.parseBoolean(property.trim());
    }

    private int add2Storage(final POP3Folder inbox, final int start, final int len, final boolean containsWarnings, final int messageCount, final TIntObjectMap<String> seqnum2uidl) throws MessagingException, OXException {
        final int retval; // The number of messages added to storage
        final int end; // The ending sequence number (inclusive)
        {
            final int startIndex = start - 1;
            final int remaining = messageCount - startIndex;
            if (remaining >= len) {
                end = startIndex + len;
                retval = len;
            } else {
                end = messageCount;
                retval = remaining;
            }
        }
        /*
         * Check for possible warnings. If so append messages to storage without filtering new messages.
         */
        if (containsWarnings) {
            /*
             * Append messages to storage
             */
            doBatchAppendWithFallback(inbox, inbox.getMessages(start, end), seqnum2uidl);
            return retval;
        }
        /*-
         * From JavaDoc for javax.mail.Folder.getMessages():
         *
         * Folder implementations are expected to provide light-weight Message objects, which get filled on demand.
         */
        final Message[] messages = inbox.getMessages(start, end);
        final Set<String> storageUIDLs = getStorageIDs();
        /*
         * Gather new messages
         */
        final List<Message> toFetch = new ArrayList<Message>(messages.length);
        for (int i = 0; i < messages.length; i++) {
            final Message message = messages[i];
            final String uidl = seqnum2uidl.get(message.getMessageNumber());
            if (!storageUIDLs.contains(uidl)) {
                /*
                 * UIDL not yet contained in storage
                 */
                toFetch.add(message);
            }
        }
        /*
         * Append new messages to storage
         */
        if (!toFetch.isEmpty()) {
            /*
             * Ensure INBOX is open
             */
            if (!inbox.isOpen()) {
                inbox.open(Folder.READ_WRITE);
            }
            /*
             * Do batch-append
             */
            doBatchAppendWithFallback(inbox, toFetch.toArray(new Message[toFetch.size()]), seqnum2uidl);
        }
        return retval;
    }

    private static final FetchProfile FETCH_PROFILE_ENVELOPE = new FetchProfile() {

        // Unnamed block
        {
            add(FetchProfile.Item.ENVELOPE);
        }
    };

    private void doBatchAppendWithFallback(final POP3Folder inbox, final Message[] msgs, final TIntObjectMap<String> seqnum2uidl) throws OXException {
        /*
         * Fetch ENVELOPE for new messages
         */
        try {
            final long start = System.currentTimeMillis();
            inbox.fetch(msgs, FETCH_PROFILE_ENVELOPE);
            MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        } catch (final MessagingException e) {
            // Try one-by-one loading
            LOG.debug("Batch retrieval of POP3 messages failed. Retry with one-by-one loading.", e);
            for (int i = 0; i < msgs.length; i++) {
                final int msgno = msgs[i].getMessageNumber();
                try {
                    msgs[i] = inbox.getMessage(msgno);
                } catch (final MessagingException inner) {
                    LOG.warn("Retrieval of POP3 message " + msgno + " failed.", inner);
                    msgs[i] = null;
                }
            }
        }
        /*
         * Append them to storage
         */
        final List<MailMessage> toAppend = new ArrayList<MailMessage>(msgs.length);
        for (int i = 0; i < msgs.length; i++) {
            final Message message = msgs[i];
            if (null != message) {
                final MailMessage mm = MimeMessageConverter.convertMessage((MimeMessage) message, false);
                mm.setMailId(seqnum2uidl.get(message.getMessageNumber()));
                toAppend.add(mm);
            }
        }
        /*
         * First try batch append operation
         */
        final MailAccountPOP3MessageStorage pop3MessageStorage = (MailAccountPOP3MessageStorage) getMessageStorage();
        try {
            pop3MessageStorage.appendPOP3Messages(toAppend.toArray(new MailMessage[toAppend.size()]));
        } catch (final OXException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Batch append operation to POP3 storage failed: " + e.getMessage(), e);
            }
            /*
             * Retry one-by-one. Handling each mail message.
             */
            final MailMessage[] arr = new MailMessage[1];
            for (final MailMessage mailMessage : toAppend) {
                try {
                    arr[0] = mailMessage;
                    pop3MessageStorage.appendPOP3Messages(arr);
                } catch (final OXException inner) {
                    LOG.warn("POP3 message could not be appended to POP3 storage: " + inner.getMessage(), inner);
                }
            }
        }
    }

    /**
     * Gets all known UIDLs of the messages kept in this storage.
     *
     * @return All known UIDLs of the messages kept in this storage
     * @throws OXException If fetching all UIDLs fails
     */
    private Set<String> getStorageIDs() throws OXException {
        final Set<String> tmp = new HashSet<String>(getUIDLMap().getAllUIDLs().keySet());
        tmp.addAll(getTrashContainer().getUIDLs());
        return tmp;
    }

    @Override
    public POP3StorageUIDLMap getUIDLMap() throws OXException {
        return SessionPOP3StorageUIDLMap.getInstance(pop3Access);
    }

    @Override
    public POP3StorageTrashContainer getTrashContainer() throws OXException {
        return SessionPOP3StorageTrashContainer.getInstance(pop3Access);
    }

    private String getRealFullname(final String fullname) throws OXException {
        return Utility.prependPath2Fullname(path, getSeparator(), fullname);
    }

    private static Message[] subarray(final Message[] messages, final int fromIndex, final int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (toIndex > messages.length) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        final int len = toIndex - fromIndex;
        final Message[] subarray = new Message[len];
        System.arraycopy(messages, fromIndex, subarray, 0, len);
        return subarray;
    }

    private static Message[] subarray(final Message[] messages, final int fromIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        final int len = messages.length - fromIndex;
        final Message[] subarray = new Message[len];
        System.arraycopy(messages, fromIndex, subarray, 0, len);
        return subarray;
    }

    @SuppressWarnings("unchecked")
    private static Vector<POP3Message> getMessageCache(final POP3Folder inbox) throws OXException {
        try {
            final Field messageCacheField = POP3Folder.class.getDeclaredField("message_cache");
            messageCacheField.setAccessible(true);
            return (Vector<POP3Message>) messageCacheField.get(inbox);
        } catch (final SecurityException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IllegalArgumentException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final NoSuchFieldException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IllegalAccessException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
