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

package com.openexchange.oauth.json.oauthaccount;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.association.Status;
import com.openexchange.oauth.json.AbstractOAuthWriter;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The OAuth account association writer
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AssociationWriter extends AbstractOAuthWriter {

    /**
     * Initializes a new {@link AssociationWriter}.
     */
    private AssociationWriter() {
        super();
    }

    /**
     * Writes specified account association as a JSON object.
     *
     * @param association The account association
     * @return The JSON object
     * @throws OXException If writing to JSON fails
     */
    public static JSONObject write(final OAuthAccountAssociation association, Session session) throws OXException {
        try {
            JSONObject jAssociation = new JSONObject(8);
            jAssociation.put(AccountField.ID.getName(), association.getId());
            jAssociation.put(AccountField.DISPLAY_NAME.getName(), association.getDisplayName());
            jAssociation.put(AccountField.SERVICE_ID.getName(), association.getServiceId());
            jAssociation.put("module", association.getModule());
            jAssociation.put("oauthAccontId", association.getOAuthAccountId());
            Status status = association.getStatus(session);
            jAssociation.put("status", status.getId());
            return jAssociation;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }
}
