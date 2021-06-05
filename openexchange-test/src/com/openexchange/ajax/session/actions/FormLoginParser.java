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

package com.openexchange.ajax.session.actions;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER_ID;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.framework.AbstractRedirectParser;

/**
 * Parses the redirect response of the formLogin action of the login servlet.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FormLoginParser extends AbstractRedirectParser<FormLoginResponse> {

    FormLoginParser(boolean cookiesNeeded) {
        super(cookiesNeeded);
    }

    @Override
    protected FormLoginResponse createResponse(String location) throws JSONException {
        int fragIndex = location.indexOf('#');
        if (-1 == fragIndex) {
            return new FormLoginResponse(location, null, null, -1, null, false);
        }
        String path = location.substring(0, fragIndex);
        String[] params = location.substring(fragIndex + 1).split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            int assignPos = param.indexOf('=');
            if (-1 == assignPos) {
                map.put(param, null);
            } else {
                map.put(param.substring(0, assignPos), param.substring(assignPos + 1));
            }
        }
        String userIdValue = map.get(PARAMETER_USER_ID);
        final int userId;
        if (null == userIdValue) {
            userId = -1;
        } else {
            try {
                userId = Integer.parseInt(userIdValue);
            } catch (NumberFormatException e) {
                throw new JSONException("Can not parse user_id value \"" + userIdValue + "\".", e);
            }
        }
        String booleanValue = map.get("store");
        final boolean store;
        if (null == booleanValue) {
            store = false;
        } else {
            store = Boolean.parseBoolean(booleanValue);
        }
        return new FormLoginResponse(path, map.get(PARAMETER_SESSION), map.get(PARAMETER_USER), userId, map.get("language"), store);
    }
}
