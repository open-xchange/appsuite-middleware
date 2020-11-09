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

package com.openexchange.mail.compose.mailstorage;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.mailstorage.association.AssociationLock;
import com.openexchange.mail.compose.mailstorage.association.AssociationLock.LockResult;
import com.openexchange.mail.compose.mailstorage.association.CompositionSpaceToDraftAssociation;
import com.openexchange.mail.compose.mailstorage.association.CompositionSpaceToDraftAssociationUpdate;
import com.openexchange.mail.compose.mailstorage.association.DraftMetadata;
import com.openexchange.mail.compose.mailstorage.storage.IMailStorage;
import com.openexchange.mail.compose.mailstorage.storage.MailStorageId;
import com.openexchange.mail.compose.mailstorage.storage.MailStorageResult;
import com.openexchange.mail.compose.mailstorage.storage.MessageInfo;
import com.openexchange.mail.compose.mailstorage.storage.MissingDraftException;
import com.openexchange.session.Session;

/**
 * {@link MailStorageExclusiveOperation} - An operation executed mutually exclusive against mail storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MailStorageExclusiveOperation {

    /**
     * A task that returns a result against mail storage and may throw an exception.
     */
    public static interface MailStorageCallable<V> {

        /**
         * Computes a result against mail storage, or throws an exception if unable to do so.
         *
         * @param lookUpResult The look-up result to pass
         * @param mailStorage The mail storage access
         * @param session The session providing user data
         * @return The computed result
         * @throws OXException If unable to compute a result
         * @throws MissingDraftException If referenced draft mail does not exist
         */
        MailStorageResult<V> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session) throws OXException, MissingDraftException;
    }

    /**
     * Performs given operation.
     *
     * @param <V> The result type
     * @param initialLookUpResult The initial look-up result
     * @param compositionSpaceService The composition space service to use
     * @param callable The operation to perform
     * @return The operation's result
     * @throws OXException If operation execution fails
     */
    public static <V> V performOperation(LookUpResult initialLookUpResult, MailStorageCompositionSpaceService compositionSpaceService, MailStorageCallable<V> callable) throws OXException {
        return new MailStorageExclusiveOperation(initialLookUpResult, compositionSpaceService).performOperation(callable);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final LookUpResult initialLookUpResult;
    private final MailStorageCompositionSpaceService compositionSpaceService;

    /**
     * Initializes a new {@link MailStorageExclusiveOperation}.
     *
     * @param lookUpResult The initial look-up result
     * @param compositionSpaceService The composition space service to use
     * @param session The session providing user data
     */
    private MailStorageExclusiveOperation(LookUpResult initialLookUpResult, MailStorageCompositionSpaceService compositionSpaceService) {
        super();
        this.initialLookUpResult = initialLookUpResult;
        this.compositionSpaceService = compositionSpaceService;
    }

    private <V> V performOperation(MailStorageCallable<V> callable) throws OXException {
        LookUpResult lookUpResult = this.initialLookUpResult;
        CompositionSpaceToDraftAssociation association = this.initialLookUpResult.getAssociation();
        UUID compositionSpaceId = association.getCompositionSpaceId();
        Session session = compositionSpaceService.getSession();

        int retryCount = 0;
        do {
            try {
                AssociationLock lock = association.getLock();
                LockResult lockResult = lock.lock();
                try {
                    if (LockResult.IMMEDIATE_ACQUISITION == lockResult) {
                        // Execute operation to assign storage result to local variable
                        MailStorageResult<V> storageResult = callable.call(lookUpResult, compositionSpaceService.getMailStorage(), session);

                        // Take over possible warnings yielded by operation execution
                        compositionSpaceService.addWarnings(storageResult.getWarnings());

                        MailStorageId newMailStorageId = storageResult.getMailStorageId();
                        if (newMailStorageId != null) {
                            if (association.isDifferentFrom(newMailStorageId)) {
                                CompositionSpaceToDraftAssociationUpdate update =
                                    new CompositionSpaceToDraftAssociationUpdate(newMailStorageId.getCompositionSpaceId())
                                    .setDraftPath(newMailStorageId.getDraftPath())
                                    .setFileCacheReference(newMailStorageId.getFileCacheReference())
                                    .setDraftMetadata(tryGetMetadataFrom(storageResult))
                                    .setValidate(false);
                                lookUpResult.getAssociationStorage().update(update);
                            } else if (association.needsValidation() && storageResult.isValidated()) {
                                // Store new association w/o "validate" flag
                                CompositionSpaceToDraftAssociationUpdate update =
                                    new CompositionSpaceToDraftAssociationUpdate(newMailStorageId.getCompositionSpaceId())
                                    .setDraftPath(newMailStorageId.getDraftPath())
                                    .setFileCacheReference(newMailStorageId.getFileCacheReference())
                                    .setDraftMetadata(tryGetMetadataFrom(storageResult))
                                    .setValidate(false);
                                lookUpResult.getAssociationStorage().update(update);
                            }
                        }

                        // Finally, return result
                        return storageResult.getResult();
                    }

                    // Lock could not be immediately acquired. Need to re-fetch association to be sure newest one is handled.
                    lookUpResult = compositionSpaceService.getCompositionSpaceToDraftAssociation(compositionSpaceId);
                    association = lookUpResult.getAssociation();
                } catch (MissingDraftException e) {
                    lookUpResult.getAssociationStorage().delete(compositionSpaceId, session, false);
                    if (lookUpResult.isFromCache()) {
                        // Cache entry might be outdated => reload & retry
                        lookUpResult = compositionSpaceService.getCompositionSpaceToDraftAssociation(compositionSpaceId);
                        association = lookUpResult.getAssociation();
                    } else {
                        throw CompositionSpaceErrorCode.CONCURRENT_UPDATE.create(e);
                    }
                } finally {
                    lock.unlock();
                }
            } catch (OXException e) {
                boolean cancel = !CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e) || !lookUpResult.isFromCache();
                if (cancel) {
                    throw e;
                }

                // Exponential back-off
                exponentialBackoffWait(++retryCount, 1000L);

                // Reload & retry
                lookUpResult = compositionSpaceService.getCompositionSpaceToDraftAssociation(compositionSpaceId);
                association = lookUpResult.getAssociation();
            }
        } while (true);
    }

    private static <V> DraftMetadata tryGetMetadataFrom(MailStorageResult<V> storageResult) {
        V result = storageResult.getResult();
        if (result instanceof MessageInfo) {
            return DraftMetadata.fromMessageInfo((MessageInfo) result);
        }
        return null;
    }

    /**
     * Performs a wait according to exponential back-off strategy.
     * <pre>
     * (retry-count * base-millis) + random-millis
     * </pre>
     *
     * @param retryCount The current number of retries
     * @param baseMillis The base milliseconds
     */
    private static void exponentialBackoffWait(int retryCount, long baseMillis) {
        long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * baseMillis) + ((long) (Math.random() * baseMillis)), TimeUnit.MILLISECONDS);
        LockSupport.parkNanos(nanosToWait);
    }

}
