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

/**
 * {@link IPCheckMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface IPCheckMBean {

    static final String DOMAIN = "com.openexchange.ipcheck.countrycode";

    static String NAME = "IPCheck MBean";

    /**
     * Returns the total amount of IP changes that were observed in sessions
     * 
     * @return the total amount of IP changes
     */
    int getIPChanges();

    /**
     * Returns the total amount of accepted IP changes
     * 
     * @return the total amount of accepted IP changes
     */
    int getAcceptedIPChanges();

    /**
     * Returns the total amount of denied IP changes
     * 
     * @return the total amount of denied IP changes
     */
    int getDeniedIPChanges();

    /**
     * Returns the amount of accepted IP changes due to a private IPv4 change
     * 
     * @return the amount of accepted IP changes due to a private IPv4 change
     */
    int getAcceptedPrivateIPChanges();

    /**
     * Returns the amount of accepted IP changes due to a white-listed IP
     * 
     * @return the amount of accepted IP changes due to a white-listed IP
     */
    int getAcceptedWhiteListedIPChanges();

    /**
     * Returns the amount of denied IP changes due to country changes
     * 
     * @return the amount of denied IP changes due to country changes
     */
    int getDeniedCountryChanges();

    /**
     * Returns the amount of denied IP changes due to an exception
     * 
     * @return the amount of denied IP changes due to an exception
     */
    int getDeniedExceptionIPChanges();

    /**
     * Returns the amount of accepted eligible IP changes
     * 
     * @return the amount of accepted eligible IP changes
     */
    int getAcceptedEligibleIPChanges();

    /**
     * Gets the acceptedPercentage
     *
     * @return The acceptedPercentage
     */
    float getAcceptedPercentage();

    /**
     * Gets the deniedPercentage
     *
     * @return The deniedPercentage
     */
    float getDeniedPercentage();

    /**
     * Gets the acceptedPrivatePercentage
     *
     * @return The acceptedPrivatePercentage
     */
    float getAcceptedPrivatePercentage();

    /**
     * Gets the acceptedWhiteListedPercentage
     *
     * @return The acceptedWhiteListedPercentage
     */
    float getAcceptedWhiteListedPercentage();

    /**
     * Gets the acceptedEligilePercentage
     *
     * @return The acceptedEligilePercentage
     */
    float getAcceptedEligilePercentage();

    /**
     * Gets the deniedExceptionPercentage
     *
     * @return The deniedExceptionPercentage
     */
    float getDeniedExceptionPercentage();

    /**
     * Gets the deniedCountryChangedPercentage
     *
     * @return The deniedCountryChangedPercentage
     */
    float getDeniedCountryChangedPercentage();

    /**
     * Gets the acceptedPrivateOverallPercentage
     *
     * @return The acceptedPrivateOverallPercentage
     */
    float getAcceptedPrivateOverallPercentage();

    /**
     * Gets the acceptedWhiteListedOverallPercentage
     *
     * @return The acceptedWhiteListedOverallPercentage
     */
    float getAcceptedWhiteListedOverallPercentage();

    /**
     * Gets the acceptedEligileOverallPercentage
     *
     * @return The acceptedEligileOverallPercentage
     */
    float getAcceptedEligileOverallPercentage();

    /**
     * Gets the deniedExceptionOverallPercentage
     *
     * @return The deniedExceptionOverallPercentage
     */
    float getDeniedExceptionOverallPercentage();

    /**
     * Gets the deniedCountryChangedOverallPercentage
     *
     * @return The deniedCountryChangedOverallPercentage
     */
    float getDeniedCountryChangedOverallPercentage();
    
    long getAcceptedIPChangesPerMinute();
    
    long getDeniedIPChangesPerMinute();
}
