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

package com.openexchange.share.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.share.subscription.ShareSubscriptionRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ResubscribeShareAction} - Updates an existing share that is associated with a specific share link from a remote server
 * <p>
 * Currently only refreshing of the password or renaming is supported.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ResubscribeShareAction extends AbstractShareSubscriptionAction {

    /**
     * Initializes a new {@link ResubscribeShareAction}.
     * 
     * @param services The service lookup
     */
    public ResubscribeShareAction(ServiceLookup services) {
        super(services);
    }

    @Override
    AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session, JSONObject json, String shareLink) throws OXException, JSONException {
        ShareSubscriptionRegistry service = services.getServiceSafe(ShareSubscriptionRegistry.class);
        String password = json.optString(PASSWORD);
        String dislpayName = json.optString(DISPLAY_NAME);
        ShareSubscriptionInformation infos = service.resubscribe(session, shareLink, dislpayName, password);
        return createResponse(infos);
    }

}
