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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;

/**
 * {@link WebDAVFileStorageDavRepr}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WebDAVFileStorageDavRepr {

    private final DavPropertySet setProperties;

    private final DavPropertyNameSet removeProperties;

    /**
     * Initializes a new {@link WebDAVFileStorageDavRepr}.
     *
     * @param isDirectory <code>true</code> if this DAV representation denotes a directory; otherwise <code>false</code> for file
     */
    public WebDAVFileStorageDavRepr(final boolean isDirectory) {
        super();
        setProperties = new DavPropertySet();
        removeProperties = new DavPropertyNameSet();
        if (isDirectory) {
            // setProperties.add(new ResourceType(ResourceType.COLLECTION));
        } else {
            // setProperties.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
        }
    }

    /**
     * Initializes a new {@link WebDAVFileStorageDavRepr} from given file.
     *
     * @param file The file
     * @see #WebDAVFileStorageDavRepr(File, List)
     */
    public WebDAVFileStorageDavRepr(final File file) {
        this(file, null);
    }

    /**
     * Initializes a new {@link WebDAVFileStorageDavRepr}.
     *
     * @param file The file
     * @param modifiedColumns The list of fields to consider; others are ignored
     */
    public WebDAVFileStorageDavRepr(final File file, final List<Field> modifiedColumns) {
        this(false);
        final Set<Field> set =
            null == modifiedColumns || modifiedColumns.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(modifiedColumns);
        if (set.contains(Field.CREATED)) {
            /*
             * This property is read-only
             */
            // setProperty0(DavConstants.PROPERTY_CREATIONDATE, WebDAVFileStorageResourceUtil.getDateProperty(file.getCreated()));
        }
        if (set.contains(Field.FILENAME)) {
            setProperty0(DavConstants.PROPERTY_DISPLAYNAME, file.getTitle());
        }
        if (set.contains(Field.FILE_SIZE)) {
            /*
             * This property is read-only
             */
            // setProperty0(DavConstants.PROPERTY_GETCONTENTLENGTH, String.valueOf(file.getFileSize()));
        }
        if (set.contains(Field.FILE_MIMETYPE)) {
            /*
             * This property is read-only
             */
            // setProperty0(DavConstants.PROPERTY_GETCONTENTTYPE, file.getFileMIMEType());
        }
        if (set.contains(Field.LAST_MODIFIED) || set.contains(Field.LAST_MODIFIED_UTC)) {
            /*
             * This property is read-only
             */
            // setProperty0(DavConstants.PROPERTY_GETLASTMODIFIED, WebDAVFileStorageResourceUtil.getDateProperty(file.getLastModified()));
        }
        /*-
         * No field for "getcontentlanguage" DAV property
         *
         * This property is read-only
         */
        // setProperty0(DavConstants.PROPERTY_GETCONTENTLANGUAGE, file.getProperty(DavConstants.PROPERTY_GETCONTENTLANGUAGE));
        /*-
         * No field for "getetag" DAV property
         *
         * This property is read-only
         */
        // setProperty0(DavConstants.PROPERTY_GETETAG, file.getProperty(DavConstants.PROPERTY_GETETAG));
        /*
         * Custom properties
         */
        setProperty0("url", file.getURL(), WebDAVConstants.OX_NAMESPACE);
        setProperty0("categories", file.getCategories(), WebDAVConstants.OX_NAMESPACE);
        setProperty0("description", file.getDescription(), WebDAVConstants.OX_NAMESPACE);
        setProperty0("versionComment", file.getVersionComment(), WebDAVConstants.OX_NAMESPACE);
        setProperty0("colorLabel", String.valueOf(file.getColorLabel()), WebDAVConstants.OX_NAMESPACE);
    }

    private void setProperty0(final String name, final String value) {
        setProperty0(name, value, DavConstants.NAMESPACE);
    }

    private void setProperty0(final String name, final String value, final Namespace namespace) {
        if (null == value) {
            removeProperties.add(DavPropertyName.create(name, namespace));
        } else {
            setProperties.add(new DefaultDavProperty<String>(name, value, namespace));
        }
    }

    /**
     * Sets specified property.
     *
     * @param name The property name
     * @param value The property value or <code>null</code> to indicate removal
     */
    public void setProperty(final String name, final String value) {
        setProperty0(name, value);
    }

    /**
     * Gets the properties to set.
     *
     * @return The properties to set
     */
    public DavPropertySet getSetProperties() {
        return setProperties;
    }

    /**
     * Gets the names of the properties to remove
     *
     * @return The names of the properties to remove
     */
    public DavPropertyNameSet getRemoveProperties() {
        return removeProperties;
    }

}
