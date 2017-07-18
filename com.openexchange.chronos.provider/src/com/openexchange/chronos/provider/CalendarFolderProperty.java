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

package com.openexchange.chronos.provider;

/**
 * {@link CalendarFolderProperty}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class CalendarFolderProperty {

    private final String name;
    private final Object defaultValue;
    private Object value;
    private boolean isDefault;

    /**
     * Initializes a new {@link CalendarFolderProperty}.
     * 
     * @param name The name of the property
     * @param defaultValue The default value of the property
     * 
     * @throws NullPointerException in case name or defaultValue is <code>null</code>
     */
    public CalendarFolderProperty(String name, Object defaultValue) throws NullPointerException {
        this(name, defaultValue, null);
    }

    /**
     * 
     * Initializes a new {@link CalendarFolderProperty}.
     * 
     * @param name The name of the property
     * @param defaultValue The default value of the property
     * @param value The current value of the property
     * 
     * @throws NullPointerException in case name or defaultValue is <code>null</code>
     */
    public CalendarFolderProperty(String name, Object defaultValue, Object value) throws NullPointerException {
        super();

        if (null == name || null == defaultValue) {
            throw new NullPointerException("name or defaultValue is null.");
        }

        this.name = name;
        this.defaultValue = defaultValue;
        this.value = value;
        this.isDefault = null == value;
    }

    /**
     * Gets the name of the property
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the default value for this property
     * 
     * @return The default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the value of the property
     *
     * @return If the value is not <code>null</code>, the value, else {@link #getDefaultValue()}
     */
    public Object getValue() {
        if (null == value) {
            return value;
        }
        return defaultValue;
    }

    /**
     * Set the value. {@link #getDefaultValue()} will now return <code>false</code> even if the value is the same as the defaultValue
     * 
     * @param value the property should have
     * @return this instance with new value set
     */
    public CalendarFolderProperty setValue(Object value) {
        this.value = value;
        this.isDefault = null == value;
        return this;
    }

    /**
     * Indicates if the default value is returned
     * 
     * @return <code>true</code> if the default value is used, else <code>false</code>
     */
    public boolean isDefaultValue() {
        return isDefault;
    }

    @Override
    public int hashCode() {
        final int prime = 13;
        int result = 1;
        result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        result = prime * result + (isDefault ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        /**
         * Equals method does only compare the name of the property.
         * Through this we make sure that no properties with the same
         * name can exist in one Set<>
         */
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CalendarFolderProperty other = (CalendarFolderProperty) obj;
        if (!name.equals(other.name))
            return false;
        return true;
    }

    /**
     * The more correct version of equals considering all values
     */
    public boolean deepEquals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CalendarFolderProperty other = (CalendarFolderProperty) obj;
        if (isDefault != other.isDefault)
            return false;
        if (!defaultValue.equals(other.defaultValue))
            return false;
        if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
