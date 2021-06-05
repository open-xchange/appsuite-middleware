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

package com.openexchange.oauth.json.oauthmeta;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.json.AbstractOAuthWriter;
import com.openexchange.session.Session;

/**
 * The OAuth service meta data writer
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MetaDataWriter extends AbstractOAuthWriter {

    /**
     * Initializes a new {@link MetaDataWriter}.
     */
    private MetaDataWriter() {
        super();
    }

    /**
     * Writes specified meta data as a JSON object.
     *
     * @param metaData The meta data
     * @return The JSON object
     * @throws JSONException If writing to JSON fails
     * @throws OXException
     */
    public static JSONObject write(final OAuthServiceMetaData metaData, Session session) throws JSONException, OXException {
        final JSONObject metaDataJSON = new JSONObject();
        metaDataJSON.put(MetaDataField.ID.getName(), metaData.getId());
        metaDataJSON.put(MetaDataField.DISPLAY_NAME.getName(), metaData.getDisplayName());
        metaDataJSON.put(MetaDataField.AVAILABLE_SCOPES.getName(), write(metaData.getAvailableScopes(session.getUserId(), session.getContextId())));
        return metaDataJSON;
    }

}
