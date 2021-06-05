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

package com.openexchange.ajax.session;

import java.io.IOException;
import java.util.UUID;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;

public class LoginTools {

    private LoginTools() {
        super();
    }

    public static LoginResponse login(final AJAXSession session, final LoginRequest request, final String protocol, final String hostname) throws OXException, IOException, JSONException {
        return Executor.execute(session, request, protocol, hostname);
    }

    public static String generateAuthId() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }
}
