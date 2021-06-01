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

package com.openexchange.subscribe.json.actions;

import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.MISSING_PARAMETER;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.json.SubscriptionSourceJSONWriter;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GetSourceAction extends AbstractSubscribeSourcesAction {

    public GetSourceAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException {
        final String identifier = subscribeRequest.getRequestData().getParameter("id");
        if (identifier == null) {
            MISSING_PARAMETER.create("id");
        }
        final SubscriptionSource source = getAvailableSources(subscribeRequest.getServerSession()).getSource(identifier);
        final JSONObject json = new SubscriptionSourceJSONWriter(createTranslator(subscribeRequest.getServerSession())).writeJSON(source);
        return new AJAXRequestResult(json, "json");
    }

}
