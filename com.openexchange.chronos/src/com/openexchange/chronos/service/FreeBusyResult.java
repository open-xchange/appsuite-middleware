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

package com.openexchange.chronos.service;

import java.util.List;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyResult}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FreeBusyResult {

    private List<OXException> warnings;
    private List<FreeBusyTime> freeBusyTimes;

    /**
     * Initialises a new {@link FreeBusyResult}.
     */
    public FreeBusyResult() {
        super();
    }

    /**
     * Initialises a new {@link FreeBusyResult}.
     *
     * @param freeBusyTimes The free/busy times to take over
     * @param The warnings to take over
     */
    public FreeBusyResult(List<FreeBusyTime> freeBusyTimes, List<OXException> warnings) {
        this();
        this.freeBusyTimes = freeBusyTimes;
        this.warnings = warnings;
    }

    /**
     * Gets the warnings
     *
     * @return The warnings
     */
    public List<OXException> getWarnings() {
        return warnings;
    }

    /**
     * Sets the warnings
     *
     * @param warnings The warnings to set
     */
    public void setWarnings(List<OXException> warnings) {
        this.warnings = warnings;
    }

    /**
     * Gets the freeBusyTimes
     *
     * @return The freeBusyTimes
     */
    public List<FreeBusyTime> getFreeBusyTimes() {
        return freeBusyTimes;
    }

    /**
     * Sets the freeBusyTimes
     *
     * @param freeBusyTimes The freeBusyTimes to set
     */
    public void setFreeBusyTimes(List<FreeBusyTime> freeBusyTimes) {
        this.freeBusyTimes = freeBusyTimes;
    }
}
