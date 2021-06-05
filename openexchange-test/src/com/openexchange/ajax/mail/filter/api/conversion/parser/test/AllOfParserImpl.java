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

package com.openexchange.ajax.mail.filter.api.conversion.parser.test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.mail.filter.api.dao.TestCommand;
import com.openexchange.ajax.mail.filter.api.dao.test.AllOfTest;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TestArgument;

/**
 * {@link AllOfParserImpl}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AllOfParserImpl implements TestParser {

    /**
     * Initialises a new {@link AllOfParserImpl}.
     */
    public AllOfParserImpl() {
        super();
    }

    @Override
    public Test<? extends TestArgument> parse(JSONObject jsonObject) throws JSONException {
        final JSONArray jsonTestArray = jsonObject.getJSONArray("tests");
        final Test<?>[] tests = new Test<?>[jsonTestArray.length()];

        for (int a = 0; a < jsonTestArray.length(); a++) {
            final JSONObject jsobSubObj = jsonTestArray.getJSONObject(a);
            final String subtestname = jsobSubObj.getString("id");
            TestCommand testCommand = TestCommand.valueOf(subtestname.toUpperCase());
            final TestParser testParser = TestParserFactory.getParser(testCommand);
            tests[a] = testParser.parse(jsobSubObj);
        }

        return new AllOfTest(tests);
    }
}
