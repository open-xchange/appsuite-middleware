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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.ComparisonWriter;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.ComparisonWriterRegistry;
import com.openexchange.ajax.mail.filter.api.dao.MatchType;
import com.openexchange.ajax.mail.filter.api.dao.comparison.Comparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.argument.ComparisonArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.CommonTestArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.SizeTestArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.argument.TestArgument;

/**
 * {@link SizeTestWriterImpl}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SizeTestWriterImpl extends AbstractWriterImpl<SizeTestArgument> {

    /**
     * Initialises a new {@link SizeTestWriterImpl}.
     */
    public SizeTestWriterImpl() {
        super();
    }

    @Override
    public JSONObject write(Test<? extends TestArgument> test, JSONObject jsonObject) throws JSONException {
        jsonObject.put(CommonTestArgument.id.name(), test.getTestCommand().name().toLowerCase());

        final Comparison<? extends ComparisonArgument> comparison = test.getComparison();

        if (comparison != null) {
            final MatchType matchType = comparison.getMatchType();
            final ComparisonWriter compWriter = ComparisonWriterRegistry.getWriter(matchType);
            compWriter.write((Comparison<ComparisonArgument>) comparison, jsonObject);
        }

        return jsonObject;
    }
}
