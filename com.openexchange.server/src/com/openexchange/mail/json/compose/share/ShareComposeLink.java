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

package com.openexchange.mail.json.compose.share;

/**
 * {@link ShareComposeLink}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public interface ShareComposeLink {

    /**
     * Gets the linked attachment's name.
     *
     * @return The attachment name, or <code>null</code> if unknown or not applicable
     */
    String getName();

    /**
     * Gets a link to access the linked attachment.
     *
     * @return The link
     */
    String getLink();

    /**
     * Gets the type identifier.
     * <p>
     * Please check <code>com.openexchange.share.notification.impl.NotificationType</code> enumeration for reasonable values.
     *
     * @return The type identifier
     */
    String getType();

}
