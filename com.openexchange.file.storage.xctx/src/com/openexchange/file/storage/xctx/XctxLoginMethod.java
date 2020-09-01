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

package com.openexchange.file.storage.xctx;

import com.openexchange.authentication.Authenticated;
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
            return null;
        }
        Context context = services.getServiceSafe(ContextService.class).getContext(guestInfo.getContextID());
        if (false == context.isEnabled()) {
            return null;
        }
        User guestUser = services.getServiceSafe(UserService.class).getUser(guestInfo.getGuestID(), context);
        if (false == guestUser.isMailEnabled()) {
            return null;
        }
        /*
         * authenticate as needed
         */
        switch (guestInfo.getAuthentication()) {
            case ANONYMOUS:
            case GUEST:
                return new XctxAuthenticated(guestUser, context);
            case ANONYMOUS_PASSWORD:
                if (Strings.isNotEmpty(password) && password.equals(services.getServiceSafe(PasswordMechRegistry.class).get(ShareConstants.PASSWORD_MECH_ID).decode(guestUser.getUserPassword(), guestUser.getSalt()))) {
                    return new XctxAuthenticated(guestUser, context);
                }
                return null;
            case GUEST_PASSWORD:
                if (Strings.isNotEmpty(password) && services.getServiceSafe(GuestService.class).authenticate(guestUser, context.getContextId(), password)) {
                    return new XctxAuthenticated(guestUser, context);
                }
                return null;
            default:
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Unknown authentication " + guestInfo.getAuthentication());
        }
    }

}
