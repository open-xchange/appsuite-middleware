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

package com.openexchange.chronos.provider.basic;

import java.util.Date;
import org.json.JSONObject;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarSettings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarSettings {

    private String name;
    private boolean containsName;
    private Date lastModified;
    private boolean containsLastModified;
    private ExtendedProperties extendedProperties;
    private boolean containsExtendedProperties;
    private JSONObject config;
    private boolean containsConfig;
    private boolean unsubscribed;
    private boolean containsSubscribed;
    private OXException error;
    private boolean containsError;

    /**
     * Initializes a new {@link CalendarSettings}.
     */
    public CalendarSettings() {
        super();
    }

    /**
     * Gets the calendar name.
     *
     * @return The calendar name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the calendar name.
     *
     * @param value The calendar name to set
     */
    public void setName(String value) {
        name = value;
        containsName = true;
    }

    /**
     * Gets a value indicating whether the calendar name within this settings object has been set or not.
     *
     * @return <code>true</code> if the name is set, <code>false</code>, otherwise
     */
    public boolean containsName() {
        return containsName;
    }

    /**
     * Removes a previously set calendar name within this settings object.
     */
    public void removeName() {
        this.name = null;
        this.containsName = false;
    }

    /**
     * Gets the last modification date of the calendar.
     *
     * @return The last modification date
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modification date of the calendar.
     *
     * @param value The last modification date to set
     */
    public void setLastModified(Date value) {
        lastModified = value;
        containsLastModified = true;
    }

    /**
     * Gets a value indicating whether the last modification date of the calendar within this settings object has been set or not.
     *
     * @return <code>true</code> if the last modification date is set, <code>false</code>, otherwise
     */
    public boolean containsLastModified() {
        return containsLastModified;
    }

    /**
     * Removes a previously set last modification date of the calendar within this settings object.
     */
    public void removeLastModified() {
        lastModified = null;
        containsLastModified = false;
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
        containsExtendedProperties = true;
    }

    /**
     * Gets a value indicating whether extended properties of the calendar within this settings object have been set or not.
     *
     * @return <code>true</code> if extended properties are set, <code>false</code>, otherwise
     */
    public boolean containsExtendedProperties() {
        return containsExtendedProperties;
    }

    /**
     * Removes previously set extended properties of the calendar within this settings object.
     */
    public void removeExtendedProperties() {
        extendedProperties = null;
        containsExtendedProperties = false;
    }

    /**
     * Gets the <i>user</i> configuration data of the calendar.
     *
     * @return The <i>user</i> configuration data
     */
    public JSONObject getConfig() {
        return config;
    }

    /**
     * Sets the <i>user</i> configuration data of the calendar.
     *
     * @param value The <i>user</i> configuration data to set
     */
    public void setConfig(JSONObject value) {
        config = value;
        containsConfig = true;
    }

    /**
     * Gets a value indicating whether the <i>user</i> configuration data of the calendar within this settings object has been set or not.
     *
     * @return <code>true</code> if the <i>user</i> configuration data is set, <code>false</code>, otherwise
     */
    public boolean containsConfig() {
        return containsConfig;
    }

    /**
     * Removes the previously set <i>user</i> configuration data of the calendar within this settings object.
     */
    public void removeConfig() {
        config = null;
        containsConfig = false;
    }

    /**
     * Gets a value indicating whether the calendar is actually subscribed or not.
     *
     * @return <code>true</code> if the calendar is subscribed, <code>false</code>, otherwise
     */
    public boolean isSubscribed() {
        return false == unsubscribed;
    }

    /**
     * Sets if the calendar is actually subscribed or not.
     *
     * @param value <code>true</code> if the calendar is subscribed, <code>false</code>, otherwise
     */
    public void setSubscribed(boolean value) {
        unsubscribed = false == value;
        containsSubscribed = true;
    }

    /**
     * Gets a value indicating whether the <i>subscribed</i>-flag of the calendar has been set within this settings object or not.
     *
     * @return <code>true</code> if the <i>subscribed</i>-flag is set, <code>false</code>, otherwise
     */
    public boolean containsSubscribed() {
        return containsSubscribed;
    }

    /**
     * Removes the previously set <i>subscribed</i>-flag of the calendar within this settings object.
     */
    public void removeSubscribed() {
        unsubscribed = false;
        containsSubscribed = false;
    }

    /**
     * Gets the stored error of the calendar.
     *
     * @return The stored error, or <code>null</code> if there is none
     */
    public OXException getError() {
        return error;
    }

    /**
     * Sets the stored error of the calendar.
     *
     * @param value The error to set
     */
    public void setError(OXException value) {
        error = value;
        containsError = true;
    }

    /**
     * Gets a value indicating whether the stored error of the calendar within this settings object has been set or not.
     *
     * @return <code>true</code> if the stored error is set, <code>false</code>, otherwise
     */
    public boolean containsError() {
        return containsError;
    }

    /**
     * Removes the previously set stored error of the calendar within this settings object.
     */
    public void removeError() {
        error = null;
        containsError = false;
    }

    @Override
    public String toString() {
        return "CalendarSettings [name=" + name + ", lastModified=" + lastModified + ", extendedProperties=" + extendedProperties + ", config=" + config + ", unsubscribed=" + unsubscribed + ", error=" + error + "]";
    }

}
