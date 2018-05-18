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

/**
 * {@link FreeBusyTime}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.2.6">RFC 5545, section 3.8.2.6</a>
 */
public class FreeBusyTime implements Comparable<FreeBusyTime> {

    private FbType fbType;
    private Date startTime;
    private Date endTime;
    private Event event;

    /**
     * Initializes a new {@link FreeBusyTime}.
     */
    public FreeBusyTime() {
        super();
    }

    /**
     * Initializes a new {@link FreeBusyTime}.
     *
     * @param fbType The free/busy type
     * @param startTime The start of the period
     * @param endTime The end of the period
     */
    public FreeBusyTime(FbType fbType, Date startTime, Date endTime) {
        this(fbType, startTime, endTime, null);
    }

    /**
     * Initializes a new {@link FreeBusyTime}.
     *
     * @param fbType The free/busy type
     * @param startTime The start of the period
     * @param endTime The end of the period
     * @param event The event behind the free/busy time, or <code>null</code> if not available
     */
    public FreeBusyTime(FbType fbType, Date startTime, Date endTime, Event event) {
        this();
        this.fbType = fbType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.event = event;
    }

    /**
     * Gets the free/busy type.
     *
     * @return The free/busy type
     */
    public FbType getFbType() {
        return fbType;
    }

    /**
     * Sets the free/busy type.
     *
     * @param fbType The free/busy type to set
     */
    public void setFbType(FbType fbType) {
        this.fbType = fbType;
    }

    /**
     * Gets the startTime
     *
     * @return The startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime
     *
     * @param startTime The startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the endTime
     *
     * @return The endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the endTime
     *
     * @param endTime The endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the optional event behind the free/busy time.
     *
     * @return The event behind the free/busy time, or <code>null</code> if not available
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Sets the optional event behind the free/busy time.
     *
     * @param event The event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public int compareTo(FreeBusyTime o) {
        int value = null == o ? 1 : startTime.compareTo(o.getStartTime());
        if (0 == value) {
            value = endTime.compareTo(o.getEndTime());
            if (0 == value) {
                value = fbType.compareTo(o.getFbType());
            }
        }
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((fbType == null || fbType.getValue() == null) ? 0 : fbType.getValue().hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FreeBusyTime other = (FreeBusyTime) obj;
        if (endTime == null) {
            if (other.endTime != null) {
                return false;
            }
        } else if (!endTime.equals(other.endTime)) {
            return false;
        }
        String fbTypeValue = null == fbType ? null : fbType.getValue();
        String otherFbTypeValue = null == other.fbType ? null : other.fbType.getValue();
        if (fbTypeValue == null) {
            if (otherFbTypeValue != null) {
                return false;
            }
        } else if (!fbTypeValue.equals(otherFbTypeValue)) {
            return false;
        }
        if (startTime == null) {
            if (other.startTime != null) {
                return false;
            }
        } else if (!startTime.equals(other.startTime)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FreeBusyTime [fbType=" + fbType + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    }

}
