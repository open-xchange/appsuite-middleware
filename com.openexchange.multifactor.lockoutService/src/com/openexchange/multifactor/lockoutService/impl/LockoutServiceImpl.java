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

package com.openexchange.multifactor.lockoutService.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.concurrent.TimeUnit;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorLockoutService;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.lockoutService.config.LockoutStringProperty;
import com.openexchange.multifactor.lockoutService.exceptions.MultifactorLockoutExceptionCodes;
import com.openexchange.ratelimit.Rate;
import com.openexchange.ratelimit.RateLimiterFactory;

/**
 * {@link LockoutServiceImpl}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class LockoutServiceImpl implements MultifactorLockoutService {

    private static final String REGION_NAME = "multifactor";

    private final LeanConfigurationService configService;
    private final RateLimiterFactory rateLimiterFactory;

    public LockoutServiceImpl(LeanConfigurationService configService, RateLimiterFactory rateLimiter) {
        this.configService = configService;
        this.rateLimiterFactory = rateLimiter;
    }

    /**
     * Generates a simple key for cache based on userId and contextId
     *
     * @param userId The user id
     * @param contextId The context id
     * @return String The key
     */
    static String newDelayKey(int userId, int contextId) {
        return userId + "." + contextId;
    }

    @Override
    public int getMaxBadAttempts (int userId, int contextId) {
        return configService.getIntProperty(userId, contextId, LockoutStringProperty.maxBadAttempts);
    }

    /**
     * Create a {@link Rate} for the user for use in rateLimiter
     *
     * @param userId The user id
     * @param contextId The context id
     * @return Rate based on the configuration for the user
     */
    private Rate createRate (int userId, int contextId) {
        return Rate.create(getMaxBadAttempts(userId, contextId), TimeUnit.MINUTES.toMillis(getLockoutTime(userId, contextId)));
    }


    /**
     * Time period for rate limiter. Acts as a lockout time once number of bad attempts exceeded
     *
     * @param userId The user id
     * @param contextId The context id
     * @return Lockout time The lockout time in minutes
     */
    private int getLockoutTime(int userId, int contextId) {
        return configService.getIntProperty(userId, contextId, LockoutStringProperty.lockoutTime);
    }


    @Override
    public void checkLockedOut(MultifactorRequest multifactorRequest) throws OXException {
        checkLockedOut(multifactorRequest.getUserId(), multifactorRequest.getContextId());
    }

    @Override
    public void checkLockedOut(int userId, int contextId) throws OXException {
        if (rateLimiterFactory.createLimiter(REGION_NAME, createRate(userId, contextId), userId, contextId).exceeded()) {
            throw MultifactorLockoutExceptionCodes.MISSING_AUTHENTICATION_LOCKOUT.create(I(getLockoutTime(userId, contextId)));
        }
    }

    @Override
    public void registerFailedAttempt(int userId, int contextId) throws OXException {
        rateLimiterFactory.createLimiter(REGION_NAME, createRate(userId, contextId), userId, contextId).acquire();
    }

    @Override
    public void registerSuccessfullLogin(int userId, int contextId) throws OXException {
        rateLimiterFactory.createLimiter(REGION_NAME, createRate(userId, contextId), userId, contextId).reset();
    }


}
