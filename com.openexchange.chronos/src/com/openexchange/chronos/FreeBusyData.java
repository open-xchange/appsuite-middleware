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
