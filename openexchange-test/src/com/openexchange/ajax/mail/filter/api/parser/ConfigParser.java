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

package com.openexchange.ajax.mail.filter.api.parser;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.filter.api.dao.ActionCommand;
import com.openexchange.ajax.mail.filter.api.dao.MailFilterConfiguration;
import com.openexchange.ajax.mail.filter.api.dao.MatchType;
import com.openexchange.ajax.mail.filter.api.dao.TestCommand;
import com.openexchange.ajax.mail.filter.api.dao.TestCondition;
import com.openexchange.ajax.mail.filter.api.response.ConfigResponse;

/**
 * {@link ConfigParser}
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ConfigParser extends AbstractAJAXParser<ConfigResponse> {

    /**
     * Initialises a new {@link ConfigParser}.
     * 
     * @param failOnError
     */
    public ConfigParser(final boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected ConfigResponse createResponse(final Response response) throws JSONException {
        final JSONObject jsonObj = (JSONObject) response.getData();

        final JSONArray jsonTestArray = jsonObj.getJSONArray("tests");
        final JSONArray jsonActionArray = jsonObj.getJSONArray("actioncommands");

        // Parse tests
        List<TestCondition> tests = new ArrayList<TestCondition>(jsonTestArray.length());
        for (int a = 0; a < jsonTestArray.length(); a++) {
            final JSONObject jsonTestObj = jsonTestArray.getJSONObject(a);

            // Parse test command
            final String testString = jsonTestObj.getString("test");
            TestCommand testCommand = TestCommand.valueOf(testString.toUpperCase());

            // Parse comparisons
            final JSONArray jsonComparisonArray = jsonTestObj.getJSONArray("comparison");
            List<MatchType> comparisons = new ArrayList<>(jsonComparisonArray.length());
            for (int b = 0; b < jsonComparisonArray.length(); b++) {
                comparisons.add(MatchType.valueOf(jsonComparisonArray.getString(b)));
            }

            tests.add(new TestCondition(testCommand, comparisons));
        }

        // Parse action commands
        List<ActionCommand> actionCommands = new ArrayList<ActionCommand>(jsonActionArray.length());
        for (int a = 0; a < jsonActionArray.length(); a++) {
            actionCommands.add(ActionCommand.valueOf(jsonActionArray.getString(a)));
        }

        return new ConfigResponse(response, new MailFilterConfiguration(tests, actionCommands));
    }
}
