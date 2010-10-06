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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageException;
import com.openexchange.file.storage.FileStoragePermission;

/**
 * {@link WebDAVFileStorageFolder}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WebDAVFileStorageFolder extends DefaultFileStorageFolder {

    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Initializes a new {@link WebDAVFileStorageFolder}.
     */
    public WebDAVFileStorageFolder(final String uri, final String rootUri, final int userId) {
        super();
        id = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
        if (id.equalsIgnoreCase(rootUri)) {
            rootFolder = true;
            parentId = "";
        } else {
            rootFolder = false;
            final int pos = id.lastIndexOf('/');
            parentId = pos > 0 ? id.substring(0, pos) : rootUri;
        }
        b_rootFolder = true;
        holdsFiles = true;
        b_holdsFiles = true;
        holdsFolders = true;
        b_holdsFolders = true;
        exists = true;
        subscribed = true;
        b_subscribed = true;
        final DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(userId);
        permissions = Collections.<FileStoragePermission> singletonList(permission);
    }

    /**
     * Parses specified value of header <code>"Allow"</code>.
     * 
     * @param allow The value of header <code>"Allow"</code>
     */
    public void parseAllowHeader(final String allow) {
        if (null == allow) {
            capabilities = Collections.emptySet();
        } else {
            final String[] sa = SPLIT.split(allow, 0);
            final Set<String> allowedCmds = new HashSet<String>(sa.length);
            for (final String element : sa) {
                allowedCmds.add(element.toUpperCase(Locale.ENGLISH));
            }
            capabilities = allowedCmds;
        }
    }

    /**
     * Parses specified DAV property set of associated MultiStatus response.
     * 
     * @param propertySet The DAV property set of associated MultiStatus response
     * @throws FileStorageException If parsing DAV property set fails
     */
    public void parseDavPropertySet(final DavPropertySet propertySet) throws FileStorageException {
        if (null != propertySet) {
            creationDate = parseDateProperty(DavConstants.PROPERTY_CREATIONDATE, propertySet);
            lastModifiedDate = parseDateProperty(DavConstants.PROPERTY_GETLASTMODIFIED, propertySet);
            name = parseStringProperty(DavConstants.PROPERTY_DISPLAYNAME, propertySet);
        }
    }

    private static String parseStringProperty(final String name, final DavPropertySet propertySet) throws WebDAVFileStorageException {
        try {
            @SuppressWarnings("unchecked") final DavProperty<String> stringProperty = (DavProperty<String>) propertySet.get(name);
            if (null == stringProperty) {
                return null;
            }
            return stringProperty.getValue();
        } catch (final ClassCastException e) {
            throw WebDAVFileStorageExceptionCodes.INVALID_PROPERTY.create(e, name, String.class.getName());
        } catch (final Exception e) {
            throw WebDAVFileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * WebDAV creation date/time formatter; e.g <code>2006-12-04T16:07:24Z</code>
     */
    private static final SimpleDateFormat WEBDAV_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static Date parseDateProperty(final String name, final DavPropertySet propertySet) throws WebDAVFileStorageException {
        try {
            final String datePropertyValue = parseStringProperty(name, propertySet);
            if (null == datePropertyValue) {
                return null;
            }
            synchronized (WEBDAV_DATE) {
                try {
                    return WEBDAV_DATE.parse(datePropertyValue);
                } catch (final ParseException e) {
                    throw WebDAVFileStorageExceptionCodes.INVALID_DATE_PROPERTY.create(datePropertyValue);
                }
            }
        } catch (final ClassCastException e) {
            throw WebDAVFileStorageExceptionCodes.INVALID_DATE_PROPERTY.create(e, name);
        } catch (final Exception e) {
            throw WebDAVFileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
