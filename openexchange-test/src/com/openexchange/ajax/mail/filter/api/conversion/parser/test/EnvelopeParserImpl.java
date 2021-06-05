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
import com.openexchange.ajax.mail.filter.api.dao.test.EnvelopeTest;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.EnvelopeTestArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TestArgument;

/**
 * {@link EnvelopeParserImpl}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EnvelopeParserImpl implements TestParser {

    /**
     * Initialises a new {@link EnvelopeParserImpl}.
     */
    public EnvelopeParserImpl() {
        super();
    }

    @Override
    public Test<? extends TestArgument> parse(JSONObject jsonObject) throws JSONException {
        final JSONArray jsonHeaderArray = jsonObject.getJSONArray("headers");
        final String[] headers = new String[jsonHeaderArray.length()];
        for (int a = 0; a < headers.length; a++) {
            headers[a] = jsonHeaderArray.getString(a);
        }

        final JSONArray jsonValueArray = jsonObject.getJSONArray("values");
        final String[] values = new String[jsonValueArray.length()];
        for (int a = 0; a < values.length; a++) {
            values[a] = jsonValueArray.getString(a);
        }

        // TODO: Check if comparison is needed
        //        final String comparisonName = jsonObject.getString("comparison");
        //        MatchType matchType = MatchType.valueOf(comparisonName);
        //
        //        final ComparisonParser compParser = ComparisonParserRegistry.getParser(matchType);
        //        final Comparison<? extends ComparisonArgument> comparison = compParser.parse(jsonObject);

        Test<EnvelopeTestArgument> addressTest = new EnvelopeTest();
        //addressTest.setComparison(comparison);
        addressTest.setTestArgument(EnvelopeTestArgument.headers, headers);
        addressTest.setTestArgument(EnvelopeTestArgument.values, values);

        return addressTest;
    }
}
