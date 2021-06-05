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

package com.openexchange.passwordchange;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.guest.GuestService;
import com.openexchange.java.Strings;
import com.openexchange.password.mechanism.PasswordDetails;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.password.mechanism.stock.StockPasswordMechs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link DefaultBasicPasswordChangeService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DefaultBasicPasswordChangeService extends BasicPasswordChangeService {

    /**
     * Initializes a new {@link DefaultBasicPasswordChangeService}.
     *
     * @param services
     */
    public DefaultBasicPasswordChangeService() {
        super();
    }

    @Override
    protected void update(PasswordChangeEvent event) throws OXException {

        Context ctx = event.getContext();

        UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
        if (userService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
        }
        User user = userService.getUser(event.getSession().getUserId(), ctx);
        UserImpl updatedUser = new UserImpl(user);

        prepareUserUpdate(event, user, updatedUser);

        userService.updatePassword(updatedUser, ctx);
        userService.invalidateUser(ctx, event.getSession().getUserId());

        if (updatedUser.isGuest()) {
            GuestService guestService = ServerServiceRegistry.getInstance().getService(GuestService.class);
            if (guestService != null) {
                guestService.updateGuestUser(updatedUser, ctx.getContextId());
            }
        }
    }

    /**
     * @param event
     * @param user
     * @param updatedUser
     * @throws OXException
     */
    protected void prepareUserUpdate(PasswordChangeEvent event, User user, UserImpl updatedUser) throws OXException {
        if (Strings.isEmpty(event.getNewPassword())) {
            updatedUser.setUserPassword(null);
            updatedUser.setSalt(null);
        } else {
            PasswordMechRegistry passwordMechRegistry = ServerServiceRegistry.getInstance().getService(PasswordMechRegistry.class);
            PasswordMech passwordMech = passwordMechRegistry.get(user.getPasswordMech());
            if (passwordMech == null) {
                passwordMech = StockPasswordMechs.BCRYPT.getPasswordMech();
            }
            PasswordDetails passwordDetails = passwordMech.encode(event.getNewPassword());
            updatedUser.setPasswordMech(passwordDetails.getPasswordMech());
            updatedUser.setUserPassword(passwordDetails.getEncodedPassword());
            updatedUser.setSalt(passwordDetails.getSalt());
        }
    }
}
