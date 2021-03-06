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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.mail.filter.api.dao.TestCommand;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.api.dao.test.TrueTest;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TestArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TrueTestArgument;

/**
 * {@link TrueParserImpl}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TrueParserImpl implements TestParser {

    /**
     * Initialises a new {@link TrueParserImpl}.
     */
    public TrueParserImpl() {
        super();
    }

    @Override
    public Test<? extends TestArgument> parse(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("test")) {
            final JSONObject jsonTestObject = jsonObject.getJSONObject("test");
            final String testname = jsonTestObject.getString("test");
            TestCommand testCommand = TestCommand.valueOf(testname.toLowerCase());
            final TestParser testParser = TestParserFactory.getParser(testCommand);
            testParser.parse(jsonTestObject);

            Test<TrueTestArgument> trueTest = new TrueTest();
            // TODO: check if 'true' test command can have a nested test
            //notTest.setTestArgument(TrueTestArgument.test, test);
            return trueTest;
        }
        return new TrueTest();
    }
}
