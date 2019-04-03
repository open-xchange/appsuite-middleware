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

import com.drew.metadata.Directory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.openexchange.java.Strings;

/**
 *
 * {@link KnownDirectory} contains known metadata directories
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public enum KnownDirectory {

    /**
     * The EXIF directory
     */
    EXIF("exif", null),
    /**
     * The GPS directory
     */
    GPS("gps", GpsDirectory.class),
    /**
     * The IPTC directory
     */
    IPTC("iptc", IptcDirectory.class);

    private final String id;
    private final Class<? extends Directory> optConcretetDirectoryType;

    private KnownDirectory(String id, Class<? extends Directory> directoryType) {
        this.id = id;
        this.optConcretetDirectoryType = directoryType;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the optional concrete directory type
     *
     * @return The concrete directory type or <code>null</code>
     */
    public Class<? extends Directory> getConcretetDirectoryType() {
        return optConcretetDirectoryType;
    }

    /**
     * Gets the known directory for given <code>Directory</code> instance.
     *
     * @param directory The <code>Directory</code> instance to look-up by
     * @return The associated known directory or <code>null</code>
     */
    public static KnownDirectory knownDirectoryFor(Directory directory) {
        if (null != directory) {
            String directoryName = Strings.asciiLowerCase(directory.getName());
            for (KnownDirectory knownDirectory : KnownDirectory.values()) {
                Class<? extends Directory> concretetDirectoryType = knownDirectory.optConcretetDirectoryType;
                if (null != concretetDirectoryType) {
                    // Check by type
                    if (concretetDirectoryType.equals(directory.getClass())) {
                        return knownDirectory;
                    }
                } else {
                    // Check by name
                    if (directoryName.indexOf(knownDirectory.id) >= 0) {
                        return knownDirectory;
                    }
                }
            }
        }
        return null;
    }
}