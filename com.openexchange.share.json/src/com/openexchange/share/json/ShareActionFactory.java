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

package com.openexchange.share.json;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.json.actions.AnalyzeAction;
import com.openexchange.share.json.actions.DeleteLinkAction;
import com.openexchange.share.json.actions.GetLinkAction;
import com.openexchange.share.json.actions.ResubscribeShareAction;
import com.openexchange.share.json.actions.SendLinkAction;
import com.openexchange.share.json.actions.SubscribeShareAction;
import com.openexchange.share.json.actions.UnsubscribeShareAction;
import com.openexchange.share.json.actions.UpdateLinkAction;

/**
 * {@link ShareActionFactory}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions = new HashMap<String, AJAXActionService>();

    /**
     * Initializes a new {@link ShareActionFactory}.
     * 
     * @param services The services
     */
    public ShareActionFactory(ServiceLookup services) {
        super();
        actions.put("update", new UpdateLinkAction(services));
        actions.put("getLink", new GetLinkAction(services));
        actions.put("updateLink", new UpdateLinkAction(services));
        actions.put("deleteLink", new DeleteLinkAction(services));
        actions.put("sendLink", new SendLinkAction(services));

        // Federated Sharing
        actions.put("analyze", new AnalyzeAction(services));
        actions.put("subscribe", new SubscribeShareAction(services));
        actions.put("unsubscribe", new UnsubscribeShareAction(services));
        actions.put("resubscribe", new ResubscribeShareAction(services));
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        return actions.get(action);
    }

}
