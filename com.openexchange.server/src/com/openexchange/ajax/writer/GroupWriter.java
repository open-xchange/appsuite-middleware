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

package com.openexchange.ajax.writer;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.GroupFields;
import com.openexchange.group.Group;
import com.openexchange.java.Strings;

/**
 * {@link GroupWriter} - Writes a group object into a JSON.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com"">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GroupWriter extends DataWriter {

    private final TimeZone utc;

    public GroupWriter() {
        super(null, null);
        utc = getTimeZone("utc");
    }

    public void writeArray(final Group group, final JSONArray json, final List<Group.Field> fields) {
        for (Group.Field field : fields) {
            if (field == Group.Field.MEMBERS) {
                json.put(Strings.join(group.getMember(), ","));
                continue;
            }
            json.put(group.get(field));
        }
    }

    public void writeGroup(final Group group, final JSONObject json) throws JSONException {
        writeParameter(GroupFields.IDENTIFIER, group.getIdentifier(), json);
        writeParameter(GroupFields.DISPLAY_NAME, group.getDisplayName(), json);
        writeParameter(GroupFields.NAME, group.getSimpleName(), json);
        writeParameter(DataFields.LAST_MODIFIED_UTC, group.getLastModified(), utc, json);

        writeMembers(group, json);
    }

    protected void writeMembers(final Group group, final JSONObject json) throws JSONException {
        int[] members = group.getMember();
        if (null == members) {
            json.put(GroupFields.MEMBERS, new JSONArray(0));
        } else {
            JSONArray jsonArray = new JSONArray(members.length);
            for (int member : members) {
                jsonArray.put(member);
            }
            json.put(GroupFields.MEMBERS, jsonArray);
        }
    }

}
