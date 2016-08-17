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

package com.openexchange.passwordchange;

import org.apache.commons.lang.Validate;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
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
