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

package com.openexchange.rest.client.httpclient.internal.cookiestore;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

/**
 * {@link AccountAwareCookieStore} is a {@link CookieStore} which delegates its methods to the {@link MultiUserCookieStore}
 *
 * In order to do this it is created with the account informations and is used as a cache key for the cache of the {@link MultiUserCookieStore}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class AccountAwareCookieStore implements CookieStore {

    private final int userId;
    private final int contextId;
    private final String accountId;
    private final int hash;

    /**
     * Initializes a new {@link AccountAwareCookieStore}.
     *
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public AccountAwareCookieStore(String accountId, int userId, int contextId) {
        super();
        Objects.requireNonNull(accountId, "Account identifier must not be null");
        this.userId = userId;
        this.contextId = contextId;
        this.accountId = accountId;

        int prime = 31;
        int h = prime * 1 + contextId;
        h = prime * h + userId;
        h = prime * h + accountId.hashCode();
        hash = h;
    }

    @Override
    public void addCookie(Cookie cookie) {
        MultiUserCookieStore.getInstance().addCookie(this, cookie);
    }

    @Override
    public List<Cookie> getCookies() {
        return MultiUserCookieStore.getInstance().getCookies(this);
    }

    @Override
    public boolean clearExpired(Date date) {
        return MultiUserCookieStore.getInstance().clearExpired(this, date);
    }

    @Override
    public void clear() {
        MultiUserCookieStore.getInstance().clear(this);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AccountAwareCookieStore)) {
            return false;
        }
        AccountAwareCookieStore other = (AccountAwareCookieStore) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (accountId == null) {
            if (other.accountId != null) {
                return false;
            }
        } else if (!accountId.equals(other.accountId)) {
            return false;
        }
        return true;
    }

}
