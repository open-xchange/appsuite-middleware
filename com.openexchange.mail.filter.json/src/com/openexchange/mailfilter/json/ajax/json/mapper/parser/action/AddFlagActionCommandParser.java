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
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.mailfilter.json.ajax.json.fields.AddFlagsActionField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AddFlagActionCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AddFlagActionCommandParser implements CommandParser<ActionCommand> {

    /**
     * Initialises a new {@link AddFlagActionCommandParser}.
     */
    public AddFlagActionCommandParser() {
        super();
    }

    @Override
    public ActionCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final JSONArray array = jsonObject.getJSONArray(AddFlagsActionField.flags.name());
        if (null == array) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Parameter " + AddFlagsActionField.flags + " is missing for " + ActionCommand.Commands.ADDFLAG.getJsonName() + " is missing in JSON-Object. This is a required field");
        }
        final ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(CommandParserJSONUtil.coerceToStringList(array));

        return new ActionCommand(ActionCommand.Commands.ADDFLAG, arrayList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, ActionCommand actionCommand) throws JSONException, OXException {
        ArrayList<Object> arguments = actionCommand.getArguments();

        jsonObject.put(GeneralField.id.name(), ActionCommand.Commands.ADDFLAG.getJsonName());
        jsonObject.put(AddFlagsActionField.flags.name(), (List<String>) arguments.get(0));
    }

}
