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

package com.openexchange.ajax.mail.filter.api.response;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.mail.filter.api.conversion.parser.MailFilterParser;
import com.openexchange.ajax.mail.filter.api.dao.Rule;

/**
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class AllResponse extends AbstractAJAXResponse {

    /**
     * @param response
     */
    public AllResponse(final Response response) {
        super(response);
    }

    public Rule[] getRules() {
        final List<Rule> rules = new ArrayList<Rule>();
        final JSONArray jsonArray = (JSONArray) getData();
        for (int a = 0; a < jsonArray.length(); a++) {
            try {
                rules.add(parseRow(jsonArray.getJSONObject(a)));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return rules.toArray(new Rule[rules.size()]);
    }

    public Rule parseRow(final JSONObject jsonObj) throws JSONException {
        final Rule rule = new Rule();
        MailFilterParser parser = new MailFilterParser();
        parser.parse(jsonObj, rule);
        return rule;
    }
}
