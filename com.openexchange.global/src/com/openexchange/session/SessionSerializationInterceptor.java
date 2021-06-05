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

package com.openexchange.session;

/**
 * Allows arbitrary bundles to interact with a session to be serialized to the distributed session storage or when it is deserialized from
 * the storage. Beware that this operation <b>MUST</b> be fast because this blocks all concurrent operations on sessions.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.0
 */
public interface SessionSerializationInterceptor {

    /**
     * Can modify a session just before the session is put into the distributed session storage.
     * @param session session to be serialized.
     */
    void serialize(Session session);

    /**
     * Can modify a session just after the session is deserialized from the distributed session storage.
     * @param session session that was just deserialized.
     */
    void deserialize(Session session);
}
