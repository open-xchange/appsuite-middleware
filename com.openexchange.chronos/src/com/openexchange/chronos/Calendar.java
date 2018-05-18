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

import java.util.List;

/**
 * {@link Calendar}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.6">RFC 5545, section 3.6</a>
 */
public class Calendar {

    private String prodId;
    private String version;
    private String calScale;
    private String method;
    private String name;
    private List<Event> events;
    private List<FreeBusyData> freeBusyDatas;
    private Availability availability;
    private ExtendedProperties extendedProperties;

    /**
     * Initializes a new {@link Calendar}.
     */
    public Calendar() {
        super();
    }

    /**
     * Gets the prodId
     *
     * @return The prodId
     */
    public String getProdId() {
        return prodId;
    }

    /**
     * Sets the prodId
     *
     * @param prodId The prodId to set
     */
    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    /**
     * Gets the version
     *
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version
     *
     * @param version The version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the calScale
     *
     * @return The calScale
     */
    public String getCalScale() {
        return calScale;
    }

    /**
     * Sets the calScale
     *
     * @param calScale The calScale to set
     */
    public void setCalScale(String calScale) {
        this.calScale = calScale;
    }

    /**
     * Gets the method
     *
     * @return The method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the method
     *
     * @param method The method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the events
     *
     * @return The events
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * Sets the events
     *
     * @param events The events to set
     */
    public void setEvents(List<Event> events) {
        this.events = events;
    }

    /**
     * Gets the freeBusyDatas
     *
     * @return The freeBusyDatas
     */
    public List<FreeBusyData> getFreeBusyDatas() {
        return freeBusyDatas;
    }

    /**
     * Sets the freeBusyDatas
     *
     * @param freeBusyDatas The freeBusyDatas to set
     */
    public void setFreeBusyDatas(List<FreeBusyData> freeBusyDatas) {
        this.freeBusyDatas = freeBusyDatas;
    }

    /**
     * Gets the availability
     *
     * @return The availability
     */
    public Availability getAvailability() {
        return availability;
    }

    /**
     * Sets the availability
     *
     * @param availability The availability to set
     */
    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    /**
     * Gets the extended properties of the calendar.
     *
     * @return The extended properties
     */
    public ExtendedProperties getExtendedProperties() {
        return extendedProperties;
    }

    /**
     * Sets the extended properties of the calendar.
     *
     * @param value The extended properties to set
     */
    public void setExtendedProperties(ExtendedProperties value) {
        extendedProperties = value;
    }

}
