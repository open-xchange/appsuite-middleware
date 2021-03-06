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

package com.openexchange.xing.json.actions;

import java.util.Collection;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.xing.UserField;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.json.XingRequest;

/**
 * {@link GetCommentsActivityAction}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetCommentsActivityAction extends AbstractXingAction {
    /**
     * Initializes a new {@link GetCommentsActivityAction}.
     */
    public GetCommentsActivityAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(XingRequest req) throws OXException, JSONException, XingException {
        String activityId = getMandatoryStringParameter(req, "activity_id");
        int optLimit = getOptIntParameter(req, "limit");
        int optOffset = getOptIntParameter(req, "offset");
        Collection<UserField> optUserFields = getUserFields(req.getParameter("user_fields"));

        Map<String, Object> response = getXingAPI(req).getComments(activityId, optLimit, optOffset, optUserFields);
        return new AJAXRequestResult(JSONCoercion.coerceToJSON(response));
    }
}
