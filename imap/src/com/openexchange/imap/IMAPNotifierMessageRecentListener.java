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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.imap;

import javax.mail.event.MessageRecentEvent;
import javax.mail.event.MessageRecentListener;
import org.apache.commons.logging.LogFactory;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.push.PushException;
import com.openexchange.push.PushUtility;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link IMAPNotifierMessageRecentListener}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPNotifierMessageRecentListener implements MessageRecentListener {

    private static final org.apache.commons.logging.Log LOG =
        org.apache.commons.logging.LogFactory.getLog(IMAPNotifierMessageRecentListener.class);

    private static final boolean INFO_ENABLED = LOG.isInfoEnabled();

    private static final String INBOX = "INBOX";

    private static final boolean INBOX_ONLY = true;

    /**
     * Adds the recent-notifier for specified IMAP folder if allowed to.
     * 
     * @param imapFolder The IMAP folder
     * @param accountId The account identifier
     * @param session The session
     */
    public static void addNotifierFor(final IMAPFolder imapFolder, final int accountId, final Session session) {
        addNotifierFor(imapFolder, null, accountId, session);
    }

    /**
     * Adds the recent-notifier for specified IMAP folder if allowed to.
     * 
     * @param imapFolder The IMAP folder
     * @param optFullName The optional full name
     * @param accountId The account identifier
     * @param session The session
     */
    public static void addNotifierFor(final IMAPFolder imapFolder, final String optFullName, final int accountId, final Session session) {
        final String fullName = optFullName == null ? imapFolder.getFullName() : optFullName;
        if (IMAPProperties.getInstance().notifyRecent() && (!INBOX_ONLY || INBOX.equalsIgnoreCase(fullName))) {
            imapFolder.addMessageRecentListener(new IMAPNotifierMessageRecentListener(fullName, accountId, session));
        }
    }

    private final String fullName;

    private final int accountId;

    private final Session session;

    /**
     * Initializes a new {@link IMAPNotifierMessageRecentListener}.
     * 
     * @param fullName The full name
     * @param accountId The account identifier
     * @param session The session
     */
    private IMAPNotifierMessageRecentListener(final String fullName, final int accountId, final Session session) {
        super();
        this.fullName = fullName;
        this.accountId = accountId;
        this.session = session;
    }

    public void recentAvailable(final MessageRecentEvent event) {
        try {
            PushUtility.triggerOSGiEvent(
                MailFolderUtility.prepareFullname(accountId, fullName.length() == 0 ? MailFolder.DEFAULT_FOLDER_ID : fullName),
                session);
            if (INFO_ENABLED) {
                LOG.debug(new StringBuilder(64).append("\n\tNotified new mails in folder \"").append(fullName).append("\" in account ").append(
                    accountId).append(" for user ").append(session.getUserId()).append(" in context ").append(session.getContextId()).toString());
            }
        } catch (final PushException e) {
            LogFactory.getLog(IMAPNotifierMessageRecentListener.class).warn("Couldn't notofy about possible recent message.", e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + accountId;
        result = prime * result + session.getUserId();
        result = prime * result + session.getContextId();
        result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IMAPNotifierMessageRecentListener)) {
            return false;
        }
        final IMAPNotifierMessageRecentListener other = (IMAPNotifierMessageRecentListener) obj;
        if (accountId != other.accountId) {
            return false;
        }
        if (fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        } else if (!fullName.equals(other.fullName)) {
            return false;
        }
        if (session.getUserId() != other.session.getUserId()) {
            return false;
        }
        if (session.getContextId() != other.session.getContextId()) {
            return false;
        }
        return true;
    }

}
