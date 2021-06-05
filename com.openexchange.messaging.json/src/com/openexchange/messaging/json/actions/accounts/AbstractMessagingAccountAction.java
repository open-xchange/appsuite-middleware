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

package com.openexchange.messaging.json.actions.accounts;

import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.json.MessagingAccountParser;
import com.openexchange.messaging.json.MessagingAccountWriter;
import com.openexchange.messaging.json.Services;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.tools.session.ServerSession;


/**
 * The common superclass for all AJAXActionServices handling messaging account management. Provides a unified handling
 * for JSONExceptions and stores commonly used services (the registry, a writer and a parser) for subclasses.
 * Subclasses must implement the {@link #doIt(AJAXRequestData, ServerSession)} method.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMessagingAccountAction implements AJAXActionService {

    protected MessagingServiceRegistry registry;
    protected MessagingAccountWriter writer;
    protected MessagingAccountParser parser;

    public AbstractMessagingAccountAction(final MessagingServiceRegistry registry) {
        this.registry = registry;
        writer = new MessagingAccountWriter();
        parser = new MessagingAccountParser(registry);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            return doIt(requestData, session);
        } catch (JSONException x) {
            throw MessagingExceptionCodes.JSON_ERROR.create(x,x.toString());
        }
    }

    protected abstract AJAXRequestResult doIt(AJAXRequestData request, ServerSession session) throws JSONException, OXException;

    // ----------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Checks specified account's configuration
     * @param account The account to check
     * @param session The associated session
     *
     * @throws OXException If configuration is invalid
     */
    protected static void checkAccountConfiguration(final MessagingAccount account, final ServerSession session) throws OXException {
        final MessagingService messagingService = account.getMessagingService();
        if (null != messagingService) {
            final Map<String, Object> configuration = account.getConfiguration();
            if (null != configuration) {
                final int oauthAccountId = parseUnsignedInt(configuration.get("account"));
                if (oauthAccountId >= 0) {
                    final OAuthService oAuthService = Services.getService(OAuthService.class);
                    if (null != oAuthService) {
                        final OAuthAccount oAuthAccount = oAuthService.getAccount(session, oauthAccountId);
                        // Check OAuth service identifier against messaging service identifier
                        if (!equalServiceIdnetifiers(messagingService.getId(), oAuthAccount.getMetaData().getId())) {
                            throw MessagingExceptionCodes.INVALID_OAUTH_ACCOUNT.create(oAuthAccount.getMetaData().getId(), messagingService.getId());
                        }
                    }
                }
            }
        }
    }

    private static boolean equalServiceIdnetifiers(final String messagingServiceId, final String oAuthServiceId) {
        final int pos1 = messagingServiceId.lastIndexOf('.');
        final int pos2 = oAuthServiceId.lastIndexOf('.');
        if (pos1 >= 0 && pos2 >= 0) {
            return Strings.toLowerCase(messagingServiceId.substring(pos1 + 1)).equals(Strings.toLowerCase(oAuthServiceId.substring(pos2 + 1)));
        }
        return true;
    }

    private static int parseUnsignedInt(final Object obj) {
        if (null == obj) {
            return -1;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
