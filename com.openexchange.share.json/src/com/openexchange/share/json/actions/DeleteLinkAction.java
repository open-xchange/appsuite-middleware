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
import com.openexchange.share.ShareTarget;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DeleteLinkAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DeleteLinkAction extends AbstractShareAction {

    /**
     * Initializes a new {@link DeleteLinkAction}.
     *
     * @param services
     */
    public DeleteLinkAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        /*
         * parse parameters & target
         */
        Date clientTimestamp = new Date(requestData.getParameter("timestamp", Long.class).longValue());
        JSONObject json = (JSONObject) requestData.requireData();
        ShareTarget target = getParser().parseTarget(json);
        /*
         * perform the deletion, return empty result in case of success
         */
        getShareService().deleteLink(session, target, clientTimestamp);
        return new AJAXRequestResult(new JSONObject(), new Date(), "json");
    }

}
