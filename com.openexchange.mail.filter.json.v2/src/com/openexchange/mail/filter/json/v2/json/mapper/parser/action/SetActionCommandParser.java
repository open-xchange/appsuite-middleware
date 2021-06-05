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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.action;

import java.util.ArrayList;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.ActionCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.fields.SetActionField;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SetActionCommandParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class SetActionCommandParser extends AbstractActionCommandParser {

    /**
     * Initializes a new {@link SetActionCommandParser}.
     */
    public SetActionCommandParser(ServiceLookup services) {
        super(services, Commands.SET);
    }

    @Override
    public ActionCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(jsonObject.getString(SetActionField.name.name()));
        arrayList.add(jsonObject.getString(SetActionField.value.name()));
        return new ActionCommand(ActionCommand.Commands.SET, arrayList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, ActionCommand actionCommand) throws JSONException {
        ArrayList<Object> arguments = actionCommand.getArguments();
        if (arguments.size() != 2) {
            throw new JSONException("Invalid sieve rule");
        }
        jsonObject.put(GeneralField.id.name(), ActionCommand.Commands.SET.getJsonName());
        jsonObject.put(SetActionField.name.name(), ((List<String>) arguments.get(0)).get(0));
        jsonObject.put(SetActionField.value.name(), ((List<String>) arguments.get(1)).get(0));
    }
}
