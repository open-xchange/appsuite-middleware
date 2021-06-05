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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test;

import java.util.ArrayList;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CurrentDateTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class CurrentDateTestCommandParser extends AbstractDateTestCommandParser {

    /**
     * Initializes a new {@link CurrentDateTestCommandParser}.
     */
    public CurrentDateTestCommandParser(ServiceLookup services) {
        super(services, Commands.CURRENTDATE);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        String commandName = Commands.CURRENTDATE.getCommandName();
        final List<Object> argList = new ArrayList<Object>();
        parseZone(argList, jsonObject, session, commandName);
        boolean isNotMatcher = parseComparisonTag(argList, jsonObject, commandName);
        parseDatePart(argList, jsonObject, commandName);

        if (isNotMatcher){
            return NotTestCommandUtil.wrapTestCommand(new TestCommand(TestCommand.Commands.CURRENTDATE, argList, new ArrayList<TestCommand>()));
        }
        return new TestCommand(TestCommand.Commands.CURRENTDATE, argList, new ArrayList<TestCommand>());
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException, OXException {
        parseZone(jsonObject, command);
        parseComparisonTag(jsonObject, command, transformToNotMatcher);
        parseDatePart(jsonObject, command);
    }
}
