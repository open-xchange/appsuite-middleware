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

package com.openexchange.saml.impl;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLSessionParameters;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;

/**
 * {@link SAMLSessionStorageParameterNamesProvider} - Provides the SAML-specific session parameters.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class SAMLSessionStorageParameterNamesProvider implements SessionStorageParameterNamesProvider {

    private final List<String> parameterNames;

    /**
     * Initializes a new {@link SAMLSessionStorageParameterNamesProvider}.
     */
    public SAMLSessionStorageParameterNamesProvider() {
        super();
        List<String> parameterNames = new ArrayList<String>(10);
        parameterNames.add(SAMLSessionParameters.AUTHENTICATED);
        parameterNames.add(SAMLSessionParameters.SUBJECT_ID);
        parameterNames.add(SAMLSessionParameters.SESSION_NOT_ON_OR_AFTER);
        parameterNames.add(SAMLSessionParameters.SESSION_INDEX);
        parameterNames.add(SAMLSessionParameters.SESSION_COOKIE);
        parameterNames.add(SAMLSessionParameters.ACCESS_TOKEN);
        parameterNames.add(SAMLSessionParameters.REFRESH_TOKEN);
        parameterNames.add(SAMLSessionParameters.SAML_PATH);
        parameterNames.add(SAMLSessionParameters.SINGLE_LOGOUT);
        this.parameterNames = ImmutableList.copyOf(parameterNames);
    }

    @Override
    public List<String> getParameterNames(int userId, int contextId) throws OXException {
        return parameterNames;
    }

}
