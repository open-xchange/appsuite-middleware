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

import java.util.Date;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.subscription.ShareSubscriptionRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UnsubscribeShareAction} - Unsubscribes a share that is associated with a specific share link from a remote server
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class UnsubscribeShareAction extends AbstractShareSubscriptionAction {

    /**
     * Initializes a new {@link UnsubscribeShareAction}.
     * 
     * @param services The service lookup
     */
    public UnsubscribeShareAction(ServiceLookup services) {
        super(services);
    }

    @Override
    AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session, JSONObject json, String shareLink) throws OXException {
        ShareSubscriptionRegistry service = services.getServiceSafe(ShareSubscriptionRegistry.class);
        service.unsubscribe(session, shareLink);
        return new AJAXRequestResult(null, new Date(System.currentTimeMillis()));
    }

}
