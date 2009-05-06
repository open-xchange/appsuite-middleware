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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.utils.MIMEStorageUtility;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3Exception;
import com.openexchange.pop3.POP3StoreConnector;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.pop3.storage.POP3StoragePropertyNames;
import com.openexchange.pop3.storage.POP3StorageTrashContainer;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.session.Session;
import com.sun.mail.pop3.POP3Folder;
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

    private final String path;

    private final POP3Access pop3Access;

    private final MailAccess<?, ?> defaultMailAccess;

    private MailAccountPOP3MessageStorage messageStorage;

    private MailAccountPOP3FolderStorage folderStorage;

    private char separator;

    MailAccountPOP3Storage(final POP3Access pop3Access, final POP3StorageProperties properties) throws MailException {
        super();
        this.pop3Access = pop3Access;
        final Session session = pop3Access.getSession();
        defaultMailAccess = MailAccess.getInstance(session);
        this.properties = properties;
        path = properties.getProperty(POP3StoragePropertyNames.PROPERTY_PATH);
        if (null == path) {
            throw new POP3Exception(
                POP3Exception.Code.MISSING_PATH,
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }
        separator = 0;
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

    /**
     * Gets the path to virtual root folder.
     * 
     * @return The path to virtual root folder
     */
    public String getPath() {
        return path;
    }

    public void close() throws MailException {
        defaultMailAccess.close(true);
    }

    public void connect() throws MailException {
        defaultMailAccess.connect();
        // Check path existence
        if (!defaultMailAccess.getFolderStorage().exists(path)) {
            final MailFolderDescription toCreate = new MailFolderDescription();

            final MailPermission mp = new DefaultMailPermission();
            mp.setEntity(pop3Access.getSession().getUserId());

            toCreate.addPermission(mp);
            toCreate.setExists(false);

            final char separator = defaultMailAccess.getFolderStorage().getFolder("INBOX").getSeparator();

            final String[] parentAndName = parseFullname(path, separator);
            toCreate.setName(parentAndName[1]);
            toCreate.setParentFullname(parentAndName[0]);
            toCreate.setSeparator(separator);

            defaultMailAccess.getFolderStorage().createFolder(toCreate);
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
            messageStorage = new MailAccountPOP3MessageStorage(defaultMailAccess.getMessageStorage(), this);
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

    public void syncMessages(final boolean expunge) throws MailException {
        final POP3Store pop3Store = POP3StoreConnector.getPOP3Store(pop3Access.getPOP3Config(), pop3Access.getMailProperties());
        /*
         * Increase counter
         */
        MailServletInterface.mailInterfaceMonitor.changeNumActive(true);
        MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.IMAP);
        try {
            final POP3Folder inbox = (POP3Folder) pop3Store.getFolder("INBOX");
            inbox.open(POP3Folder.READ_WRITE);
            boolean doExpunge = false;
            try {
                final Message[] all = inbox.getMessages();
                {
                    // Initiate fetch
                    final FetchProfile fetchProfile = MIMEStorageUtility.getUIDFetchProfile();
                    fetchProfile.add(FetchProfile.Item.ENVELOPE);
                    final long start = System.currentTimeMillis();
                    inbox.fetch(all, fetchProfile);
                    MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                }
                final String[] uidlsFromPOP3 = new String[all.length];
                for (int i = 0; i < all.length; i++) {
                    uidlsFromPOP3[i] = inbox.getUID(all[i]);
                }
                /*
                 * Create collections
                 */
                final List<String> storageUIDLs = Arrays.asList(getStorageIDs());
                final List<String> actualUIDLs = Arrays.asList(uidlsFromPOP3);

                // TODO: Shall we determine & delete removed UIDLs from storage, too?
                /*-
                 * 
                final Set<String> removedUIDLs = new HashSet<String>(storageUIDLs);
                removedUIDLs.removeAll(actualUIDLs);
                deleteMessagesFromTables(removedUIDLs, user, cid);
                 */

                // Determine & insert new UIDLs
                final Set<String> newUIDLs = new HashSet<String>(actualUIDLs);
                newUIDLs.removeAll(storageUIDLs);
                addMessagesToStorage(newUIDLs, inbox, all);

                if (expunge) {
                    // Mark messages as \Deleted
                    for (int i = 0; i < all.length; i++) {
                        all[i].setFlags(FLAGS_DELETED, true);
                    }
                    doExpunge = true;
                } else {
                    // Check if delete is set to write-through
                    final String deleteWriteThroughStr = properties.getProperty(POP3StoragePropertyNames.PROPERTY_DELETE_WRITE_THROUGH);
                    if (Boolean.parseBoolean(deleteWriteThroughStr)) {
                        final Set<String> trashedUIDLs = getTrashContainer().getUIDLs();
                        for (int i = 0; i < all.length; i++) {
                            final Message message = all[i];
                            final String uidl = inbox.getUID(message);
                            if (trashedUIDLs.contains(uidl)) {
                                message.setFlags(FLAGS_DELETED, true);
                            }
                        }
                        doExpunge = true;
                    }
                }
            } finally {
                inbox.close(doExpunge);
                if (doExpunge) {
                    // Trashed UIDLs not needed anymore
                    getTrashContainer().clear();
                }
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Access.getPOP3Config());
        } finally {
            try {
                pop3Store.close();
            } catch (final MessagingException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                /*
                 * Decrease counters
                 */
                MailServletInterface.mailInterfaceMonitor.changeNumActive(false);
                MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.IMAP);
            }
        }
    }

    private void addMessagesToStorage(final Set<String> newUIDLs, final POP3Folder inbox, final Message[] all) throws MessagingException, MailException {
        final List<MailMessage> toAppend = new ArrayList<MailMessage>(newUIDLs.size());
        for (int i = 0; i < all.length; i++) {
            final Message message = all[i];
            final String uidl = inbox.getUID(message);
            if (newUIDLs.contains(uidl)) {
                final MailMessage mm = MIMEMessageConverter.convertMessage((MimeMessage) message);
                mm.setMailId(uidl);
                toAppend.add(mm);
            }
        }
        ((MailAccountPOP3MessageStorage) getMessageStorage()).appendPOP3Messages(toAppend.toArray(new MailMessage[toAppend.size()]));
    }

    /**
     * Gets all known UIDLs of the messages kept in this storage.
     * 
     * @return All known UIDLs of the messages kept in this storage
     * @throws MailException If fetching all UIDLs fails
     */
    private String[] getStorageIDs() throws MailException {
        final Set<String> tmp = new HashSet<String>();
        tmp.addAll(getUIDLMap().getAllUIDLs().keySet());
        tmp.addAll(getTrashContainer().getUIDLs());
        return tmp.toArray(new String[tmp.size()]);
    }

    public POP3StorageUIDLMap getUIDLMap() throws MailException {
        return SessionPOP3StorageUIDLMap.getInstance(pop3Access);
    }

    public POP3StorageTrashContainer getTrashContainer() throws MailException {
        return SessionPOP3StorageTrashContainer.getInstance(pop3Access);
    }

}
