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

package com.openexchange.imap.notify.internal;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.mail.event.MessageRecentEvent;
import javax.mail.event.MessageRecentListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.push.PushUtility;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;

/**
 * {@link IMAPNotifierMessageRecentListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPNotifierMessageRecentListener implements MessageRecentListener {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(IMAPNotifierMessageRecentListener.class);

    /**
     * Checks validity of a specified IMAP folder's full name
     */
    private static interface FullNameChecker {

        /**
         * Checks validity of specified IMAP folder's full name
         *
         * @param fullName The full name
         * @return <code>true</code> if valid; otherwise <code>false</code>
         */
        boolean check(String fullName);
    }

    private static volatile FullNameChecker fullNameChecker;

    /**
     * Gets the full name checker.
     *
     * @return The full name checker
     */
    private static FullNameChecker getFullNameChecker() {
        if (null == fullNameChecker) {
            synchronized (LOG) {
                if (null == fullNameChecker) {
                    final String notifierFullNames = IMAPProperties.getInstance().getNotifyFullNames();
                    if (com.openexchange.java.Strings.isEmpty(notifierFullNames)) {
                        fullNameChecker = new FullNameChecker() {

                            @Override
                            public boolean check(final String fullName) {
                                return false;
                            }
                        };
                    } else {
                        final String[] fullNames = Pattern.compile(" *, *").split(notifierFullNames);
                        final int length = fullNames.length;
                        if (1 == length) {
                            final String fn = BASE64MailboxEncoder.encode(fullNames[0]).toUpperCase(Locale.US);
                            fullNameChecker = new FullNameChecker() {

                                @Override
                                public boolean check(final String fullName) {
                                    if (null == fullName) {
                                        return false;
                                    }
                                    return fn.equals(BASE64MailboxEncoder.encode(fullName).toUpperCase(Locale.US));
                                }
                            };
                        } else {
                            final Set<String> set = new HashSet<String>(length);
                            for (final String fn : fullNames) {
                                set.add(BASE64MailboxEncoder.encode(fn).toUpperCase(Locale.US));
                            }
                            fullNameChecker = new FullNameChecker() {

                                @Override
                                public boolean check(final String fullName) {
                                    if (null == fullName) {
                                        return false;
                                    }
                                    return set.contains(BASE64MailboxEncoder.encode(fullName).toUpperCase(Locale.US));
                                }
                            };
                        }
                    }
                }
            }
        }
        return fullNameChecker;
    }

    /**
     * Drops full name checker instance.
     */
    public static void dropFullNameChecker() {
        if (null != fullNameChecker) {
            synchronized (LOG) {
                if (null != fullNameChecker) {
                    fullNameChecker = null;
                }
            }
        }
    }

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(final ConfigurationService configService) {
                dropFullNameChecker();
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.imap.notifyFullNames");
            }
        });
    }

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
        addNotifierFor(imapFolder, optFullName, accountId, session, false);
    }

    /**
     * Adds the recent-notifier for specified IMAP folder if allowed to.
     *
     * @param imapFolder The IMAP folder
     * @param optFullName The optional full name (if <code>null</code> gets from {@link IMAPFolder#getFullName()})
     * @param accountId The account identifier
     * @param session The session
     * @param knownGranted <code>true</code> to indicate known granted recent-notifier; otherwise <code>false</code>
     */
    public static void addNotifierFor(final IMAPFolder imapFolder, final String optFullName, final int accountId, final Session session, final boolean knownGranted) {
        if (knownGranted || (MailAccount.DEFAULT_ID == accountId && IMAPProperties.getInstance().notifyRecent())) {
            final String fullName = optFullName == null ? imapFolder.getFullName() : optFullName;
            if (getFullNameChecker().check(fullName)) {
                imapFolder.addMessageRecentListener(new IMAPNotifierMessageRecentListener(fullName, accountId, session));
            }
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

    @Override
    public void recentAvailable(final MessageRecentEvent event) {
        try {
            PushUtility.triggerOSGiEvent(MailFolderUtility.prepareFullname(accountId, fullName.length() == 0 ? MailFolder.DEFAULT_FOLDER_ID : fullName), session);
            LOG.info("\n\tNotified new mails in folder \"{}\" in account {} for user {} in context {}", fullName, accountId, session.getUserId(), session.getContextId());
        } catch (final OXException e) {
            org.slf4j.LoggerFactory.getLogger(IMAPNotifierMessageRecentListener.class).warn("Couldn't notify about possible recent message.", e);
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
