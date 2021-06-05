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

package com.openexchange.push.udp;

import java.util.HashMap;
import java.util.Map;

/**
 * RegisterHandler
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public final class RegisterHandler {

    private static final Map<String, RegisterObject> register = new HashMap<String, RegisterObject>();

    private RegisterHandler() {
        super();
    }

    public static void addRegisterObject(final RegisterObject registerObj) {
        final String key = getKey(registerObj.getUserId(), registerObj.getContextId());
        register.put(key, registerObj);
    }

    public static boolean isRegistered(final int userId, final int contextId) {
        final String key = getKey(userId, contextId);
        if (register.containsKey(key)) {
            final RegisterObject registerObj = register.get(key);

            if (registerObj.getTimestamp().getTime() < System.currentTimeMillis()) {
                return true;
            }
            register.remove(key);
            return false;
        }
        return false;
    }

    public static RegisterObject getRegisterObject(final int userId, final int contextId) {
        final String key = getKey(userId, contextId);
        return register.get(key);
    }

    public static int getNumberOfRegistedClients() {
        return register.size();
    }

    private static String getKey(final int userId, final int contextId) {
        return new StringBuilder().append('U').append(userId).append('C').append(contextId).toString();
    }
}
