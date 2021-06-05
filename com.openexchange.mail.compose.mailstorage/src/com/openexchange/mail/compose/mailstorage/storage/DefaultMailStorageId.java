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
 * {@link DefaultMailStorageId} - The default mail storage identifier implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class DefaultMailStorageId implements MailStorageId {

    private final UUID compositionSpaceId;
    private final MailPath draftPath;
    private final Optional<CacheReference> cacheReference;
    private int hash;

    /**
     * Initializes a new {@link DefaultMailStorageId}.
     *
     * @param draftPath The path of the draft mail
     * @param compositionSpaceId The composition space identifier
     * @param cacheReference The file cache reference
     */
    public DefaultMailStorageId(MailPath draftPath, UUID compositionSpaceId, Optional<CacheReference> cacheReference) {
        super();
        this.compositionSpaceId = compositionSpaceId;
        this.draftPath = draftPath;
        this.cacheReference = cacheReference;
        hash = 0;
    }

    @Override
    public UUID getCompositionSpaceId() {
        return compositionSpaceId;
    }

    @Override
    public MailPath getDraftPath() {
        return draftPath;
    }

    @Override
    public Optional<CacheReference> getFileCacheReference() {
        return cacheReference;
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
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
        if (draftPath == null) {
            if (other.getDraftPath() != null) {
                return false;
            }
        } else if (!draftPath.equals(other.getDraftPath())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DefaultMailStorageId [compositionSpaceId=" + compositionSpaceId + ", draftPath=" + draftPath + ", cacheReference=" + cacheReference + "]";
    }

}
