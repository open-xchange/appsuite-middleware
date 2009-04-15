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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.utils.MIMEStorageUtility;
import com.sun.mail.pop3.POP3Folder;

/**
 * {@link POP3InboxFolder} - Wrapper for POP3 INBOX folder which keeps track of open/connected status to manage a opened POP3 session.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3InboxFolder {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3InboxFolder.class);

    private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

    private final POP3Folder inbox;

    private final Set<String> deletedUIDLs;

    private Message[] msgs;

    private String[] uidls;

    private boolean open;

    /**
     * Initializes a new {@link POP3InboxFolder}.
     * 
     * @param inbox The POP3 INBOX folder
     */
    public POP3InboxFolder(final POP3Folder inbox) {
        super();
        this.inbox = inbox;
        deletedUIDLs = new HashSet<String>();
    }

    /**
     * (Possibly opens and) Gets all messages currently contained in POP3 INBOX folder, which are not marked as deleted.
     * 
     * @return All messages currently contained in POP3 INBOX folder.
     * @throws MailException If fetching messages' UIDLs fails
     */
    public Message[] getMessages() throws MailException {
        open();
        /*
         * The UIDLs will not change while the folder is open because the POP3 protocol doesn't support notification of new messages
         * arriving in open folders. Therefore it is safe to remember the UIDLs once.
         */
        if (msgs == null) {
            try {
                msgs = inbox.getMessages();
                inbox.fetch(msgs, MIMEStorageUtility.getUIDFetchProfile());
                uidls = new String[msgs.length];
                for (int i = 0; i < msgs.length; i++) {
                    uidls[i] = inbox.getUID(msgs[i]);
                }
            } catch (final MessagingException e) {
                throw MIMEMailException.handleMessagingException(e);
            }
        }
        if (deletedUIDLs.isEmpty()) {
            return msgs;
        }
        final List<Message> l = new ArrayList<Message>(msgs.length);
        for (int i = 0; i < msgs.length; i++) {
            if (!deletedUIDLs.contains(uidls[i])) {
                l.add(msgs[i]);
            }
        }
        return l.toArray(new Message[l.size()]);
    }

    /**
     * Opens the POP3 INBOX folder.
     * 
     * @throws MailException If opening POP3 INBOX folder fails
     */
    public void open() throws MailException {
        if (open) {
            return;
        }
        try {
            inbox.open(com.sun.mail.pop3.POP3Folder.READ_WRITE);
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
                getMessages();
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

}
