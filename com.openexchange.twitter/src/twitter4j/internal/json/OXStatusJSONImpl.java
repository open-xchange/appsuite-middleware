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

package twitter4j.internal.json;

import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONObject;

/**
 * {@link OXStatusJSONImpl} - A data class representing one single status of a user.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXStatusJSONImpl extends StatusJSONImpl {

    private static final long serialVersionUID = 7548618898682727456L;

    /**
     * Initializes a new {@link OXStatusJSONImpl}.
     */
    public OXStatusJSONImpl() {
        super();
    }

    /**
     * Initializes a new {@link OXStatusJSONImpl}.
     * 
     * @param json The JSON object
     * @throws TwitterException If an error occurs
     */
    public OXStatusJSONImpl(JSONObject json) throws TwitterException {
        super(json);
    }

    /**
     * Initializes a new {@link OXStatusJSONImpl}.
     * 
     * @param res The HTTP response
     * @param conf The configuration
     * @throws TwitterException If an error occurs
     */
    public OXStatusJSONImpl(HttpResponse res, Configuration conf) throws TwitterException {
        super(res, conf);
    }

    /**
     * Initializes a new {@link OXStatusJSONImpl}.
     * 
     * @param json The JSON object
     * @param conf The configuration
     * @throws TwitterException If an error occurs
     */
    public OXStatusJSONImpl(JSONObject json, Configuration conf) throws TwitterException {
        super(json, conf);
    }

}
