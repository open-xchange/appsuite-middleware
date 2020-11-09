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
