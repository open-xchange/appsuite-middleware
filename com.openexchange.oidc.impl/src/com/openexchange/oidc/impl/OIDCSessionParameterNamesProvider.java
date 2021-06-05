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

package com.openexchange.oidc.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hazelcast.core.Hazelcast;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;

/**
 * Adds the {@link OIDCTools}.IDTOKEN to potential {@link Session} attributes.
 * This way those attribute will be synchronized throughout {@link Hazelcast} stored Sessions
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCSessionParameterNamesProvider implements SessionStorageParameterNamesProvider {

    private static final List<String> PARAMS;
    static {
        List<String> tmp = new ArrayList<>(3);
        tmp.add(OIDCTools.IDTOKEN);
        tmp.add(OIDCTools.BACKEND_PATH);
        PARAMS = Collections.unmodifiableList(tmp);
    }

    @Override
    public List<String> getParameterNames(int userId, int contextId) throws OXException {
        return PARAMS;
    }

}
