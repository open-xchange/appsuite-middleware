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

package com.openexchange.user.json.actions;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.service.MailService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.json.dto.Me;

/**
 * {@link MeAction} - Maps the action to a <tt>GET</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction()
public final class MeAction extends AbstractUserAction {

    /**
     * The <tt>GET</tt> action string.
     */
    public static final String ACTION = "GET";

    /**
     * Initializes a new {@link MeAction}.
     */
    public MeAction(ServiceLookup services) {
        super(services);
    }

    @SuppressWarnings("null")
    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        // Obtain mail login
        String mailLogin = null;
        MailService mailService = services.getOptionalService(MailService.class);
        if (null != mailService) {
            mailLogin = mailService.getMailLoginFor(session.getUserId(), session.getContextId(), 0);
        }

        return new AJAXRequestResult(new Me(session.getUser(), session.getContext(), session.getLoginName(), mailLogin), "user/me");
    }

}
