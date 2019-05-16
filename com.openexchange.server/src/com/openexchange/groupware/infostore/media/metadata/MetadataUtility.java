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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.infostore.media.metadata;

import java.lang.reflect.Array;
import java.util.Collection;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.openexchange.java.Strings;

/**
 * {@link MetadataUtility} - Utility methods for dealing with metadata.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MetadataUtility {

    /**
     * Initializes a new {@link MetadataUtility}.
     */
    public MetadataUtility() {
        super();
    }

    /**
     * Puts directory's tags into media metadata and signals if given directory can be discarded.
     *
     * @param directoryIdentifier The directory identifier to use
     * @param directory The directory to extract from
     * @param mediaMeta The media metadata map to put the directory to
     * @return <code>true</code> if given directory can be discarded; otherwise <code>false</code>
     */
    public static boolean putMediaMeta(String directoryIdentifier, Directory directory, MetadataMapImpl.Builder mediaMeta) {
        Collection<Tag> tags = directory.getTags();

        boolean mapAlreadyExists = true;
        MetadataImpl.Builder directoryMap = mediaMeta.getMetadata(directoryIdentifier);
        if (null == directoryMap) {
            // No such map, yet
            directoryMap = MetadataImpl.builder(tags.size());
            mapAlreadyExists = false;
            mediaMeta.putMetadata(directoryIdentifier, directoryMap);
        }


        boolean somethingAdded = false;
        for (Tag tag : tags) {
            somethingAdded |= putTagIntoDirectoryMapIfAbsent(tag, directory, directoryMap, mapAlreadyExists);
        }
        if (somethingAdded) {
            // Something put into media metadata for current directory. Hence, it should not be discarded.
            return false;
        }

        // Nothing put into media metadata for current directory
        if (!mapAlreadyExists) {
            // Directory map was newly added, thus remove it as it is empty
            mediaMeta.removeMetadata(directoryIdentifier);
            return true;
        }

        // Do not discard it as such a map already exists in media metadata
        return false;
    }

    private static boolean putTagIntoDirectoryMapIfAbsent(Tag tag, Directory directory, MetadataImpl.Builder directoryMap, boolean checkExistence) {
        int tagType = tag.getTagType();
        String name = tag.getTagName();

        String key = name == null ? Integer.toString(tagType) : name;
        if (key.startsWith("Unknown tag")) {
            // Discard unknown tags
            return false;
        }

        String description = tag.getDescription();
        if (null != description && description.startsWith("[") && description.endsWith(" values]")) {
            // Any non-categorized binary data; e.g. thumbnail or image areas
            return false;
        }

        if (false == checkExistence || false == directoryMap.containsKey(key)) {
            String value = getStringValue(tagType, directory);
            if (Strings.isNotEmpty(value)) {
                MetadataEntryImpl.Builder entry = MetadataEntryImpl.builder();

                entry.withId(Integer.toString(tagType));

                if (null != name) {
                    entry.withName(name);
                }

                entry.withValue(value);

                if (null != description) {
                    entry.withDescription(description.trim());
                }

                directoryMap.putEntry(key, entry.build());
                return true;
            }
        }

        // Nothing put into directory map
        return false;
    }

    private static String getStringValue(int tagType, Directory directory) {
        Object value = directory.getObject(tagType);
        if (null == value) {
            return null;
        }
        return isArray(value) ? array2String(value) : value.toString().trim();
    }

    private static int getMaxAllowByteArraysLength() {
        return 100;
    }

    private static String array2String(Object array) {
        int length = Array.getLength(array);
        int iMax = length - 1;
        if (iMax < 0) {
            return "[]";
        }

        StringBuilder b = new StringBuilder(length << 2);
        b.append('[');
        for (int i = 0; ; i++) {
            Object obj = Array.get(array, i);
            if (isArray(obj)) {
                String sArray = array2String(obj);
                if (null != sArray) {
                    b.append(sArray);
                } else {
                    if (i == iMax) {
                        return b.append(']').toString();
                    }
                    continue;
                }
            } else {
                if (length > getMaxAllowByteArraysLength() && (obj instanceof Byte)) {
                    // Byte array is too big
                    return null;
                }
                b.append(String.valueOf(obj));
            }

            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(", ");
        }
    }

    /**
     * Checks if specified object is an array.
     *
     * @param object The object to check
     * @return <code>true</code> if specified object is an array; otherwise <code>false</code>
     */
    private static boolean isArray(final Object object) {
        /*-
         * getClass().isArray() is significantly slower on Sun Java 5 or 6 JRE than on IBM.
         * So much that using clazz.getName().charAt(0) == '[' is faster on Sun JVM.
         */
        // return (null != object && object.getClass().isArray());
        return (null != object && '[' == object.getClass().getName().charAt(0));
    }

}
