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

package com.openexchange.ratelimit;


/**
 * {@link Rate} defines a rate limit (the amount and time frame).
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class Rate {

    /**
     * Creates a new rate for given max. amount of permits in specified time frame.
     *
     * @param amount The max. number of permits
     * @param timeframe The time frame in milliseconds, in which at max given number of permits are legit
     * @return The new rate
     */
    public static Rate create(int amount, long timeframe) {
        return new Rate(amount, timeframe);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final int amount;
    private final long timeframe;

    /**
     * Initializes a new {@link Rate}.
     */
    private Rate(int amount, long timeframe) {
        this.amount = amount;
        this.timeframe = timeframe;
    }

    /**
     * Gets the amount
     *
     * @return The amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets the time frame in milliseconds
     *
     * @return The time frame
     */
    public long getTimeframe() {
        return timeframe;
    }

    /**
     * Checks is this rate is effectively enabled; that is specified max. number of permits is greater than 0 (zero).
     *
     * @return <code>true</code> if enabled; other wise <code>false</code>
     */
    public boolean isEnabled() {
        return amount >= 0;
    }
}
