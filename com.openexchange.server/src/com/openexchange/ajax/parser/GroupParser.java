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

package com.openexchange.ajax.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.GroupFields;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;

/**
 * This class parses a JSON into a group object.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GroupParser extends DataParser {

    /**
     * Default constructor.
     */
    public GroupParser() {
        super(true, null);
    }

    public void parse(final Group group, final JSONObject json)
        throws OXException, JSONException {
        if (json.has(GroupFields.IDENTIFIER)) {
            group.setIdentifier(parseInt(json, GroupFields.IDENTIFIER));
        }
        if (json.has(GroupFields.DISPLAY_NAME)) {
            group.setDisplayName(parseString(json, GroupFields.DISPLAY_NAME));
        }
        if (json.has(GroupFields.NAME)) {
            group.setSimpleName(parseString(json, GroupFields.NAME));
        }
        if (json.has(GroupFields.MEMBERS)) {
            group.setMember(parseMember(json));
        }
    }

    private int[] parseMember(final JSONObject json) throws JSONException {
        final JSONArray jmembers = json.getJSONArray(GroupFields.MEMBERS);
        final int[] retval = new int[jmembers.length()];
        for (int i = 0; i < jmembers.length(); i++) {
            retval[i] = jmembers.getInt(i);
        }
        return retval;
    }
}
