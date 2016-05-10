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
     * Puts specified login location into this registry.
     *
     * @param loginLocation The associated login location
     * @return The token referring to the login location
     */
    public String put2(LoginLocation loginLocation) {
        String token = UUIDs.getUnformattedString(UUID.randomUUID());
        cache.put(token, loginLocation);
        return token;
    }

    /**
     * Puts specified login location into this registry and performs the redirect using {@link LoginLocation#DEFAULT_ALLOWED_ATTRIBUTES default allowed attributes}.
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
     *
     * @param loginLocation The associated login location
     * @param response The associated HTTP response
     * @param allowedAttributes Specifies those attributes kept in given <code>LoginLocation</code> instance that are allowed to be passed to client
     * @throws IOException If redirect fails due to an I/O error
     */
    public void putAndRedirect(LoginLocation loginLocation, HttpServletResponse response, Collection<String> allowedAttributes) throws IOException {
        String token = UUIDs.getUnformattedString(UUID.randomUUID());
        cache.put(token, loginLocation);
        response.sendRedirect(LoginLocation.buildRedirectWith(token, loginLocation, allowedAttributes));
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
