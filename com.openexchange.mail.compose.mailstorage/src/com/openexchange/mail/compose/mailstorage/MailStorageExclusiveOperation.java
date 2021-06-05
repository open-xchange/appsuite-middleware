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

package com.openexchange.mail.compose.mailstorage;

import static com.openexchange.java.util.UUIDs.getUnformattedString;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.ClientToken;
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

    private static final Logger LOG = LoggerFactory.getLogger(MailStorageExclusiveOperation.class);

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
         * @param clientToken The current client token to check against for preventing concurrent modifications
         * @return The computed result
         * @throws OXException If unable to compute a result
         * @throws MissingDraftException If referenced draft mail does not exist
         */
        MailStorageResult<V> call(LookUpResult lookUpResult, IMailStorage mailStorage, Session session, ClientToken clientToken) throws OXException, MissingDraftException;
    }

    /**
     * Performs given operation.
     *
     * @param <V> The result type
     * @param initialLookUpResult The initial look-up result
     * @param compositionSpaceService The composition space service to use
     * @param callable The operation to perform
     * @param clientToken The current client token to check against for preventing concurrent modifications
     * @return The operation's result
     * @throws OXException If operation execution fails
     */
    public static <V> V performOperation(LookUpResult initialLookUpResult, MailStorageCompositionSpaceService compositionSpaceService, MailStorageCallable<V> callable, ClientToken clientToken) throws OXException {
        return new MailStorageExclusiveOperation(initialLookUpResult, compositionSpaceService, clientToken).performOperation(callable);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final LookUpResult initialLookUpResult;
    private final MailStorageCompositionSpaceService compositionSpaceService;
    private final ClientToken clientToken;

    /**
     * Initializes a new {@link MailStorageExclusiveOperation}.
     *
     * @param lookUpResult The initial look-up result
     * @param compositionSpaceService The composition space service to use
     * @param clientToken The current client token to check against for preventing concurrent modifications
     */
    private MailStorageExclusiveOperation(LookUpResult initialLookUpResult, MailStorageCompositionSpaceService compositionSpaceService, ClientToken clientToken) {
        super();
        this.initialLookUpResult = initialLookUpResult;
        this.compositionSpaceService = compositionSpaceService;
        this.clientToken = clientToken;
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
                        // Check validity
                        if (association.isInvalid()) {
                            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
                        }

                        // Execute operation to assign storage result to local variable
                        MailStorageResult<V> storageResult = callable.call(lookUpResult, compositionSpaceService.getMailStorage(), session, clientToken);

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
                    lookUpResult = compositionSpaceService.requireCompositionSpaceToDraftAssociation(compositionSpaceId);
                    association = lookUpResult.getAssociation();
                } catch (MissingDraftException e) {
                    lookUpResult.getAssociationStorage().delete(compositionSpaceId, false);
                    if (lookUpResult.isFromCache()) {
                        // Cache entry might be outdated => reload & retry
                        LOG.warn("Draft mail is missing for association: {}. Reloading.", association, e);
                        lookUpResult = compositionSpaceService.requireCompositionSpaceToDraftAssociation(compositionSpaceId);
                        association = lookUpResult.getAssociation();
                        LOG.debug("Found new association on reload: {}", association);
                    } else {
                        throw CompositionSpaceErrorCode.CONCURRENT_UPDATE.create(e);
                    }
                } finally {
                    lock.unlock();
                }
            } catch (OXException e) {
                throw e;
                /*
                boolean cancel = !CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e) || !lookUpResult.isFromCache();
                if (cancel) {
                    throw e;
                }

                // Exponential back-off
                exponentialBackoffWait(++retryCount, 1000L);

                // Reload & retry
                lookUpResult = compositionSpaceService.requireCompositionSpaceToDraftAssociation(compositionSpaceId);
                association = lookUpResult.getAssociation();
                */
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
