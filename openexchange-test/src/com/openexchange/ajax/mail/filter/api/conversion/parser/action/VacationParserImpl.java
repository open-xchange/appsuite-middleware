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

package com.openexchange.ajax.mail.filter.api.conversion.parser.action;

import static com.openexchange.java.Autoboxing.I;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.Vacation;
import com.openexchange.ajax.mail.filter.api.dao.action.argument.VacationActionArgument;

/**
 * {@link VacationParserImpl}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class VacationParserImpl implements ActionParser {

    /**
     * Initialises a new {@link VacationParserImpl}.
     */
    public VacationParserImpl() {
        super();
    }

    @Override
    public Action<VacationActionArgument> parse(JSONObject jsonObject) throws JSONException {
        Vacation vacation = new Vacation();

        int days = jsonObject.getInt(VacationActionArgument.days.name());
        vacation.setArgument(VacationActionArgument.days, I(days));

        JSONArray jsonAddressArray = jsonObject.optJSONArray(VacationActionArgument.addresses.name());
        String[] addresses;
        if (jsonAddressArray != null) {
            addresses = new String[jsonAddressArray.length()];
            for (int a = 0; a < addresses.length; a++) {
                addresses[a] = jsonAddressArray.getString(a);
            }
        } else {
            addresses = new String[0];
        }
        vacation.setArgument(VacationActionArgument.addresses, addresses);

        String subject = null;
        if (jsonObject.has(VacationActionArgument.subject.name())) {
            subject = jsonObject.getString(VacationActionArgument.subject.name());
            vacation.setArgument(VacationActionArgument.subject, subject);
        }

        String text = null;
        if (jsonObject.has(VacationActionArgument.text.name())) {
            text = jsonObject.getString(VacationActionArgument.text.name());
            vacation.setArgument(VacationActionArgument.text, text);
        }

        return vacation;
    }
}
