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

package com.openexchange.mail.compose.json.action;

import static com.openexchange.mail.compose.CompositionSpaces.buildConsoleTableFor;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UpdateCompositionSpaceAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class UpdateCompositionSpaceAction extends AbstractMailComposeAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateCompositionSpaceAction.class);

    /**
     * Initializes a new {@link UpdateCompositionSpaceAction}.
     *
     * @param services The service look-up
     */
    public UpdateCompositionSpaceAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        JSONObject jMessage = (JSONObject) requestData.requireData();

        String sId = requestData.requireParameter("id");
        CompositionSpaceId compositionSpaceId = parseCompositionSpaceId(sId);

        CompositionSpaceService compositionSpaceService = getCompositionSpaceService(compositionSpaceId.getServiceId(), session);

        MessageDescription md = new MessageDescription();
        parseJSONMessage(jMessage, md);

        CompositionSpace compositionSpace = compositionSpaceService.updateCompositionSpace(compositionSpaceId.getId(), md, getClientToken(requestData));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updated composition space '{}':{}{}", compositionSpace.getId(), Strings.getLineSeparator(), buildConsoleTableFor(compositionSpace, Optional.ofNullable(requestData.getUserAgent())));
        }
        return new AJAXRequestResult(compositionSpace, "compositionSpace").addWarnings(compositionSpaceService.getWarnings());
    }

}
