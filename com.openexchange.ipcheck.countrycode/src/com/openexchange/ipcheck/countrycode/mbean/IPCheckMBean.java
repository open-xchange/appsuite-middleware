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

package com.openexchange.ipcheck.countrycode.mbean;


/**
 * {@link IPCheckMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface IPCheckMBean {

    static String NAME = "com.openexchange.metrics" + ":type=" + IPCheckMetricCollector.COMPONENT_NAME + ",name=percentages";

    /**
     * Returns the total amount of IP checks per hour
     *
     * @return the total amount of IP checks per hour
     */
    long getIPChangesPerHour();

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

    long getAcceptedIPChangesPerHour();

    long getDeniedIPChangesPerHour();
}
