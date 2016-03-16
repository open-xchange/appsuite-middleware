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

package com.openexchange.folderstorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link FolderServiceDecorator} - The decorator for {@link FolderService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderServiceDecorator implements Cloneable {

    public static final String PROPERTY_IGNORE_GUEST_PERMISSIONS = "com.openexchange.folderstorage.ignoreGuestPermissions";

    private TimeZone timeZone;

    private Locale locale;

    private List<ContentType> allowedContentTypes;

    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link FolderServiceDecorator}.
     */
    public FolderServiceDecorator() {
        super();
        allowedContentTypes = Collections.<ContentType> emptyList();
        properties = new ConcurrentHashMap<String, Object>(8, 0.9f, 1);
    }

    /**
     * Checks if specified content type is allowed by this decorator.
     *
     * @param toCheck The content type to check
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public boolean isContentTypeAllowed(ContentType toCheck) {
        if (null == toCheck) {
            return false;
        }

        if (allowedContentTypes.isEmpty()) {
            return true;
        }

        for (ContentType allowedContentType : allowedContentTypes) {
            if (allowedContentType.getModule() == toCheck.getModule()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the list of allowed content types or an empty list if all are allowed.
     *
     * @return The list of allowed content types
     */
    public List<ContentType> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    /**
     * Sets the list of allowed content types or an empty list if all are allowed.
     *
     * @param allowedContentTypes The list of allowed content types
     * @return This decorator with allowed content types applied
     */
    public FolderServiceDecorator setAllowedContentTypes(final List<ContentType> allowedContentTypes) {
        this.allowedContentTypes = (null == allowedContentTypes || allowedContentTypes.isEmpty()) ? Collections.<ContentType> emptyList() : new ArrayList<ContentType>(
            allowedContentTypes);
        return this;
    }

    /**
     * Gets the time zone.
     *
     * @return The time zone or <code>null</code>
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time zone.
     *
     * @param timeZone The time zone to set
     * @return This decorator with time zone applied
     */
    public FolderServiceDecorator setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    /**
     * Gets the locale.
     *
     * @return The locale or <code>null</code>
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     *
     * @param locale The locale to set
     * @return This decorator with locale applied
     */
    public FolderServiceDecorator setLocale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Checks for existence of specified property.
     *
     * @param propertyName The property name
     * @return <code>true</code> if such a property exists; otherwsie <code>false</code>
     */
    public boolean containsProperty(final String propertyName) {
        return properties.containsKey(propertyName);
    }

    /**
     * Puts specified property.
     *
     * @param propertyName The property name
     * @param propertyValue The property value
     * @return This decorator with property put
     */
    public FolderServiceDecorator put(final String propertyName, final Object propertyValue) {
        if (null == propertyName || null == propertyValue) {
            return this;
        }
        properties.put(propertyName, propertyValue);
        return this;
    }

    /**
     * Removes specified property.
     *
     * @param propertyName The property name
     * @return This decorator with property removed
     */
    public FolderServiceDecorator remove(final String propertyName) {
        properties.remove(propertyName);
        return this;
    }

    /**
     * Puts specified properties.
     *
     * @param properties The properties to put
     * @return This decorator with properties put
     */
    public FolderServiceDecorator putProperties(final Map<? extends String, ? extends Object> properties) {
        if (null != properties) {
            this.properties.putAll(properties);
        }
        return this;
    }

    /**
     * Gets the named property.
     *
     * @param propertyName The property name
     * @return The property value or <code>null</code>
     */
    public Object getProperty(final String propertyName) {
        return properties.get(propertyName);
    }

    private static final Set<String> BOOL_VALS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "true",
        "1",
        "yes",
        "y",
        "on")));

    /**
     * Parses denoted <tt>boolean</tt> value from specified <tt>String</tt> parameter.
     * <p>
     * <code>true</code> if given value is not <code>null</code> and equals ignore-case to one of the values "true", "yes", "y", "on", or
     * "1".
     *
     * @param name The parameter
     * @return The parsed <tt>boolean</tt> value (<code>false</code> on absence)
     */
    public boolean getBoolProperty(final String name) {
        if (null == name) {
            return false;
        }
        final Object value = properties.get(name);
        return (null != value) && BOOL_VALS.contains(value.toString().trim().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Checks whether to hand-down permissions on update operation.
     *
     * @return <code>true</code> to hand down; otherwise <code>false</code>
     */
    public boolean isHandDownPermissions() {
        final Object permissionsHandling = properties.get("permissions");
        return null != permissionsHandling && "inherit".equalsIgnoreCase(permissionsHandling.toString());
    }

    /**
     * Gets this decorator's properties.
     *
     * @return The properties
     */
    public Map<String, Object> getProperties() {
        return new HashMap<String, Object>(properties);
    }

    @Override
    public FolderServiceDecorator clone() throws CloneNotSupportedException {
        FolderServiceDecorator fsDecorator = new FolderServiceDecorator();
        fsDecorator.setAllowedContentTypes(allowedContentTypes);
        fsDecorator.setLocale(locale);
        fsDecorator.setTimeZone(timeZone);
        fsDecorator.putProperties(properties);
        return fsDecorator;
    }

}
