/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
        } catch (final JSONException x) {
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
                        final OAuthAccount oAuthAccount = oAuthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());
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
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

}
