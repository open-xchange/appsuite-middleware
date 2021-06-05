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

package com.openexchange.mail.compose.mailstorage.association;

import java.util.Optional;
import java.util.UUID;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.mailstorage.cache.CacheReference;

/**
 * {@link CompositionSpaceToDraftAssociationUpdate} - Describes fields for a {@link CompositionSpaceToDraftAssociation} instance that can be
 * updated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class CompositionSpaceToDraftAssociationUpdate {

    private final UUID compositionSpaceId;

    private MailPath draftPath;
    private boolean b_draftPath;

    private Optional<CacheReference> fileCacheReference;
    private boolean b_fileCacheReference;

    private DraftMetadata draftMetadata;
    private boolean b_draftMetadata;

    private boolean validate;
    private boolean b_validate;

    /**
     * Initializes a new {@link CompositionSpaceToDraftAssociationUpdate}.
     *
     * @param compositionSpaceId The composition space identifier
     */
    public CompositionSpaceToDraftAssociationUpdate(UUID compositionSpaceId) {
        super();
        this.compositionSpaceId = compositionSpaceId;
    }

    /**
     * Gets the composition space identifier.
     *
     * @return The composition space identifier
     */
    public UUID getCompositionSpaceId() {
        return compositionSpaceId;
    }

    /**
     * Gets the draft path.
     *
     * @return The draft path
     */
    public MailPath getDraftPath() {
        return draftPath;
    }

    /**
     * Checks if this instance has draft path set.
     *
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean containsDraftPath() {
        return b_draftPath;
    }

    /**
     * Sets the draft path.
     *
     * @param draftPath The draft path to set
     * @return This instance with new argument applied
     */
    public CompositionSpaceToDraftAssociationUpdate setDraftPath(MailPath draftPath) {
        this.draftPath = draftPath;
        b_draftPath = true;
        return this;
    }

    /**
     * Gets the file cache reference.
     *
     * @return The cache reference
     */
    public Optional<CacheReference> getFileCacheReference() {
        return fileCacheReference;
    }

    /**
     * Checks if this instance has file cache reference set.
     *
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean containsFileCacheReference() {
        return b_fileCacheReference;
    }

    /**
     * Sets the file cache reference.
     *
     * @param fileCacheReference The file cache reference to set
     * @return This instance with new argument applied
     */
    public CompositionSpaceToDraftAssociationUpdate setFileCacheReference(Optional<CacheReference> fileCacheReference) {
        this.fileCacheReference = fileCacheReference;
        b_fileCacheReference = true;
        return this;
    }

    /**
     * Gets the draft meta-data.
     *
     * @return The draft meta-data
     */
    public DraftMetadata getDraftMetadata() {
        return draftMetadata;
    }

    /**
     * Checks if this instance has draft meta-data set.
     *
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean containsDraftMetadata() {
        return b_draftMetadata;
    }

    /**
     * Sets the draft meta-data.
     *
     * @param draftMetadata The draft meta-data to set
     * @return This instance with new argument applied
     */
    public CompositionSpaceToDraftAssociationUpdate setDraftMetadata(DraftMetadata draftMetadata) {
        this.draftMetadata = draftMetadata;
        b_draftMetadata = true;
        return this;
    }

    /**
     * Gets the validate flag.
     *
     * @return The validate flag
     */
    public boolean isValidate() {
        return validate;
    }

    /**
     * Checks if this instance has validate flag set.
     *
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean containsValidate() {
        return b_validate;
    }

    /**
     * Sets the validate flag.
     *
     * @param validate The validate flag to set
     * @return This instance with new argument applied
     */
    public CompositionSpaceToDraftAssociationUpdate setValidate(boolean validate) {
        this.validate = validate;
        b_validate = true;
        return this;
    }

}
