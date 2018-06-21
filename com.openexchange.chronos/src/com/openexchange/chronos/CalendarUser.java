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

import java.util.Objects;

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

    @Override
    public boolean equals(Object other) {
        if (null != other && CalendarUser.class.isInstance(other)) {
            CalendarUser otherCalendarUser = (CalendarUser) other;
            return Objects.equals(uri, otherCalendarUser.getUri()) && Objects.equals(cn, otherCalendarUser.getCn()) && Objects.equals(entity, otherCalendarUser.getEntity()) && Objects.equals(sentBy, otherCalendarUser.getSentBy()) && Objects.equals(email, otherCalendarUser.getEMail());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, cn, entity, sentBy, email);
    }
}
