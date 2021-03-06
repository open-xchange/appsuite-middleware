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
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.ComparisonParser;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.ComparisonParserRegistry;
import com.openexchange.ajax.mail.filter.api.dao.MatchType;
import com.openexchange.ajax.mail.filter.api.dao.comparison.Comparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.argument.ComparisonArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.SizeTest;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TestArgument;

/**
 * {@link SizeTestParserImpl}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SizeTestParserImpl implements TestParser {

    /**
     * Initialises a new {@link SizeTestParserImpl}.
     */
    public SizeTestParserImpl() {
        super();
    }

    @Override
    public Test<? extends TestArgument> parse(JSONObject jsonObject) throws JSONException {
        final String comparisonName = jsonObject.getString("comparison");
        MatchType matchType = MatchType.valueOf(comparisonName);

        final ComparisonParser compParser = ComparisonParserRegistry.getParser(matchType);
        final Comparison<? extends ComparisonArgument> comparison = compParser.parse(jsonObject);
        return new SizeTest(comparison);
    }
}
