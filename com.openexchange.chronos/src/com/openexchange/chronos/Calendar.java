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
     * Initializes a new {@link Calendar}, taking over the properties from another calendar.
     * 
     * @param calendar The calendar to take over the properties from
     */
    public Calendar(Calendar calendar) {
        this();
        setProdId(calendar.getProdId());
        setVersion(calendar.getVersion());
        setCalScale(calendar.getCalScale());
        setMethod(calendar.getMethod());
        setName(calendar.getName());
        setEvents(calendar.getEvents());
        setFreeBusyDatas(calendar.getFreeBusyDatas());
        setAvailability(calendar.getAvailability());
        setExtendedProperties(calendar.getExtendedProperties());
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
