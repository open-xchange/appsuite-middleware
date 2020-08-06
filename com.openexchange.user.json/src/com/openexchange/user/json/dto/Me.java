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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.user.json.dto;

import java.util.Objects;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;

/**
 * {@link Me}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class Me {

    private final User user;

    private final Context context;

    private final String loginName;

    private final String mailLogin;


    /**
     * Initializes a new {@link Me}.
     *
     * @param user The current session user
     * @param context The users' context
     * @param loginName The login name used to create the current user's session
     * @param mailLogin The users' mail login used to authenticate at the primary mail account
     */
    public Me(@NonNull User user, @NonNull Context context, @Nullable String loginName, @Nullable String mailLogin) {
        super();
        this.user = Objects.requireNonNull(user);
        this.context = Objects.requireNonNull(context);
        this.loginName = loginName;
        this.mailLogin = mailLogin;
    }

    /**
     * Gets the User
     *
     * @return The {@link User}
     */
    @SuppressWarnings("null")
    public @NonNull User getUser() {
        return user;
    }

    /**
     * Gets the context
     *
     * @return The {@link Context}
     */
    @SuppressWarnings("null")
    public @NonNull Context getContext() {
        return context;
    }

    /**
     * Gets the users' mail login name used to authenticate at the primary mail account
     *
     * @return The login
     */
    public @Nullable String getMailLogin() {
        return mailLogin;
    }

    /**
     *  Get the login name used to create the current user's session
     *
     *  @return The login
     */
    public @Nullable String getLoginName() {
        return loginName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, context, loginName, mailLogin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Me other = (Me) obj;
        return Objects.equals(user, other.user)
            && Objects.equals(context, other.context)
            && Objects.equals(loginName, other.loginName)
            && Objects.equals(mailLogin, other.mailLogin);
    }

    @Override
    public String toString() {
        return "Me [user=" + user + ", context=" + context + ", loginName=" + loginName + ", mailLogin=" + mailLogin + "]";
    }

}
