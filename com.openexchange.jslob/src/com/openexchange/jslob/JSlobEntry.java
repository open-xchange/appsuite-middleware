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

package com.openexchange.jslob;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link JSlobEntry} - A dynamically registerable JSlob entry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface JSlobEntry {

    /**
     * Gets this entry's key.
     * <pre>
     *   io.ox/files//folder/pictures
     *   ^^^^^^^^^^
     *  The key portion
     * </pre>
     *
     * @return The key
     */
    String getKey();

    /**
     * Gets this entry's path.
     * <pre>
     *   io.ox/files//folder/pictures
     *                ^^^^^^^^^^^^^^
     *                The path portion
     * </pre>
     *
     * @return The path
     */
    String getPath();

    /**
     * Signals whether this entry is writable or read-only for session-associated user.
     *
     * @param session The session providing user data
     * @return <code>true</code> if writable; otherwise read-only
     * @throws OXException If writable/read-only behavior cannot be checked
     */
    boolean isWritable(Session session) throws OXException;

    /**
     * Gets the value suitable for session-associated user.
     * <p>
     * The value can be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     *
     * @param session The session providing user data
     * @return The value
     * @throws OXException If value cannot be returned
     */
    Object getValue(Session sessiond) throws OXException;

    /**
     * Sets the value for session-associated user.
     * <p>
     * The value can be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     *
     * @param value The new value to apply
     * @param session The session providing user data
     * @throws OXException If value cannot be set
     */
    void setValue(Object value, Session sessiond) throws OXException;

    /**
     * Gets further meta-data from this entry suitable for session-associated user.
     * <p>
     * Values can be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     *
     * @param session The session providing user data
     * @return The optional meta-data
     * @throws OXException If meta-data cannot be returned
     */
    Map<String, Object> metadata(Session session) throws OXException;

}
