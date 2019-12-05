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

package com.openexchange.chronos.scheduling;

/**
 * {@link ScheduleStatus}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.2.9">RFC 6638 Section 3.2.9</a>
 */
public enum ScheduleStatus {

    /**
     * The scheduling message is pending.
     */
    PENDING("The scheduling message is pending.", 1, 0),

    /**
     * The scheduling message has been successfully sent.
     */
    SENT("The scheduling message has been successfully sent.", 1, 1),

    /**
     * The scheduling message has been successfully delivered.
     */
    DELIVERED("The scheduling message has been successfully delivered.", 1, 2),

    /**
     * The scheduling message was not delivered because the server did not recognize the calendar user address as a valid calendar user.
     */
    UNKOWN_CALENDAR_USER("The scheduling message was not delivered because the server did not recognize the calendar user address as a valid calendar user.", 3, 7),

    /**
     * The scheduling message was not delivered due to insufficient privileges.
     */
    NO_PRIVILEGES("The scheduling message was not delivered due to insufficient privileges.", 3, 8),

    /**
     * The scheduling message was not delivered because the server could not complete delivery of the message.
     */
    NOT_DELIVERED("The scheduling message was not delivered because the server could not complete delivery of the message.", 5, 1),

    /**
     * The scheduling message was not delivered because the server was not able to find a way to deliver the message.
     */
    NO_TRANSPORT("The scheduling message was not delivered because the server was not able to find a way to deliver the message.", 5, 2),

    /**
     * The scheduling message was not delivered and was rejected because scheduling with that recipient is not allowed.
     */
    REJECTED("The scheduling message was not delivered and was rejected because scheduling with that recipient is not allowed.", 5, 3);

    private final String message;
    private final int major, minor;

    /**
     * Initializes a new {@link ScheduleStatus}.
     */
    private ScheduleStatus(String message, int major, int minor) {
        this.message = message;
        this.major = major;
        this.minor = minor;
    }

    /**
     * Gets the message
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the major
     *
     * @return The major
     */
    public int getMajor() {
        return major;
    }

    /**
     * Gets the minor
     *
     * @return The minor
     */
    public int getMinor() {
        return minor;
    }

    public float getDeliveryStatusCode() {
        return major + (minor / 10f);
    }

}
