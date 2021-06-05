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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test.simplified;

import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.fields.HeaderTestField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.AbstractTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.HeaderTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.TestCommandUtil;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SimplifiedHeaderTestParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public class SimplifiedHeaderTestParser extends AbstractTestCommandParser {

    /**
     * Creates a new {@link SimplifiedHeaderTestParser} instance
     *
     * @param services The {@link ServiceLookup} instance
     * @return The {@link SimplifiedHeaderTestParser} instance
     */
    public static SimplifiedHeaderTestParser newInstance(ServiceLookup services) {
        return new SimplifiedHeaderTestParser(services);
    }

    // --------------------------------------------------------------------------------------------------------------

    private final HeaderTestCommandParser headerParser;

    /**
     * Initializes a new {@link SimplifiedHeaderTestParser}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    private SimplifiedHeaderTestParser(ServiceLookup services) {
        super(services, Commands.HEADER);
        headerParser = new HeaderTestCommandParser(services);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        String id = jsonObject.getString("id");
        if (Commands.HEADER.getCommandName().equals(id)) {
            // Fall back to default behaviour
            return headerParser.parse(jsonObject, session);
        }
        SimplifiedHeaderTest test = SimplifiedHeaderTest.getTestByName(id);
        // use contains as default comparator
        if (!jsonObject.has(HeaderTestField.comparison.name())) {
            jsonObject.put(HeaderTestField.comparison.name(), "contains");
        }
        switch (test) {
            case From:
                jsonObject.put(HeaderTestField.headers.name(), SimplifiedHeaderTest.From.getHeaderNames());
                return headerParser.parse(jsonObject, session);
            case To:
                jsonObject.put(HeaderTestField.headers.name(), SimplifiedHeaderTest.To.getHeaderNames());
                return headerParser.parse(jsonObject, session);
            case Cc:
                jsonObject.put(HeaderTestField.headers.name(), SimplifiedHeaderTest.Cc.getHeaderNames());
                return headerParser.parse(jsonObject, session);
            case Subject:
                jsonObject.put(HeaderTestField.headers.name(), SimplifiedHeaderTest.Subject.getHeaderNames());
                return headerParser.parse(jsonObject, session);
            case MailingList:
                jsonObject.put(HeaderTestField.headers.name(), SimplifiedHeaderTest.MailingList.getHeaderNames());
                return headerParser.parse(jsonObject, session);
            case AnyRecipient:
                jsonObject.put(HeaderTestField.headers.name(), SimplifiedHeaderTest.AnyRecipient.getHeaderNames());
                return headerParser.parse(jsonObject, session);
            default:
                // should never occur
                throw new IllegalArgumentException("Unknown/Unhandled SimplifiedHeaderTest '" + test.getCommandName() + "'");
        }
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException, OXException {
        headerParser.parse(jsonObject, command, transformToNotMatcher);

        JSONArray headers = jsonObject.getJSONArray(HeaderTestField.headers.name());
        for (SimplifiedHeaderTest test : SimplifiedHeaderTest.values()) {
            if (TestCommandUtil.isSimplified(test, headers)) {
                simplify(test.getCommandName(), jsonObject);
            }
        }
    }

    /**
     * Adds the id of the simplified rule to the specified {@link JSONObject}
     * 
     * @param id The id to add
     * @param jsonObject The {@link JSONObject}
     * @throws JSONException if a JSON parsing error is occurred
     */
    private void simplify(String id, JSONObject jsonObject) throws JSONException {
        jsonObject.put(GeneralField.id.name(), id);
        jsonObject.remove(HeaderTestField.headers.name());
    }
}
