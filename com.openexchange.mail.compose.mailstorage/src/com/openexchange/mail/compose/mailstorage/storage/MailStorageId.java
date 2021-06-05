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

package com.openexchange.mail.compose.mailstorage.storage;

import java.util.Optional;
import java.util.UUID;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.mailstorage.cache.CacheReference;

/**
 * {@link MailStorageId} - Basically a pair of composition space identifier and draft mail path with a session reference.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public interface MailStorageId {

    /**
     * Gets the composition space identifier
     *
     * @return The composition space identifier
     */
    UUID getCompositionSpaceId();

    /**
     * Gets the draft path
     *
     * @return The draft path
     */
    MailPath getDraftPath();

    /**
     * Gets a reference to the currently locally cached draft message
     *
     * @return The reference or empty
     */
    Optional<CacheReference> getFileCacheReference();

    /**
     * Gets the folder identifier.
     * <p>
     * Convenience method that performs:<br>
     * <pre>
     * <code>getDraftPath().getFolder()</code>
     * </pre>
     *
     * @return The folder identifier
     */
    default String getFolderId() {
        return getDraftPath().getFolder();
    }

    /**
     * Gets the mail identifier.
     * <p>
     * Convenience method that performs:<br>
     * <pre>
     * <code>getDraftPath().getMailID()</code>
     * </pre>
     *
     * @return The mail identifier
     */
    default String getMailId() {
        return getDraftPath().getMailID();
    }

    /**
     * Gets the identifier for the mail account.
     * <p>
     * Convenience method that performs:<br>
     * <pre>
     * <code>getDraftPath().getAccountId()</code>.
     * </pre>
     *
     * @return The mail account identifier
     */
    default int getAccountId() {
        return getDraftPath().getAccountId();
    }

    /**
     * Checks if this instance holds a file cache reference.
     * <p>
     * Convenience method that performs:<br>
     * <pre>
     * <code>getFileCacheReference().isPresent()</code>
     * </pre>
     *
     * @return <code>true</code> if a file cache reference is present; otherwise <code>false</code>
     */
    default boolean hasFileCacheReference() {
        return getFileCacheReference().isPresent();
    }

    /**
     * Checks if this instance holds a file cache reference AND that {@link CacheReference#isValid() the reference is valid}.
     * <p>
     * Convenience method that basically performs:<br>
     * <pre>
     * <code>getFileCacheReference().isPresent() && getFileCacheReference().get().isValid()</code>
     * </pre>
     *
     * @return <code>true</code> if a valid file cache reference is present; otherwise <code>false</code>
     */
    default boolean hasValidFileCacheReference() {
        return getFileCacheReference().map(CacheReference::isValid).orElse(Boolean.FALSE).booleanValue();
    }

}
