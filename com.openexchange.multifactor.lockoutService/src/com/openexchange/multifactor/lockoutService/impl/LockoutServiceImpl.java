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

package com.openexchange.multifactor.lockoutService.impl;

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
            throw MultifactorLockoutExceptionCodes.MISSING_AUTHENTICATION_LOCKOUT.create(getLockoutTime(userId, contextId));
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
