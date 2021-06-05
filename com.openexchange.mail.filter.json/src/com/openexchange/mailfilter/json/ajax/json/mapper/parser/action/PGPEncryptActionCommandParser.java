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

package com.openexchange.mailfilter.json.ajax.json.mapper.parser.action;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.PGPEncryptActionField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PGPEncryptActionCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class PGPEncryptActionCommandParser implements CommandParser<ActionCommand> {

    /**
     * Initialises a new {@link PGPEncryptActionCommandParser}.
     */
    public PGPEncryptActionCommandParser() {
        super();
    }

    @Override
    public ActionCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final ArrayList<Object> arrayList = new ArrayList<Object>();
        final JSONArray keys = jsonObject.optJSONArray(PGPEncryptActionField.keys.getFieldName());
        if (null != keys) {
            if (0 == keys.length()) {
                throw new JSONException("Empty string-arrays are not allowed in sieve.");
            }
            arrayList.add(ArgumentUtil.createTagArgument(PGPEncryptActionField.keys));
            arrayList.add(CommandParserJSONUtil.coerceToStringList(keys));
        }

        return new ActionCommand(ActionCommand.Commands.PGP_ENCRYPT, arrayList);
    }

    @Override
    public void parse(JSONObject jsonObject, ActionCommand actionCommand) throws JSONException, OXException {
        jsonObject.put(GeneralField.id.name(), ActionCommand.Commands.PGP_ENCRYPT.getJsonName());
        final Hashtable<String, List<String>> tagarguments = actionCommand.getTagArguments();
        final List<String> keys = tagarguments.get(PGPEncryptActionField.keys.getTagName());
        if (null != keys) {
            jsonObject.put(PGPEncryptActionField.keys.getFieldName(), keys);
        }
    }

}
