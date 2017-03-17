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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ipcheck.countrycode.mbean;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link IPCheckMetrics}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class IPCheckMetrics {

    private final AtomicInteger totalIPChanges;
    private final AtomicInteger acceptedIPChanges;
    private final AtomicInteger deniedIPChanges;

    private final AtomicInteger acceptedPrivateIP;
    private final AtomicInteger acceptedWhiteListed;
    private final AtomicInteger acceptedEligibleIPChanges;

    private final AtomicInteger deniedException;
    private final AtomicInteger deniedCountryChanged;

    private float acceptedPercentage;
    private float deniedPercentage;

    private float acceptedPrivatePercentage;
    private float acceptedWhiteListedPercentage;
    private float acceptedEligilePercentage;
    private float deniedExceptionPercentage;
    private float deniedCountryChangedPercentage;

    private float acceptedPrivateOverallPercentage;
    private float acceptedWhiteListedOverallPercentage;
    private float acceptedEligileOverallPercentage;
    private float deniedExceptionOverallPercentage;
    private float deniedCountryChangedOverallPercentage;

    /**
     * Initialises a new {@link IPCheckMetrics}.
     */
    public IPCheckMetrics() {
        super();

        totalIPChanges = new AtomicInteger();
        acceptedIPChanges = new AtomicInteger();
        deniedIPChanges = new AtomicInteger();
        acceptedPrivateIP = new AtomicInteger();
        acceptedWhiteListed = new AtomicInteger();
        acceptedEligibleIPChanges = new AtomicInteger();

        deniedException = new AtomicInteger();
        deniedCountryChanged = new AtomicInteger();
    }

    public synchronized void calculatePercentages() {
        // Work with local copies
        int total = totalIPChanges.get();
        int totalAccepted = acceptedIPChanges.get();
        int totalDenied = deniedIPChanges.get();
        acceptedPercentage = ((float) totalAccepted / total) * 100;
        deniedPercentage = ((float) totalDenied / total) * 100;

        // Accepted percentages
        int acceptedPrivate = acceptedPrivateIP.get();
        acceptedPrivatePercentage = ((float) acceptedPrivate / totalAccepted) * 100;
        int acceptedWL = acceptedWhiteListed.get();
        acceptedWhiteListedPercentage = ((float) acceptedWL / totalAccepted) * 100;
        int acceptedEligible = acceptedEligibleIPChanges.get();
        acceptedEligilePercentage = ((float) acceptedEligible / totalAccepted) * 100;

        // Overall accepted percentages
        acceptedPrivateOverallPercentage = ((float) acceptedPrivate / total) * 100;
        acceptedWhiteListedOverallPercentage = ((float) acceptedWL / total) * 100;
        acceptedEligileOverallPercentage = ((float) acceptedEligible / total) * 100;

        // Denied percentages
        int deniedEx = deniedException.get();
        deniedExceptionPercentage = ((float) deniedEx / totalDenied) * 100;
        int deniedCC = deniedCountryChanged.get();
        deniedCountryChangedPercentage = ((float) deniedCC / totalDenied) * 100;

        // Overall denied percentages
        deniedExceptionOverallPercentage = ((float) deniedEx / total) * 100;
        deniedCountryChangedOverallPercentage = ((float) deniedCC / total) * 100;
    }

    public void incrementTotalIPChanges() {
        totalIPChanges.incrementAndGet();
    }

    public void incrementAcceptedIPChanges() {
        acceptedIPChanges.incrementAndGet();
    }

    public void incrementDeniedIPChanges() {
        deniedIPChanges.incrementAndGet();
    }

    public void incrementAcceptedPrivateIP() {
        acceptedPrivateIP.incrementAndGet();
    }

    public void incrementAcceptedWhiteListed() {
        acceptedWhiteListed.incrementAndGet();
    }

    public void incrementAcceptedEligibleIPChange() {
        acceptedEligibleIPChanges.incrementAndGet();
    }

    public void incrementDeniedException() {
        deniedException.incrementAndGet();
    }

    public void incrementDeniedCountryChange() {
        deniedCountryChanged.incrementAndGet();
    }

    public int getTotalIPChanges() {
        return totalIPChanges.get();
    }

    public int getAcceptedIPChanges() {
        return acceptedIPChanges.get();
    }

    /**
     * Gets the denied ip changes
     * 
     * @return the denied ip changes
     */
    public int getDeniedIPChanges() {
        return deniedIPChanges.get();
    }

    /**
     * Gets the acceptedPrivateIP
     *
     * @return The acceptedPrivateIP
     */
    public int getAcceptedPrivateIP() {
        return acceptedPrivateIP.get();
    }

    /**
     * Gets the acceptedWhiteListed
     *
     * @return The acceptedWhiteListed
     */
    public int getAcceptedWhiteListed() {
        return acceptedWhiteListed.get();
    }

    /**
     * Gets the acceptedCountryNotChanged
     *
     * @return The acceptedCountryNotChanged
     */
    public int getAcceptedEligibleIPChanges() {
        return acceptedEligibleIPChanges.get();
    }

    /**
     * Gets the deniedException
     *
     * @return The deniedException
     */
    public int getDeniedException() {
        return deniedException.get();
    }

    /**
     * Gets the deniedDefault
     *
     * @return The deniedDefault
     */
    public int getDeniedCountryChanges() {
        return deniedCountryChanged.get();
    }

    /**
     * Gets the deniedCountryChanged
     *
     * @return The deniedCountryChanged
     */
    public AtomicInteger getDeniedCountryChanged() {
        return deniedCountryChanged;
    }

    /**
     * Gets the acceptedPercentage
     *
     * @return The acceptedPercentage
     */
    public float getAcceptedPercentage() {
        return acceptedPercentage;
    }

    /**
     * Gets the deniedPercentage
     *
     * @return The deniedPercentage
     */
    public float getDeniedPercentage() {
        return deniedPercentage;
    }

    /**
     * Gets the acceptedPrivatePercentage
     *
     * @return The acceptedPrivatePercentage
     */
    public float getAcceptedPrivatePercentage() {
        return acceptedPrivatePercentage;
    }

    /**
     * Gets the acceptedWhiteListedPercentage
     *
     * @return The acceptedWhiteListedPercentage
     */
    public float getAcceptedWhiteListedPercentage() {
        return acceptedWhiteListedPercentage;
    }

    /**
     * Gets the acceptedEligilePercentage
     *
     * @return The acceptedEligilePercentage
     */
    public float getAcceptedEligilePercentage() {
        return acceptedEligilePercentage;
    }

    /**
     * Gets the deniedExceptionPercentage
     *
     * @return The deniedExceptionPercentage
     */
    public float getDeniedExceptionPercentage() {
        return deniedExceptionPercentage;
    }

    /**
     * Gets the deniedCountryChangedPercentage
     *
     * @return The deniedCountryChangedPercentage
     */
    public float getDeniedCountryChangedPercentage() {
        return deniedCountryChangedPercentage;
    }

    /**
     * Gets the acceptedPrivateOverallPercentage
     *
     * @return The acceptedPrivateOverallPercentage
     */
    public float getAcceptedPrivateOverallPercentage() {
        return acceptedPrivateOverallPercentage;
    }

    /**
     * Gets the acceptedWhiteListedOverallPercentage
     *
     * @return The acceptedWhiteListedOverallPercentage
     */
    public float getAcceptedWhiteListedOverallPercentage() {
        return acceptedWhiteListedOverallPercentage;
    }

    /**
     * Gets the acceptedEligileOverallPercentage
     *
     * @return The acceptedEligileOverallPercentage
     */
    public float getAcceptedEligileOverallPercentage() {
        return acceptedEligileOverallPercentage;
    }

    /**
     * Gets the deniedExceptionOverallPercentage
     *
     * @return The deniedExceptionOverallPercentage
     */
    public float getDeniedExceptionOverallPercentage() {
        return deniedExceptionOverallPercentage;
    }

    /**
     * Gets the deniedCountryChangedOverallPercentage
     *
     * @return The deniedCountryChangedOverallPercentage
     */
    public float getDeniedCountryChangedOverallPercentage() {
        return deniedCountryChangedOverallPercentage;
    }
}
