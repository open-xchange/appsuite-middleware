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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.utils.MIMEStorageUtility;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.pop3.storage.POP3StoragePropertyNames;
import com.openexchange.pop3.storage.POP3StorageProvider;
import com.openexchange.pop3.storage.POP3StorageProviderRegistry;
import com.openexchange.pop3.util.POP3StorageUtil;
import com.openexchange.session.Session;
import com.sun.mail.pop3.POP3Folder;

/**
 * {@link POP3InboxFolder} - Wrapper for POP3 INBOX folder which keeps track of open/connected status to manage a opened POP3 session.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3InboxFolder {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3InboxFolder.class);

    private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

    private POP3Storage storage;

    private final POP3Folder inbox;

    private final int accountId;

    private final Session session;

    private final Set<String> deletedUIDLs;

    private String[] uidls;

    private boolean open;

    /**
     * Initializes a new {@link POP3InboxFolder}.
     * 
     * @param inbox The POP3 INBOX folder
     * @param user The user ID
     * @param cid The context ID
     */
    public POP3InboxFolder(final POP3Folder inbox, final int accountId, final Session session) {
        super();
        this.inbox = inbox;
        this.accountId = accountId;
        this.session = session;
        deletedUIDLs = new HashSet<String>();
    }

    /**
     * Gets the total number of messages in POP3 INBOX folder which will not change while the folder is open because the POP3 protocol
     * doesn't support notification of new messages arriving in open folders.
     * 
     * @return The total number of messages in POP3 INBOX folder
     * @throws MailException If determining total number of messages fails
     */
    public int getMessageCount() throws MailException {
        open();
        try {
            return inbox.getMessageCount();
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    /**
     * (Possibly opens and) Gets all messages currently contained in POP3 INBOX folder, which are not marked as deleted.
     * 
     * @return All messages currently contained in POP3 INBOX folder.
     * @throws MailException If fetching messages' UIDLs fails
     */
    public Message[] getMessages() throws MailException {
        open();
        final Message[] msgs;
        try {
            msgs = inbox.getMessages();
            getUIDLs(msgs);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
        if (deletedUIDLs.isEmpty()) {
            return msgs;
        }
        // Withhold messages which are marked as deleted
        final List<Message> tmp = new ArrayList<Message>(msgs.length);
        for (int i = 0; i < msgs.length; i++) {
            if (!deletedUIDLs.contains(uidls[i])) {
                tmp.add(msgs[i]);
            }
        }
        return tmp.toArray(new Message[tmp.size()]);
    }

    /**
     * Gets matching message by specified UIDL.
     * 
     * @param uidl The UIDL
     * @return The corresponding message
     * @throws MailException If message cannot be returned
     */
    public Message getMessage(final String uidl) throws MailException {
        open();
        // Get matching message by UID
        final Message[] allmsgs = getMessages();
        final String[] uidls = getUIDLs();
        for (int i = 0; i < uidls.length; i++) {
            if (uidls[i].equals(uidl)) {
                return allmsgs[i];
            }
        }
        return null;
    }

    /**
     * Prefetch information about POP3 messages. If the <code>FetchProfile</code> contains <code>UIDFolder.FetchProfileItem.UID</code>, POP3
     * UIDs for all messages in the folder are fetched using the POP3 UIDL command. If the FetchProfile contains
     * <code>FetchProfile.Item.ENVELOPE</code>, the headers and size of all messages are fetched using the POP3 TOP and LIST commands.
     * 
     * @param msgs The messages whose items are fetched
     * @param fetchProfile The fetch profile poviding the items to fetch (Only <code>UIDFolder.FetchProfileItem.UID</code> and
     *            <code>FetchProfile.Item.ENVELOPE</code> supported for POP3)
     * @throws MailException If fetching items fails
     */
    public void fetch(final Message[] msgs, final FetchProfile fetchProfile) throws MailException {
        open();
        try {
            final long start = System.currentTimeMillis();
            inbox.fetch(msgs, fetchProfile);
            mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    private String[] getUIDLs(final Message[] allMessages) throws MailException {
        /*
         * The UIDLs will not change while the folder is open because the POP3 protocol doesn't support notification of new messages
         * arriving in open folders. Therefore it is safe to remember the UIDLs once.
         */
        if (uidls == null) {
            try {
                final Message[] all = allMessages == null ? inbox.getMessages() : allMessages;
                // Initiate fetch
                final long start = System.currentTimeMillis();
                inbox.fetch(all, MIMEStorageUtility.getUIDFetchProfile());
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                // Remember UIDLs
                uidls = new String[all.length];
                for (int i = 0; i < all.length; i++) {
                    uidls[i] = inbox.getUID(all[i]);
                }
            } catch (final MessagingException e) {
                throw MIMEMailException.handleMessagingException(e);
            }
        }
        return uidls;
    }

    /**
     * Gets the messages' UIDLs currently contained in POP3 INBOX folder.
     * 
     * @return The UIDLs of the messages.
     */
    public String[] getUIDLs() throws MailException {
        open();
        final String[] uidls = getUIDLs(null);
        if (deletedUIDLs.isEmpty()) {
            final String[] ret = new String[uidls.length];
            System.arraycopy(uidls, 0, ret, 0, uidls.length);
            return ret;
        }
        // Withhold UIDLs which are marked as deleted
        final List<String> tmp = new ArrayList<String>(uidls.length);
        for (int i = 0; i < uidls.length; i++) {
            final String uidl = uidls[i];
            if (!deletedUIDLs.contains(uidl)) {
                tmp.add(uidl);
            }
        }
        return tmp.toArray(new String[tmp.size()]);
    }

    /**
     * Opens the POP3 INBOX folder and synchronizes its content with database since number of messages will not change while the folder is
     * open because the POP3 protocol doesn't support notification of new messages arriving in open folders.
     * 
     * @throws MailException If opening POP3 INBOX folder fails
     */
    public void open() throws MailException {
        if (open) {
            return;
        }
        try {
            if (!inbox.isOpen()) {
                inbox.open(com.sun.mail.pop3.POP3Folder.READ_WRITE);
            }

            /*
             * Get POP3 storage provider from registry
             */
            final int user = session.getUserId();
            final int cid = session.getContextId();
            final Map<String, String> properties = POP3StorageUtil.getUserPOP3StorageProperties(accountId, user, cid);
            final String providerName = properties.get(POP3StoragePropertyNames.PROPERTY_STORAGE);
            if (null == providerName) {
                throw new POP3Exception(POP3Exception.Code.MISSING_POP3_STORAGE_NAME, Integer.valueOf(user), Integer.valueOf(cid));
            }
            final POP3StorageProvider provider = POP3StorageProviderRegistry.getInstance().getPOP3StorageProvider(providerName);
            if (null == provider) {
                throw new POP3Exception(POP3Exception.Code.MISSING_POP3_STORAGE, Integer.valueOf(user), Integer.valueOf(cid));
            }
            /*
             * Fetch appropriate storage
             */
            storage = provider.getPOP3Storage(session, properties);
            final Message[] allMessages = inbox.getMessages();
            {
                // Initiate fetch
                final FetchProfile fetchProfile = MIMEStorageUtility.getUIDFetchProfile();
                fetchProfile.add(FetchProfile.Item.ENVELOPE);
                final long start = System.currentTimeMillis();
                inbox.fetch(allMessages, fetchProfile);
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            }
            final MailMessage[] allMails = new MailMessage[allMessages.length];
            for (int i = 0; i < allMails.length; i++) {
                allMails[i] = MIMEMessageConverter.convertMessage((MimeMessage) allMessages[i]);
            }
            // Sync with storage
            storage.syncMessages(allMails);
            // POP3 connection not needed anymore since all messages should be sync'ed to storage
            inbox.close(true);
            // Mark as "open"
            open = true;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    /**
     * Closes and expunges POP3 INBOX folder.
     * <p>
     * This method should only be invoked when closing parental POP3 access instance since re-connect to POP3 INBOX folder might not be
     * possible due to POP3 server restrictions.
     * 
     * @throws MailException If either closing and/or expunging POP3 INBOX folder fails.
     */
    public void closeAndExpunge() throws MailException {
        if (!open) {
            return;
        }
        boolean expunge = false;
        try {
            if (!deletedUIDLs.isEmpty()) {
                final Message[] msgs = inbox.getMessages();
                for (int i = 0; i < uidls.length; i++) {
                    if (deletedUIDLs.contains(uidls[i])) {
                        msgs[i].setFlags(FLAGS_DELETED, true);
                    }
                }
                expunge = true;
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        } finally {
            try {
                inbox.close(expunge);
                if (expunge) {
                    POP3StorageUtil.deleteMessagesFromTables(deletedUIDLs, user, cid);
                }
                open = false;
            } catch (final IllegalStateException e) {
                LOG.warn("Invoked close() on a closed folder", e);
            } catch (final MessagingException e) {
                throw MIMEMailException.handleMessagingException(e);
            }
        }
    }

    /**
     * Marks the messages identified by given UIDLs as deleted.
     * <p>
     * Expunge takes place when invoking final {@link #closeAndExpunge()} method.
     * 
     * @param uidls The UIDLs identifying the messages which shall be deleted
     */
    public void deleteByUIDLs(final Collection<String> uidls) {
        deletedUIDLs.addAll(uidls);
    }

    /**
     * Gets the wrapped POP3 INBOX folder.
     * 
     * @return The wrapped POP3 INBOX folder
     */
    public Folder getPOP3InboxFolder() {
        return inbox;
    }

    /**
     * Clears this POP3 INBOX folder.
     * 
     * @throws MailException If clearing POP3 INBOX folder fails
     */
    public void clear() throws MailException {
        deletedUIDLs.addAll(Arrays.asList(getUIDLs(null)));
    }
}
