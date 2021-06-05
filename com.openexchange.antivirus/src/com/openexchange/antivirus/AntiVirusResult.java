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

package com.openexchange.antivirus;

import java.io.Serializable;
import com.openexchange.exception.OXException;

/**
 * {@link AntiVirusResult}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public interface AntiVirusResult extends Serializable {

    /**
     * Returns the Anti-Virus service id as returned from
     * the ICAP Server (if available).
     *
     * @return the Anti-Virus service id, or an empty string if none available
     */
    String getAntiVirusServiceId();

    /**
     * Returns the ISTag (a.k.a. ICAP Service Tag) of the Anti-Virus service.
     * This tag represents the service's current state and validates that
     * all previous ICAP responses are still considered fresh. If the ISTag is
     * changed then all previous responses are considered invalid.
     *
     * @return the ISTag of the Anti-Virus service never <code>null</code>.
     */
    String getISTag();

    /**
     * Returns the name of the found thread
     *
     * @return the name of the found thread
     */
    String getThreatName();

    /**
     * Returns whether the scan result yielded an infected status.
     *
     * @return <code>true</code> if an infection was detected; <code>false</code> if clean;
     *         <code>null</code> if scanning was not performed.
     */
    Boolean isInfected();

    /**
     * Returns the error that may have occurred during the anti-virus scanning process.
     *
     * @return the error that may have occurred during the anti-virus scanning process
     *         or <code>null</code> if no error occurred.
     */
    OXException getError();

    /**
     * Returns the scan time stamp
     *
     * @return The scan time stamp
     */
    long getScanTimestamp();

    /**
     * Returns <code>true</code> if the stream was indeed scanned, <code>false</code>
     * otherwise. This also indicates implicitly whether the result is being served
     * from a cache.
     *
     * @return Returns <code>true</code> if the stream was indeed scanned, <code>false</code> otherwise
     */
    boolean isStreamScanned();
}
