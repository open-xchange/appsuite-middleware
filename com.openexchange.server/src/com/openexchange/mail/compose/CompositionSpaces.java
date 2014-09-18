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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.compose;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;


/**
 * {@link CompositionSpaces} - Utility class for composition spaces.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CompositionSpaces {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CompositionSpaces.class);

    /**
     * Initializes a new {@link CompositionSpaces}.
     */
    private CompositionSpaces() {
        super();
    }

    /**
     * Destroys the denoted composition space.
     *
     * @param csid The composition space identifier
     * @param mailAccess The associated mail access instance
     */
    public static void destroy(String csid, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        CompositionSpaceRegistry registry = CompositionSpace.getRegistry(mailAccess.getSession());
        CompositionSpace space = registry.removeCompositionSpace(csid);
        if (null != space) {

            // Delete clean-ups
            Queue<MailPath> cleanUps = space.getCleanUps();
            for (MailPath mailPath; (mailPath = cleanUps.poll()) != null;) {
                try {
                    if (mailPath.getAccountId() == mailAccess.getAccountId()) {
                        mailAccess.getMessageStorage().deleteMessages(mailPath.getFolder(), new String[] { mailPath.getMailID() }, true);
                    } else {
                        LOGGER.warn("Account identifier mismatch. Clean-up for {}, but access for {} ({})", mailPath.getAccountId(), mailAccess.getAccountId(), mailAccess);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to delete {}", mailPath, e);
                }
            }
        }
    }

    /**
     * Destroys the denoted composition space.
     *
     * @param csid The composition space identifier
     * @param session The session
     */
    public static void destroy(String csid, Session session) {
        CompositionSpaceRegistry registry = CompositionSpace.getRegistry(session);
        CompositionSpace space = registry.removeCompositionSpace(csid);
        if (null != space) {
            Map<Integer, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> accesses = new ConcurrentHashMap<Integer, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>(4);
            try {

                // Delete clean-ups
                Queue<MailPath> cleanUps = space.getCleanUps();
                for (MailPath mailPath; (mailPath = cleanUps.poll()) != null;) {
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = accesses.get(Integer.valueOf(mailPath.getAccountId()));
                    if (null == mailAccess) {
                        try {
                            mailAccess = MailAccess.getNewInstance(session, mailPath.getAccountId());
                            mailAccess.connect(false);
                            accesses.put(Integer.valueOf(mailPath.getAccountId()), mailAccess);
                        } catch (OXException e) {
                            LOGGER.warn("Could not obtain access for {}", mailPath.getAccountId(), e);
                        }
                    }

                    if (null != mailAccess) {
                        try {
                            mailAccess.getMessageStorage().deleteMessages(mailPath.getFolder(), new String[] { mailPath.getMailID() }, true);
                        } catch (Exception e) {
                            LOGGER.warn("Failed to delete {}", mailPath, e);
                        }
                    }
                }
            } finally {
                for (MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access : accesses.values()) {
                    access.close(false);
                }
            }
        }
    }

    /**
     * Destroys the given session's composition spaces.
     *
     * @param session The session
     */
    public static void destroyFor(Session session) {
        Map<Integer, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> accesses = new ConcurrentHashMap<Integer, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>(4);
        try {
            for (CompositionSpace space : CompositionSpace.getRegistry(session).removeAllCompositionSpaces()) {

                // Delete clean-ups
                Queue<MailPath> cleanUps = space.getCleanUps();
                for (MailPath mailPath; (mailPath = cleanUps.poll()) != null;) {
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access = accesses.get(Integer.valueOf(mailPath.getAccountId()));
                    if (null == access) {
                        try {
                            access = MailAccess.getNewInstance(session, mailPath.getAccountId());
                            access.connect(false);
                            accesses.put(Integer.valueOf(mailPath.getAccountId()), access);
                        } catch (Exception e) {
                            LOGGER.warn("Could not obtain access for {}", mailPath.getAccountId(), e);
                        }
                    }

                    if (null != access) {
                        try {
                            access.getMessageStorage().deleteMessages(mailPath.getFolder(), new String[] { mailPath.getMailID() }, true);
                        } catch (Exception e) {
                            LOGGER.warn("Failed to delete {}", mailPath, e);
                        }
                    }
                }
            }
        } finally {
            for (MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access : accesses.values()) {
                access.close(false);
            }
        }
    }

    /**
     * Destroys the given composition space registry.
     *
     * @param registry The composition space registry
     * @param session The associated session
     */
    static void destroy(CompositionSpaceRegistry registry, Session session) {
        Map<Integer, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> accesses = new ConcurrentHashMap<Integer, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>(4);
        try {
            for (CompositionSpace space : registry.removeAllCompositionSpaces()) {

                // Delete clean-ups
                Queue<MailPath> cleanUps = space.getCleanUps();
                for (MailPath mailPath; (mailPath = cleanUps.poll()) != null;) {
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access = accesses.get(Integer.valueOf(mailPath.getAccountId()));
                    if (null == access) {
                        try {
                            access = MailAccess.getNewInstance(session, mailPath.getAccountId());
                            access.connect(false);
                            accesses.put(Integer.valueOf(mailPath.getAccountId()), access);
                        } catch (Exception e) {
                            LOGGER.warn("Could not obtain access for {}", mailPath.getAccountId(), e);
                        }
                    }

                    if (null != access) {
                        try {
                            access.getMessageStorage().deleteMessages(mailPath.getFolder(), new String[] { mailPath.getMailID() }, true);
                        } catch (Exception e) {
                            LOGGER.warn("Failed to delete {}", mailPath, e);
                        }
                    }
                }
            }
        } finally {
            for (MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access : accesses.values()) {
                access.close(false);
            }
        }
    }

}
