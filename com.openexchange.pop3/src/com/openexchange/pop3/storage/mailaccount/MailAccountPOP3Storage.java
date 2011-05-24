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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3Exception;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.connect.POP3StoreConnector;
import com.openexchange.pop3.connect.POP3StoreConnector.POP3StoreResult;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.pop3.storage.POP3StorageConnectCounter;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.pop3.storage.POP3StoragePropertyNames;
import com.openexchange.pop3.storage.POP3StorageTrashContainer;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.pop3.storage.mailaccount.util.Utility;
import com.openexchange.server.ServiceException;
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

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailAccountPOP3Storage.class);

    /*-
     * Member section
     */

    private final POP3StorageProperties properties;

    private String path;

    private final POP3Access pop3Access;

    private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> defaultMailAccess;

    private final int pop3AccountId;

    private MailAccountPOP3MessageStorage messageStorage;

    private MailAccountPOP3FolderStorage folderStorage;

    private char separator;

    private final Collection<MailException> warnings;

    MailAccountPOP3Storage(final POP3Access pop3Access, final POP3StorageProperties properties) throws MailException {
        super();
        warnings = new ArrayList<MailException>(2);
        this.pop3Access = pop3Access;
        pop3AccountId = pop3Access.getAccountId();
        final Session session = pop3Access.getSession();
        defaultMailAccess = MailAccess.getInstance(session);
        this.properties = properties;
        {
            String tmp = properties.getProperty(POP3StoragePropertyNames.PROPERTY_PATH);
            if (null == tmp) {
                final POP3Exception e = new POP3Exception(
                    POP3Exception.Code.MISSING_PATH,
                    Integer.valueOf(session.getUserId()),
                    Integer.valueOf(session.getContextId()));
                LOG.warn("Path is null. Error: " + e.getMessage(), e);
                // Try to compose path
                tmp = composeUniquePath(pop3Access.getAccountId(), session.getUserId(), session.getContextId());
                // Add to properties
                properties.addProperty(POP3StoragePropertyNames.PROPERTY_PATH, tmp);
            }
            path = tmp;
        }
        if (null == path) {
            throw new POP3Exception(
                POP3Exception.Code.MISSING_PATH,
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }
        separator = 0;
    }

    private String composeUniquePath(final int pop3AccountId, final int user, final int cid) throws MailException {
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
            } catch (final ServiceException e) {
                throw new MailException(e);
            } catch (final MailAccountException e) {
                throw new MailException(e);
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
                    fullname = pre + stripSpecials(String.valueOf(secureRandom.nextLong()));
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
            return String.valueOf(System.currentTimeMillis());
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

    public Collection<MailException> getWarnings() {
        return Collections.unmodifiableCollection(warnings);
    }

    /**
     * Gets the separator character of underlying mail account.
     * 
     * @return The separator character of underlying mail account
     * @throws MailException If separator character cannot be returned
     */
    public char getSeparator() throws MailException {
        if (0 == separator) {
            separator = defaultMailAccess.getFolderStorage().getFolder("INBOX").getSeparator();
        }
        return separator;
    }

    public void drop() throws MailException {
        if (null != path) {
            if (defaultMailAccess.isConnected()) {
                try {
                    defaultMailAccess.getFolderStorage().deleteFolder(path, true);
                } catch (final MailException e) {
                    if (MIMEMailException.Code.FOLDER_NOT_FOUND.getNumber() == e.getDetailNumber()) {
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
                } catch (final MailException e) {
                    if (MIMEMailException.Code.FOLDER_NOT_FOUND.getNumber() == e.getDetailNumber()) {
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

    public void close() {
        defaultMailAccess.close(true);
    }

    public int getUnreadMessagesCount(final String fullname) throws MailException {
        final String realFullname = getRealFullname(fullname);
        return defaultMailAccess.getUnreadMessagesCount(realFullname);
    }

    public void connect() throws MailException {
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
                } catch (final MailException e) {
                    throw new POP3Exception(
                        POP3Exception.Code.ILLEGAL_PATH,
                        e,
                        path,
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }

                /*
                 * Check default folders
                 */
                getFolderStorage().checkDefaultFolders();
            }
        } catch (final MailException e) {
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
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        }
    }

    private static String[] parseFullname(final String fullname, final char separator) {
        final int pos = fullname.lastIndexOf(separator);
        if (-1 == pos) {
            return new String[] { MailFolder.DEFAULT_FOLDER_ID, fullname };
        }
        return new String[] { fullname.substring(0, pos), fullname.substring(pos + 1) };
    }

    public IMailFolderStorage getFolderStorage() throws MailException {
        if (null == folderStorage) {
            folderStorage = new MailAccountPOP3FolderStorage(defaultMailAccess.getFolderStorage(), this, pop3Access);
        }
        return folderStorage;
    }

    public IMailMessageStorage getMessageStorage() throws MailException {
        if (null == messageStorage) {
            messageStorage = new MailAccountPOP3MessageStorage(
                defaultMailAccess.getMessageStorage(),
                this,
                pop3AccountId,
                pop3Access.getSession());
        }
        return messageStorage;
    }

    IMailMessageStorage getInternalMessageStorage() throws MailException {
        return defaultMailAccess.getMessageStorage();
    }

    public void releaseResources() {
        try {
            IMailFolderStorage folderStorage = getFolderStorage();
            if (folderStorage != null) {
                try {
                    folderStorage.releaseResources();
                } catch (final MailException e) {
                    LOG.error(new StringBuilder("Error while closing POP3 folder storage: ").append(e.getMessage()).toString(), e);
                } finally {
                    folderStorage = null;
                }
            }
        } catch (final MailException e) {
            LOG.error(e.getMessage(), e);
        }
        try {
            IMailMessageStorage messageStorage = getMessageStorage();
            if (messageStorage != null) {
                try {
                    messageStorage.releaseResources();
                } catch (final MailException e) {
                    LOG.error(new StringBuilder("Error while closing POP3 message storage: ").append(e.getMessage()).toString(), e);
                } finally {
                    messageStorage = null;
                }
            }
        } catch (final MailException e) {
            LOG.error(e.getMessage(), e);
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

    public void syncMessages(final boolean expunge, final POP3StorageConnectCounter connectCounter) throws MailException {
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
            connectCounter.incrementCounter();
            final POP3Folder inbox = (POP3Folder) pop3Store.getFolder("INBOX");
            boolean doExpunge = false;
            /*
             * Get message count
             */
            final int messageCount;
            final String[] uidlsFromPOP3;
            inbox.open(POP3Folder.READ_WRITE);
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
                    final Map<Integer, String> seqnum2uidl;
                    {
                        /*
                         * The current UIDLs
                         */
                        final Message[] all = inbox.getMessages();
                        seqnum2uidl = new HashMap<Integer, String>(all.length);
                        final long startMillis = System.currentTimeMillis();
                        inbox.fetch(all, FETCH_PROFILE_UID);
                        MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - startMillis);
                        uidlsFromPOP3 = new String[all.length];
                        for (int i = 0; i < all.length; i++) {
                            final Message message = all[i];
                            final String uidl = inbox.getUID(message);
                            uidlsFromPOP3[i] = uidl;
                            seqnum2uidl.put(Integer.valueOf(message.getMessageNumber()), uidl);
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
                    final boolean dwt = Boolean.parseBoolean(properties.getProperty(POP3StoragePropertyNames.PROPERTY_DELETE_WRITE_THROUGH));
                    if (containsWarnings || expunge || dwt) {
                        /*
                         * Expunge all messages
                         */
                        final Message[] messages = inbox.getMessages();
                        if (dwt) {
                            final Set<String> trashedUIDLs = getTrashContainer().getUIDLs();
                            for (int i = 0; i < messages.length; i++) {
                                final Message message = messages[i];
                                final String uidl = uidlsFromPOP3[i];
                                if (trashedUIDLs.contains(uidl)) {
                                    message.setFlags(FLAGS_DELETED, true);
                                }
                            }
                        } else {
                            for (int i = 0; i < messages.length; i++) {
                                messages[i].setFlags(FLAGS_DELETED, true);
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
                warnings.add(new MailException(MailException.Code.IO_ERROR, nested, nested.getMessage()));
            } else {
                LOG.warn("Connect to POP3 account failed: " + e.getMessage(), e);
                warnings.add(MIMEMailException.handleMessagingException(e, pop3Access.getPOP3Config(), pop3Access.getSession()));
            }
        } catch (final MailException e) {
            if (MIMEMailException.Code.LOGIN_FAILED.getNumber() == e.getDetailNumber() || MIMEMailException.Code.INVALID_CREDENTIALS.getNumber() == e.getDetailNumber()) {
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
            } finally {
                connectCounter.decrementCounter();
            }
        }
    }

    private int add2Storage(final POP3Folder inbox, final int start, final int len, final boolean containsWarnings, final int messageCount, final Map<Integer, String> seqnum2uidl) throws MessagingException, MailException {
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
            final String uidl = seqnum2uidl.get(Integer.valueOf(message.getMessageNumber()));
            if (!storageUIDLs.contains(uidl)) {
                /*
                 * Unknown UIDL...
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
                inbox.open(POP3Folder.READ_WRITE);
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

    private void doBatchAppendWithFallback(final POP3Folder inbox, final Message[] msgs, final Map<Integer, String> seqnum2uidl) throws MessagingException, MailException {
        /*
         * Fetch ENVELOPE for new messages
         */
        final long start = System.currentTimeMillis();
        inbox.fetch(msgs, FETCH_PROFILE_ENVELOPE);
        MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        /*
         * Append them to storage
         */
        final List<MailMessage> toAppend = new ArrayList<MailMessage>(msgs.length);
        final NoFolderMimeMessage delegater = new NoFolderMimeMessage(MIMEDefaultSession.getDefaultSession());
        for (int i = 0; i < msgs.length; i++) {
            final Message message = msgs[i];
            delegater.setMimeMessage((MimeMessage) message);
            final MailMessage mm = MIMEMessageConverter.convertMessage(delegater);
            mm.setMailId(seqnum2uidl.get(Integer.valueOf(message.getMessageNumber())));
            toAppend.add(mm);
        }
        /*
         * First try batch append operation
         */
        final MailAccountPOP3MessageStorage pop3MessageStorage = (MailAccountPOP3MessageStorage) getMessageStorage();
        try {
            pop3MessageStorage.appendPOP3Messages(toAppend.toArray(new MailMessage[toAppend.size()]));
        } catch (final MailException e) {
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
                } catch (final MailException inner) {
                    LOG.warn("POP3 message could not be appended to POP3 storage: " + inner.getMessage(), inner);
                }
            }
        }
    }

    /**
     * Gets all known UIDLs of the messages kept in this storage.
     * 
     * @return All known UIDLs of the messages kept in this storage
     * @throws MailException If fetching all UIDLs fails
     */
    private Set<String> getStorageIDs() throws MailException {
        final Set<String> tmp = new HashSet<String>(getUIDLMap().getAllUIDLs().keySet());
        tmp.addAll(getTrashContainer().getUIDLs());
        return tmp;
    }

    public POP3StorageUIDLMap getUIDLMap() throws MailException {
        return SessionPOP3StorageUIDLMap.getInstance(pop3Access);
    }

    public POP3StorageTrashContainer getTrashContainer() throws MailException {
        return SessionPOP3StorageTrashContainer.getInstance(pop3Access);
    }

    private String getRealFullname(final String fullname) throws MailException {
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
    private static Vector<POP3Message> getMessageCache(final POP3Folder inbox) throws MailException {
        try {
            final Field messageCacheField = POP3Folder.class.getDeclaredField("message_cache");
            messageCacheField.setAccessible(true);
            return (Vector<POP3Message>) messageCacheField.get(inbox);
        } catch (final SecurityException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final IllegalArgumentException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final NoSuchFieldException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final IllegalAccessException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        }
    }

    private static final class NoFolderMimeMessage extends MimeMessage {

        private MimeMessage mimeMessage;

        public NoFolderMimeMessage(final javax.mail.Session session) {
            super(session);
        }

        public void setMimeMessage(final MimeMessage mimeMessage) {
            this.mimeMessage = mimeMessage;
        }

        @Override
        public int hashCode() {
            return mimeMessage.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return mimeMessage.equals(obj);
        }

        @Override
        public String toString() {
            return mimeMessage.toString();
        }

        @Override
        public void setRecipient(final javax.mail.Message.RecipientType type, final Address address) throws MessagingException {
            mimeMessage.setRecipient(type, address);
        }

        @Override
        public Address[] getFrom() throws MessagingException {
            return mimeMessage.getFrom();
        }

        @Override
        public void setFrom(final Address address) throws MessagingException {
            mimeMessage.setFrom(address);
        }

        @Override
        public void addRecipient(final javax.mail.Message.RecipientType type, final Address address) throws MessagingException {
            mimeMessage.addRecipient(type, address);
        }

        @Override
        public void setFrom() throws MessagingException {
            mimeMessage.setFrom();
        }

        @Override
        public void addFrom(final Address[] addresses) throws MessagingException {
            mimeMessage.addFrom(addresses);
        }

        @Override
        public Address getSender() throws MessagingException {
            return mimeMessage.getSender();
        }

        @Override
        public void setSender(final Address address) throws MessagingException {
            mimeMessage.setSender(address);
        }

        @Override
        public Address[] getRecipients(final javax.mail.Message.RecipientType type) throws MessagingException {
            return mimeMessage.getRecipients(type);
        }

        @Override
        public Address[] getAllRecipients() throws MessagingException {
            return mimeMessage.getAllRecipients();
        }

        @Override
        public void setRecipients(final javax.mail.Message.RecipientType type, final Address[] addresses) throws MessagingException {
            mimeMessage.setRecipients(type, addresses);
        }

        @Override
        public void setFlag(final Flag flag, final boolean set) throws MessagingException {
            mimeMessage.setFlag(flag, set);
        }

        @Override
        public void setRecipients(final javax.mail.Message.RecipientType type, final String addresses) throws MessagingException {
            mimeMessage.setRecipients(type, addresses);
        }

        @Override
        public int getMessageNumber() {
            return mimeMessage.getMessageNumber();
        }

        @Override
        public Folder getFolder() {
            return null;
        }

        @Override
        public boolean isExpunged() {
            return mimeMessage.isExpunged();
        }

        @Override
        public void addRecipients(final javax.mail.Message.RecipientType type, final Address[] addresses) throws MessagingException {
            mimeMessage.addRecipients(type, addresses);
        }

        @Override
        public void addRecipients(final javax.mail.Message.RecipientType type, final String addresses) throws MessagingException {
            mimeMessage.addRecipients(type, addresses);
        }

        @Override
        public Address[] getReplyTo() throws MessagingException {
            return mimeMessage.getReplyTo();
        }

        @Override
        public void setReplyTo(final Address[] addresses) throws MessagingException {
            mimeMessage.setReplyTo(addresses);
        }

        @Override
        public boolean match(final SearchTerm term) throws MessagingException {
            return mimeMessage.match(term);
        }

        @Override
        public String getSubject() throws MessagingException {
            return mimeMessage.getSubject();
        }

        @Override
        public void setSubject(final String subject) throws MessagingException {
            mimeMessage.setSubject(subject);
        }

        @Override
        public void setSubject(final String subject, final String charset) throws MessagingException {
            mimeMessage.setSubject(subject, charset);
        }

        @Override
        public Date getSentDate() throws MessagingException {
            return mimeMessage.getSentDate();
        }

        @Override
        public void setSentDate(final Date d) throws MessagingException {
            mimeMessage.setSentDate(d);
        }

        @Override
        public Date getReceivedDate() throws MessagingException {
            return mimeMessage.getReceivedDate();
        }

        @Override
        public int getSize() throws MessagingException {
            return mimeMessage.getSize();
        }

        @Override
        public int getLineCount() throws MessagingException {
            return mimeMessage.getLineCount();
        }

        @Override
        public String getContentType() throws MessagingException {
            return mimeMessage.getContentType();
        }

        @Override
        public boolean isMimeType(final String mimeType) throws MessagingException {
            return mimeMessage.isMimeType(mimeType);
        }

        @Override
        public String getDisposition() throws MessagingException {
            return mimeMessage.getDisposition();
        }

        @Override
        public void setDisposition(final String disposition) throws MessagingException {
            mimeMessage.setDisposition(disposition);
        }

        @Override
        public String getEncoding() throws MessagingException {
            return mimeMessage.getEncoding();
        }

        @Override
        public String getContentID() throws MessagingException {
            return mimeMessage.getContentID();
        }

        @Override
        public void setContentID(final String cid) throws MessagingException {
            mimeMessage.setContentID(cid);
        }

        @Override
        public String getContentMD5() throws MessagingException {
            return mimeMessage.getContentMD5();
        }

        @Override
        public void setContentMD5(final String md5) throws MessagingException {
            mimeMessage.setContentMD5(md5);
        }

        @Override
        public String getDescription() throws MessagingException {
            return mimeMessage.getDescription();
        }

        @Override
        public void setDescription(final String description) throws MessagingException {
            mimeMessage.setDescription(description);
        }

        @Override
        public void setDescription(final String description, final String charset) throws MessagingException {
            mimeMessage.setDescription(description, charset);
        }

        @Override
        public String[] getContentLanguage() throws MessagingException {
            return mimeMessage.getContentLanguage();
        }

        @Override
        public void setContentLanguage(final String[] languages) throws MessagingException {
            mimeMessage.setContentLanguage(languages);
        }

        @Override
        public String getMessageID() throws MessagingException {
            return mimeMessage.getMessageID();
        }

        @Override
        public String getFileName() throws MessagingException {
            return mimeMessage.getFileName();
        }

        @Override
        public void setFileName(final String filename) throws MessagingException {
            mimeMessage.setFileName(filename);
        }

        @Override
        public InputStream getInputStream() throws IOException, MessagingException {
            return mimeMessage.getInputStream();
        }

        @Override
        public InputStream getRawInputStream() throws MessagingException {
            return mimeMessage.getRawInputStream();
        }

        @Override
        public Object getContent() throws IOException, MessagingException {
            return mimeMessage.getContent();
        }

        @Override
        public void setContent(final Object o, final String type) throws MessagingException {
            mimeMessage.setContent(o, type);
        }

        @Override
        public void setText(final String text) throws MessagingException {
            mimeMessage.setText(text);
        }

        @Override
        public void setText(final String text, final String charset) throws MessagingException {
            mimeMessage.setText(text, charset);
        }

        @Override
        public void setText(final String text, final String charset, final String subtype) throws MessagingException {
            mimeMessage.setText(text, charset, subtype);
        }

        @Override
        public void setContent(final Multipart mp) throws MessagingException {
            mimeMessage.setContent(mp);
        }

        @Override
        public Message reply(final boolean replyToAll) throws MessagingException {
            return mimeMessage.reply(replyToAll);
        }

        @Override
        public void writeTo(final OutputStream os) throws IOException, MessagingException {
            mimeMessage.writeTo(os);
        }

        @Override
        public void writeTo(final OutputStream os, final String[] ignoreList) throws IOException, MessagingException {
            mimeMessage.writeTo(os, ignoreList);
        }

        @Override
        public String[] getHeader(final String name) throws MessagingException {
            return mimeMessage.getHeader(name);
        }

        @Override
        public String getHeader(final String name, final String delimiter) throws MessagingException {
            return mimeMessage.getHeader(name, delimiter);
        }

        @Override
        public void setHeader(final String name, final String value) throws MessagingException {
            mimeMessage.setHeader(name, value);
        }

        @Override
        public void addHeader(final String name, final String value) throws MessagingException {
            mimeMessage.addHeader(name, value);
        }

        @Override
        public void removeHeader(final String name) throws MessagingException {
            mimeMessage.removeHeader(name);
        }

        @Override
        public Enumeration<?> getAllHeaders() throws MessagingException {
            return mimeMessage.getAllHeaders();
        }

        @Override
        public Enumeration<?> getMatchingHeaders(final String[] names) throws MessagingException {
            return mimeMessage.getMatchingHeaders(names);
        }

        @Override
        public Enumeration<?> getNonMatchingHeaders(final String[] names) throws MessagingException {
            return mimeMessage.getNonMatchingHeaders(names);
        }

        @Override
        public void addHeaderLine(final String line) throws MessagingException {
            mimeMessage.addHeaderLine(line);
        }

        @Override
        public Enumeration<?> getAllHeaderLines() throws MessagingException {
            return mimeMessage.getAllHeaderLines();
        }

        @Override
        public Enumeration<?> getMatchingHeaderLines(final String[] names) throws MessagingException {
            return mimeMessage.getMatchingHeaderLines(names);
        }

        @Override
        public Enumeration<?> getNonMatchingHeaderLines(final String[] names) throws MessagingException {
            return mimeMessage.getNonMatchingHeaderLines(names);
        }

        @Override
        public Flags getFlags() throws MessagingException {
            return mimeMessage.getFlags();
        }

        @Override
        public boolean isSet(final Flag flag) throws MessagingException {
            return mimeMessage.isSet(flag);
        }

        @Override
        public void setFlags(final Flags flag, final boolean set) throws MessagingException {
            mimeMessage.setFlags(flag, set);
        }

        @Override
        public void saveChanges() throws MessagingException {
            mimeMessage.saveChanges();
        }

    }

}
