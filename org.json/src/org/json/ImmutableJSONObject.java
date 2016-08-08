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

package org.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ImmutableJSONObject} - An immutable {@link JSONObject}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ImmutableJSONObject extends JSONObject {

    private static final long serialVersionUID = 7348084518800542046L;

    /**
     * Gets the immutable view for specified JSON object.
     *
     * @param jsonObject The JSON object
     * @return The immutable JSON object
     */
    public static ImmutableJSONObject immutableFor(JSONObject jsonObject) {
        return jsonObject instanceof ImmutableJSONObject ? (ImmutableJSONObject) jsonObject : new ImmutableJSONObject(jsonObject);
    }

    // --------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ImmutableJSONObject}.
     *
     * @param jsonObject The JSON object to copy from
     */
    private ImmutableJSONObject(JSONObject jsonObject) {
        super(createImmutableMapFrom(jsonObject.getMyHashMap()), true);
    }

    /**
     * Creates the immutable view for given map.
     *
     * @param map The map
     * @return The immutable map
     */
    static ImmutableMap<String, Object> createImmutableMapFrom(Map<String, Object> map) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object object = entry.getValue();
            if (object instanceof JSONArray) {
                builder.put(entry.getKey(), ImmutableJSONArray.immutableFor((JSONArray) object));
            } else if (object instanceof JSONObject) {
                builder.put(entry.getKey(), immutableFor((JSONObject) object));
            } else {
                builder.put(entry.getKey(), object);
            }
        }
        return builder.build();
    }

}
