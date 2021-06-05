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

package com.openexchange.secret.recovery;

import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SecretConsistencyCheck}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface SecretConsistencyCheck {

    /**
     * Find out if a secret is valid for decrypting all data
     *
     * @param session The session
     * @param secret The secret
     * @return <code>null</code> if everything could be decrypted, if not return a pointer to something that could not be decrypted (for
     *         debugging)
     * @throws OXException If an error occurs
     */
    public String checkSecretCanDecryptStrings(ServerSession session, String secret) throws OXException;
}
