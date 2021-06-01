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

package com.openexchange.chronos.provider;

import java.util.Collections;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.Transp;

/**
 * {@link CalendarFolderProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum CalendarFolderProperty {
    ;

    /**
     * Initializes a new calendar folder property for the <code>color</code> property.
     * <p/>
     * The color may be used by clients when presenting events from the calendar to a user. Typically, this would appear as the
     * background color of events.
     * <p/>
     * The value is defined as a <code>CSS3</code> color value.
     *
     * @param value The value to take over
     * @return The extended property
     * @see <a href="https://tools.ietf.org/html/rfc7986#section-5.9">RFC 7986, section 5.9</a>
     * @see <a href="https://www.w3.org/TR/2011/REC-css3-color-20110607">W3C CSS Color Module Level 3</a>
     */
    public static ExtendedProperty COLOR(String value) {
        return COLOR(value, false);
    }

    /**
     * Initializes a new calendar folder property for the <code>color</code> property.
     * <p/>
     * The color may be used by clients when presenting events from the calendar to a user. Typically, this would appear as the
     * background color of events.
     * <p/>
     * The value is defined as a <code>CSS3</code> color value.
     *
     * @param value The value to take over
     * @param protekted <code>true</code> if the property should be protected, <code>false</code>, otherwise
     * @return The extended property
     * @see <a href="https://tools.ietf.org/html/rfc7986#section-5.9">RFC 7986, section 5.9</a>
     * @see <a href="https://www.w3.org/TR/2011/REC-css3-color-20110607">W3C CSS Color Module Level 3</a>
     */
    public static ExtendedProperty COLOR(String value, boolean protekted) {
        return createProperty(COLOR_LITERAL, value, protekted);
    }

    /**
     * Initializes a new calendar folder property for the <code>description</code> property.
     * <p/>
     * The description is used to specify a lengthy textual description of the calendar that can be used by clients when describing the
     * nature of the calendar data to a user.
     *
     * @param value The value to take over
     * @return The extended property
     * @see <a href="https://tools.ietf.org/html/rfc7986#section-5.2">RFC 7986, section 5.2</a>
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-5.2.1">RFC 4791, section 5.2.1</a>
     */
    public static ExtendedProperty DESCRIPTION(String value) {
        return DESCRIPTION(value, false);
    }

    /**
     * Initializes a new calendar folder property for the <code>description</code> property.
     * <p/>
     * The description is used to specify a lengthy textual description of the calendar that can be used by clients when describing the
     * nature of the calendar data to a user.
     *
     * @param value The value to take over
     * @param protekted <code>true</code> if the property should be protected, <code>false</code>, otherwise
     * @return The extended property
     * @see <a href="https://tools.ietf.org/html/rfc7986#section-5.2">RFC 7986, section 5.2</a>
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-5.2.1">RFC 4791, section 5.2.1</a>
     */
    public static ExtendedProperty DESCRIPTION(String value, boolean protekted) {
        return createProperty(DESCRIPTION_LITERAL, value, protekted);
    }

    /**
     * Initializes a new calendar folder property for the <code>scheduleTransp</code> property.
     * <p/>
     * The schedule transparency is used to determine whether the calendar object resources in a calendar collection will affect the
     * owner's busy time information or not. The value is either {@link Transp#TRANSPARENT} if contained events do not contribute to the
     * user's busy time, or {@link Transp#OPAQUE}, otherwise.
     *
     * @param value The value to take over
     * @return The extended property
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-9.1">RFC 6638, section 9.1</a>
     */
    public static ExtendedProperty SCHEDULE_TRANSP(String value) {
        return SCHEDULE_TRANSP(value, false);
    }

    /**
     * Initializes a new calendar folder property for the <code>scheduleTransp</code> property.
     * <p/>
     * The schedule transparency is used to determine whether the calendar object resources in a calendar collection will affect the
     * owner's busy time information or not. The value is either {@link Transp#TRANSPARENT} if contained events do not contribute to the
     * user's busy time, or {@link Transp#OPAQUE}, otherwise.
     *
     * @param value The value to take over
     * @param protekted <code>true</code> if the property should be protected, <code>false</code>, otherwise
     * @return The extended property
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-9.1">RFC 6638, section 9.1</a>
     */
    public static ExtendedProperty SCHEDULE_TRANSP(String value, boolean protekted) {
        return createProperty(SCHEDULE_TRANSP_LITERAL, value, protekted);
    }

    /**
     * Initializes a new calendar folder property for the <code>scheduleTransp</code> property.
     * <p/>
     * The schedule transparency is used to determine whether the calendar object resources in a calendar collection will affect the
     * owner's busy time information or not. The value is either {@link Transp#TRANSPARENT} if contained events do not contribute to the
     * user's busy time, or {@link Transp#OPAQUE}, otherwise.
     *
     * @param value The value to take over
     * @return The extended property
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-9.1">RFC 6638, section 9.1</a>
     */
    public static ExtendedProperty SCHEDULE_TRANSP(Transp value) {
        return SCHEDULE_TRANSP(value, false);
    }

    /**
     * Initializes a new calendar folder property for the <code>scheduleTransp</code> property.
     * <p/>
     * The schedule transparency is used to determine whether the calendar object resources in a calendar collection will affect the
     * owner's busy time information or not. The value is either {@link Transp#TRANSPARENT} if contained events do not contribute to the
     * user's busy time, or {@link Transp#OPAQUE}, otherwise.
     *
     * @param value The value to take over
     * @param protekted <code>true</code> if the property should be protected, <code>false</code>, otherwise
     * @return The extended property
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-9.1">RFC 6638, section 9.1</a>
     */
    public static ExtendedProperty SCHEDULE_TRANSP(Transp value, boolean protekted) {
        return SCHEDULE_TRANSP(null == value ? null : value.getValue(), protekted);
    }

    /**
     * Initializes a new calendar folder property for the <code>usedForSync</code> property.
     * <p/>
     * This flag (either <code>true</code> or <code>false</code>} indicates whether the folder should be considered for synchronization
     * with external clients or not.
     *
     * @param value The value to take over
     * @param protekted <code>true</code> if the property should be protected, <code>false</code>, otherwise
     * @return The extended property
     */
    public static ExtendedProperty USED_FOR_SYNC(String value, boolean protekted) {
        return createProperty(USED_FOR_SYNC_LITERAL, value, protekted);
    }

    /**
     * Initializes a new calendar folder property for the <code>usedForSync</code> property.
     * <p/>
     * This flag (either <code>true</code> or <code>false</code>} indicates whether the folder should be considered for synchronization
     * with external clients or not.
     *
     * @param value The value to take over
     * @param protekted <code>true</code> if the property should be protected, <code>false</code>, otherwise
     * @return The extended property
     */
    public static ExtendedProperty USED_FOR_SYNC(Boolean value, boolean protekted) {
        return USED_FOR_SYNC(null == value ? null : value.toString(), protekted);
    }

    /**
     * Initializes a new calendar folder property for the <code>lastUpdate</code> property, implicitly <i>protected</i>.
     * <p/>
     * The value indicates the timestamp of the last update of the calendar contents.
     *
     * @param value The value to take over
     * @return The extended property
     */
    public static ExtendedProperty LAST_UPDATE(Long value) {
        return new ExtendedProperty(LAST_UPDATE_LITERAL, value, Collections.singletonList(PROTECTED_PARAMETER));
    }

    /** The literal used for the {@link CalendarFolderProperty#COLOR} property. */
    public static final String COLOR_LITERAL = "color";

    /** The literal used for the {@link CalendarFolderProperty#DESCRIPTION} property. */
    public static final String DESCRIPTION_LITERAL = "description";

    /** The literal used for the {@link CalendarFolderProperty#SCHEDULE_TRANSP} property. */
    public static final String SCHEDULE_TRANSP_LITERAL = "scheduleTransp";

    /** The literal used for the {@link CalendarFolderProperty#USED_FOR_SYNC} property. */
    public static final String USED_FOR_SYNC_LITERAL = "usedForSync";

    /** The literal used for the {@link CalendarFolderProperty#LAST_UPDATE} property. */
    public static final String LAST_UPDATE_LITERAL = "lastUpdate";

    /**
     * Gets a value indicating whether a specific property is marked as <i>protected</i> or not.
     *
     * @param property The property to check
     * @return <code>true</code> if the property is protected, <code>false</code>, otherwise
     * @see <a href="https://tools.ietf.org/html/rfc3253#section-1.4.2">RFC 3253, section 1.4.2</a>
     */
    public static boolean isProtected(ExtendedProperty property) {
        ExtendedPropertyParameter parameter = property.getParameter(PROTECTED_PARAMETER.getName());
        return null != parameter && Boolean.parseBoolean(parameter.getValue());
    }

    /**
     * Optionally gets the value of a specific extended property from the supplied extended properties container.
     *
     * @param extendedProperties The extended properties to get the value from
     * @param name The name of the extended property to get the value from
     * @return The property value, or <code>null</code> if not defined
     */
    public static Object optPropertyValue(ExtendedProperties extendedProperties, String name) {
        if (null != extendedProperties) {
            ExtendedProperty property = extendedProperties.get(name);
            if (null != property) {
                return property.getValue();
            }
        }
        return null;
    }

    /**
     * Optionally gets the value of a specific extended property from the supplied extended properties container.
     *
     * @param extendedProperties The extended properties to get the value from
     * @param name The name of the extended property to get the value from
     * @param clazz The value's target type
     * @return The property value, or <code>null</code> if not defined or incompatible for the target type
     */
    public static <T> T optPropertyValue(ExtendedProperties extendedProperties, String name, Class<T> clazz) {
        return null == extendedProperties ? null : optPropertyValue(extendedProperties.get(name), clazz);
    }

    /**
     * Optionally gets the value of a specific extended property from the supplied extended properties container.
     *
     * @param extendedProperties The extended properties to get the value from
     * @param name The name of the extended property to get the value from
     * @param clazz The value's target type
     * @return The property value, or <code>null</code> if not defined or incompatible for the target type
     * @throws IllegalArgumentException If given target type is <code>null</code>
     */
    public static <T> T optPropertyValue(ExtendedProperty property, Class<T> clazz) {
        if (null == property) {
            return null;
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Target type must not be null");
        }

        try {
            return clazz.cast(property.getValue());
        } catch (ClassCastException e) {
            // Special handling for boolean strings, ignore, otherwise
            if (Boolean.class.equals(clazz) && String.class.isInstance(property.getValue())) {
                return clazz.cast(Boolean.valueOf((String) property.getValue()));
            }
        }
        return null;
    }

    private static final ExtendedPropertyParameter PROTECTED_PARAMETER = new ExtendedPropertyParameter("protected", "true");

    private static ExtendedProperty createProperty(String name, String value, boolean protekted) {
        return protekted ? new ExtendedProperty(name, value, Collections.singletonList(PROTECTED_PARAMETER)) : new ExtendedProperty(name, value);
    }

}
