/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.compose.old;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mailaccount.UnifiedInboxUID;
import com.openexchange.session.Session;


/**
 * {@link OldCompositionSpaces} - Utility class for composition spaces.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OldCompositionSpaces {

    /** The logger constant */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OldCompositionSpaces.class);

    /**
     * Initializes a new {@link OldCompositionSpaces}.
     */
    private OldCompositionSpaces() {
        super();
    }

    /**
     * Gets the denoted composition space.
     *
     * @param csid The composition space identifier
     * @param session The session
     * @return The composition space or <code>null</code>
     */
    public static OldCompositionSpace get(String csid, Session session) {
        OldCompositionSpaceRegistry registry = OldCompositionSpace.getRegistry(session);
        return registry.optCompositionSpace(csid);
    }

    /**
     * Destroys the denoted composition space.
     * <ul>
     * <li>Drops messages held in composition space's clean-ups</li>
     * <li>Cleanses the composition space from registry</li>
     * </ul>
     *
     * @param csid The composition space identifier
     * @param session The session
     */
    public static void destroy(String csid, Session session) {
        OldCompositionSpaceRegistry registry = OldCompositionSpace.getRegistry(session);
        OldCompositionSpace space = registry.removeCompositionSpace(csid);
        if (null != space) {
            Map<Integer, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> accesses = new ConcurrentHashMap<Integer, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>(4, 0.9f, 1);
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
     * <p>
     * For each composition space:
     * <ul>
     * <li>Drops messages held in composition space's clean-ups</li>
     * <li>Cleanses the composition space from registry</li>
     * </ul>
     *
     * @param session The session
     */
    public static void destroyFor(Session session) {
        Map<Integer, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> accesses = new ConcurrentHashMap<Integer, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>(4, 0.9f, 1);
        try {
            for (OldCompositionSpace space : OldCompositionSpace.getRegistry(session).removeAllCompositionSpaces()) {

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
     * <p>
     * For each composition space:
     * <ul>
     * <li>Drops messages held in composition space's clean-ups</li>
     * <li>Cleanses the composition space from registry</li>
     * </ul>
     *
     * @param registry The composition space registry
     * @param session The associated session
     */
    static void destroy(OldCompositionSpaceRegistry registry, Session session) {
        Map<Integer, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> accesses = new ConcurrentHashMap<Integer, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>(4, 0.9f, 1);
        try {
            for (OldCompositionSpace space : registry.removeAllCompositionSpaces()) {

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

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Applies denoted composition space's state
     *
     * @param csid The composition space identifier
     * @param session The associated session
     * @throws OXException If operation fails
     */
    public static void applyCompositionSpace(String csid, Session session) throws OXException {
        applyCompositionSpace(csid, session, null, false);
    }

    /**
     * Applies denoted composition space's state
     *
     * @param csid The composition space identifier
     * @param session The associated session
     * @param optMailAccess The optional pre-initialized mail access
     * @param updateMailFlags <code>true</code> if the messages flags should be updated; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public static void applyCompositionSpace(String csid, Session session, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> optMailAccess, boolean updateMailFlags) throws OXException {
        OldCompositionSpace space = OldCompositionSpace.optCompositionSpace(csid, session);
        if (null == space) {
            return;
        }

        /*-
         *
        int unifiedMailId = -1;
        {
            UnifiedInboxManagement uim = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
            if (null != uim) {
                unifiedMailId = uim.getUnifiedINBOXAccountID(session);
            }
        }
         */

        Map<Integer, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> accesses = new ConcurrentHashMap<Integer, MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage>>(4, 0.9f, 1);
        try {
            {
                final MailPath replyFor = space.getReplyFor();
                if (null != replyFor && updateMailFlags) {
                    if (null != optMailAccess && replyFor.getAccountId() == optMailAccess.getAccountId()) {
                        new SafeAction<Void>() {

                            @Override
                            Void doPerform(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws Exception {
                                mailAccess.getMessageStorage().updateMessageFlags(replyFor.getFolder(), new String[] { replyFor.getMailID() }, MailMessage.FLAG_ANSWERED, true);
                                return null;
                            }
                        }.performSafe(optMailAccess);
                    } else {
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access = accesses.get(Integer.valueOf(replyFor.getAccountId()));
                        if (null == access) {
                            access = MailAccess.getNewInstance(session, replyFor.getAccountId());
                            access.connect(false);
                            accesses.put(Integer.valueOf(replyFor.getAccountId()), access);
                        }
                        new SafeAction<Void>() {

                            @Override
                            Void doPerform(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws Exception {
                                mailAccess.getMessageStorage().updateMessageFlags(replyFor.getFolder(), new String[] { replyFor.getMailID() }, MailMessage.FLAG_ANSWERED, true);
                                return null;
                            }
                        }.performSafe(access);
                    }
                }
            }

            {
                Queue<MailPath> forwardsFor = space.getForwardsFor();
                if ((null != forwardsFor && !forwardsFor.isEmpty()) && updateMailFlags) {
                    for (MailPath mailPath : forwardsFor) {
                        if (null != optMailAccess && mailPath.getAccountId() == optMailAccess.getAccountId()) {
                            new SafeAction<Void>() {

                                @Override
                                Void doPerform(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws Exception {
                                    mailAccess.getMessageStorage().updateMessageFlags(mailPath.getFolder(), new String[] { mailPath.getMailID() }, MailMessage.FLAG_FORWARDED, true);
                                    return null;
                                }
                            }.performSafe(optMailAccess);
                        } else {
                            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access = accesses.get(Integer.valueOf(mailPath.getAccountId()));
                            if (null == access) {
                                access = MailAccess.getNewInstance(session, mailPath.getAccountId());
                                access.connect(false);
                                accesses.put(Integer.valueOf(mailPath.getAccountId()), access);
                            }
                            new SafeAction<Void>() {

                                @Override
                                Void doPerform(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws Exception {
                                    mailAccess.getMessageStorage().updateMessageFlags(mailPath.getFolder(), new String[] { mailPath.getMailID() }, MailMessage.FLAG_FORWARDED, true);
                                    return null;
                                }
                            }.performSafe(access);
                        }
                    }
                }
            }

            {
                Queue<MailPath> draftEditsFor = space.getDraftEditsFor();
                if (null != draftEditsFor && !draftEditsFor.isEmpty()) {
                    for (MailPath mailPath : draftEditsFor) {
                        // Only delete draft-edit if not already referenced by either replyFor or forwardsFor.
                        if (!space.isMarkedAsReplyOrForward(mailPath)) {
                            if (null != optMailAccess && mailPath.getAccountId() == optMailAccess.getAccountId()) {
                                new SafeAction<Void>() {

                                    @Override
                                    Void doPerform(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws Exception {
                                        mailAccess.getMessageStorage().deleteMessages(mailPath.getFolder(), new String[] { mailPath.getMailID() }, true);
                                        return null;
                                    }
                                }.performSafe(optMailAccess);
                            } else {
                                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access = accesses.get(Integer.valueOf(mailPath.getAccountId()));
                                if (null == access) {
                                    access = MailAccess.getNewInstance(session, mailPath.getAccountId());
                                    access.connect(false);
                                    accesses.put(Integer.valueOf(mailPath.getAccountId()), access);
                                }
                                new SafeAction<Void>() {

                                    @Override
                                    Void doPerform(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws Exception {
                                        mailAccess.getMessageStorage().deleteMessages(mailPath.getFolder(), new String[] { mailPath.getMailID() }, true);
                                        return null;
                                    }
                                }.performSafe(access);
                            }
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
     * Extracts possible bested mail path
     *
     * @param mailPath The mail path to examine
     * @param unifiedMailId The unified mail account identifier
     * @return The extracted path or <code>null</code>
     */
    public static MailPath optUnifiedInboxUID(MailPath mailPath, int unifiedMailId) {
        if (unifiedMailId <= 0 || null == mailPath || mailPath.getAccountId() != unifiedMailId) {
            return null;
        }

        return UnifiedInboxUID.extractPossibleNestedMailPath(mailPath.getStr());
    }

    // ----------------------------------------------------------------------------------------------------------------------------- //

    private static abstract class SafeAction<V> {

        SafeAction() {
            super();
        }

        V performSafe(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
            try {
                return doPerform(mailAccess);
            } catch (Exception e) {
                // Ignore
                LOGGER.warn("Failed to perform action.", e);
                return null;
            }
        }

        abstract V doPerform(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws Exception;
    }

}
