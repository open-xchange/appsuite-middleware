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

package com.openexchange.mail.compose.mailstorage.association;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.mailstorage.cache.CacheReference;
import com.openexchange.mail.compose.mailstorage.storage.MailStorageId;
import com.openexchange.mail.compose.mailstorage.storage.ValidateAwareMailStorageId;
import com.openexchange.session.Session;

/**
 * {@link CompositionSpaceToDraftAssociation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class CompositionSpaceToDraftAssociation implements ValidateAwareMailStorageId {

    /**
     * Creates a new builder for an instance of <code>CompositionSpaceToDraftAssociation</code> w/o validation.
     *
     * @return The newly created builder
     */
    public static Builder builder() {
        return builder(false);
    }

    /**
     * Creates a new builder for an instance of <code>CompositionSpaceToDraftAssociation</code>.
     *
     * @param validate Whether the composition space is supposed to be validate on access
     * @return The newly created builder
     */
    public static Builder builder(boolean validate) {
        return new Builder(validate);
    }

    /**
     * Creates a new builder for an instance of <code>CompositionSpaceToDraftAssociation</code>.
     *
     * @param other Another instance. The returned builder is pre-filled with all data from that instance.
     * @return The newly created builder
     */
    public static Builder builder(CompositionSpaceToDraftAssociation other) {
        Builder builder = builder(other.variants.get().validate)
            .withMailStorageId(other)
            .withContextId(other.contextId)
            .withUserId(other.userId)
            .withDraftMetadata(other.variants.get().draftMetadata);

        if (other.session != null) {
            builder.withSession(other.session);
        }

        return builder;
    }

    /** A builder for an instance of <code>CompositionSpaceToDraftAssociation</code>. */
    public static class Builder {

        private final boolean validate;
        private UUID compositionSpaceId;
        private int contextId;
        private int userId;
        private MailPath draftPath;
        private DraftMetadata draftMetadata;
        private Session session;
        private Optional<CacheReference> cacheReference = Optional.empty();

        /**
         * Initializes a new {@link Builder}.
         */
        Builder(boolean validate) {
            super();
            this.validate = validate;
        }

        /**
         * Sets the context identifier.
         *
         * @param contextId The context identifier to set
         * @return This builder
         */
        public Builder withContextId(int contextId) {
            this.contextId = contextId;
            return this;
        }

        /**
         * Sets the user identifier.
         *
         * @param userId The user identifier to set
         * @return This builder
         */
        public Builder withUserId(int userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the session
         *
         * @param session The session to set
         * @return This builder
         */
        public Builder withSession(Session session) {
            this.session = session;
            this.userId = session.getUserId();
            this.contextId = session.getContextId();
            return this;
        }

        /**
         * Sets the draft metadata
         *
         * @param The draft metadata
         * @return This builder
         */
        public Builder withDraftMetadata(DraftMetadata draftMetadata) {
            this.draftMetadata = draftMetadata;
            return this;
        }

        public Builder withMailStorageId(MailStorageId mailStorageId) {
            this.compositionSpaceId = mailStorageId.getCompositionSpaceId();
            this.draftPath = mailStorageId.getDraftPath();
            this.cacheReference = mailStorageId.getFileCacheReference();
            return this;
        }

        /**
         * Creates the <code>CompositionSpaceToDraftAssociation</code> instance from this builder's arguments.
         *
         * @return The <code>CompositionSpaceToDraftAssociation</code> instance
         */
        public CompositionSpaceToDraftAssociation build() {
            Objects.requireNonNull(compositionSpaceId, "compositionSpaceId must be set!");
            Objects.requireNonNull(session, "session must be set!");
            Objects.requireNonNull(draftPath, "draftPath must be set!");
            Objects.requireNonNull(cacheReference, "cacheReference must be set!");
            return new CompositionSpaceToDraftAssociation(compositionSpaceId, contextId, userId, draftPath, draftMetadata, cacheReference, validate, session);
        }

    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class AssociationVariants {

        final MailPath draftPath;
        final Optional<CacheReference> fileCacheReference;
        final DraftMetadata draftMetadata;
        final boolean validate;
        final boolean invalid;

        /**
         * Initializes a new {@link AssociationVariants}.
         */
        AssociationVariants(MailPath draftPath, Optional<CacheReference> fileCacheReference, DraftMetadata draftMetadata, boolean validate, boolean invalid) {
            super();
            this.draftPath = draftPath;
            this.fileCacheReference = fileCacheReference;
            this.draftMetadata = draftMetadata;
            this.validate = validate;
            this.invalid = invalid;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final UUID compositionSpaceId;
    private final int contextId;
    private final int userId;
    private final Session session;
    private final AssociationLock lock;
    private final AtomicReference<AssociationVariants> variants;
    private int hash;

    /**
     * Initializes a new {@link CompositionSpaceToDraftAssociation}.
     */
    CompositionSpaceToDraftAssociation(UUID compositionSpaceId, int contextId, int userId, MailPath draftPath, DraftMetadata draftMetadata, Optional<CacheReference> cacheReference, boolean validate, Session session) {
        super();
        this.compositionSpaceId = compositionSpaceId;
        this.contextId = contextId;
        this.userId = userId;
        this.variants = new AtomicReference<>(new AssociationVariants(draftPath, cacheReference, draftMetadata, validate, false));
        this.session = session;
        lock = new AssociationLock();
        hash = 0;
    }

    /**
     * Updates the variants.
     *
     * @param associationUpdate The update
     */
    void updateVariants(CompositionSpaceToDraftAssociationUpdate associationUpdate) {
        AssociationVariants prev;
        AssociationVariants newVariants;
        do {
            prev = variants.get();
            MailPath draftPath = associationUpdate.containsDraftPath() ? associationUpdate.getDraftPath() : prev.draftPath;
            Optional<CacheReference> fileCacheReference = associationUpdate.containsFileCacheReference() ? associationUpdate.getFileCacheReference() : prev.fileCacheReference;
            DraftMetadata draftMetadata = associationUpdate.containsDraftMetadata() ? associationUpdate.getDraftMetadata() : prev.draftMetadata;
            boolean validate = associationUpdate.containsValidate() ? associationUpdate.isValidate() : prev.validate;
            newVariants = new AssociationVariants(draftPath, fileCacheReference, draftMetadata, validate, prev.invalid);
        } while (!variants.compareAndSet(prev, newVariants));
    }

    /**
     * Invalidates this association.
     */
    void invalidate() {
        AssociationVariants prev;
        AssociationVariants newVariants;
        do {
            prev = variants.get();
            MailPath draftPath = prev.draftPath;
            Optional<CacheReference> fileCacheReference = prev.fileCacheReference;
            DraftMetadata draftMetadata = prev.draftMetadata;
            boolean validate = prev.validate;
            newVariants = new AssociationVariants(draftPath, fileCacheReference, draftMetadata, validate, true);
        } while (!variants.compareAndSet(prev, newVariants));
    }

    /**
     * Gets the lock.
     *
     * @return The lock
     */
    public AssociationLock getLock() {
        return lock;
    }

    @Override
    public UUID getCompositionSpaceId() {
        return compositionSpaceId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    @Override
    public MailPath getDraftPath() {
        return variants.get().draftPath;
    }

    @Override
    public Optional<CacheReference> getFileCacheReference() {
        return variants.get().fileCacheReference;
    }

    /**
     * Gets the draft metadata
     *
     * @return The draft metadata
     */
    public Optional<DraftMetadata> getOptionalDraftMetadata() {
        return Optional.ofNullable(variants.get().draftMetadata);
    }

    @Override
    public boolean needsValidation() {
        return variants.get().validate;
    }

    /**
     * Checks if this association is marked as invalid (associated composition space has been closed).
     *
     * @return <code>true</code> if invalid; otherwise <code>false</code>
     */
    public boolean isInvalid() {
        return variants.get().invalid;
    }

    /**
     * Gets the associated session.
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Checks if this association appears to be different from given mail storage identifier.
     * <p>
     * That is if either:
     * <ul>
     * <li>Different composition space identifier</li>
     * <li>Different draft path</li>
     * <li>Different file cache reference</li>
     * </ul>
     *
     * @param mailStorageId The mail storage identifier to compare against
     * @return <code>true</code> if different; otherwise <code>false</code>
     */
    public boolean isDifferentFrom(MailStorageId mailStorageId) {
        if (!compositionSpaceId.equals(mailStorageId.getCompositionSpaceId())) {
            return true;
        }

        if (!getDraftPath().equals(mailStorageId.getDraftPath())) {
            return true;
        }

        if (!getFileCacheReference().equals(mailStorageId.getFileCacheReference())) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            MailPath draftPath = getDraftPath();
            int prime = 31;
            result = prime * 1 + ((compositionSpaceId == null) ? 0 : compositionSpaceId.hashCode());
            result = prime * result + ((draftPath == null) ? 0 : draftPath.hashCode());
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MailStorageId)) {
            return false;
        }
        MailStorageId other = (MailStorageId) obj;
        if (compositionSpaceId == null) {
            if (other.getCompositionSpaceId() != null) {
                return false;
            }
        } else if (!compositionSpaceId.equals(other.getCompositionSpaceId())) {
            return false;
        }
        if (getDraftPath() == null) {
            if (other.getDraftPath() != null) {
                return false;
            }
        } else if (!getDraftPath().equals(other.getDraftPath())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CompositionSpaceToDraftAssociation [compositionSpaceId=" + compositionSpaceId + ", draftPath=" + getDraftPath() + ", cacheReference=" + getFileCacheReference() + "]";
    }

}
