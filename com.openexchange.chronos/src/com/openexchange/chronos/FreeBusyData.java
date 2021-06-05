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

package com.openexchange.chronos;

import java.util.Date;
import java.util.List;
import org.dmfs.rfc5545.DateTime;

/**
 * {@link FreeBusyData}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.6.4">RFC 5545, section 3.6.4</a>
 */
public class FreeBusyData {

    private String uid;
    private Date timestamp;
    private DateTime startDate;
    private DateTime endDate;
    private Organizer organizer;
    private List<Attendee> attendees;
    private List<FreeBusyTime> freeBusyTimes;

    /**
     * Initializes a new {@link FreeBusyData}.
     */
    public FreeBusyData() {
        super();
    }

    /**
     * Gets the universal identifier of the free/busy data.
     *
     * @return The universal identifier
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the universal identifier of the free/busy data.
     *
     * @param value The universal identifier to set
     */
    public void setUid(String value) {
        uid = value;
    }

    /**
     * Gets the timestamp of the free/busy data.
     *
     * @return The timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the creation date of the free/busy data.
     *
     * @param value The timestamp to set
     */
    public void setTimestamp(Date value) {
        timestamp = value;
    }

    /**
     * Gets the start date of the free/busy data.
     *
     * @return The start date
     */
    public DateTime getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the free/busy data.
     *
     * @param value The start date to set
     */
    public void setStartDate(DateTime value) {
        startDate = value;
    }

    /**
     * Gets the end date of the free/busy data.
     *
     * @return The end date
     */
    public DateTime getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the free/busy data.
     *
     * @param value The end date to set
     */
    public void setEndDate(DateTime value) {
        endDate = value;
    }

    /**
     * Gets the organizer of the free/busy data.
     *
     * @return The organizer
     */
    public Organizer getOrganizer() {
        return organizer;
    }

    /**
     * Sets the organizer of the free/busy data.
     *
     * @param value The organizer to set
     */
    public void setOrganizer(Organizer value) {
        organizer = value;
    }

    /**
     * Gets the attendees of the free/busy data.
     *
     * @return The attendees
     */
    public List<Attendee> getAttendees() {
        return attendees;
    }

    /**
     * Sets the attendees of the free/busy data.
     *
     * @param value The attendees to set
     */
    public void setAttendees(List<Attendee> value) {
        attendees = value;
    }

    /**
     * Gets the free and busy times of the free/busy data.
     *
     * @return The free and busy times
     */
    public List<FreeBusyTime> getFreeBusyTimes() {
        return freeBusyTimes;
    }

    /**
     * Sets the free and busy times of the free/busy data.
     *
     * @param freeBusyTimes The free and busy times to set
     */
    public void setFreeBusyTimes(List<FreeBusyTime> freeBusyTimes) {
        this.freeBusyTimes = freeBusyTimes;
    }

}
