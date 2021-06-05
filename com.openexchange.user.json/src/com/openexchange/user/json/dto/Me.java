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
