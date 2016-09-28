/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
     * @return The value
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
