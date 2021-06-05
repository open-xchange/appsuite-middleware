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

package com.openexchange.client.onboarding.plist;

import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link PListSigner} - Signs PLIST content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface PListSigner {

    /**
     * Signs specified PLIST content.
     *
     * @param toSign The PLIST content to sign
     * @param session The associated session
     * @return The signed PLIST content
     * @throws OXException If signing PLIST content fails
     */
    IFileHolder signPList(IFileHolder toSign, Session session) throws OXException;

    /**
     * Signs specified PLIST content.
     *
     * @param toSign The PLIST content to sign
     * @param userId The user id
     * @param contextId The contextId
     * @return The signed PLIST content
     * @throws OXException If signing PLIST content fails
     */
    IFileHolder signPList(IFileHolder toSign, int userId, int contextId) throws OXException;

}
