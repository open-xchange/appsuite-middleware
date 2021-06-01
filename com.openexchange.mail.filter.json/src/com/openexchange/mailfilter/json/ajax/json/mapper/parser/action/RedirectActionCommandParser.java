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
import javax.mail.internet.AddressException;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.ActionCommand.Commands;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.RedirectActionField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RedirectActionCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RedirectActionCommandParser implements CommandParser<ActionCommand> {

    /**
     * Initialises a new {@link RedirectActionCommandParser}.
     */
    public RedirectActionCommandParser() {
        super();
    }

    @SuppressWarnings("unused")
    @Override
    public ActionCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        ArrayList<Object> argList = new ArrayList<>();

        boolean copy = jsonObject.optBoolean(RedirectActionField.copy.name(), false);
        if (copy) {
            argList.add(ArgumentUtil.createTagArgument(RedirectActionField.copy.name()));
        }

        String stringParam = CommandParserJSONUtil.getString(jsonObject, RedirectActionField.to.name(), Commands.REDIRECT.getCommandName());
        // Check for valid email address here:
        try {
            new QuotedInternetAddress(stringParam, true);
        } catch (AddressException e) {
            throw MailFilterExceptionCode.INVALID_REDIRECT_ADDRESS.create(e, stringParam);
        }
        // And finally check of that forward address is allowed
        ConfigurationService service = Services.getService(ConfigurationService.class);
        Filter filter;
        if (null != service && (null != (filter = service.getFilterFromProperty("com.openexchange.mail.filter.redirectWhitelist"))) && !filter.accepts(stringParam)) {
            throw MailFilterExceptionCode.REJECTED_REDIRECT_ADDRESS.create(stringParam);
        }
        argList.add(CommandParserJSONUtil.stringToList(stringParam));
        ActionCommand result = new ActionCommand(Commands.REDIRECT, argList);
        if (copy) {
            result.addOptionalRequired(RedirectActionField.copy.name());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, ActionCommand actionCommand) throws JSONException, OXException {
        ArrayList<Object> arguments = actionCommand.getArguments();

        jsonObject.put(GeneralField.id.name(), Commands.REDIRECT.getJsonName());
        if (arguments.size() == 1) {
            jsonObject.put(RedirectActionField.to.name(), ((List<String>) arguments.get(0)).get(0));
        } else {
            String copyCommandString = ArgumentUtil.createTagArgument(RedirectActionField.copy.name()).toString();
            if (actionCommand.getTagArguments().get(copyCommandString) != null) {
                jsonObject.put(RedirectActionField.copy.name(), true);
            }
            jsonObject.put(RedirectActionField.to.name(), ((List<String>) arguments.get(1)).get(0));
        }
    }
}
