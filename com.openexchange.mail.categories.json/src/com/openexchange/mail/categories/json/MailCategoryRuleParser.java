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

package com.openexchange.mail.categories.json;

import java.util.Arrays;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.java.Strings;
import com.openexchange.mail.categories.ruleengine.MailCategoryRule;

/**
 * {@link MailCategoryRuleParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoryRuleParser {

    private static final String SUBTESTS_STR = "subrules";
    private static final String OPERATOR_STR = "operator";
    private static final String HEADER_STR = "header";
    private static final String VALUE_STR = "value";

    /**
     * Initializes a new {@link MailCategoryRuleParser}.
     *
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    public static MailCategoryRule parseJSON(JSONObject json, String flag) throws JSONException {
        MailCategoryRule result = null;
        boolean isAND = false;
        if (!json.has(SUBTESTS_STR)) {
            String[] headers = Strings.splitByComma(json.getString(HEADER_STR));
            String[] values = Strings.splitByComma(json.getString(VALUE_STR));
            return new MailCategoryRule(Arrays.asList(headers), Arrays.asList(values), flag);
        }

        if (json.has(OPERATOR_STR)) {
            if (json.getString(OPERATOR_STR).equalsIgnoreCase("and")) {
                isAND = true;
            }
        }
        result = new MailCategoryRule(flag, isAND);

        JSONArray array = (JSONArray) json.get(SUBTESTS_STR);
        for (Object subObject : array.asList()) {
            if (!(subObject instanceof Map)) {
                throw new JSONException("conditions element is not a JSONObject!");
            }

            JSONObject subTest = new JSONObject((Map<String, ? extends Object>) subObject);
            result.addSubRule(parseJSON(subTest, flag));
        }
        return result;
    }
}
