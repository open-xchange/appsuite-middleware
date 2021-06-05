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

package com.openexchange.chronos.provider.basic;

import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC_LITERAL;
import java.util.Date;
import java.util.Optional;
import org.json.JSONObject;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.chronos.provider.UsedForSync;
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
    private UsedForSync usedForSync;

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
        checkUsedForSync(value);
    }

    /**
     * Checks if the given {@link ExtendedProperties} contain any used for sync value and remove it and set the according field instead
     *
     * @param props The {@link ExtendedProperties} to check
     */
    private void checkUsedForSync(ExtendedProperties props) {
        if (props == null) {
            return;
        }

        ExtendedProperty usedForSyncProperty = props.get(USED_FOR_SYNC_LITERAL);
        if (usedForSyncProperty == null) {
            return;
        }

        boolean protekted = CalendarFolderProperty.isProtected(usedForSyncProperty);
        Boolean usedForSync = CalendarFolderProperty.optPropertyValue(usedForSyncProperty, Boolean.class);
        if (getUsedForSync().isPresent() == false) {
            setUsedForSync(new UsedForSync(usedForSync.booleanValue(), protekted));
        }
        props.removeAll(USED_FOR_SYNC_LITERAL);
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
     * Gets the usedForSync
     *
     * @return The usedForSync
     */
    public Optional<UsedForSync> getUsedForSync() {
        return Optional.ofNullable(usedForSync);
    }

    /**
     * Sets the usedForSync
     *
     * @param usedForSync The usedForSync to set
     */
    public void setUsedForSync(UsedForSync usedForSync) {
        this.usedForSync = usedForSync;
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
