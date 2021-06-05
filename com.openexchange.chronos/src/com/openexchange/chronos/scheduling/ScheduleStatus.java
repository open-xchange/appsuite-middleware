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
