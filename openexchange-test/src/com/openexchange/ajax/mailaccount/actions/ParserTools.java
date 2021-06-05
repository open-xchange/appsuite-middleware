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

package com.openexchange.ajax.mailaccount.actions;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.json.fields.SetSwitch;

/**
 * {@link ParserTools}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ParserTools {

    public static List<MailAccountDescription> parseList(final JSONArray arrayOfArrays, final int[] cols) throws JSONException {
        try {
            final List<MailAccountDescription> accounts = new LinkedList<MailAccountDescription>();

            for (int i = 0, size = arrayOfArrays.length(); i < size; i++) {
                final JSONArray row = arrayOfArrays.getJSONArray(i);
                final MailAccountDescription desc = new MailAccountDescription();
                final SetSwitch setter = new SetSwitch(desc);
                int j = 0;

                for (final int col : cols) {
                    final Attribute attr = Attribute.getById(col);

                    Object value = row.get(j++);
                    if (value == JSONObject.NULL) {
                        value = null;
                    }
                    setter.setValue(value);
                    attr.doSwitch(setter);
                }
                accounts.add(desc);
            }
            return accounts;
        } catch (OXException e) {
            throw new JSONException(e);
        }
    }
}
