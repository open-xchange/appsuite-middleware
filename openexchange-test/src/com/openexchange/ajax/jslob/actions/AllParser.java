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

package com.openexchange.ajax.jslob.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;

/**
 * @author <a href="mailto:markus.wagner@open-xchange.com">Markus Wagner</a>
 */
public final class AllParser extends AbstractAJAXParser<AllResponse> {

    /**
     * Initializes a new {@link ListParser}.
     */
    protected AllParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected AllResponse createResponse(Response response) throws JSONException {
        final JSONArray jArray = (JSONArray) response.getData();
        final int length = jArray.length();
        final List<JSlob> jSlobs = new ArrayList<JSlob>(length);
        for (int i = 0; i < length; i++) {
            final JSONObject jObject = jArray.getJSONObject(i);
            final DefaultJSlob jSlob = new DefaultJSlob(jObject.getJSONObject("tree"));
            jSlob.setMetaObject(jObject.optJSONObject("meta"));
            jSlob.setId(new JSlobId(null, jObject.getString("id"), 0, 0));
            jSlobs.add(jSlob);
        }
        return new AllResponse(response, jSlobs);
    }
}
