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

import org.apache.commons.lang.Validate;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.userconf.UserPermissionService;

/**
 * Checks if the user with provided session does have the capability to change the password ('edit_password').
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.1
 */
public class EditPasswordCapabilityChecker implements CapabilityChecker {

    public final static String EDIT_PASSWORD_CAP = "edit_password";
    private ServiceLookup serviceLookup;

    public EditPasswordCapabilityChecker(ServiceLookup serviceLookup) {
        Validate.notNull(serviceLookup);
        this.serviceLookup = serviceLookup;
    }

    @Override
    public boolean isEnabled(String capability, Session ses) throws OXException {
        if (EDIT_PASSWORD_CAP.equals(capability)) {
            if ((Strings.isEmpty(capability)) || (ses == null)) {
                return false;
            }

            final ServerSession session = ServerSessionAdapter.valueOf(ses);
            if ((session == null) || (session.isAnonymous())) {
                return false;
            }
            int contextId = session.getContextId();
            int userId = session.getUserId();
            if ((contextId > 0) && (userId > 0)) {
                User user = session.getUser();
                if ((user != null) && (user.isGuest())) {
                    if (Strings.isNotEmpty(user.getMail())) {
                        return true;
                    }
                } else {
                    PasswordChangeService optionalService = serviceLookup.getOptionalService(PasswordChangeService.class);
                    if (optionalService != null) {
                        final UserPermissionService userPermissionService = serviceLookup.getService(UserPermissionService.class);
                        if (userPermissionService != null) {
                            UserPermissionBits userPermissionBits = userPermissionService.getUserPermissionBits(userId, session.getContext());
                            if (userPermissionBits != null) {
                                return userPermissionBits.hasPermission(Permission.EDIT_PASSWORD);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static Class<?>[] getNeededServices() {
        return new Class<?>[] { UserPermissionService.class };
    }
}
