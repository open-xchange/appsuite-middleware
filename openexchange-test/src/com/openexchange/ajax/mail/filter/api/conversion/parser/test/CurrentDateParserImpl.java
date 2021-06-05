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
import com.openexchange.ajax.mail.filter.api.dao.test.CurrentDateTest;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TestArgument;

/**
 * {@link CurrentDateParserImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CurrentDateParserImpl implements TestParser {

    /**
     * Initialises a new {@link CurrentDateParserImpl}.
     */
    public CurrentDateParserImpl() {
        super();
    }

    @Override
    public Test<? extends TestArgument> parse(JSONObject jsonObject) throws JSONException {
        JSONArray dateValueArray = jsonObject.optJSONArray("datevalue");
        long time = -1;
        if (dateValueArray != null && !dateValueArray.isEmpty()) {
            time = dateValueArray.getLong(0);
        }
        String dateTag = jsonObject.getString("datepart");
        String comparisonName = jsonObject.getString("comparison");
        //        MatchType matchType = MatchType.valueOf(comparisonName);
        //
        //        final ComparisonParser compParser = ComparisonParserRegistry.getParser(matchType);
        //        final Comparison<? extends ComparisonArgument> comparison = compParser.parse(jsonObject);

        return new CurrentDateTest(comparisonName, dateTag, time);
    }

}
