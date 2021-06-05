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

package com.openexchange.authentication.application.impl;

import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;

/**
 * {@link AppPasswordSessionStorageParameterNamesProvider} Store the restrictions in session storage
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordSessionStorageParameterNamesProvider implements SessionStorageParameterNamesProvider {

    /** The parameter marking the session as having restricted capabilities, contains comma-separated string of allowed restricted scopes. */
    static final String PARAM_RESTRICTED = Session.PARAM_RESTRICTED;

    /** Holds the identifier of the app-specific password that was used for authentication */
    static final String PARAM_APP_PASSWORD_ID = "com.openexchange.authentication.application.passwordId";

    private final List<String> parameterNames;

    /**
     * Initializes a new {@link AppPasswordSessionStorageParameterNamesProvider}.
     */
    public AppPasswordSessionStorageParameterNamesProvider() {
        super();
        this.parameterNames = ImmutableList.of(PARAM_RESTRICTED, PARAM_APP_PASSWORD_ID);
    }

    @Override
    public List<String> getParameterNames(int userId, int contextId) throws OXException {
        return parameterNames;
    }

}
