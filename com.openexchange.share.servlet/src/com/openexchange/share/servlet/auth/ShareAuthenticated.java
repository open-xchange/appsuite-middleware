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

package com.openexchange.share.servlet.auth;

import com.openexchange.authentication.GuestAuthenticated;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;

/**
 * {@link AuthenticationMode}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareAuthenticated implements GuestAuthenticated, SessionEnhancement {

    private final User user;
    private final Context context;
    private final SessionEnhancement enhancement;;

    /**
     * Initializes a new {@link ShareAuthenticated}.
     *
     * @param user The user
     * @param context The context
     * @param enhancement The session enhancement delegate, or <code>null</code> if not applicable
     */
    public ShareAuthenticated(User user, Context context, SessionEnhancement enhancement) {
        super();
        this.user = user;
        this.context = context;
        this.enhancement = enhancement;
    }

    /**
     * Gets the user
     *
     * @return The user
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the context
     *
     * @return The context
     */
    public Context getContext() {
        return context;
    }

    @Override
    public String getContextInfo() {
        String[] loginInfo = context.getLoginInfo();
        return null != loginInfo && 0 < loginInfo.length ? loginInfo[0] : context.getName();
    }

    @Override
    public String getUserInfo() {
        return user.getMail();
    }

    @Override
    public int getContextID() {
        return context.getContextId();
    }

    @Override
    public int getUserID() {
        return user.getId();
    }

    @Override
    public void enhanceSession(Session session) {
        if (null != enhancement) {
            enhancement.enhanceSession(session);
        }
    }

}
