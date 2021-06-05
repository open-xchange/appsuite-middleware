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

package com.openexchange.share.impl.xctx;

import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.guest.GuestService;
import com.openexchange.java.Strings;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.core.ShareConstants;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link XctxLoginMethod}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XctxLoginMethod implements LoginMethodClosure {

    private final ServiceLookup services;
    private final String baseToken;
    private final String password;

    /**
     * Initializes a new {@link XctxLoginMethod}.
     * 
     * @param services A service lookup reference
     * @param baseToken The share base token
     * @param password The optional password for the guest account
     */
    public XctxLoginMethod(ServiceLookup services, String baseToken, String password) {
        super();
        this.services = services;
        this.baseToken = baseToken;
        this.password = password;
    }

    @Override
    public Authenticated doAuthentication(LoginResultImpl loginResult) throws OXException {
        /*
         * resolve guest user from share base token
         */
        GuestInfo guestInfo = services.getServiceSafe(ShareService.class).resolveGuest(baseToken);
        if (null == guestInfo) {
            throw ShareExceptionCodes.UNKNOWN_SHARE.create(baseToken);
        }
        Context context = services.getServiceSafe(ContextService.class).getContext(guestInfo.getContextID());
        if (false == context.isEnabled()) {
            throw LoginExceptionCodes.USER_NOT_ACTIVE.create();
        }
        UserService userService = services.getServiceSafe(UserService.class);
        User guestUser = userService.getUser(guestInfo.getGuestID(), context);
        if (false == guestUser.isMailEnabled()) {
            throw LoginExceptionCodes.USER_NOT_ACTIVE.create();
        }
        /*
         * authenticate as needed
         */
        switch (guestInfo.getAuthentication()) {
            case ANONYMOUS:
            case GUEST:
                return new XctxAuthenticated(guestUser, context);
            case ANONYMOUS_PASSWORD:
                if (Strings.isNotEmpty(password) && password.equals(services.getServiceSafe(PasswordMechRegistry.class)
                    .get(ShareConstants.PASSWORD_MECH_ID).decode(guestUser.getUserPassword(), guestUser.getSalt()))) {
                    return new XctxAuthenticated(guestUser, context);
                }
                throw LoginExceptionCodes.INVALID_GUEST_PASSWORD.create();
            case GUEST_PASSWORD:
                if (Strings.isNotEmpty(password) && userService.authenticate(guestUser, password)) {
                    return new XctxAuthenticated(guestUser, context);
                }
                GuestService guestService = services.getOptionalService(GuestService.class);
                if (null != guestService && guestService.authenticate(guestUser, context.getContextId(), password)) {
                    return new XctxAuthenticated(guestUser, context);
                }
                throw LoginExceptionCodes.INVALID_GUEST_PASSWORD.create();
            default:
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Unknown authentication " + guestInfo.getAuthentication());
        }
    }

}
