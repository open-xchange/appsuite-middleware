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

import static org.junit.Assert.fail;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.ComparisonWriter;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.ComparisonWriterRegistry;
import com.openexchange.ajax.mail.filter.api.dao.MatchType;
import com.openexchange.ajax.mail.filter.api.dao.comparison.Comparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.argument.ComparisonArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.CommonTestArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TestArgument;
import com.openexchange.ajax.tools.JSONCoercion;

/**
 * {@link AbstractWriterImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractWriterImpl<T extends TestArgument> implements TestWriter {

    /**
     * Initialises a new {@link AbstractWriterImpl}.
     */
    AbstractWriterImpl() {
        super();
    }

    /**
     *
     * @param test
     * @param arguments
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    @SuppressWarnings("null")
    JSONObject write(Test<TestArgument> test, Set<T> arguments, JSONObject jsonObject) throws JSONException {
        jsonObject.put(CommonTestArgument.id.name(), test.getTestCommand().name().toLowerCase());

        final Comparison<? extends ComparisonArgument> comparison = test.getComparison();

        if (comparison != null) {
            final MatchType matchType = comparison.getMatchType();
            final ComparisonWriter compWriter = ComparisonWriterRegistry.getWriter(matchType);
            if (compWriter == null) {
                fail("Missing ComparisonWriter for matchtype '" + matchType + "'");
            }
            compWriter.write((Comparison<ComparisonArgument>) comparison, jsonObject);
        }

        for (T argument : arguments) {
            Object value = test.getTestArgument(argument);
            if (JSONCoercion.needsJSONCoercion(value)) {
                JSONValue jsonValue = (JSONValue) JSONCoercion.coerceToJSON(value);
                value = jsonValue;
            }
            jsonObject.put(argument.toString(), value);
        }
        return jsonObject;
    }
}
