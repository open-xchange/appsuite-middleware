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
 *     Copyright (C) 2018-2020 OX Software GmbH
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
     * Returns the scan timestamp
     * 
     * @return The scan timestamp
     */
    long getScanTimestamp();

    /**
     * Sets whether the stream was actually scanned.
     * 
     * @param scanned whether the stream was actually scanned
     */
    void setStreamScanned(boolean scanned);

    /**
     * Returns <code>true</code> if the stream was indeed scanned, <code>false</code>
     * otherwise. This also indicates implicitly whether the result is being served
     * from a cache.
     * 
     * @return Returns <code>true</code> if the stream was indeed scanned, <code>false</code> otherwise
     */
    boolean isStreamScanned();
}
