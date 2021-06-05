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

package com.openexchange.share.servlet.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.java.util.UUIDs;

/**
 * {@link LoginLocationRegistry} - A simple registry for login locations with auto-expiration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class LoginLocationRegistry {

    private static final LoginLocationRegistry INSTANCE = new LoginLocationRegistry();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static LoginLocationRegistry getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private final Cache<String, LoginLocation> cache;

    /**
     * Initializes a new {@link LoginLocationRegistry}.
     */
    private LoginLocationRegistry() {
        super();
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .maximumSize(Integer.MAX_VALUE)
            .initialCapacity(256)
            .expireAfterAccess(300000, TimeUnit.MILLISECONDS);
        Cache<String, LoginLocation> cache = cacheBuilder.build();
        this.cache = cache;
    }

    /**
     * Gets the login location associated with specified token (if any)
     *
     * @param token The token
     * @return The associated login location or <code>null</code>
     */
    public LoginLocation getIfPresent(String token) {
        return cache.getIfPresent(token);
    }

    /**
     * Puts specified login location into this registry and performs the redirect using {@link LoginLocation#DEFAULT_ALLOWED_ATTRIBUTES default allowed attributes}.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Note: Specified <tt>HttpServletResponse</tt> is required to be not yet in {@link HttpServletResponse#isCommitted() committed} state. Otherwise calling this method has no effect.
     * </div>
     * <p>
     *
     * @param loginLocation The associated login location
     * @param response The associated HTTP response
     * @throws IOException If redirect fails due to an I/O error
     */
    public void putAndRedirect(LoginLocation loginLocation, HttpServletResponse response) throws IOException {
        putAndRedirect(loginLocation, response, LoginLocation.DEFAULT_ALLOWED_ATTRIBUTES);
    }

    /**
     * Puts specified login location into this registry and performs the redirect.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Note: Specified <tt>HttpServletResponse</tt> is required to be not yet in {@link HttpServletResponse#isCommitted() committed} state. Otherwise calling this method has no effect.
     * </div>
     * <p>
     *
     * @param loginLocation The associated login location
     * @param response The associated HTTP response
     * @param allowedAttributes Specifies those attributes kept in given <code>LoginLocation</code> instance that are allowed to be passed to client
     * @throws IOException If redirect fails due to an I/O error
     */
    public void putAndRedirect(LoginLocation loginLocation, HttpServletResponse response, Collection<String> allowedAttributes) throws IOException {
        if (response.isCommitted()) {
            // HTTP response has already been comitted. Most likely because outgoing data were already in transfer, but connection to client got lost.
            // Hence, no redirect possible...
            return;
        }

        String token = UUIDs.getUnformattedString(UUID.randomUUID());
        cache.put(token, loginLocation);
        response.sendRedirect(LoginLocation.buildRedirectWith(token, loginLocation, allowedAttributes, null));
    }

    /**
     * Looks-up the login location associated with specified token and discards it if present.
     *
     * @param token The token
     * @return The invalidated login location or <code>null</code>
     */
    public LoginLocation getAndInvalidateIfPresent(String token) {
        return cache.asMap().remove(token);
    }

    /**
     * Discards the login location associated with specified token.
     *
     * @param token The token
     */
    public void invalidate(String token) {
        cache.invalidate(token);
    }

    /**
     * Discards all login locations in this registry.
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * Gets the number of login locations currently held in this registry
     *
     * @return The number of login locations
     */
    public long size() {
        return cache.size();
    }

    /**
     * Cleans-up this registry.
     */
    public void cleanUp() {
        cache.cleanUp();
    }

}
