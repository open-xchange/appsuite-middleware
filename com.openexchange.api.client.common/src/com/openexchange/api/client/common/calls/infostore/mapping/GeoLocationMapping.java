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

package com.openexchange.api.client.common.calls.infostore.mapping;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
import com.openexchange.java.GeoLocation;
import com.openexchange.session.Session;

/**
 * {@link GeoLocationMapping}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @param <O> The object type
 * @since v7.10.5
 */
public abstract class GeoLocationMapping<O> extends DefaultJsonMapping<GeoLocation, O> {

    private static Pattern pattern = Pattern.compile("\\((.*),(.*)\\)");

    /**
     * Initializes a new {@link GeoLocationMapping}.
     * 
     * @param ajaxName The name of object ID in the JSON response
     * @param columnID The The column ID in the request
     */
    public GeoLocationMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException, OXException {
        GeoLocation geolocation = this.get(from);
        if (geolocation != null) {
            return geolocation.toString();
        }
        return JSONObject.NULL;
    }

    @Override
    public void deserialize(JSONObject from, O to) throws JSONException, OXException {
        if (from.has("geolocation")) {
            Matcher matcher = pattern.matcher(from.getString("geolocation"));
            if (matcher.find()) {
                this.set(to, new GeoLocation(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2))));
            }
        }
    }
}
