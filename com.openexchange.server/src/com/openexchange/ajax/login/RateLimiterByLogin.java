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
