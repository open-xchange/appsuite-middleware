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

package com.openexchange.mail.autoconfig.json.actions;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.AutoconfigService;
import com.openexchange.mail.oauth.MailOAuthService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAction}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class GetAction extends AutoconfigAction {

    private static final String EMAIL = "email";

    private static final String PASSWORD = "password";

    private static final String FORCE_SECURE = "force_secure";

    private static final String OAUTH = "oauth";

    /**
     * Initializes a new {@link GetAction}.
     *
     * @param services
     */
    public GetAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        if (request.containsParameter(OAUTH)) {
            String sOAuth = request.getParameter(OAUTH);
            int id = parseInt(sOAuth);
            if (id >= 0) {
                MailOAuthService mailOAuthService = getService(MailOAuthService.class);
                if (null == mailOAuthService) {
                    throw ServiceExceptionCode.absentService(MailOAuthService.class);
                }

                Autoconfig autoconfig = mailOAuthService.getAutoconfigFor(id, session);
                return new AJAXRequestResult(autoconfig, "autoconfig");
            }
        }

        String mail = request.getParameter(EMAIL, String.class);
        String password = request.getParameter(PASSWORD);

        boolean forceSecure = true;
        if (request.containsParameter(FORCE_SECURE)) {
            forceSecure = request.getParameter(FORCE_SECURE, Boolean.class).booleanValue();
        }

        if (Strings.isEmpty(password)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PASSWORD);
        }

        AutoconfigService autoconfigService = getAutoconfigService();
        Autoconfig autoconfig = autoconfigService.getConfig(mail, password, session.getUserId(), session.getContextId(), forceSecure);
        return new AJAXRequestResult(autoconfig, "autoconfig");
    }

    private int parseInt(String str) {
        return Tools.getUnsignedInteger(str);
    }

}
