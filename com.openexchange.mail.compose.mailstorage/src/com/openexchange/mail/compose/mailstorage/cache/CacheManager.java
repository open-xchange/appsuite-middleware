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

package com.openexchange.mail.compose.mailstorage.cache;

import java.util.UUID;
import javax.mail.internet.MimeMessage;

/**
 * Locally caches raw MIME messages for fast access without mail storage interaction between
 * subsequent operations.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public interface CacheManager {

    /**
     * Caches a physical copy of given {@link MimeMessage} to enable cheap (local) access
     * as long as composition space is actively worked on.
     *
     * @param compositionSpaceId The composition space identifier
     * @param mimeMessage The message to cache
     * @return A result object containing either the cache reference or failure reason
     */
    Result cacheMessage(UUID compositionSpaceId, MimeMessage mimeMessage);

}
