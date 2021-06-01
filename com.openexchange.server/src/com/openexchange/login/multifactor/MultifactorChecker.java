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

package com.openexchange.login.multifactor;

import java.util.Optional;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.login.LoginRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link MultifactorChecker}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.1
 */
public class MultifactorChecker {

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link MultifactorChecker}.
     *
     * @param serviceLookup The service look-up
     */
    public MultifactorChecker(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    /**
     * Performs multi-factor authentication for a user if enabled
     *
     * @param request The login request
     * @param context The user's context
     * @param user The user
     */
    public Optional<SessionEnhancement> checkMultiFactorAuthentication(LoginRequest request, Context context, User user) {
        MultifactorLoginService multifactorService = serviceLookup.getOptionalService(MultifactorLoginService.class);
        if (multifactorService == null) {
            return Optional.empty();
        }

        try {
            if (multifactorService.checkMultiFactorAuthentication(user.getId(), context.getContextId(), user.getLocale(), request)) {
                return Optional.of(new SessionEnhancement() {

                    @Override
                    public void enhanceSession(Session session) {
                        session.setParameter(Session.MULTIFACTOR_PARAMETER, Boolean.TRUE);
                        session.setParameter(Session.MULTIFACTOR_AUTHENTICATED, Boolean.TRUE);
                    }
                });
            }

            // No multifactor required
            return Optional.empty();
        } catch (OXException ex) {
            // Failed to authenticate
            return Optional.of(new SessionEnhancement() {

                @Override
                public void enhanceSession(Session session) {
                    session.setParameter(Session.MULTIFACTOR_PARAMETER, Boolean.TRUE);
                }
            });
        }
    }

    /**
     * Returns true if session is marked for multifactor but not yet authenticated
     *
     * @param session The user session
     * @return <code>true</code> if session is marked for multifactor but not yet authenticated
     */
    public static boolean requiresMultifactor(Session session) {
        return (Boolean.TRUE.equals(session.getParameter(Session.MULTIFACTOR_PARAMETER)) && !Boolean.TRUE.equals(session.getParameter(Session.MULTIFACTOR_AUTHENTICATED)));
    }

}
