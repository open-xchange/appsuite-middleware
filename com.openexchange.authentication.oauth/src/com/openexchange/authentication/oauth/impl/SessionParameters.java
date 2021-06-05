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

package com.openexchange.authentication.oauth.impl;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;

/**
 * {@link SessionParameters}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class SessionParameters implements SessionStorageParameterNamesProvider {

    public static final String PASSWORD_GRANT_MARKER = "com.openexchange.authentication.oauth.passwordGrant";

    @Override
    public List<String> getParameterNames(int userId, int contextId) throws OXException {
        return Collections.singletonList(PASSWORD_GRANT_MARKER);
    }

}
