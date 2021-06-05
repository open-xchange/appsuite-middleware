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

package com.openexchange.messaging;

import com.openexchange.exception.OXException;

/**
 * {@link MultipartContent} - A multipart content.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MultipartContent extends MessagingContent {

    /**
     * Gets this multipart's sub-type; e.g <code>"mixed"</code>.
     *
     * @return The sub-type
     */
    public String getSubType();

    /**
     * Gets the number of enclosed {@link MessagingBodyPart parts}.
     *
     * @return The number of enclosed parts
     * @throws OXException If the number of enclosed parts cannot be determined
     */
    public int getCount() throws OXException;

    /**
     * Get the specified {@link MessagingBodyPart part}. Parts are numbered starting at zero.
     *
     * @param index The zero-based index
     * @return The indexed {@link MessagingBodyPart part}
     * @throws OXException If {@link MessagingBodyPart part} at index position cannot be returned
     */
    public MessagingBodyPart get(int index) throws OXException;

}
