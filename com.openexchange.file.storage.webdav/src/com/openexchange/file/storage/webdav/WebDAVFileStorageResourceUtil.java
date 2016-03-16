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

package com.openexchange.file.storage.webdav;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.w3c.dom.Element;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;

/**
 * {@link WebDAVFileStorageResourceUtil} - Utility class for WebDAV resources.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a> - Exceptions
 */
public final class WebDAVFileStorageResourceUtil {

    /**
     * Initializes a new {@link WebDAVFileStorageResourceUtil}.
     */
    private WebDAVFileStorageResourceUtil() {
        super();
    }

    /**
     * Parses the string from named property contained in given property set
     *
     * @param davPropertyName The DAV property name
     * @param propertySet The property set
     * @return The string
     * @throws OXException If string cannot be parsed
     */
    public static String parseStringProperty(final DavPropertyName davPropertyName, final DavPropertySet propertySet) throws OXException {
        try {
            @SuppressWarnings("unchecked") final DavProperty<String> stringProperty =
                (DavProperty<String>) propertySet.get(davPropertyName);
            if (null == stringProperty) {
                return null;
            }
            return stringProperty.getValue();
        } catch (final ClassCastException e) {
            throw FileStorageExceptionCodes.INVALID_PROPERTY.create(e, davPropertyName.getName(), String.class.getName());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses the string from named property contained in given property set
     *
     * @param name The property name
     * @param propertySet The property set
     * @return The string
     * @throws OXException If string cannot be parsed
     */
    public static String parseStringProperty(final String name, final DavPropertySet propertySet) throws OXException {
        try {
            @SuppressWarnings("unchecked") final DavProperty<String> stringProperty = (DavProperty<String>) propertySet.get(name);
            if (null == stringProperty) {
                return null;
            }
            return stringProperty.getValue();
        } catch (final ClassCastException e) {
            throw FileStorageExceptionCodes.INVALID_PROPERTY.create(e, name, String.class.getName());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses the integer from named property contained in given property set
     *
     * @param name The property name
     * @param propertySet The property set
     * @return The integer
     * @throws OXException If integer cannot be parsed
     */
    public static int parseIntProperty(final String name, final DavPropertySet propertySet) throws OXException {
        try {
            @SuppressWarnings("unchecked") final DavProperty<String> intProperty = (DavProperty<String>) propertySet.get(name);
            if (null == intProperty) {
                throw FileStorageExceptionCodes.INVALID_PROPERTY.create(name, int.class.getName());
            }
            return Integer.parseInt(intProperty.getValue());
        } catch (final ClassCastException e) {
            throw FileStorageExceptionCodes.INVALID_PROPERTY.create(e, name, int.class.getName());
        } catch (final NumberFormatException e) {
            throw FileStorageExceptionCodes.INVALID_PROPERTY.create(e, name, int.class.getName());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * WebDAV creation date/time formatter; e.g <code>2006-12-04T16:07:24Z</code>
     */
    private static final SimpleDateFormat WEBDAV_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final SimpleDateFormat WEBDAV_DATE_FROM_EXCHANGE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Gets the date property value from specified date.
     *
     * @param date The date
     * @return The date property value
     */
    public static String getDateProperty(final Date date) {
        if (null == date) {
            return null;
        }
        synchronized (WEBDAV_DATE) {
            return WEBDAV_DATE.format(date);
        }
    }

    /**
     * Parses the date from named property contained in given property set
     *
     * @param name The property name
     * @param propertySet The property set
     * @return The date
     * @throws OXException If date cannot be parsed
     */
    public static Date parseDateProperty(final String name, final DavPropertySet propertySet) throws OXException {
        try {
            final String datePropertyValue = parseStringProperty(name, propertySet);
            if (null == datePropertyValue) {
                return null;
            }
            Date ret = parseDate(datePropertyValue, WEBDAV_DATE);
            if (null != ret) {
                return ret;
            }
            ret = parseDate(datePropertyValue, WEBDAV_DATE_FROM_EXCHANGE);
            if (null != ret) {
                return ret;
            }
            ret = DateUtil.parseDate(datePropertyValue);
            if (null == ret) {
                throw FileStorageExceptionCodes.INVALID_TYPE_PROPERTY.create("date", datePropertyValue);
            }
            return ret;
        } catch (final ClassCastException e) {
            throw FileStorageExceptionCodes.INVALID_TYPE_PROPERTY.create(e, "date", name);
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static Date parseDate(final String datePropertyValue, final SimpleDateFormat sdf) {
        synchronized (sdf) {
            try {
                return sdf.parse(datePropertyValue);
            } catch (final ParseException e) {
                return null;
            }
        }
    }

    /**
     * Checks if specified folder identifier ends with a <code>'/'</code> character.
     *
     * @param folderId The folder identifier to check
     * @param rootUri The root URI of the connected WebDAV server
     * @return The checked folder identifier
     */
    public static String checkFolderId(final String folderId, final String rootUri) {
        if (FileStorageFolder.ROOT_FULLNAME.equals(folderId)) {
            return rootUri;
        }
        return checkFolderId(folderId);
    }

    /**
     * Checks if specified folder identifier ends with a <code>'/'</code> character.
     *
     * @param folderId The folder identifier to check
     * @return The checked folder identifier
     */
    public static String checkFolderId(final String folderId) {
        if (null == folderId) {
            return null;
        }
        if (folderId.endsWith("/")) {
            return folderId;
        }
        return new StringBuilder(folderId).append('/').toString();
    }

    /**
     * Gets the proper "href" attribute from specified DAV property set.
     * <p>
     * If DAV property set denotes a collection (directory) the returned href is ensured to end with a <code>'/'</code> character; otherwise
     * the returned href does not end with a <code>'/'</code>.
     *
     * @param href The href as proved by multi-status response
     * @param propertySet The DAV property set
     * @return The proper href
     */
    public static String getHref(final String href, final DavPropertySet propertySet) {
        if (null != propertySet && !propertySet.isEmpty()) {
            /*
             * Check for collection
             */
            @SuppressWarnings("unchecked") final DavProperty<Element> davProperty =
                (DavProperty<Element>) propertySet.get(DavConstants.PROPERTY_RESOURCETYPE);
            if (null == davProperty) {
                return href;
            }
            final Element resourceType = davProperty.getValue();
            return checkHref(href, (null != resourceType && "collection".equalsIgnoreCase(resourceType.getLocalName())));
        }
        return href;
    }

    /**
     * Checks the href provided by a multi-status response.
     *
     * @param href The multi-status response's href
     * @param isDirectory <code>true</code> if href denotes a directory; otherwise <code>false</code>
     * @return The checked href
     */
    public static String checkHref(final String href, final boolean isDirectory) {
        return isDirectory ? checkFolderId(href) : checkFileId(href);
    }

    /**
     * Checks specified file identifier.
     *
     * @param fileId The file identifier
     * @return The checked file identifier
     */
    public static String checkFileId(final String fileId) {
        if (null == fileId) {
            return null;
        }
        if (fileId.endsWith("/")) {
            final int length = fileId.length();
            return length == 1 ? "" : fileId.substring(0, length - 1);
        }
        return fileId;
    }
}
