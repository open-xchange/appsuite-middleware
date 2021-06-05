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
import java.util.Hashtable;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.ActionCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.EnotifyActionField;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.mapper.ArgumentUtil;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EnotifyActionCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EnotifyActionCommandParser extends AbstractActionCommandParser {

    /**
     * Initializes a new {@link EnotifyActionCommandParser}.
     */
    public EnotifyActionCommandParser(ServiceLookup services) {
        super(services, Commands.ENOTIFY);
    }

    @Override
    public ActionCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final ArrayList<Object> arrayList = new ArrayList<Object>();
        final String messageFieldName = EnotifyActionField.message.getFieldName();
        if (jsonObject.has(messageFieldName)) {
            final String message = jsonObject.getString(messageFieldName);
            arrayList.add(ArgumentUtil.createTagArgument(EnotifyActionField.message));
            arrayList.add(CommandParserJSONUtil.stringToList(message));
        }
        final String method = jsonObject.getString(EnotifyActionField.method.getFieldName());
        if (null == method) {
            throw new JSONException("Parameter " + EnotifyActionField.method.getFieldName() + " is missing for " + ActionCommand.Commands.ENOTIFY.getJsonName() + " is missing in JSON-Object. This is a required field");
        }
        arrayList.add(CommandParserJSONUtil.stringToList(method.replaceAll("(\r)?\n", "\r\n")));

        return new ActionCommand(ActionCommand.Commands.ENOTIFY, arrayList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, ActionCommand actionCommand) throws JSONException, OXException {
        ArrayList<Object> arguments = actionCommand.getArguments();

        jsonObject.put(GeneralField.id.name(), ActionCommand.Commands.ENOTIFY.getJsonName());
        final Hashtable<String, List<String>> tagArguments = actionCommand.getTagArguments();
        final List<String> message = tagArguments.get(EnotifyActionField.message.getTagName());
        if (null != message) {
            jsonObject.put(EnotifyActionField.message.getFieldName(), message.get(0));
        }
        jsonObject.put(EnotifyActionField.method.getFieldName(), ((List<String>) arguments.get(arguments.size() - 1)).get(0));
    }
}
