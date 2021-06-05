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

package com.openexchange.sso.action;

import java.util.Locale;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.sso.SSOConstants;
import com.openexchange.sso.services.SSOServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link GetAction} - Performs the <code>GET</code> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetAction implements AJAXActionService {

    /**
     * The <code>GET</code> action string.
     */
    static final String ACTION = AJAXServlet.ACTION_GET;

    /**
     * Initializes a new {@link GetAction}.
     */
    public GetAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            /*
             * Ensure a secure connection to not transfer sensitive data in plain text
             */
            if (!requestData.isSecure() && !isLocalHost(requestData.getHostname())) {
                throw AjaxExceptionCodes.NON_SECURE_DENIED.create( ACTION, SSOServiceRegistry.getInstance().getService(DispatcherPrefixService.class).getPrefix() + SSOConstants.SERVLET_PATH_APPENDIX);
            }
            /*
             * Create & fill JSON object
             */
            final JSONObject obj = new JSONObject();
            obj.put("login", session.getLogin());
            final User user = session.getUser();
            obj.put("username", user.getLoginInfo());
            obj.put("password", session.getPassword());
            obj.put("context_id", session.getContextId());
            obj.put("context_name", session.getContext().getName());
            obj.put("imap_login", user.getImapLogin());
            /*
             * Return
             */
            return new AJAXRequestResult(obj);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    private static final Set<String> LOCAL_HOST = ImmutableSet.of("127.0.0.1", "localhost", "::1/128", "::1");

    private boolean isLocalHost(final String hostname) {
        return null != hostname && LOCAL_HOST.contains(hostname.toLowerCase(Locale.US));
    }

}
