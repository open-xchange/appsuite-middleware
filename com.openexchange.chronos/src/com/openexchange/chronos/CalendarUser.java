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

/**
 * {@link CalendarUser}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.3.3">RFC 5545, section 3.3.3</a>
 */
public class CalendarUser {

    protected String uri;
    protected String cn;
    protected int entity = -1; // initialize with -1 to avoid ambiguities with 'all users' default group
    protected CalendarUser sentBy;
    protected String email;

    /**
     * Initializes a new {@link CalendarUser}.
     */
    public CalendarUser() {
        super();
    }

    /**
     * Initializes a new {@link CalendarUser}, copying over all properties from another calendar user.
     *
     * @param calendarUser The calendar user to copy the properties from
     */
    public CalendarUser(CalendarUser calendarUser) {
        super();
        uri = calendarUser.getUri();
        cn = calendarUser.getCn();
        entity = calendarUser.getEntity();
        sentBy = null != calendarUser.getSentBy() ? new CalendarUser(calendarUser.getSentBy()) : null;
        email = calendarUser.getEMail();
    }

    /**
     * Gets the address URI identifying the calendar user.
     *
     * @return The calendar user address URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the calendar user address URI
     *
     * @param uri The calendar user address URI to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the common name associated with the calendar user.
     *
     * @return The common name
     */
    public String getCn() {
        return cn;
    }

    /**
     * Sets the common name associated with the calendar user.
     *
     * @param cn The common name to set
     */
    public void setCn(String cn) {
        this.cn = cn;
    }

    /**
     * Gets the internal entity identifier of the calendar user.
     *
     * @return The entity identifier
     */
    public int getEntity() {
        return entity;
    }

    /**
     * Sets the internal entity identifier of the calendar user.
     *
     * @param entity The entity identifier to set
     */
    public void setEntity(int entity) {
        this.entity = entity;
    }

    /**
     * Gets the calendar user that is acting on behalf of this calendar user.
     *
     * @return The calendar user that is acting on behalf of this calendar user.
     */
    public CalendarUser getSentBy() {
        return sentBy;
    }

    /**
     * Sets the calendar user that is acting on behalf of this calendar user.
     *
     * @param sentBy The "sent-by" calendar user to set
     */
    public void setSentBy(CalendarUser sentBy) {
        this.sentBy = sentBy;
    }

    /**
     * Gets the e-mail address of the calendar user.
     *
     * @return The e-mail address
     */
    public String getEMail() {
        return email;
    }

    /**
     * Sets the e-mail address of the calendar user.
     *
     * @param email The e-mail address to set
     */
    public void setEMail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "CalendarUser [uri=" + uri + ", entity=" + entity + "]";
    }

}
