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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.login.internal;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.UserService;

/**
 * The {@link LoginNameRecorder} stores the users login name as an user attribute.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class LoginNameRecorder implements LoginHandlerService {

    private UserService userService = null;
    public final static String LOGIN_ATTRIBUTE_NAME = LoginNameRecorder.class.getSimpleName().toLowerCase() + "/user_login";

    public LoginNameRecorder() {
        super();

    }

    @Override
    public void handleLogin(LoginResult login) throws OXException {
        String loginValue = login.getSession().getLogin();
        Context ctx = login.getContext();
        if (login.getContext().isReadOnly()) {
            return;
        }

        if (userService == null) {
            userService = ServerServiceRegistry.getInstance().getService(UserService.class);
        }
        try {
            userService.setAttribute(LOGIN_ATTRIBUTE_NAME, loginValue, login.getUser().getId(), ctx);
        } catch (OXException ex) {
            if (ex.getCode() == UserExceptionCode.CONCURRENT_ATTRIBUTES_UPDATE.getNumber() && ex.getPrefix().equalsIgnoreCase(UserExceptionCode.CONCURRENT_ATTRIBUTES_UPDATE.getPrefix())) {
                //do nothing
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void handleLogout(LoginResult logout) throws OXException {
        // nothing to do
    }
}
