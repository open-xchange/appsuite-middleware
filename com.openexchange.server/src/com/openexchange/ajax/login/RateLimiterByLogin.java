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

package com.openexchange.ajax.login;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.SimpleInMemoryRateLimiter;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.listener.LoginListener;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

/**
 * {@link RateLimiterByLogin}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class RateLimiterByLogin implements LoginListener {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RateLimiterByLogin.class);
    }

    private final LoadingCache<String, Bucket> limiters;

    /**
     * Initializes a new {@link RateLimiterByLogin}.
     *
     * @param permits The number of permits
     * @param timeFrameInSeconds The time frame in seconds
     */
    public RateLimiterByLogin(final int permits, final long timeFrameInSeconds) {
        super();
        CacheLoader<String, Bucket> loader = new CacheLoader<String, Bucket>() {

            @Override
            public Bucket load(String key) {
                long capacity = permits;
                Refill refill = Refill.intervally(permits, Duration.ofSeconds(timeFrameInSeconds));
                Bandwidth limit = Bandwidth.classic(capacity, refill);
                return Bucket4j.builder().addLimit(limit).build();
            }
        };
        limiters = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build(loader);
    }

    @Override
    public void onBeforeAuthentication(LoginRequest request, Map<String, Object> properties) throws OXException {
        String login = request.getLogin();
        if (Strings.isNotEmpty(login)) {
            Bucket rateLimiter = limiters.getIfPresent(login);
            if (null != rateLimiter && !rateLimiter.estimateAbilityToConsume(1).canBeConsumed()) {
                // Rate limit is already exhausted
                LoggerHolder.LOG.debug("Too many preceding failed login attempts due to invalid credentials for login {}", login);
                throw LoginExceptionCodes.TOO_MANY_LOGIN_ATTEMPTS.create(login);
            }
        }
    }

    @Override
    public void onSucceededAuthentication(LoginResult result) throws OXException {
        String login = result.getRequest().getLogin();
        if (Strings.isNotEmpty(login)) {
            // Drop rate limiter
            LoggerHolder.LOG.debug("Detected successful login attempt for login {}", login);
            limiters.invalidate(login);
        }
    }

    @Override
    public void onFailedAuthentication(LoginRequest request, Map<String, Object> properties, OXException e) throws OXException {
        if (LoginExceptionCodes.INVALID_CREDENTIALS.equals(e) || LoginExceptionCodes.INVALID_GUEST_PASSWORD.equals(e)) {
            String login = request.getLogin();
            if (Strings.isNotEmpty(login)) {
                // Consume...
                LoggerHolder.LOG.debug("Detected failed login attempt due to invalid credentials for login {}", login);
                limiters.getUnchecked(login).tryConsume(1);
            }
        }
    }

    @Override
    public void onRedirectedAuthentication(LoginRequest request, Map<String, Object> properties, OXException e) throws OXException {
        // Don't care
    }

}
