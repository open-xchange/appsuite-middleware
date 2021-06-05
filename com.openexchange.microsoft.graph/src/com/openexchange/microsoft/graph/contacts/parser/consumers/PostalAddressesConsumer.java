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

package com.openexchange.microsoft.graph.contacts.parser.consumers;

import java.util.function.BiConsumer;
import org.json.JSONObject;
import com.openexchange.groupware.container.Contact;

/**
 * {@link PostalAddressesConsumer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class PostalAddressesConsumer implements BiConsumer<JSONObject, Contact> {

    /**
     * Initialises a new {@link PostalAddressesConsumer}.
     */
    public PostalAddressesConsumer() {
        super();
    }

    @Override
    public void accept(JSONObject t, Contact u) {
        if (t.hasAndNotNull("homeAddress")) {
            JSONObject homeAddress = t.optJSONObject("homeAddress");
            if (homeAddress.hasAndNotNull("street")) {
                u.setStreetHome(homeAddress.optString("street"));
            }
            if (homeAddress.hasAndNotNull("city")) {
                u.setCityHome(homeAddress.optString("city"));
            }
            if (homeAddress.hasAndNotNull("postalCode")) {
                u.setPostalCodeHome(homeAddress.optString("postalCode"));
            }
            if (homeAddress.hasAndNotNull("country")) {
                u.setCountryHome(homeAddress.optString("country"));
            }
            if (homeAddress.hasAndNotNull("state")) {
                u.setStateHome(homeAddress.optString("state"));
            }
        }

        if (t.hasAndNotNull("businessAddress")) {
            JSONObject businessAddress = t.optJSONObject("businessAddress");
            if (businessAddress.hasAndNotNull("street")) {
                u.setStreetBusiness(businessAddress.optString("street"));
            }
            if (businessAddress.hasAndNotNull("city")) {
                u.setCityBusiness(businessAddress.optString("city"));
            }
            if (businessAddress.hasAndNotNull("postalCode")) {
                u.setPostalCodeBusiness(businessAddress.optString("postalCode"));
            }
            if (businessAddress.hasAndNotNull("country")) {
                u.setCountryBusiness(businessAddress.optString("country"));
            }
            if (businessAddress.hasAndNotNull("state")) {
                u.setStateBusiness(businessAddress.optString("state"));
            }
        }

        if (t.hasAndNotNull("otherAddress")) {
            JSONObject otherAddress = t.optJSONObject("otherAddress");
            if (otherAddress.hasAndNotNull("street")) {
                u.setStreetOther(otherAddress.optString("street"));
            }
            if (otherAddress.hasAndNotNull("city")) {
                u.setCityOther(otherAddress.optString("city"));
            }
            if (otherAddress.hasAndNotNull("postalCode")) {
                u.setPostalCodeOther(otherAddress.optString("postalCode"));
            }
            if (otherAddress.hasAndNotNull("country")) {
                u.setCountryOther(otherAddress.optString("country"));
            }
            if (otherAddress.hasAndNotNull("state")) {
                u.setStateOther(otherAddress.optString("state"));
            }
        }
    }
}
