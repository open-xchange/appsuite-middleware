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

package com.openexchange.cluster.lock.policies;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * {@link ExponentialBackOffRetryPolicy}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ExponentialBackOffRetryPolicy implements RetryPolicy {

    private final int maxTries;
    private int retryCount = 1;
    private final Random random;
    private double multiplier = 1.5;
    private double randomFactor = 0.5;
    private double interval = 0.5;

    /**
     * Initialises a new {@link ExponentialBackOffRetryPolicy} with a default amount of 10 retries
     */
    public ExponentialBackOffRetryPolicy() {
        this(10);
    }

    /**
     * Initialises a new {@link ExponentialBackOffRetryPolicy}.
     * 
     * @param maxTries The amount of maximum retries
     */
    public ExponentialBackOffRetryPolicy(int maxTries) {
        super();
        this.maxTries = maxTries;
        random = new Random(System.nanoTime());
        randomFactor = random.nextDouble();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.policies.RetryPolicy#getMaxTries()
     */
    @Override
    public int getMaxTries() {
        return maxTries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.policies.RetryPolicy#retryCount()
     */
    @Override
    public int retryCount() {
        return retryCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.policies.RetryPolicy#isRetryAllowed()
     */
    @Override
    public boolean isRetryAllowed() {
        if (retryCount++ <= maxTries) {
            try {
                TimeUnit.MILLISECONDS.sleep(getSleepTime());
            } catch (InterruptedException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the sleep time in milliseconds
     * 
     * @return the sleep time in milliseconds
     */
    private long getSleepTime() {
        double max = interval * multiplier;
        double min = max - interval;
        interval = interval * multiplier;
        double factor = (randomFactor * (max - min)) * 1000;
        return (long) factor;
    }
}
