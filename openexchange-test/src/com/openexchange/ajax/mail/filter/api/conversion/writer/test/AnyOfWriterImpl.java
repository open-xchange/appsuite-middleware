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

package com.openexchange.ajax.mail.filter.api.conversion.writer.test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.mail.filter.api.dao.TestCommand;
import com.openexchange.ajax.mail.filter.api.dao.test.AnyOfTest;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.AnyOfTestArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TestArgument;

/**
 * {@link AnyOfWriterImpl}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AnyOfWriterImpl extends AbstractWriterImpl<AnyOfTestArgument> {

    /**
     * Initialises a new {@link AnyOfWriterImpl}.
     */
    public AnyOfWriterImpl() {
        super();
    }

    @Override
    public JSONObject write(Test<? extends TestArgument> type, JSONObject jsonObject) throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        final Test<AnyOfTestArgument> anyOfTest = (AnyOfTest) type;

        final JSONArray jsonArrayTests = new JSONArray();
        Test<?>[] tests = (Test<?>[]) anyOfTest.getTestArgument(AnyOfTestArgument.tests);
        for (int a = 0; a < tests.length; a++) {
            TestCommand testCommand = tests[a].getTestCommand();
            TestWriter testWriter = TestWriterFactory.getWriter(testCommand);
            JSONObject jsonSubTest = testWriter.write(tests[a], new JSONObject());
            jsonArrayTests.put(jsonSubTest);
        }

        jsonObj.put("id", anyOfTest.getTestCommand().name().toLowerCase());
        jsonObj.put("tests", jsonArrayTests);

        return jsonObj;
    }
}
